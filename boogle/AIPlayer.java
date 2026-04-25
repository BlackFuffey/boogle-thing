package boogle;

import java.util.*;

import boogle.Gameboard.GameboardWalker;
import boogle.util.FastOrderedSet;
import boogle.util.Tree;

public class AIPlayer implements Player {

    public enum Level {
        PERFECT(5),   // always makes best move
        SMART(4),     // randomly select from better half
        GOOD(3),      // randomly select from all possibilities
        NORMAL(2),    // randomly select from worse half
        DUMB(1);      // always make worst move

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

    private Random random = new Random();

    private String name;

    private int lastPlayedUpdate = 0;

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

    private void initialize(Gameboard board, Set<String> dictionary) {
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
                    new GameboardWalker(board, x, y),
                    letterRoot, 
                    new ArrayList<>(), new HashSet<>()
                );
                possibilities.addAll(found);
            }
        }

        // finally, sort possible words by length and insert into available
        possibilities.sort((a, b) -> a.length() - b.length());
        this.available.addAll(possibilities);
    }
    
    private static HashSet<String> dfs(GameboardWalker walker, Tree<Character> trie, ArrayList<Character> path, HashSet<String> collected) {
        path.add(walker.here());

        // end node, add to list of found words
        if (trie.getChild(null) != null)
            collected.add(arrayListToString(path));

        for (GameboardWalker.Direction dir : GameboardWalker.Direction.values()) {
            GameboardWalker next = walker.step(dir);

            if (next == null) continue;

            Tree<Character> nextTrie = trie.getChild(next.here());
            if (nextTrie != null) {
                dfs(next, nextTrie, path, collected);
            }
        }

        // backtrack this call
        path.remove(path.size()-1);

        return collected;
    }

    private static String arrayListToString(ArrayList<Character> arr) {
        StringBuilder builder = new StringBuilder(arr.size());

        for (char c : arr) {
            builder.append(c);
        }

        return builder.toString();
    }

    public String nextMove(Gameboard board, Set<String> dictionary, ArrayList<String> playedWords) {
        System.out.println(this.getName() + " is thinking...");

        if (this.available == null) {
            initialize(board, dictionary);
        }

        // update available move list
        for (int i = lastPlayedUpdate; i < playedWords.size(); i++) {
            available.remove(playedWords.get(i));
        }
        lastPlayedUpdate = Math.max(playedWords.size()-1, 0);

        // make a move according to the AI level
        switch(this.level) {
            case PERFECT: return available.pop();

            case DUMB: return available.shift();

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



}
