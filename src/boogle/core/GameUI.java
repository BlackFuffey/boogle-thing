package boogle.core;

import java.util.*;

import boogle.core.Launcher.GameOptions;

/**
 * Contract between the game engine and a user interface implementation.
 * Implementations of this interface are responsible for presenting game
 * state to the user, gathering input during the lobby and on each turn,
 * and providing feedback such as scores and results. The lifecycle of a
 * {@code GameUI} typically consists of:
 * <ol>
 *   <li>Invoking {@link #lobby(GameOptions)} to allow players and options
 *       to be configured and to decide whether to start the game.</li>
 *   <li>For each turn, calling {@link #startTurn(Gameboard, List, ArrayList, String)}
 *       to render the board and scoreboard, followed by either
 *       {@link #active()} when the current player is human or
 *       {@link #passive()} when the current player is controlled by the AI.</li>
 *   <li>Calling {@link #endTurn(TurnStatus, String, int, int)} to report the
 *       outcome of the move and optionally pausing for user confirmation via
 *       {@link #confirm()}.</li>
 *   <li>After the game loop finishes, invoking {@link #results(List, int, int)}
 *       to display the final scoreboard, and finally {@link #confirmForSure()}
 *       to wait for the user before returning to the caller.</li>
 * </ol>
 */
public interface GameUI extends AutoCloseable {

    /**
     * Displays the lobby screen and allows the user to configure game options
     * such as player list, dictionary path, minimum word length and winning
     * score. When this method returns {@code true} the game engine will
     * proceed to the main loop using the updated {@code options}; returning
     * {@code false} indicates that the user wishes to quit before starting.
     *
     * @param options a mutable holder for game configuration. Implementations
     *                should read and modify the fields of this object to
     *                reflect the user’s choices.
     * @return {@code true} to start the game, {@code false} to abort
     */
    public boolean lobby(GameOptions options);

    /**
     * Called at the start of each player’s turn. Implementations should
     * present the current game state to the user, including the letter board,
     * current scores and list of played words. The current player’s name
     * should be highlighted or otherwise indicated. The UI may also decide
     * whether to gather input immediately or defer to {@link #passive()} or
     * {@link #active()} depending on whether the current player is an AI.
     *
     * @param board the current {@link Gameboard}
     * @param leaderboard an ordered list of player names and their scores,
     *                    sorted descending by score
     * @param playedWords a history of words that have been successfully played
     * @param currentPlayerName the name of the player whose turn it is
     */
    public void startTurn(Gameboard board, List<Map.Entry<String, Integer>> leaderboard, ArrayList<String> playedWords, String currentPlayerName);

    /**
     * Indicates that it is not the user’s turn to move. This method should
     * update the UI accordingly (for example, display a “thinking” message or
     * animate an AI player) and then return immediately. It is invoked when
     * the current player is controlled by the computer.
     */
    public void passive();

    /**
     * Requests a move from the human player. When called the UI should prompt
     * the user to enter a word or to signal that they wish to skip their
     * turn. Implementations must not modify any internal game state here; the
     * returned value will be validated by the game engine. Returning
     * {@code null} indicates that the player chooses to skip this turn.
     *
     * @return the move requested by user
     */
    public Player.Move active();

    /**
     * Enumeration of possible outcomes for a player’s move. These values
     * provide context when reporting the result of a turn via
     * {@link #endTurn(TurnStatus, String, int, int)}.
     */
    public enum TurnStatus {
        /** The word was valid, on the board and has been scored. */
        OK,
        /** The player elected to skip this turn. */
        SKIPPED,
        /** The word was shorter than the minimum allowed length. */
        TOO_SHORT,
        /** The word has already been played earlier in this game. */
        DUPLICATE,
        /** The word was not found in the dictionary. */
        NOT_IN_DICT,
        /** The word could not be formed on the current board. */
        NOT_ON_BOARD,
        /** Game state was saved successfully */
        SAVE_OK,
        /** Failed to save game */
        SAVE_ERR
    }

    /**
     * Called at the end of a player’s turn to communicate the result. The UI
     * should display an appropriate message (and optionally play sound
     * effects) based on the supplied {@code status}. If the status is
     * {@link TurnStatus#OK} then the score gained should also be displayed.
     * This method does not block waiting for user input; that should be done
     * by {@link #confirm()} if desired.
     *
     * @param status the outcome of the attempted move
     * @param move the word the player attempted to play (uppercase). May be
     *             {@code null} when {@code status} is {@link TurnStatus#SKIPPED}
     * @param scoreGained the number of points awarded for this move; ignored
     *                    if the status is not {@code OK}
     * @param minWordLength the minimum allowable word length for reference in
     *                      error messages
     */
    public void endTurn(TurnStatus status, String move, int scoreGained, int minWordLength);

    /**
     * Pauses the UI to allow the user to read the end‑of‑turn message. When
     * auto‑confirmation is enabled implementations may choose to return
     * immediately after a short delay; otherwise they should block until the
     * user acknowledges the message (for example, by pressing enter).
     */
    public void confirm();

    /**
     * Forces the UI to wait for a definitive user acknowledgement. This
     * method is typically called once after the results have been shown to
     * prevent the game from immediately exiting back to the shell.
     */
    public void confirmForSure();

    /**
     * Displays the final results at the end of the game. Implementations
     * should render the final leaderboard, the total number of skipped turns
     * (expressed as calories in the terminal UI) and the theoretical maximum
     * score obtainable on the given board. This method does not block; use
     * {@link #confirmForSure()} afterwards if the caller needs to wait.
     *
     * @param leaderboard list of player names and their final scores sorted
     *                    descending by score
     * @param skips total number of skipped turns across all players
     * @param maxScore the maximum score possible for the generated board
     */
    public void results(List<Map.Entry<String, Integer>> leaderboard, int skips, int maxScore);
}
