package boogle;

import java.util.HashSet;

public interface Player {
    public String nextMove(Gameboard board, HashSet<String> wordlist, String[] prevMoves);
    public void setName(String name);
    public String getName();
}
