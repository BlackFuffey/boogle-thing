package boogle.core;

import java.util.*;
import java.nio.file.*;
import java.io.*;

public class Launcher {
    public static class GameOptions {
        ArrayList<Player> playerlist;
        String wordlistPath;
        char[][] customBoard;
        int minWordLength;
        int winScore;
    }

    private GameUI ui;

    public Launcher(GameUI ui) {
        this.ui = ui;
    }

    public void start() {
        GameOptions options = this.ui.lobby();
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
