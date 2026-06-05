package boogle.core;

import java.util.*;
import java.nio.file.*;
import java.io.*;

import boogle.player.*;

/**
 * Top-level coordinator that moves between lobby setup, saved-game loading, and
 * game execution.
 *
 * <p>The launcher owns the active {@link GameUI}, the mutable game options, and
 * the current {@link GameMaster}. It is serializable so a running game can be
 * saved and later restored with a newly supplied UI instance.</p>
 */
public class Launcher implements Serializable {
    /**
     * Mutable settings collected by a UI before a game starts.
     */
    public static class GameOptions implements Serializable {
        /**
         * Creates an empty options container for a UI or caller to populate.
         */
        public GameOptions() {
        }

        /** Ordered players participating in the next game. */
        public ArrayList<Player> playerlist;

        /** Path to the dictionary file, read one word per line. */
        public String wordlistPath;

        /** Optional custom board; {@code null} means generate a random board. */
        public char[][] customBoard;

        /** Minimum legal word length; zero means no minimum. */
        public int minWordLength;

        /** Score needed to win; zero means the game has no score target. */
        public int winScore;
    
        /** Loaded launcher that should replace this one before play resumes. */
        public Launcher replacement;
    }

    /** Active UI; transient because a restored launcher receives a fresh UI. */
    protected transient GameUI ui;

    /** Current launcher options, retained across lobby/game cycles. */
    private GameOptions options;

    /** Current or restored game master, when a game is in progress. */
    private GameMaster gm;

    /**
     * Creates a launcher bound to a user-interface implementation.
     *
     * @param ui UI used for lobby interaction, turns, and results
     */
    public Launcher(GameUI ui) {
        this.ui = ui;
    }

    /**
     * Starts the lobby/game cycle using mutable options.
     *
     * <p>The method repeatedly shows the lobby, rebuilds AI players so their
     * transient search state is clean, loads the word list, creates a game
     * master, and runs a game. If the lobby loads a save file, the loaded
     * launcher replaces this launcher's state before continuing.</p>
     *
     * @param options initial game options to present in the lobby
     */
    public void start(GameOptions options) {
        this.options = options;
        for (;;) { try {
            options = this.options;
            System.out.println(gm);

            if (this.gm != null) {
                this.gm.begin();
                this.gm = null;
                continue;
            }

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

            if (options.replacement != null) {
                this.replace(options.replacement);
                continue;
            }

            HashSet<String> dictionary = new HashSet<>();

            Scanner wordlist = new Scanner(new File(options.wordlistPath));
            while (wordlist.hasNext()) {
                String word = wordlist.nextLine();

                if (word.length() < options.minWordLength)
                continue;

                dictionary.add(word.toUpperCase());
            }
            wordlist.close();

            this.gm = new GameMaster(options.playerlist, dictionary, options.customBoard, options.minWordLength, options.winScore, this);
            this.gm.begin();
            this.gm = null;
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
     * Writes a generated tournament board and its complete ordered answer list.
     *
     * <p>The board output contains the board grid. The word-list output contains
     * every word a perfect AI can find on that board, numbered from highest-value
     * words to lowest-value words.</p>
     *
     * @param options source options for dictionary path, minimum word length, and
     *        optional custom board
     * @param boardOutPath destination path for the printable board grid
     * @param wordlistOutPath destination path for numbered possible moves
     * @throws IOException if any input or output file cannot be read or written
     */
    public static void writeTournamentFiles(GameOptions options, String boardOutPath, String wordlistOutPath) throws IOException{
        HashSet<String> dictionary = new HashSet<>();

        try (Scanner wordlist = new Scanner(new File(options.wordlistPath))) {
            while (wordlist.hasNext()) {
                String word = wordlist.nextLine();

                if (word.length() < options.minWordLength)
                continue;

                dictionary.add(word.toUpperCase());
            }
        }

        Gameboard gameboard;
        if (options.customBoard == null)
            gameboard = new Gameboard();
        else
            gameboard = new Gameboard(options.customBoard);

        List<String> moves = AIPlayer.computePossibleMoves(gameboard, dictionary);

        try (PrintWriter boardout = new PrintWriter(boardOutPath)) {
            for (char[] row : gameboard.board) {
                for (char letter : row) {
                    boardout.append(letter);
                    boardout.append(' ');
                }
                boardout.append('\n');
            }

            boardout.flush();
        }

        try (PrintWriter wordlistOut = new PrintWriter(wordlistOutPath)) {
            int counter = 1;
            for (int i = moves.size()-1; i >= 0; i--) {
                wordlistOut.write(String.format("%d. %s\n", counter, moves.get(i)));
                counter++;
            }

            wordlistOut.flush();
        }
    }

    /**
     * Loads a whitespace-separated board file.
     *
     * <p>Every row must contain the same number of one-character cells. Letters
     * are uppercased as they are loaded. The method prints the parsing error and
     * returns {@code null} instead of throwing when the file is invalid.</p>
     *
     * @param path file containing a board grid
     * @return loaded board, or {@code null} if the file could not be parsed
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

    /**
     * Replaces this launcher's serializable state with a loaded launcher.
     *
     * @param replacement launcher loaded from a save file
     */
    private void replace(Launcher replacement) {
        this.options = replacement.options;
        this.ui = replacement.ui;
        this.gm = replacement.gm;
        this.options.replacement = null;
    }

    /**
     * Serializes this launcher, including any in-progress game master.
     *
     * @param out destination stream for Java object serialization
     * @throws IOException if the launcher cannot be written
     */
    public void serialize(OutputStream out) throws IOException {
        try (ObjectOutputStream objout = new ObjectOutputStream(out)){
            objout.writeObject(this);
        }
    }

    /**
     * Reads a saved launcher and attaches it to the current UI instance.
     *
     * @param in serialized launcher input stream
     * @param ui UI that should control the restored launcher
     * @return restored launcher with its transient UI field repopulated
     * @throws IOException if deserialization fails while reading bytes
     * @throws ClassNotFoundException if the save file references unavailable
     *         classes
     */
    public static Launcher fromSerialized(InputStream in, GameUI ui) throws IOException, ClassNotFoundException {
        try (ObjectInputStream objin = new ObjectInputStream(in)) {
            Launcher launcher = (Launcher) objin.readObject();

            launcher.ui = ui;

            return launcher;
        }
    }

}
