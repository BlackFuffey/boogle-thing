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
public class GameMaster implements Serializable {

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
    private transient GameUI ui;

    /** Launcher reference for serializing*/
    private Launcher launcher;

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
     * @param launcher launcher reference to serialize when user requests so
     */
    public GameMaster(List<Player> playerlist, Set<String> dictionary, char[][] board, int minWordLen, int winScore,  Launcher launcher) {
        this.playerlist = playerlist;
        this.dictionary = dictionary;

        if (board == null)
            this.gameboard = new Gameboard();
        else
            this.gameboard = new Gameboard(board);

        this.minWordLen = minWordLen;
        this.winScore = winScore;

        this.launcher = launcher;
    }

    /** Serializable state for a game that may be paused and resumed. */
    private static class GameState implements Serializable {
        private static final long serialVersionUID = 1L;

        private HashMap<Player, Integer> scoreboard = new HashMap<>();
        private HashSet<String> playedWords = new HashSet<>();
        private ArrayList<String> playedWordList = new ArrayList<>();

        private int atPlayer = 0;
        private int skipChain = 0;
        private int totalSkips = 0;
        private int maxScore = 0;
    }

    private GameState state;
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
        this.ui = launcher.ui;

        if (state == null) {
            state = new GameState();

            for (Player player : playerlist) {
                state.scoreboard.put(player, 0);
            }

            state.maxScore = AIPlayer.computeMaxScore(gameboard, dictionary);
        }

        for (Player player : playerlist) {
            player.setGame(gameboard, dictionary);
        }

        while (state.skipChain < playerlist.size() * 2) try {
            Player currentPlayer = playerlist.get(state.atPlayer);

            List<Map.Entry<String, Integer>> leaderboard = new ArrayList<>();
            for (Map.Entry<Player, Integer> entry : state.scoreboard.entrySet()) {
                leaderboard.add(Map.entry(entry.getKey().getName(), entry.getValue()));
            }
            leaderboard.sort((a, b) -> b.getValue() - a.getValue());

            ui.startTurn(gameboard, leaderboard, state.playedWordList, currentPlayer.getName());

            Player.Move move = currentPlayer.nextMove();

            if (move.type == Player.Move.Type.DEFER) {
                move = ui.active();
            } else {
                ui.passive();
            }

            switch (move.type) {
                case WORD: break;

                case SKIP: {
                    ui.endTurn(TurnStatus.SKIPPED, null, 0, minWordLen);
                    state.skipChain++;
                    state.totalSkips++;
                    state.atPlayer = (state.atPlayer+1) % playerlist.size();
                    continue;
                }

                case SAVE: {
                    try { this.launcher.serialize(new FileOutputStream(move.value)); }
                    catch (IOException e) {
                        ui.endTurn(TurnStatus.SAVE_ERR, e.getMessage(), 0, 0);
                        continue;
                    }

                    ui.endTurn(TurnStatus.SAVE_OK, null, 0, 0);
                    continue;
                }

                default: 
                    throw new UnsupportedOperationException(move.type.toString() + " is not implemented");
            }

            move.value = move.value.toUpperCase();
            if (move.value.length() < minWordLen) {
                ui.endTurn(TurnStatus.TOO_SHORT, move.value, 0, minWordLen);
                continue;
            }

            if (state.playedWords.contains(move.value)) {
                ui.endTurn(TurnStatus.DUPLICATE, move.value, 0, minWordLen);
                continue;
            }

            if (!dictionary.contains(move.value)) {
                ui.endTurn(TurnStatus.NOT_IN_DICT, move.value, 0, minWordLen);
                continue;
            }

            if (!gameboard.wordExists(move.value)) {
                ui.endTurn(TurnStatus.NOT_ON_BOARD, move.value, 0, minWordLen);
                continue;
            }

            state.skipChain = 0;

            dictionary.remove(move.value);    // totally neccesary optimization
            state.playedWords.add(move.value);
            state.playedWordList.add(move.value);

            int scoreGained = move.value.length();
            ui.endTurn(TurnStatus.OK, move.value, scoreGained, minWordLen);

            state.scoreboard.put(currentPlayer, state.scoreboard.get(currentPlayer)+scoreGained);

            state.atPlayer = (state.atPlayer+1) % playerlist.size();

            for (Player player : playerlist) {
                player.updateGameState(move.value, playerlist.get(state.atPlayer).getName());
            }

            if (winScore > 0 && state.scoreboard.get(currentPlayer) >= winScore)
                break;
        } finally {
            ui.confirm();
        }

        List<Map.Entry<String, Integer>> leaderboard = new ArrayList<>();
        for (Map.Entry<Player, Integer> entry : state.scoreboard.entrySet()) {
            leaderboard.add(Map.entry(entry.getKey().getName(), entry.getValue()));
        }
        leaderboard.sort((a, b) -> b.getValue() - a.getValue());

        ui.results(leaderboard, state.totalSkips, state.maxScore);
        ui.confirmForSure();
    }
}
