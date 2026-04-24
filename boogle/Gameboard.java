package boogle;

import java.util.*;

public class Gameboard {

    private static final String[][] template = {
        { "AAAFRS", "AEEGMU", "CEIILT", "DHHNOT", "FIPRSY" },
        { "AAEEEE", "AEGMNN", "CEILPT", "DHLNOR", "GORRVW" },
        { "AAFIRS", "AFIRSY", "CEIPST", "EIIITT", "HIPRRY" },
        { "ADENNN", "BJKQXZ", "DDLNOR", "EMOTTT", "NOOTUW" },
        { "AEEEEM", "CCNSTW", "DHHLOR", "ENSSSU", "OOOTTU" },
    };

    public static class GameboardWalker {
        final int x, y;

        private Gameboard gb;

        private HashSet<Integer> visited;

        public GameboardWalker(Gameboard board, int x, int y) {
            this(board, x, y, new HashSet<>());
        }

        private GameboardWalker(Gameboard board, int x, int y, HashSet<Integer> visited) {
            this.gb = board;

            if (!isPosValid(x, y))
                throw new IllegalArgumentException("GameboardWalker got invalid position ("+x+", "+y+")");

            this.x = x;
            this.y = y;
            this.visited = new HashSet<>(visited);

            this.visited.add(pair(x, y));
        }

        public char here() {
            return gb.board[y][x];
        }

        public enum Direction {
            UP_LEFT,    UP,         UP_RIGHT,
            LEFT,    /*center*/     RIGHT,
            DOWN_LEFT,  DOWN,       DOWN_RIGHT
        }
        public GameboardWalker step(Direction dir) {
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
                return null;

            // check if grid already visited
            if (visited.contains(pair(x ,y))) 
                return null;

            // all clear, return new walker
            return new GameboardWalker(this.gb, x, y, this.visited);
        }

        /* pairing function, with base-N encoding.
         * calculate a unique integer from two given ints, considering order
        */
        private int pair(int x, int y) {
            return x * (gb.board.length + 1) + y;
        }

        // check if position is in-bound
        private boolean isPosValid(int x, int y) {
            return (y < gb.board.length && y >= 0) &&
                   (x < gb.board[y].length && x >= 0);
        }
    }
    
    char[][] board;
    private HashMap<Character, ArrayList<int[]>> charLocs;

    public Gameboard() {
        board = new char[template.length][template[0].length];
        charLocs = new HashMap<>();

        Random rand = new Random();

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                String charset = template[i][j];
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
            if (dfs(new GameboardWalker(this, loc[0], loc[1]), wordChars, 1))
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
            GameboardWalker next = walker.step(dir);

            if (next == null) continue;

            if (next.here() == word[index]) {
                if (dfs(next, word, index + 1)) {
                    return true;
                }
            }
        }

        return false;
    }
}
