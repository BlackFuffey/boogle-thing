package boogle.core;

import java.util.*;

import boogle.util.*;

/**
 * Represents the Boogle playing field. A {@code Gameboard} is a two‑dimensional
 * grid of characters drawn from a predefined set of letter dice. New boards
 * may be generated randomly using a pool of 25 six‑sided dice or constructed
 * from an existing character matrix. The board exposes its internal
 * {@link #board} array directly for performance reasons and maintains a
 * secondary index mapping letters to their coordinates to accelerate word
 * lookup. The {@link GameboardWalker} inner class provides a lightweight
 * cursor for depth‑first traversal when validating whether a word can be
 * formed by adjacent tiles without reusing the same position.
 */
public class Gameboard {

    /**
     * Pool of dice used to generate a random board. Each string in this list
     * encodes the six faces of a single die. The {@link boogle.util.Raffle}
     * ensures that each die is drawn exactly once when constructing a new
     * random board, thereby emulating the real Boggle game.
     */
    private final Raffle<String> diceSet = new Raffle<>(Arrays.asList(
        "AAAFRS", "AEEGMU", "CEIILT", "DHHNOT", "FIPRSY", 
        "AAEEEE", "AEGMNN", "CEILPT", "DHLNOR", "GORRVW", 
        "AAFIRS", "AFIRSY", "CEIPST", "EIIITT", "HIPRRY", 
        "ADENNN", "BJKQXZ", "DDLNOR", "EMOTTT", "NOOTUW", 
        "AEEEEM", "CCNSTW", "DHHLOR", "ENSSSU", "OOOTTU"
    ));

    /**
     * Number of rows in a standard Boogle board. The board is always square
     * so this constant defines both height and width in conjunction with
     * {@link #BOARD_WIDTH}.
     */
    private static final int BOARD_HEIGHT = 5;
    /**
     * Number of columns in a standard Boogle board. The board is always square
     * so this constant defines both width and height in conjunction with
     * {@link #BOARD_HEIGHT}.
     */
    private static final int BOARD_WIDTH = 5;

    /**
     * Cursor object used when performing depth‑first search on a game board.
     * A {@code GameboardWalker} tracks its current {@code x} and {@code y}
     * coordinates along with an ordered set of previously visited positions.
     * Positions are encoded into a single integer via {@link #pair(int, int)}
     * so they can be efficiently stored in a {@link boogle.util.FastOrderedSet}
     * without allocating coordinate objects. The walker offers methods to move
     * in one of eight {@link Direction} values, backtrack to the previous
     * position and produce the sequence of characters visited so far.
     */
    public class GameboardWalker {
        /** The walker’s current column (zero‑based). */
        private int x;
        /** The walker’s current row (zero‑based). */
        private int y;

        /**
         * Ordered set of encoded coordinates that have been visited by this
         * walker. The set prevents the walker from reusing the same tile
         * during a single search and preserves visitation order for
         * reconstruction of the word via {@link #journey()}.
         */
        private FastOrderedSet<Integer> visited = new FastOrderedSet<>();

        /**
         * Creates a new walker positioned at the given coordinates. The
         * starting position is immediately added to the internal visited set.
         *
         * @param x the column index within the board
         * @param y the row index within the board
         * @throws IllegalArgumentException if the provided position lies
         *         outside of the board bounds
         */
        public GameboardWalker(int x, int y) {
            if (!isPosValid(x, y))
                throw new IllegalArgumentException("GameboardWalker got invalid position ("+x+", "+y+")");

            this.x = x;
            this.y = y;

            this.visited.add(pair(x, y));
        }

        /**
         * Returns the character located at the walker’s current position.
         *
         * @return the letter on the board at {@code (x, y)}
         */
        public char here() {
            return board[y][x];
        }

