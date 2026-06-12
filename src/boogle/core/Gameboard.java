/*
 * File: Gameboard.java
 * Author: Ethan Ding
 * Description: Represents the Boogle board and provides helpers for generating boards and validating word paths.
 */

package boogle.core;

import java.io.Serializable;
import java.util.*;

import boogle.util.*;

/**
 * Serializable Boogle board and word-search helper.
 *
 * <p>A board is a rectangular grid of uppercase letters. The no-argument
 * constructor builds the standard 5-by-5 board by drawing each die from the
 * configured dice set once and then selecting a random face. The custom-board
 * constructor accepts a caller-provided grid and indexes it for fast lookup.</p>
 *
 * <p>The main validation API is {@link #wordExists(String)}, which checks
 * whether a word can be formed by walking through adjacent cells without using
 * the same cell twice in one word.</p>
 */
public class Gameboard implements Serializable {

    /** Dice face definitions used when randomly generating a board. */
    private final Raffle<String> diceSet = new Raffle<>(Arrays.asList(
        "AAAFRS", "AEEGMU", "CEIILT", "DHHNOT", "FIPRSY", 
        "AAEEEE", "AEGMNN", "CEILPT", "DHLNOR", "GORRVW", 
        "AAFIRS", "AFIRSY", "CEIPST", "EIIITT", "HIPRRY", 
        "ADENNN", "BJKQXZ", "DDLNOR", "EMOTTT", "NOOTUW", 
        "AEEEEM", "CCNSTW", "DHHLOR", "ENSSSU", "OOOTTU"
    ));

    private static final int BOARD_HEIGHT = 5;
    private static final int BOARD_WIDTH = 5;

    /**
     * Mutable cursor used by depth-first search to walk the board.
     *
     * <p>The walker records every visited cell in insertion order so that search
     * paths can be extended, backtracked, and converted into the string they
     * spell. A walker never allows a step outside the board or back onto a cell
     * that is already part of the current path.</p>
     */
    public class GameboardWalker implements Serializable {
        /** Current zero-based column. */
        private int x;
        /** Current zero-based row. */
        private int y;

        /** Encoded coordinates visited by this path in traversal order. */
        private FastOrderedSet<Integer> visited = new FastOrderedSet<>();

        /**
         * Creates a walker positioned on a valid board cell.
         *
         * @param x zero-based column index
         * @param y zero-based row index
         * @throws IllegalArgumentException if the coordinate is outside the board
         */
        public GameboardWalker(int x, int y) {
            if (!isPosValid(x, y))
                throw new IllegalArgumentException("GameboardWalker got invalid position ("+x+", "+y+")");

            this.x = x;
            this.y = y;

            this.visited.add(pair(x, y));
        }

        /**
         * Returns the letter at the walker's current cell.
         *
         * @return current board letter
         */
        public char here() {
            return board[y][x];
        }

        /**
         * Directions in which a word path may advance from a board cell.
         */
        public enum Direction {
            /** Move one row up and one column left. */
            UP_LEFT,
            /** Move one row up. */
            UP,
            /** Move one row up and one column right. */
            UP_RIGHT,
            /** Move one column left. */
            LEFT,
            /** Move one column right. */
            RIGHT,
            /** Move one row down and one column left. */
            DOWN_LEFT,
            /** Move one row down. */
            DOWN,
            /** Move one row down and one column right. */
            DOWN_RIGHT
        }
        /**
         * Attempts to move one cell in the requested direction.
         *
         * @param dir direction to move
         * @return {@code true} when the move was applied, or {@code false} when
         *         it would leave the board or revisit the current path
         */
        public boolean step(Direction dir) {
            int x, y;

            // new y position
            switch (dir) {
                case UP: case UP_LEFT: case UP_RIGHT:
                    y = this.y - 1;
                break;

                case DOWN: case DOWN_LEFT: case DOWN_RIGHT:
                    y = this.y + 1;
                break;

                case LEFT: case RIGHT: default:
                    y = this.y;
                break;
            }

            // new x position
            switch (dir) {
                case LEFT: case UP_LEFT: case DOWN_LEFT:
                    x = this.x - 1;
                break;

                case RIGHT: case UP_RIGHT: case DOWN_RIGHT:
                    x = this.x + 1;
                break;

                case UP: case DOWN: default:
                    x = this.x;
                break;
            }

            // check position
            if (!isPosValid(x, y))
                return false;

            // check if grid already visited
            if (visited.contains(pair(x ,y))) 
                return false;

            // all clear, update state
            this.x = x;
            this.y = y;
            this.visited.add(pair(x, y));

            return true;
        }

