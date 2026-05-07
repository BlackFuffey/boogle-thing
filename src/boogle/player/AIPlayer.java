package boogle.player;

import java.util.*;

import boogle.core.*;
import boogle.core.Gameboard.GameboardWalker;
import boogle.util.FastOrderedSet;
import boogle.util.Tree;

public class AIPlayer implements Player {

    public enum Level {
        PERFECT(5),   // always makes best move, keep 100% of moves
        SMART(4),     // randomly select from better half, keep 70%-90% of moves
        GOOD(3),      // randomly select from all possibilities, keep 30%-50% of moves
        NORMAL(2),    // randomly select from worse half, keep 10%-20% of moves
        DUMB(1);      // always make worst move, keep 5% of moves

        private final int value;

        Level(int value) {
            this.value = value;
        }

        public static Level fromValue(int value) {
            for (Level l : Level.values()) {
                if (l.getValue() == value) {
                    return l;
                }
            }
            throw new IllegalArgumentException();
        }

        public int getValue() {
            return value;
        }
    }

    private Level level;
    public Level getLevel() { return this.level; }
    public void setLevel(Level level) { this.level = level; }

    private FastOrderedSet<String> available = null;

    private Gameboard board;
    private Set<String> dictionary;

    private Random random = new Random();

    private String name;


    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public AIPlayer(String name, Level level) {
        this.level = level;
        this.name = name;
    }

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

        // finally, sort possible words by length and insert into available
        possibilities.sort((a, b) -> a.length() - b.length());
        this.available.addAll(possibilities);
    }
    
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

    public String nextMove() {
        if (this.available == null)
            initialize();

        // make a move according to the AI level
        switch(this.level) {
            case PERFECT: return available.get(0);

            case DUMB: return available.get(available.size()-1);

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
                        return move;
                    i++;
                }
            }
        }

        return null;
    }

    public void setGame(Gameboard board, Set<String> dictionary) {
        this.board = board;
        this.dictionary = dictionary;
    }

    public void updateGameState(String wordPlayed, String nextMove) {
        if (available == null)
            initialize();

        available.remove(wordPlayed);
    }

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

}