        /**
         * Directions in which a walker may move. These eight values
         * correspond to the relative positions of the eight neighbouring
         * squares around the current tile on a Boogle board. The center
         * position is omitted because the walker always occupies it.
         */
        public enum Direction {
            UP_LEFT,    UP,         UP_RIGHT,
            LEFT,    /*center*/     RIGHT,
            DOWN_LEFT,  DOWN,       DOWN_RIGHT
        }
        /**
         * Attempts to move the walker one step in the specified direction.
         * Movement fails if the resulting position would be outside of the
         * board or if that position has already been visited by this walker.
         * On a successful move the new position is recorded in the
         * {@link #visited} set.
         *
         * @param dir the direction in which to move
         * @return {@code true} if the walker successfully moved; {@code false}
         *         if the move was invalid or would revisit a tile
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
         * Moves the walker back to the previous position in its visitation
         * history. The current position is removed from the visited set. If
         * there is no prior position (i.e. the walker is at its starting
         * location) this method returns {@code false} and the walker remains
         * unchanged.
         *
         * @return {@code true} if the walker backtracked successfully;
         *         {@code false} if there was no earlier position to return to
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
         * Builds the sequence of characters visited by this walker in order
         * from the start position through the most recent step. The sequence
         * does not include any separator characters.
         *
         * @return a string representing the current path through the board
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
         * Encodes a pair of coordinates into a single integer. This pairing
         * function treats the board as a matrix of size {@code N × N} and
         * computes {@code z = x*N + y}. Using a pairing function allows
         * coordinates to be stored in hash‑based collections without
         * allocating arrays for each visited location.
         *
         * @param x the column index
         * @param y the row index
         * @return a unique integer representing the ordered pair
         */
        private int pair(int x, int y) {
            return x * board.length + y;
        }

        /**
         * Decodes an integer produced by {@link #pair(int, int)} back into
         * its constituent {@code x} and {@code y} coordinates. The board
         * dimension is used as the base for modulo and division operations.
         *
         * @param pair the encoded coordinate
         * @return an array of length two where index 0 is {@code x} and
         *         index 1 is {@code y}
         */
        private int[] unpair(int pair) {
            int N = board.length;
            return new int[]{
                pair / N,   // x = floor(z/N). integer division already truncates correctly so we leave out the floor
                pair % N    // y = z mod N
            };
        }

        // check if position is in-bound
        /**
         * Determines whether a potential move stays within the bounds of the
         * current {@link #board}. Both row and column indices must be
         * non‑negative and less than the respective dimensions of the board.
         *
         * @param x the column to test
         * @param y the row to test
         * @return {@code true} if the position lies within the board;
         *         {@code false} otherwise
         */
        private boolean isPosValid(int x, int y) {
            return (y < board.length && y >= 0) &&
                   (x < board[y].length && x >= 0);
        }
    }
    
    /**
     * Two‑dimensional character array representing the board state. The rows
     * and columns of this array correspond directly to the coordinates used
     * throughout the {@code Gameboard}. This field is intentionally public
     * despite violating strict encapsulation because many parts of the code
     * require efficient read access to the grid and the data structure is
     * immutable after construction (letters are never changed during game
     * play).
     */
    public char[][] board;

    /**
     * Reverse lookup from a letter to the list of grid coordinates where that
     * letter appears. Each entry contains arrays of two integers in the form
     * {@code [x, y]}. Maintaining this map significantly reduces the number
     * of starting positions that need to be considered when checking whether
     * a word exists on the board.
     */
    private HashMap<Character, ArrayList<int[]>> charLocs;

    /**
     * Creates a new {@code Gameboard} with random letters. The board will
     * always be {@link #BOARD_HEIGHT}×{@link #BOARD_WIDTH}. Each position is
     * filled by drawing a die from {@link #diceSet} and selecting a random
     * face on that die. The {@link #charLocs} map is populated to track the
     * positions of each letter.
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
     * Creates a new {@code Gameboard} using a pre‑defined grid of letters.
     * The provided array is not copied: the caller is responsible for not
     * modifying the array after construction. The {@link #charLocs} map is
     * rebuilt to reflect the positions of each character.
     *
     * @param board a rectangular matrix of uppercase letters representing the
     *              desired board configuration
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
     * Determines whether the supplied word can be formed on this board. A
     * word exists if its characters can be followed sequentially by moving
     * between orthogonally or diagonally adjacent tiles without revisiting
     * any position. The search is case‑sensitive; callers should convert
     * candidate words to uppercase prior to invoking this method to match
     * the letters stored in the board. This method performs a depth‑first
     * search starting from each occurrence of the word’s first character.
     *
     * @param word the word to search for
     * @return {@code true} if the word can be formed, {@code false} otherwise
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

    // depth first search with our walker class
    /**
     * Recursive helper used by {@link #wordExists(String)} to walk the board
     * according to the supplied word. The search proceeds by attempting to
     * step the provided {@link GameboardWalker} in all directions and
     * matching the next character in the word. If the end of the word is
     * reached the method returns {@code true}. Backtracking occurs
     * automatically via {@link GameboardWalker#backtrack()} when a path does
     * not yield a complete match.
     *
     * @param walker the current walker state
     * @param word an array of characters representing the target word
     * @param index the index within {@code word} of the next character to
     *              match
     * @return {@code true} if a complete match is found from the current
     *         state; {@code false} otherwise
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
