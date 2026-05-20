package boogle.core;

import java.io.*;
import java.util.*;

import boogle.core.GameUI.TurnStatus;
import boogle.player.AIPlayer;

public class GameMaster {

    private List<Player> playerlist;
    private Set<String> dictionary;
    private Gameboard gameboard;
    private GameUI ui;

    private int minWordLen;
    private int winScore;

    public GameMaster(List<Player> playerlist, Set<String> dictionary, char[][] board, int minWordLen, int winScore, GameUI ui) {
        this.playerlist = playerlist;
        this.dictionary = dictionary;

        if (board == null)
            this.gameboard = new Gameboard();
        else
            this.gameboard = new Gameboard(board);

        this.minWordLen = minWordLen;
        this.winScore = winScore;

        this.ui = ui;
    }

    public void begin() throws IOException {
        HashMap<Player, Integer> scoreboard = new HashMap<>();
        HashSet<String> playedWords = new HashSet<>();
        ArrayList<String> playedWordList = new ArrayList<>();

        for (Player player : playerlist) {
            scoreboard.put(player, 0);
        }

        int atPlayer = 0;
        int skipChain = 0;
        int totalSkips = 0;

        int maxScore = AIPlayer.computeMaxScore(gameboard, dictionary);

        for (Player player : playerlist) {
            player.setGame(gameboard, dictionary);
        }

        while (skipChain < playerlist.size() * 2) try {
            Player currentPlayer = playerlist.get(atPlayer);

            List<Map.Entry<String, Integer>> leaderboard = new ArrayList<>();
            for (Map.Entry<Player, Integer> entry : scoreboard.entrySet()) {
                leaderboard.add(Map.entry(entry.getKey().getName(), entry.getValue()));
            }
            leaderboard.sort((a, b) -> b.getValue() - a.getValue());

            ui.startTurn(gameboard, leaderboard, playedWordList, currentPlayer.getName());

            String move = currentPlayer.nextMove();

            // yes ik magic string is bad. blame java for making struct defs so verbose
            if (move != null && move.equals("__defer")) {
                move = ui.active();
            } else {
                ui.passive();
            }

            if (move == null) {
                ui.endTurn(TurnStatus.SKIPPED, move, 0, minWordLen);
                skipChain++;
                totalSkips++;
                atPlayer = (atPlayer+1) % playerlist.size();
                continue;
            }

            move = move.toUpperCase();

            if (move.length() < minWordLen) {
                ui.endTurn(TurnStatus.TOO_SHORT, move, 0, minWordLen);
                continue;
            }

            if (playedWords.contains(move)) {
                ui.endTurn(TurnStatus.DUPLICATE, move, 0, minWordLen);
                continue;
            }

            if (!dictionary.contains(move)) {
                ui.endTurn(TurnStatus.NOT_IN_DICT, move, 0, minWordLen);
                continue;
            }

            if (!gameboard.wordExists(move)) {
                ui.endTurn(TurnStatus.NOT_ON_BOARD, move, 0, minWordLen);
                continue;
            }

            skipChain = 0;

            dictionary.remove(move);    // totally neccesary optimization
            playedWords.add(move);
            playedWordList.add(move);

            int scoreGained = move.length();
            ui.endTurn(TurnStatus.OK, move, scoreGained, minWordLen);

            scoreboard.put(currentPlayer, scoreboard.get(currentPlayer)+scoreGained);

            atPlayer = (atPlayer+1) % playerlist.size();

            for (Player player : playerlist) {
                player.updateGameState(move, playerlist.get(atPlayer).getName());
            }

            if (winScore > 0 && scoreboard.get(currentPlayer) >= winScore)
                break;
        } finally {
            ui.confirm();
        }

        List<Map.Entry<String, Integer>> leaderboard = new ArrayList<>();
        for (Map.Entry<Player, Integer> entry : scoreboard.entrySet()) {
            leaderboard.add(Map.entry(entry.getKey().getName(), entry.getValue()));
        }
        leaderboard.sort((a, b) -> b.getValue() - a.getValue());

        ui.results(leaderboard, totalSkips, maxScore);
        ui.confirmForSure();
    }
}
