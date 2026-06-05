package boogle.player;

import java.util.*;

import boogle.core.*;
import boogle.core.Gameboard.GameboardWalker;
import boogle.util.FastOrderedSet;
import boogle.util.Tree;

/**
 * Computer-controlled player that searches the board for legal words.
 *
 * <p>The AI builds a trie from the active dictionary, walks the board to find all
 * words that can be played, and stores the results ordered by length. Difficulty
 * levels control how much of that ordered list remains available and how strongly
 * the AI favors the better half of its remaining moves.</p>
 */
public class AIPlayer implements Player {

    /**
     * Difficulty levels from strongest to weakest.
     *
     * <p>The numeric value is used by the text and graphical settings screens.
     * Higher levels retain more possible words and choose stronger moves.</p>
     */
    public enum Level {
        /** Finds and can play every possible word, preferring the strongest word. */
        PERFECT(5),
        /** Keeps a large share of possible words and favors stronger choices. */
        SMART(4),
        /** Keeps a moderate share of possible words with mixed move quality. */
        GOOD(3),
        /** Keeps fewer possible words and tends toward weaker choices. */
        NORMAL(2),
        /** Keeps only a small share of possible words and prefers weak choices. */
        DUMB(1);

        private final int value;

        Level(int value) {
            this.value = value;
        }

        /**
         * Converts a menu value to a difficulty level.
         *
         * @param value integer value from 1 to 5
         * @return matching level
         * @throws IllegalArgumentException if no level has the supplied value
         */
        public static Level fromValue(int value) {
            for (Level l : Level.values()) {
                if (l.getValue() == value) {
                    return l;
                }
            }
            throw new IllegalArgumentException();
        }

        /**
         * Returns the menu value for this difficulty.
         *
         * @return integer value from 1 to 5
         */
        public int getValue() {
            return value;
        }
    }

    /** Current AI difficulty level. */
    private Level level;
    /**
     * Returns this AI's current difficulty level.
     *
     * @return configured difficulty
     */
    public Level getLevel() { return this.level; }
    /**
     * Changes this AI's difficulty level.
     *
     * @param level new difficulty
     */
    public void setLevel(Level level) { this.level = level; }

    /** Remaining playable words ordered from shortest to longest. */
    private FastOrderedSet<String> available = null;

    /** Board supplied by the game engine for move computation. */
    private Gameboard board;
    /** Dictionary supplied by the game engine for move computation. */
    private Set<String> dictionary;

    /** Random source used for non-perfect move selection. */
    private Random random = new Random();

    /** Player display name. */
    private String name;


    /**
     * Sets the display name used in UI screens and leaderboards.
     *
     * @param name new player name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns this AI player's display name.
     *
     * @return player name
     */
    public String getName() {
        return name;
    }

    /**
     * Creates an AI player with a name and difficulty.
     *
     * @param name display name
     * @param level initial difficulty
     */
    public AIPlayer(String name, Level level) {
        this.level = level;
        this.name = name;
    }

    /**
     * Builds the set of currently playable words for this AI.
     *
     * <p>The dictionary is converted into a trie, the board is searched from each
     * matching starting cell, and the result list is sorted by length. The list is
     * then trimmed according to the AI level so weaker levels have fewer strong
     * moves available.</p>
     *
     * @throws IllegalStateException if the board or dictionary has not been set
     */
    private void initialize() {
        if (board == null || dictionary == null) {
            throw new IllegalStateException("Cannot initialize without game states");
        }

        this.available = new FastOrderedSet<>();

        Tree<Character> trieRoot = new Tree<>(null);
        
        // build trie of wordlist
        for (String word : dictionary) {
            Tree<Character> currentRoot = trieRoot;

            // optimization to avoid repeated method calls
            int wordLen = word.length();

            for (int i = 0; i < wordLen; i++) {
                char letter = word.charAt(i); 

                Tree<Character> child = currentRoot.getChild(letter);

                if (child == null) {
                    child = new Tree<>(letter);
                    currentRoot.addChild(child);
                }

                currentRoot = child;

                // last char, add a terminating node if doesnt exist already
                if (i == wordLen-1 && child.getChild(null) == null) {
                    child.addChild(new Tree<>(null));
                }
            }
        }

        // dfs the entire board according to trie
        ArrayList<String> possibilities = new ArrayList<>();
        for (int y = 0; y < board.board.length; y++) {
            for (int x = 0; x < board.board[y].length; x++) {
                Tree<Character> letterRoot = trieRoot.getChild(board.board[y][x]);

                if (letterRoot == null) continue;

                HashSet<String> found = dfs(
                    board.new GameboardWalker(x, y),
                    letterRoot, new HashSet<>()
                );
                possibilities.addAll(found);
            }
        }

        // finally, sort possible words by length, trim based on level, and insert into available
        possibilities.sort((a, b) -> a.length() - b.length());
        int trimFrom = -1;
        switch (this.level) {
            case PERFECT: trimFrom = possibilities.size(); break;
            
            case SMART: trimFrom = (int)(
                possibilities.size() * ((0.9-0.7) * Math.random() + 0.7)
            ); break;
            
            case GOOD: trimFrom = (int)(
                possibilities.size() * ((0.7-0.5) * Math.random() + 0.5)
            ); break;

            case NORMAL: trimFrom = (int)(
                possibilities.size() * ((0.5-0.2) * Math.random() + 0.2)
            ); break;

            case DUMB: trimFrom = (int)(
                possibilities.size() * ((0.2-0.1) * Math.random() + 0.1)
            ); break;
        }

        possibilities.subList(trimFrom, possibilities.size()).clear();

        this.available.addAll(possibilities);
    }
    
