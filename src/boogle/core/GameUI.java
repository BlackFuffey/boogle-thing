/*
 * File: GameUI.java
 * Author: Ethan Ding
 * Description: Defines the shared interface used by all user interfaces to interact with with the game engine.
 */

package boogle.core;

import java.util.*;

import boogle.core.Launcher.GameOptions;

/**
 * Common contract implemented by all Boogle user interfaces.
 *
 * <p>The game engine calls this interface at each phase of play: first to let
 * the user configure a lobby, then once per turn to display state, collect moves
 * for human-controlled players, display turn outcomes, and finally present the
 * results. Implementations may be blocking and interactive, as both the terminal
 * and Swing UIs wait for user input during lobby setup, active turns, and
 * confirmation prompts.</p>
 *
 * <p>For human players, {@link Player#nextMove()} returns a defer move and the
 * engine calls {@link #active()} to obtain input from the UI. For AI players, the
 * engine calls {@link #passive()} while the AI move is being chosen.</p>
 */
public interface GameUI extends AutoCloseable {

    /**
     * Shows the lobby and lets the user mutate the supplied game options.
     *
     * <p>The same {@link GameOptions} instance is passed by the launcher and is
     * expected to be edited in place. A UI may add, remove, rename, reorder, or
     * configure players; set a custom board; choose a word list; load a saved
     * launcher into {@link GameOptions#replacement}; and adjust presentation
     * options such as music or automatic confirmations.</p>
     *
     * @param options mutable launcher options that describe the game to start
     * @return {@code true} when the launcher should start or resume a game, or
     *         {@code false} when the user chose to quit from the lobby
     */
    public boolean lobby(GameOptions options);

    /**
     * Displays the beginning-of-turn game state.
     *
     * @param board current board used for word validation
     * @param leaderboard player names and scores sorted from highest to lowest
     * @param playedWords words that have already been accepted, in play order
     * @param currentPlayerName display name of the player whose turn is starting
     */
    public void startTurn(Gameboard board, List<Map.Entry<String, Integer>> leaderboard, ArrayList<String> playedWords, String currentPlayerName);

    /**
     * Displays a non-interactive turn state for an automated player.
     *
     * <p>The game engine calls this after an AI player has already returned a
     * move. Implementations use it only to show progress or clear interactive
     * controls; no move should be collected here.</p>
     */
    public void passive();

    /**
     * Collects a move from the active human player.
     *
     * <p>Implementations translate UI commands into {@link Player.Move} values,
     * including word submissions, skips, save requests, game stops, and leaving
     * the game.</p>
     *
     * @return the move selected by the current human player
     */
    public Player.Move active();

    /**
     * Result categories that the engine reports after resolving a move.
     */
    public enum TurnStatus {
        /** A word was accepted and scored. */
        OK,
        /** The player skipped the turn. */
        SKIPPED,
        /** The submitted word was shorter than the configured minimum. */
        TOO_SHORT,
        /** The submitted word was already accepted earlier in the game. */
        DUPLICATE,
        /** The submitted word was not present in the active dictionary. */
        NOT_IN_DICT,
        /** The submitted word could not be formed on the current board. */
        NOT_ON_BOARD,

        /** A save request completed successfully. */
        SAVE_OK,

        /** A save request failed; the move string contains the error message. */
        SAVE_ERR,

        /** The user requested that the game stop. */
        STOPPED,

        /** The current player left the game. */
        PLAYER_LEFT,

        /** Player was TAKING TOO LONG */
        TIMEOUT
    }

    /**
     * Displays the outcome of the current turn.
     *
     * @param status classification of the turn outcome
     * @param move accepted word, rejected word, or save error text depending on
     *        {@code status}; may be {@code null} for outcomes with no associated
     *        word
     * @param scoreGained score awarded for an accepted word, otherwise zero
     * @param minWordLength configured minimum word length, used by UIs when
     *        explaining {@link TurnStatus#TOO_SHORT}
     */
    public void endTurn(TurnStatus status, String move, int scoreGained, int minWordLength);

    /**
     * Waits for the normal between-turn acknowledgement.
     *
     * <p>Depending on UI settings this may block until the user presses continue
     * or sleep briefly for automatic confirmation.</p>
     */
    public void confirm();

    /**
     * Waits for a stronger final acknowledgement after the results screen.
     *
     * <p>The terminal implementation deliberately resists accidental key presses
     * by waiting for input over a short time window; the GUI implementation has
     * no extra behavior here because the results screen has its own continue
     * button.</p>
     */
    public void confirmForSure();

    /**
     * Displays the final game results.
     *
     * @param leaderboard final player names and scores sorted from highest to
     *        lowest
     * @param skips total number of skipped turns
     * @param maxScore total score represented by all possible words on the board
     */
    public void results(List<Map.Entry<String, Integer>> leaderboard, int skips, int maxScore);
}
