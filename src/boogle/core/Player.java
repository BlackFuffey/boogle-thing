/*
 * File: Player.java
 * Author: Ethan Ding
 * Description: Defines the common behavior required for all Boogle players implementations.
 */

package boogle.core;

import java.io.Serializable;
import java.util.*;

/**
 * A participant in a Boogle game.
 *
 * <p>Players are serializable because the launcher can save an in-progress game.
 * Implementations include human UI-backed players, which defer move collection
 * to the active {@link GameUI}, and AI players, which inspect the current board
 * and dictionary to produce word moves directly.</p>
 */
public interface Player extends Serializable {
    /**
     * Supplies the board and dictionary that define the current game.
     *
     * <p>The engine calls this before play begins or resumes. AI implementations
     * use the state to precompute possible words, while UI-backed players may
     * ignore it because the UI collects their moves.</p>
     *
     * @param board board on which submitted words must be formable
     * @param dictionary mutable dictionary of legal words remaining in the game
     */
    public void setGame(Gameboard board, Set<String> dictionary);

    /**
     * Notifies the player that the game state changed after an accepted word.
     *
     * @param lastWordPlayed word accepted on the previous turn
     * @param nextPlayer name of the player who will act next
     */
    public void updateGameState(String lastWordPlayed, String nextPlayer);

    /**
     * A single action returned by a player or UI for the game engine to process.
     */
    public static class Move implements Serializable{
        /**
         * Kinds of actions that can be taken during a turn.
         */
        public enum Type {
            /** Submit {@link Move#value} as a word candidate. */
            WORD,

            /** Skip the current turn without scoring. */
            SKIP,

            /** Remove the current player from the rest of the game. */
            LEAVE,

            /** Serialize the game to the path stored in {@link Move#value}. */
            SAVE,

            /** Stop the current game and show results. */
            STOP,
            
            /** Ask the active UI to collect the real move for a human player. */
            DEFER,

            /** Turn timelimit exceeded */
            TIMEOUT
        }

        /** Type of action represented by this move. */
        public Type type;

        /** Optional value associated with the action, such as a word or file path. */
        public String value = null;

        /**
         * Creates a move with an explicit value.
         *
         * @param type action kind
         * @param value word, path, or other action-specific text; may be
         *        {@code null}
         */
        public Move(Type type, String value){
            this.type = type;
            this.value = value;
        }

        /**
         * Creates a move with no associated value.
         *
         * @param type action kind
         */
        public Move(Type type) {
            this(type, null);
        }
    }

    /**
     * Chooses the next move for this player.
     *
     * @return a concrete move for automated players, or {@link Move.Type#DEFER}
     *         for human players whose input must be collected by the UI
     */
    public Move nextMove();

    /**
     * Changes the player display name.
     *
     * @param name new name shown in lobbies, turn prompts, and leaderboards
     */
    public void setName(String name);

    /**
     * Returns the player display name.
     *
     * @return current player name
     */
    public String getName();
}
