package boogle.player;

import java.util.*;

import boogle.core.*;
import boogle.core.Gameboard.GameboardWalker;
import boogle.util.FastOrderedSet;
import boogle.util.Tree;

/**
 * Automated opponent for Boogle. An {@code AIPlayer} analyses the current
 * dictionary and board to determine all possible words and then selects
 * moves based on a configurable {@link Level} of skill. It maintains a
 * dynamic list of available words that is pruned as words are played or
 * other players move. This class also exposes a utility method to compute
 * the theoretical maximum score obtainable on a given board.
 */
public class AIPlayer implements Player {

    /**
     * Difficulty settings for {@link AIPlayer}s. Each level defines how
     * aggressively the AI prunes its list of candidate words and how it
     * selects among them:
     * <ul>
     *   <li>{@code PERFECT} – keeps all possible words and always plays the
     *       highest‑scoring one. This level will never miss an opportunity.</li>
     *   <li>{@code SMART} – retains roughly 70–90% of the available words and
     *       chooses randomly from the longer half of that list.</li>
     *   <li>{@code GOOD} – keeps about 30–50% of the words and selects
     *       uniformly at random from the entire retained set.</li>
     *   <li>{@code NORMAL} – prunes more aggressively, keeping approximately
     *       10–20% of the words and preferring shorter words over longer ones
     *       by randomly picking from the lower half.</li>
     *   <li>{@code DUMB} – retains only a handful (≈5%) of the shortest
     *       possible words and always plays the worst scoring move.</li>
     * </ul>
     * Each level is associated with an integer value used for user input.
     */
    public enum Level {
        PERFECT(5),
        SMART(4),
        GOOD(3),
        NORMAL(2),
        DUMB(1);

        /** Numeric representation of the difficulty used for configuration. */
        private final int value;

        Level(int value) {
            this.value = value;
        }

        /**
         * Maps a numeric difficulty to a {@link Level}. The integer must be
         * between 1 and 5 inclusive; otherwise an exception is thrown.
         *
         * @param value numeric difficulty value
         * @return corresponding level
         * @throws IllegalArgumentException if the value does not correspond
         *                                  to any defined level
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
         * Returns the integer value associated with this level. Higher
         * numbers correspond to stronger AI.
         *
         * @return the numeric difficulty value
         */
        public int getValue() {
            return value;
        }
    }

    /** Difficulty setting controlling how the AI selects moves. */
    private Level level;
    /**
     * Retrieves the current difficulty level.
     *
     * @return the current {@link Level}
     */
    public Level getLevel() { return this.level; }
    /**
     * Updates the AI’s difficulty setting. Changing the level will not
     * immediately recalculate the available word list; a new game must be
     * started for the change to take effect.
     *
     * @param level the new difficulty level
     */
    public void setLevel(Level level) { this.level = level; }

    /**
     * Collection of candidate words that the AI may play. The set preserves
     * insertion order and allows efficient removal and access by index. It is
     * populated lazily in {@link #initialize()} and pruned as words are played.
     */
    private FastOrderedSet<String> available = null;

    /** The current game board assigned to this player. */
    private Gameboard board;
    /** Set of valid words remaining to play. */
    private Set<String> dictionary;

    /** Source of randomness used when selecting moves at non‑perfect levels. */
    private Random random = new Random();

    /** Display name of this AI player. */
    private String name;


    /**
     * Sets the display name of this AI. Useful when renaming players in the
     * lobby.
     *
     * @param name new name to assign
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the display name of this AI.
     *
     * @return the current player name
     */
    public String getName() {
        return name;
    }

    /**
     * Constructs a new AI player. The AI is not immediately ready to play; it
     * must be provided with a board and dictionary via {@link #setGame} and
     * lazily initialised via {@link #initialize()} when the first move is
     * requested.
     *
     * @param name display name for the AI
     * @param level difficulty setting controlling selection strategy
     */
    public AIPlayer(String name, Level level) {
        this.level = level;
        this.name = name;
    }

    /**
     * Prepares the AI to play by computing all possible words on the current
     * board. This method builds a prefix tree (trie) from the dictionary,
     * performs a depth‑first search from every board position to collect
     * words reachable by adjacent moves and then sorts and trims the
     * resulting list according to the configured {@link #level}. The final
     * candidate list is stored in {@link #available}. This method must only
     * be called after {@link #setGame(Gameboard, Set)} has been invoked and
     * will throw an exception otherwise.
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
     * Recursively explores the board to collect all words matching a given
     * prefix tree. Starting from a particular {@link GameboardWalker} and a
     * trie node corresponding to the current prefix, this method steps the
     * walker in all possible directions, descends into the child node of
     * {@code trie} matching the letter at the new position and recurs. When a
     * terminating node (a child keyed by {@code null}) is encountered the
     * walker’s journey is added to the result set. Backtracking ensures that
     * each path is explored independently without revisiting tiles.
     *
     * @param walker traversal cursor that maintains the current path and
     *               visited positions
     * @param trie prefix tree node representing the current partial word
     * @param collected accumulation set for discovered words
     * @return the set of discovered words (same as {@code collected})
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
     * Chooses the AI’s next move. On the first invocation this method
     * triggers initialisation of the available word list. It then selects a
     * word from {@link #available} according to the configured difficulty:
     * <ul>
     *   <li>{@link Level#PERFECT} returns the longest word.</li>
     *   <li>{@link Level#DUMB} returns the shortest word.</li>
     *   <li>For intermediate levels a random index within either the upper
     *       or lower half of the list is selected. Whether the upper or
     *       lower half is used depends on the level: SMART always picks from
     *       the upper half, NORMAL always from the lower half, and GOOD picks
     *       randomly between halves.</li>
     * </ul>
     * Returning {@code null} indicates that the AI has no moves left.
     *
     * @return the word chosen by the AI, or {@code null} if none are left
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
     * Assigns the board and dictionary for this AI. These references are used
     * when building the trie and searching for words. This method must be
     * called before {@link #nextMove()} is invoked.
     *
     * @param board the current game board
     * @param dictionary the set of valid words for this game
     */
    public void setGame(Gameboard board, Set<String> dictionary) {
        this.board = board;
        this.dictionary = dictionary;
    }

    /**
     * Removes the specified word from the AI’s list of available moves. This
     * method is called after each turn to keep the AI’s internal list in sync
     * with the dictionary used by the game engine.
     *
     * @param wordPlayed the word that was just played
     * @param nextMove ignored by this implementation but provided for
     *                 consistency with the {@link Player} interface
     */
    public void updateGameState(String wordPlayed, String nextMove) {
        if (available == null)
            initialize();

        available.remove(wordPlayed);
    }

    /**
     * Calculates the theoretical maximum score that could be achieved on a
     * given board using the current dictionary. This is done by creating a
     * temporary {@link AIPlayer} with {@link Level#PERFECT}, initialising it
     * with the supplied board and dictionary and summing the lengths of all
     * available words. The result is used for reporting percentage scores at
     * the end of the game.
     *
     * @param board the game board
     * @param dictionary the set of valid words
     * @return the sum of the lengths of all possible words on the board
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

    public static List<String> computePossibleMoves(Gameboard board, Set<String> dictionary) {
        AIPlayer player = new AIPlayer("4chan.org", Level.PERFECT);     // we love 4chan
        
        player.setGame(board, dictionary);
        player.initialize();

        return new ArrayList<>(player.available);
    }
}