        /**
         * Removes the current cell from the path and returns to the previous cell.
         *
         * @return {@code true} when backtracking succeeded, or {@code false} when
         *         the walker was already at its starting cell
         */
        public boolean backtrack() {
            if (visited.size() <= 1)
                return false;

            visited.pop();

            int[] cords = unpair(visited.get(visited.size() - 1));

            this.x = cords[0];
            this.y = cords[1];
            
            return true;
        }

        /**
         * Builds the word spelled by the path visited so far.
         *
         * @return letters from the starting cell through the current cell
         */
        public String journey() {
            StringBuilder builder = new StringBuilder(this.visited.size());

            for (int encoded : this.visited) {
                int[] cords = unpair(encoded);

                builder.append(board[cords[1]][cords[0]]);
            }

            return builder.toString();
        }

        /**
         * Encodes a coordinate pair into a single integer key.
         *
         * @param x zero-based column
         * @param y zero-based row
         * @return reversible integer representation for the coordinate
         */
        private int pair(int x, int y) {
            return x * board.length + y;
        }

        /**
         * Decodes a coordinate produced by {@link #pair(int, int)}.
         *
         * @param pair encoded coordinate value
         * @return two-element array containing {@code x} then {@code y}
         */
        private int[] unpair(int pair) {
            int N = board.length;
            return new int[]{
                pair / N,   // x = floor(z/N). integer division already truncates correctly so we leave out the floor
                pair % N    // y = z mod N
            };
        }

        /**
         * Checks whether a coordinate is inside the current board.
         *
         * @param x zero-based column
         * @param y zero-based row
         * @return {@code true} when the coordinate references an existing cell
         */
        private boolean isPosValid(int x, int y) {
            return (y < board.length && y >= 0) &&
                   (x < board[y].length && x >= 0);
        }
    }
    
    /**
     * Public board grid used by the UI renderers and AI search code.
     */
    public char[][] board;
    // Yes this is abstraction leak, but It'll also be unneccesarily expensive
    // if we were to use a private value and copy on every get().
    // So we are just gonna trust caller here

    /** Map from a letter to all board coordinates containing that letter. */
    private HashMap<Character, ArrayList<int[]>> charLocs;

    /**
     * Creates a random standard 5-by-5 board from the configured Boogle dice.
     */
    public Gameboard() {
        board = new char[BOARD_HEIGHT][BOARD_WIDTH];
        charLocs = new HashMap<>();

        Random rand = new Random();

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                String charset = diceSet.draw();
                board[i][j] = charset.charAt(rand.nextInt(charset.length()));

                ArrayList<int[]> locs = charLocs.get(board[i][j]);
                if (locs == null) {
                    locs = new ArrayList<>();
                    charLocs.put(board[i][j], locs);
                }

                locs.add(new int[]{ j, i });
            }
        }
    }

    /**
     * Creates a board from a supplied grid.
     *
     * <p>The grid is stored directly, not copied, and each character is indexed
     * by location for faster first-letter lookup during validation.</p>
     *
     * @param board rectangular or jagged grid of board letters
     */
    public Gameboard(char[][] board) {
        charLocs = new HashMap<>();

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                ArrayList<int[]> locs = charLocs.get(board[i][j]);
                if (locs == null) {
                    locs = new ArrayList<>();
                    charLocs.put(board[i][j], locs);
                }

                locs.add(new int[]{ j, i });
            }
        }

        this.board = board;
    }

    /**
     * Determines whether a word can be formed on this board.
     *
     * <p>Search starts from every cell containing the first letter and then walks
     * through all eight neighboring directions. A cell may appear at most once in
     * a single word path.</p>
     *
     * @param word uppercase word to search for; an empty string never exists
     * @return {@code true} if the word can be formed, otherwise {@code false}
     */
    public boolean wordExists(String word) {
        if (word.length() == 0)
            return false;

        char[] wordChars = word.toCharArray();

        ArrayList<int[]> locs = charLocs.get(wordChars[0]);

        if (locs == null)
            return false;

        for (int[] loc : locs) {
            if (dfs(this.new GameboardWalker(loc[0], loc[1]), wordChars, 1))
                return true;
        }

        return false;
    }

    /**
     * Recursively searches for the remaining suffix of a word from a walker.
     *
     * @param walker current path through the board
     * @param word target word as characters
     * @param index index of the next character to match
     * @return {@code true} when the suffix can be completed from this path
     */
    private boolean dfs(GameboardWalker walker, char[] word, int index) {
        if (index == word.length) {
            return true;
        }

        for (GameboardWalker.Direction dir : GameboardWalker.Direction.values()) {
            if (!walker.step(dir)) continue;

            if (walker.here() == word[index]) {
                if (dfs(walker, word, index + 1)) {
                    return true;
                }
            }

            walker.backtrack();
        }

        return false;
    }
}
