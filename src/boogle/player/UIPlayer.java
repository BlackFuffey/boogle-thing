/*
 * File: UIPlayer.java
 * Author: Ethan Ding
 * Description: Represents a human player whose moves are collected through the active user interface.
 */

package boogle.player;

import java.util.*;
import boogle.core.*;

/**
 * Human player placeholder that delegates move collection to the active UI.
 *
 * <p>The game engine treats this player as a participant in ordering and
 * scoring, but {@link #nextMove()} always returns {@link Player.Move.Type#DEFER}
 * so the current {@link boogle.core.GameUI} can prompt the user.</p>
 */
public class UIPlayer implements Player {
    /** Player display name. */
    private String name;
    /** Reusable move telling the engine to ask the UI for input. */
    private Player.Move deferMove = new Player.Move(Player.Move.Type.DEFER);

    /**
     * Creates a human player with a display name.
     *
     * @param name player name shown in lobbies and leaderboards
     */
    public UIPlayer(String name) {
        this.name = name;
    }

    /**
     * Changes the human player's display name.
     *
     * @param name new display name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the human player's display name.
     *
     * @return display name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Defers move selection to the active UI.
     *
     * @return a reusable defer move
     */
    public Player.Move nextMove() {
        return deferMove;
    }

    /**
     * Ignores accepted-word notifications because human state lives in the UI.
     *
     * @param a accepted word, ignored
     * @param b next player name, ignored
     */
    public void updateGameState(String a, String b) {
        // do nothing
    }

    /**
     * Ignores board and dictionary state because the UI handles human input.
     *
     * @param board active board, ignored
     * @param dictionary active dictionary, ignored
     */
    public void setGame(Gameboard board, Set<String> dictionary) {
        // do nothing
    }
}
