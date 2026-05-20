package boogle.core;

import java.util.*;

import boogle.core.Launcher.GameOptions;

public interface GameUI extends AutoCloseable {
    /**
     * @param options default options
     * @return true if start game, false if quit
    */
    public boolean lobby(GameOptions options);

    public void startTurn(Gameboard board, List<Map.Entry<String, Integer>> leaderboard, ArrayList<String> playedWords, String currentPlayerName);

    public void passive();
    public String active();

    public enum TurnStatus {
        OK, SKIPPED, TOO_SHORT, DUPLICATE, NOT_IN_DICT, NOT_ON_BOARD
    }
    public void endTurn(TurnStatus status, String move, int scoreGained, int minWordLength);

    public void confirm();
    public void confirmForSure();

    public void results(List<Map.Entry<String, Integer>> leaderboard, int skips, int maxScore);
}
