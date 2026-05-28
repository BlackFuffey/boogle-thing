package boogle.core;

import java.io.Serializable;
import java.util.*;

/**
 * Represents a participant in a Boogle game. Implementations may represent
 * either human players that interact through a UI or AI players that
 * automatically select words. The game engine communicates with a {@code Player}
 * to set up the game state, request moves and update the player with
 * information about other players’ moves.
 */
public interface Player extends Serializable {
    /**
     * Provides the player with the current board and dictionary. This method
     * is invoked once before the first turn. Implementations should use the
     * provided information to prepare any internal data structures (for
     * example, build a trie of valid words) but must not modify the
     * {@code board} or {@code dictionary} themselves.
     *
     * @param board the game board on which words will be played
     * @param dictionary the set of valid words remaining to play
     */
    public void setGame(Gameboard board, Set<String> dictionary);

    /**
     * Updates the player with information about the most recent move. After
     * each turn the game engine invokes this method on all players (including
     * the one who just played) to allow them to adjust their internal state.
     * This method may remove the played word from the player’s list of
     * available options or adjust strategies based on the next player.
     *
     * @param lastWordPlayed the word that was just played, or {@code null}
     *                       when the current player skipped
     * @param nextPlayer the name of the player who will move next
     */
    public void updateGameState(String lastWordPlayed, String nextPlayer);

    /**
     * Represents a player's requested action for their turn.
     * <p>
         * A move may play a word, skip the turn, leave the game, or save the game.
         * For {@link Type#WORD} moves, {@link #value} stores the word to play. For
         * all other move types, {@link #value} is typically {@code null}.
     */
    public static class Move implements Serializable{
        /**
         * The kind of action requested by a player.
         */
        public enum Type {
            /** Play the word stored in {@link Move#value}. */
            WORD,

            /** Skip the current turn without playing a word. */
            SKIP,

            /** Leave the current game. */
            LEAVE,

            /** Save the current game. */
            SAVE,

            /** Stop the current game for all players. */
            STOP,
            
            /** Defer decision to UI */
            DEFER
        }

        /** The requested move type. */
        public Type type;

        /**
         * Optional move payload. For {@link Type#WORD}, this is the word to
         * play; For {@link Type#SAVE}, this is the path to save to.
         * Otherwise, this variable is ignored.
         */
        public String value = null;

        /**
         * Creates a move with the given type and optional value.
         *
         * @param type the kind of move being requested
         * @param value the optional value associated with the move
         */
        public Move(Type type, String value){
            this.type = type;
            this.value = value;
        }

        /**
         * Creates a move with no associated value.
         *
         * @param type the kind of move being requested
         */
        public Move(Type type) {
            this(type, null);
        }
    }

    /**
     * Requests the player’s next move. Implementations must not mutate their
     * internal state in this method; all state updates should be done in
     * {@link #updateGameState(String, String)}.
     *
     * @return the player’s requested move
     */
    public Move nextMove();

    /**
     * Sets the display name for this player. The name may be shown in the
     * user interface and on the scoreboard.
     *
     * @param name the new player name
     */
    public void setName(String name);

    /**
     * Returns the display name of this player.
     *
     * @return the player’s current name
     */
    public String getName();
}
