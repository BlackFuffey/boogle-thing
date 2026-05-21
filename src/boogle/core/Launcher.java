package boogle.core;

import java.util.*;
import java.nio.file.*;
import java.io.*;

import boogle.player.*;

/**
 * Launches a new Boogle game. The launcher holds a reference to a
 * {@link GameUI} and exposes methods for configuring game options, reading
 * the dictionary and board files and starting the {@link GameMaster}. It
 * serves as the bridge between user configuration in the lobby and the
 * underlying game engine.
 */
public class Launcher {
    /**
     * Mutable container of options that influence how a game is played. An
     * instance of this class is passed to the UI to allow the user to set
     * players, choose a dictionary and adjust rule parameters before the
     * game begins. None of these fields are final so that the lobby may
     * update them directly.
     */
    public static class GameOptions {
        /** List of players in turn order. Must be non‑empty before starting. */
        public ArrayList<Player> playerlist;
        /** Path to the dictionary file from which to load valid words. */
        public String wordlistPath;
        /**
         * Optional custom board. When {@code null} the board will be
         * generated randomly. When non‑null the array’s contents are used
         * directly and must represent a rectangular matrix of uppercase
         * letters.
         */
        public char[][] customBoard;
        /** Minimum length for any word that may be played. */
        public int minWordLength;
        /**
         * Score threshold required to win. A value of zero indicates that
         * there is no winning score and the game ends only when skip limits
         * are reached. Each point corresponds to one letter in a valid word.
         */
        public int winScore;
    }

    /** User interface used to interact with the user during lobby and game. */
    private GameUI ui;

    /**
     * Constructs a launcher bound to a specific user interface. The launcher
     * does not take ownership of the UI; the caller remains responsible for
     * closing it.
     *
     * @param ui the user interface to use for this session
     */
    public Launcher(GameUI ui) {
        this.ui = ui;
    }

    /**
     * Starts the game using the supplied options. This method repeatedly
     * presents the lobby, constructs the dictionary, instantiates a
     * {@link GameMaster} and runs the game. It replaces each {@link AIPlayer}
     * in the {@code playerlist} with a fresh instance configured with the
     * same name and level so that state from previous rounds is not leaked.
     * If any exception is thrown during setup or execution, the UI is
     * closed and the method returns to the caller.
     *
     * @param options mutable configuration prepared by the UI
     */
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

    /**
     * Loads a custom game board from a text file. The file must contain
     * whitespace‑separated characters, one row per line. All rows must have
     * the same number of columns and each token must consist of exactly one
     * character. Characters are converted to uppercase for consistency.
     *
     * @param path file system path to the board definition
     * @return a two‑dimensional array representing the board or {@code null}
     *         if the file could not be parsed; error messages will be
     *         printed to standard output when parsing fails
     */
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
