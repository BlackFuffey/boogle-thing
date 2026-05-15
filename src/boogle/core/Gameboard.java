package boogle.core;

import java.util.*;

import boogle.util.*;

public class Gameboard {

    private final Raffle<String> diceSet = new Raffle<>(Arrays.asList(
        "AAAFRS", "AEEGMU", "CEIILT", "DHHNOT", "FIPRSY", 
        "AAEEEE", "AEGMNN", "CEILPT", "DHLNOR", "GORRVW", 
        "AAFIRS", "AFIRSY", "CEIPST", "EIIITT", "HIPRRY", 
        "ADENNN", "BJKQXZ", "DDLNOR", "EMOTTT", "NOOTUW", 
        "AEEEEM", "CCNSTW", "DHHLOR", "ENSSSU", "OOOTTU"
    ));

    private static final int BOARD_HEIGHT = 5;
    private static final int BOARD_WIDTH = 5;

    public class GameboardWalker {
        private int x, y;

        private FastOrderedSet<Integer> visited = new FastOrderedSet<>();

        public GameboardWalker(int x, int y) {
            if (!isPosValid(x, y))
                throw new IllegalArgumentException("GameboardWalker got invalid position ("+x+", "+y+")");

            this.x = x;
            this.y = y;

            this.visited.add(pair(x, y));
        }

        public char here() {
            return board[y][x];
        }

        public enum Direction {
            UP_LEFT,    UP,         UP_RIGHT,
            LEFT,    /*center*/     RIGHT,
            DOWN_LEFT,  DOWN,       DOWN_RIGHT
        }
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

        public boolean backtrack() {
            if (visited.size() <= 1)
                return false;

            visited.pop();

            int[] cords = unpair(visited.get(visited.size() - 1));

            this.x = cords[0];
            this.y = cords[1];
            
            return true;
        }

        public String journey() {
            StringBuilder builder = new StringBuilder(this.visited.size());

            for (int encoded : this.visited) {
                int[] cords = unpair(encoded);

                builder.append(board[cords[1]][cords[0]]);
            }

            return builder.toString();
        }

        /* pairing function & inverse, with base-N encoding.
         * calculate a unique integer from two given ints, considering order
         * Formula: z = xN + y, where N is larger than max possible value of y
        */
        private int pair(int x, int y) {
            return x * board.length + y;
        }

        private int[] unpair(int pair) {
            int N = board.length;
            return new int[]{
                pair / N,   // x = floor(z/N). integer division already truncates correctly so we leave out the floor
                pair % N    // y = z mod N
            };
        }

        // check if position is in-bound
        private boolean isPosValid(int x, int y) {
            return (y < board.length && y >= 0) &&
                   (x < board[y].length && x >= 0);
        }
    }
    
    // yes i know this is encapsulation leak,
    // but its for convinience and performance sake
    public char[][] board;

    private HashMap<Character, ArrayList<int[]>> charLocs;

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
