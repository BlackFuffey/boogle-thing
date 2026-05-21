package boogle.player;

import java.util.*;
import boogle.core.*;

/**
 * Lightweight {@link Player} implementation representing a human user. A
 * {@code UIPlayer} delegates all decision‑making to the {@link GameUI}
 * implementation. Its {@link #nextMove()} method returns a sentinel value
 * telling the UI to solicit input from the user.
 */
public class UIPlayer implements Player {
    private String name;

    /**
     * Creates a new human player with the specified name.
     *
     * @param name display name of the player
     */
    public UIPlayer(String name) {
        this.name = name;
    }

    /**
     * Changes the player’s display name.
     *
     * @param name new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the current name of the player.
     *
     * @return the player’s display name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Indicates that the UI should ask the user for a move. The game engine
     * interprets the returned sentinel string and calls
     * {@link GameUI#active()} on the associated UI. Implementations must not
     * return {@code null} here; skipping is handled by the UI after
     * prompting.
     *
     * @return a sentinel value instructing the engine to defer to the UI
     */
    public String nextMove() {
        return "__defer";
    }

    /**
     * Human players do not maintain internal state between turns, so this
     * method performs no operation.
     *
     * @param a ignored
     * @param b ignored
     */
    public void updateGameState(String a, String b) {
        // do nothing
    }

    /**
     * Human players do not need the board or dictionary ahead of time. This
     * method is a no‑op for {@code UIPlayer}.
     *
     * @param board ignored
     * @param dictionary ignored
     */
    public void setGame(Gameboard board, Set<String> dictionary) {
        // do nothing
    }
}
