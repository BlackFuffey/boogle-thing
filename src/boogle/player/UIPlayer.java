package boogle.player;

import java.util.*;
import boogle.core.*;

public class UIPlayer implements Player {
    private String name;

    public UIPlayer(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String nextMove() {
        return "__defer";
    }

    public void updateGameState(String a, String b) {
        // do nothing
    }

    public void setGame(Gameboard board, Set<String> dictionary) {
        // do nothing
    }
}
