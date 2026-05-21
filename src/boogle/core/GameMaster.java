package boogle.core;

import java.io.*;
import java.util.*;

import boogle.core.GameUI.TurnStatus;
import boogle.player.AIPlayer;

/**
 * Coordinates a single match of Boogle. The {@code GameMaster} is responsible
 * for managing the list of players, loading or generating the game board,
 * enforcing the dictionary and word length rules, tracking scores and
 * communicating with the chosen {@link GameUI}. It encapsulates the
 * high‑level game loop and determines when the game has been won or should
 * terminate due to successive passes.
 */
public class GameMaster {

    /** List of players participating in this game in turn order. */
    private List<Player> playerlist;
    /**
     * Set of valid words that may be played. Words are removed from the set
     * after being played to prevent duplicates and trimmed to uppercase
     * outside of this class.
     */
    private Set<String> dictionary;
    /** Current game board on which words are played. */
    private Gameboard gameboard;
    /** User interface used to present game state and accept moves. */
    private GameUI ui;

    /** Minimum allowed word length. Words shorter than this are rejected. */
    private int minWordLen;
    /**
     * Winning score threshold. When a player’s accumulated score reaches or
     * exceeds this value the game ends. A value of zero disables the
     * threshold and produces an endless game.
     */
    private int winScore;

    /**
     * Constructs a new game master with the given settings.
     *
     * @param playerlist ordered list of players participating in the game. Each
     *                   player will be asked to make a move in turn. The list
     *                   is not copied so callers should not modify it during
     *                   game play.
     * @param dictionary set of valid uppercase words. Played words are
     *                   removed from this set to prevent reuse.
     * @param board predefined board to use or {@code null} to generate a
     *              random board of standard size via {@link Gameboard#Gameboard()}.
     * @param minWordLen minimum length of a valid word. Words shorter than
     *                   this value will be rejected by the UI.
     * @param winScore score needed to win. A value of zero means no score
     *                 limit and the game will end only when all players have
     *                 consecutively skipped twice around the table.
     * @param ui user interface through which the game communicates with
     *           players
     */
    public GameMaster(List<Player> playerlist, Set<String> dictionary, char[][] board, int minWordLen, int winScore, GameUI ui) {
        this.playerlist = playerlist;
        this.dictionary = dictionary;

        if (board == null)
            this.gameboard = new Gameboard();
        else
            this.gameboard = new Gameboard(board);

        this.minWordLen = minWordLen;
        this.winScore = winScore;

        this.ui = ui;
    }

    /**
     * Runs the main game loop. This method repeatedly asks each player for
     * their move until either a winning score is reached or all players
     * consecutively skip their turns twice. During each turn the method
     * displays the board and score via the {@link GameUI}, validates the
     * player’s input against the dictionary, length and board constraints,
     * updates scores and internal state accordingly and notifies all players
     * of the move. Upon termination the final results are displayed.
     *
     * @throws IOException if the underlying UI throws an I/O exception while
     *                     interacting with the terminal or files
     */
    public void begin() throws IOException {
        HashMap<Player, Integer> scoreboard = new HashMap<>();
        HashSet<String> playedWords = new HashSet<>();
        ArrayList<String> playedWordList = new ArrayList<>();

        for (Player player : playerlist) {
            scoreboard.put(player, 0);
        }

        int atPlayer = 0;
        int skipChain = 0;
        int totalSkips = 0;

        int maxScore = AIPlayer.computeMaxScore(gameboard, dictionary);

        for (Player player : playerlist) {
            player.setGame(gameboard, dictionary);
        }

        while (skipChain < playerlist.size() * 2) try {
            Player currentPlayer = playerlist.get(atPlayer);

            List<Map.Entry<String, Integer>> leaderboard = new ArrayList<>();
            for (Map.Entry<Player, Integer> entry : scoreboard.entrySet()) {
                leaderboard.add(Map.entry(entry.getKey().getName(), entry.getValue()));
            }
            leaderboard.sort((a, b) -> b.getValue() - a.getValue());

            ui.startTurn(gameboard, leaderboard, playedWordList, currentPlayer.getName());

            String move = currentPlayer.nextMove();

            // yes ik magic string is bad. blame java for making struct defs so verbose
            if (move != null && move.equals("__defer")) {
                move = ui.active();
            } else {
                ui.passive();
            }

            if (move == null) {
                ui.endTurn(TurnStatus.SKIPPED, move, 0, minWordLen);
                skipChain++;
                totalSkips++;
                atPlayer = (atPlayer+1) % playerlist.size();
                continue;
            }

            move = move.toUpperCase();

            if (move.length() < minWordLen) {
                ui.endTurn(TurnStatus.TOO_SHORT, move, 0, minWordLen);
                continue;
            }

            if (playedWords.contains(move)) {
                ui.endTurn(TurnStatus.DUPLICATE, move, 0, minWordLen);
                continue;
            }

            if (!dictionary.contains(move)) {
                ui.endTurn(TurnStatus.NOT_IN_DICT, move, 0, minWordLen);
                continue;
            }

            if (!gameboard.wordExists(move)) {
                ui.endTurn(TurnStatus.NOT_ON_BOARD, move, 0, minWordLen);
                continue;
            }

            skipChain = 0;

            dictionary.remove(move);    // totally neccesary optimization
            playedWords.add(move);
            playedWordList.add(move);

            int scoreGained = move.length();
            ui.endTurn(TurnStatus.OK, move, scoreGained, minWordLen);

            scoreboard.put(currentPlayer, scoreboard.get(currentPlayer)+scoreGained);

            atPlayer = (atPlayer+1) % playerlist.size();

            for (Player player : playerlist) {
                player.updateGameState(move, playerlist.get(atPlayer).getName());
            }

            if (winScore > 0 && scoreboard.get(currentPlayer) >= winScore)
                break;
        } finally {
            ui.confirm();
        }

        List<Map.Entry<String, Integer>> leaderboard = new ArrayList<>();
        for (Map.Entry<Player, Integer> entry : scoreboard.entrySet()) {
            leaderboard.add(Map.entry(entry.getKey().getName(), entry.getValue()));
        }
        leaderboard.sort((a, b) -> b.getValue() - a.getValue());

        ui.results(leaderboard, totalSkips, maxScore);
        ui.confirmForSure();
    }
}
