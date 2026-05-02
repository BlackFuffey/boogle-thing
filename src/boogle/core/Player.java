package boogle.core;

import java.util.*;

public interface Player {
    public void updateGameState(String lastWordPlayed, String nextPlayer);

    // this MUST NEVER update player's internal state.
    // ALL state updates should be done with updateGameState()
    public String nextMove(Gameboard board, Set<String> dictionary);

    public void setName(String name);
    public String getName();
}
