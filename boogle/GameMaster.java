package boogle;

import java.io.*;
import java.util.*;

import boogle.util.*;

public class GameMaster {

    private List<Player> playerlist;
    private Set<String> dictionary;
    private Gameboard gameboard;

    private int minWordLen;
    private int winScore;

    private Scanner console;

    public GameMaster(List<Player> playerlist, Set<String> dictionary, char[][] board, int minWordLen, int winScore, Scanner console) {
        this.playerlist = playerlist;
        this.dictionary = dictionary;

        if (board == null)
            this.gameboard = new Gameboard();
        else
            this.gameboard = new Gameboard(board);

        this.minWordLen = minWordLen;
        this.winScore = winScore;

        this.console = console;
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

        for (;
            skipChain < playerlist.size() * 2;
            atPlayer = (atPlayer+1) % playerlist.size()
        ) try {
            Terminal.clearScreen();

            Player currentPlayer = playerlist.get(atPlayer);

            char[][] board = gameboard.board;
            for (char[] row : board) {
                System.out.println('\n');
                for (char letter : row) {
                    System.out.print("   "+letter);
                }
            }

            System.out.println("\n\n");

            System.out.println("==== Played Words ====");
            int lineLength = 0;
            for (String word : playedWordList) {
                if (lineLength / 60 >= 1) {
                    System.out.println();
                    lineLength = 0;
                }
                System.out.print(word+" ");
                lineLength += word.length()+1;
            }
            if (playedWordList.size() == 0) 
                System.out.println("(No played words yet)");

            System.out.println("\n\n");

            System.out.printf("It's %s's turn\n\n", currentPlayer.getName());

            String move = currentPlayer.nextMove(gameboard, dictionary, playedWordList);

            System.out.println();

            if (move == null) {
                System.out.println(currentPlayer.getName() + " skipped their turn!");
                skipChain++;
                continue;
            }

            move = move.toUpperCase();

            if (move.length() < minWordLen) {
                System.out.printf("Word too short! Minimal word length is %d\n", minWordLen);
                atPlayer--;
                continue;
            }

            if (playedWords.contains(move)) {
                System.out.println("This word was already played! Try a different one.");
                atPlayer--;
                continue;
            }

            if (!dictionary.contains(move)) {
                System.out.println("Word not in wordlist! Try a different one.");
                atPlayer--;
                continue;
            }

            if (!gameboard.wordExists(move)) {
                System.out.println("Word does not exist on board! Try a different one");
                atPlayer--;
                continue;
            }

            skipChain = 0;

            dictionary.remove(move);    // totally neccesary optimization
            playedWords.add(move);
            playedWordList.add(move);

            int scoreGained = move.length();
            System.out.printf("%s played the word '%s' and gained +%d score!\n", currentPlayer.getName(), move, scoreGained);

            scoreboard.put(currentPlayer, scoreboard.get(currentPlayer)+scoreGained);

            if (winScore > 0 && scoreboard.get(currentPlayer) >= winScore)
                break;
        } finally {
            System.out.println("\n==== Press enter to continue ====");
            console.nextLine();
        }

        Terminal.clearScreen();
        Terminal.hideCursor();

        FileInputStream stream = new FileInputStream("ui/results_head.txt");
        stream.transferTo(System.out);
        stream.close();

        playerlist.sort((a, b) -> scoreboard.get(b) - scoreboard.get(a));
        for (Player player : playerlist) {
            String name = player.getName();
            int score = scoreboard.get(player);

            System.out.println(
                StringUtils.padEnd(String.format("┃    %s", name), 49, ' ') +
                StringUtils.padStart(String.format("%d    ┃", score), 22, ' ')
            );
            System.out.println("┃    ─────────────────────────────────────────────────────────────    ┃");
        }

        stream = new FileInputStream("ui/results_tail.txt");
        stream.transferTo(System.out);
        stream.close();

        console.nextLine();
    }
}
