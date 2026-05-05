package boogle.core;

import java.util.*;

import boogle.core.Launcher.GameOptions;

public interface GameUI extends AutoCloseable{
    /**
     * @param options default options
     * @return true if start game, false if quit
    */
    public boolean lobby(GameOptions options);

    public void newTurn(Gameboard board, HashMap<Player, Integer> scoreboard, ArrayList<String> playedWords, Player player);

    public void passive();
    public String active();

    public void confirm();

    public void results(List<Map.Entry<String, Integer>> leaderboard, int skips, int maxScore);
}