    /**
     * Searches the board and dictionary trie from one current walker position.
     *
     * @param walker current board path
     * @param trie trie node matching the walker's current letter
     * @param collected mutable set of words found so far
     * @return {@code collected}, after adding every word found below this path
     */
    private static HashSet<String> dfs(GameboardWalker walker, Tree<Character> trie, HashSet<String> collected) {
        if (trie.getChild(null) != null)
            collected.add(walker.journey());

        for (GameboardWalker.Direction dir : GameboardWalker.Direction.values()) {
            if (!walker.step(dir)) continue;

            Tree<Character> nextTrie = trie.getChild(walker.here());
            if (nextTrie != null) {
                dfs(walker, nextTrie, collected);
            }

            walker.backtrack();
        }

        return collected;
    }

    /**
     * Chooses the next AI move from remaining available words.
     *
     * @return a word move when at least one word remains, or a leave move when
     *         the AI has no legal words left
     */
    public Player.Move nextMove() {
        if (this.available == null)
            initialize();

        if (available.size() == 0) 
            return new Player.Move(Player.Move.Type.LEAVE);

        // make a move according to the AI level
        switch(this.level) {
            case PERFECT: return new Player.Move(Player.Move.Type.WORD, available.get(available.size()-1));

            case DUMB: return new Player.Move(Player.Move.Type.WORD, available.get(0));

            case SMART:
            case GOOD:
            case NORMAL: {
                if (available.size() == 0) break;

                int half = available.size() / 2;
                int target = random.nextInt(available.size() - half);

                boolean useUpperHalf = 
                    level!=Level.SMART ? 
                        level!=Level.NORMAL ?
                            random.nextBoolean() :
                        false :
                    true;
                
                int i = 0;
                for (String move : useUpperHalf ? available.reverse() : available) {
                    if (i == target)
                        return new Player.Move(Player.Move.Type.WORD, move);
                    i++;
                }
            }
        }

        return null;
    }

    /**
     * Supplies the board and dictionary used to compute AI moves.
     *
     * @param board active game board
     * @param dictionary active dictionary of remaining legal words
     */
    public void setGame(Gameboard board, Set<String> dictionary) {
        this.board = board;
        this.dictionary = dictionary;
    }

    /**
     * Removes an accepted word from this AI's available move set.
     *
     * @param wordPlayed word accepted on the previous turn
     * @param nextMove name of the next player; ignored by this implementation
     */
    public void updateGameState(String wordPlayed, String nextMove) {
        if (available == null)
            initialize();

        available.remove(wordPlayed);
    }

    /**
     * Computes the total score represented by every possible word on a board.
     *
     * @param board board to search
     * @param dictionary dictionary of candidate words
     * @return sum of lengths of all words a perfect AI can find
     */
    public static int computeMaxScore(Gameboard board, Set<String> dictionary) {
        AIPlayer player = new AIPlayer("4chan.org", Level.PERFECT);     // we love 4chan
        
        player.setGame(board, dictionary);
        player.initialize();

        int result = 0;

        for (String move : player.available) {
            result += move.length();
        }
        
        return result;
    }

    /**
     * Computes every playable word on a board.
     *
     * <p>The returned list is ordered the same way the perfect AI stores moves:
     * shortest words first and longest words last.</p>
     *
     * @param board board to search
     * @param dictionary dictionary of candidate words
     * @return list of all playable words
     */
    public static List<String> computePossibleMoves(Gameboard board, Set<String> dictionary) {
        AIPlayer player = new AIPlayer("4chan.org", Level.PERFECT);     // we love 4chan
        
        player.setGame(board, dictionary);
        player.initialize();

        return new ArrayList<>(player.available);
    }
}
