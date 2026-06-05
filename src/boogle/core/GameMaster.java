package boogle.core;

import java.io.*;
import java.util.*;

import boogle.core.GameUI.TurnStatus;
import boogle.player.AIPlayer;

/**
 * Serializable controller for a single Boogle game.
 *
 * <p>The game master owns the board, dictionary, player order, scoreboard, and
 * resume state for an in-progress match. It drives the turn loop, delegates
 * input and rendering to a {@link GameUI}, validates submitted words, awards
 * length-based points, and stops when a player reaches the winning score or all
 * remaining players skip enough consecutive turns.</p>
 */
public class GameMaster implements Serializable {

    /** Ordered list of participating players. */
    private List<Player> playerlist;
    /** Mutable uppercase dictionary of unplayed legal words. */
    private Set<String> dictionary;
    /** Board used for the current match. */
    private Gameboard gameboard;
    private transient GameUI ui;

    /** Owning launcher, used for UI access and serialization. */
    private Launcher launcher;

    /** Minimum accepted word length for this match. */
    private int minWordLen;
    /** Score target; zero disables score-target termination. */
    private int winScore;

    /**
     * Creates a game controller with the supplied players and rules.
     *
     * @param playerlist ordered list of players who will take turns
     * @param dictionary uppercase legal words available for play
     * @param board custom board grid, or {@code null} to generate a random board
     * @param minWordLen minimum accepted word length; zero disables the limit
     * @param winScore target score; zero enables endless play until termination
     *        by skips, stops, or leaving players
     * @param launcher launcher that owns the UI and serialization state
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

    /**
     * Serializable mutable state needed to resume the match after saving.
     */
    private static class GameState implements Serializable {
        private HashMap<Player, Integer> scoreboard = new HashMap<>();
        private HashSet<String> playedWords = new HashSet<>();
        private ArrayList<String> playedWordList = new ArrayList<>();
        private Set<Player> leftPlayers = new HashSet<>();

        private int atPlayer = 0;
        private int skipChain = 0;
        private int totalSkips = 0;
        private int maxScore = 0;
    }

    /** Current serializable progress state for this match. */
    private GameState state;
    /**
     * Runs or resumes the game loop until a terminal condition is reached.
     *
     * <p>On first entry the method initializes scores and computes the maximum
     * possible board score using a perfect AI search. On resumed games it keeps
     * the existing state and reconnects the transient UI through the launcher.</p>
     *
     * @throws IOException if a UI operation or save-related operation reports an
     *         I/O failure to the game loop
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

        gameloop: while (state.skipChain < (playerlist.size()-state.leftPlayers.size()) * 2) {
            Player currentPlayer = playerlist.get(state.atPlayer);

            if (state.leftPlayers.contains(currentPlayer)) {
                state.atPlayer = (state.atPlayer+1) % playerlist.size();
                continue;
            }

        try {

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

                case STOP: {
                    ui.endTurn(TurnStatus.STOPPED, null, 0, 0);
                    break gameloop;
                }

                case LEAVE: {
                    state.leftPlayers.add(currentPlayer);
                    ui.endTurn(TurnStatus.PLAYER_LEFT, null, 0, 0);
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
        }}

        List<Map.Entry<String, Integer>> leaderboard = new ArrayList<>();
        for (Map.Entry<Player, Integer> entry : state.scoreboard.entrySet()) {
            leaderboard.add(Map.entry(entry.getKey().getName(), entry.getValue()));
        }
        leaderboard.sort((a, b) -> b.getValue() - a.getValue());

        ui.results(leaderboard, state.totalSkips, state.maxScore);
        ui.confirmForSure();
    }
}
