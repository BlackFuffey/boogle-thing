package boogle.core;

import java.util.*;
import java.nio.file.*;
import java.io.*;

import boogle.player.*;

public class Launcher {
    public static class GameOptions {
        public ArrayList<Player> playerlist;
        public String wordlistPath;
        public char[][] customBoard;
        public int minWordLength;
        public int winScore;
    }

    private GameUI ui;

    public Launcher(GameUI ui) {
        this.ui = ui;
    }

    public void start(GameOptions options) {
        for (;;) { try {
            for (int i = 0; i < options.playerlist.size(); i++) {
                Player player = options.playerlist.get(i);
                if (player instanceof AIPlayer) {
                    AIPlayer oldAI = (AIPlayer) player;
                    AIPlayer newAI = new AIPlayer(oldAI.getName(), oldAI.getLevel());

                    options.playerlist.set(i, newAI);
                }
            }

            if (!this.ui.lobby(options))
                return;

            HashSet<String> dictionary = new HashSet<>();

            Scanner wordlist = new Scanner(new File(options.wordlistPath));
            while (wordlist.hasNext()) {
                String word = wordlist.nextLine();

                if (word.length() < options.minWordLength)
                continue;

                dictionary.add(word.toUpperCase());
            }
            wordlist.close();

            GameMaster gm = new GameMaster(options.playerlist, dictionary, options.customBoard, options.minWordLength, options.winScore, ui);
            gm.begin();
        } catch (Exception e) {
            try { ui.close(); }
            catch (Exception e2){
                e2.printStackTrace();
            }
            e.printStackTrace();
            return;
        } }
    }

    public static char[][] loadGameboardFile(String path) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(path));

            char[][] result = new char[lines.size()][];

            int width = -1;
            for (int i = 0; i < lines.size(); i++) {
                String[] chars = lines.get(i).split("\\s+");
                
                if (width == -1)
                    width = chars.length;
                else if (width != chars.length)
                    throw new IOException(
                        String.format(
                            "Malformed gameboard file: row %d is not of the expected length %d (got %d instead)",
                            i+1, width, chars.length
                        )
                    );

                char[] row = new char[chars.length];

                for (int j = 0; j < chars.length; j++) {
                    if (chars[j].length() != 1)
                        throw new IOException(
                            String.format(
                                "Malformed gameboard file: found multiple characters in one grid at (%d, %d)",
                                i+1, j+1
                            )
                        );

                    row[j] = Character.toUpperCase(chars[j].charAt(0));
                }

                result[i] = row;
            }

            return result;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

}
