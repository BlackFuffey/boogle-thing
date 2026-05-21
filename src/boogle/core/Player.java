package boogle.core;

import java.util.*;

/**
 * Represents a participant in a Boogle game. Implementations may represent
 * either human players that interact through a UI or AI players that
 * automatically select words. The game engine communicates with a {@code Player}
 * to set up the game state, request moves and update the player with
 * information about other players’ moves.
 */
public interface Player {
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
     * Requests the player’s next move. Implementations must not mutate their
     * internal state in this method; all state updates should be done in
     * {@link #updateGameState(String, String)}. Returning {@code null}
     * indicates that the player wishes to skip this turn.
     *
     * @return the player’s chosen word, or {@code null} to skip the turn
     */
    public String nextMove();

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
