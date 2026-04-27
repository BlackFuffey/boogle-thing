package boogle;

import java.util.*;

public interface Player {
    public String nextMove(Gameboard board, Set<String> dictionary, ArrayList<String> playedWords);
    public void setName(String name);
    public String getName();
}
