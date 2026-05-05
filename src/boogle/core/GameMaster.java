package boogle.core;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import boogle.util.*;
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

        for (;
            skipChain < playerlist.size() * 2;
        ) try {
            Player currentPlayer = playerlist.get(atPlayer);

            List<Map.Entry<String, Integer>> leaderboard = new ArrayList<>();

            for (Map.Entry<Player, Integer> entry : scoreboard.entrySet()) {
                leaderboard.add(Map.entry(entry.getKey().getName(), entry.getValue()));
            }

            ui.startTurn(gameboard, leaderboard, playedWordList, currentPlayer.getName());

            String move = currentPlayer.nextMove(gameboard, dictionary);

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
                continue;
            }

            move = move.toUpperCase();

            if (move.length() < minWordLen) {
                ui.endTurn(TurnStatus.TOO_SHORT, move, 0, minWordLen);
                atPlayer--;
                continue;
            }

            if (playedWords.contains(move)) {
                ui.endTurn(TurnStatus.DUPLICATE, move, 0, minWordLen);
                atPlayer--;
                continue;
            }

            if (!dictionary.contains(move)) {
                ui.endTurn(TurnStatus.NOT_IN_DICT, move, 0, minWordLen);
                atPlayer--;
                continue;
            }

            if (!gameboard.wordExists(move)) {
                ui.endTurn(TurnStatus.NOT_ON_BOARD, move, 0, minWordLen);
                atPlayer--;
                continue;
            }

            skipChain = 0;

            dictionary.remove(move);    // totally neccesary optimization
            playedWords.add(move);
            playedWordList.add(move);

            int scoreGained = move.length();
            ui.endTurn(TurnStatus.OK, move, scoreGained, minWordLen);

            scoreboard.put(currentPlayer, scoreboard.get(currentPlayer)+scoreGained);

            for (Player player : playerlist) {
                atPlayer = (atPlayer+1) % playerlist.size();
                player.updateGameState(move, playerlist.get(atPlayer).getName());
            }

            if (winScore > 0 && scoreboard.get(currentPlayer) >= winScore)
                break;
        } finally {
            ui.confirm();
        }

        Terminal.clearScreen();
        Terminal.hideCursor();

        FileInputStream stream = new FileInputStream("ui/results_head.txt");
        stream.transferTo(System.out);
        stream.close();
        
        System.out.println(
            "┃    Calories" +
            StringUtils.padStart(
                String.format("%d    ┃", totalSkips*10),
                31, ' '
            )
        );

        stream = new FileInputStream("ui/results_body.txt");
        stream.transferTo(System.out);
        stream.close();

        playerlist.sort((a, b) -> scoreboard.get(b) - scoreboard.get(a));
        for (Player player : playerlist) {
            String name = StringUtils.truncateWithEllipsis(player.getName(), 16);
            int score = scoreboard.get(player);

            System.out.println(
                StringUtils.padEnd(String.format("┃    %s (%dc)", name, score), 30, ' ') +
                StringUtils.padStart(
                    String.format("%d%%    ┃", (score*100)/(maxScore==0 ? Integer.MAX_VALUE : maxScore)),
                    14, ' '
                )
            );
        }

        stream = new FileInputStream("ui/results_tail.txt");
        stream.transferTo(System.out);
        stream.close();

        console.nextLine();
    }
}
