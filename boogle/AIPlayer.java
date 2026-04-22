package boogle;

import java.util.*;

import boogle.Gameboard.GameboardWalker;

public class AIPlayer implements Player {

    public enum Level{
        PERFECT,    // always makes best move
        SMART,      // randomly select from better half of possibilities
        GOOD,       // randomly select from all possibilities
        NORMAL,     // randomly select from worse half of possibilities
        DUMB        // always makes worst move
    }

    private Level level;

    private FastOrderedSet<String> available = new FastOrderedSet<>();

    private Random random = new Random();

    public AIPlayer(Gameboard board, String[] wordlist, Level level) {
        this.level = level;

        Tree<Character> trieRoot = new Tree<>(null);
        
        // build trie of wordlist
        for (String word : wordlist) {
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

    public String nextMove(String[] prevMoves) {
        // update available move list
        for (String move : prevMoves) {
            available.remove(move);
        }

        // make a move according to the AI level
        switch(this.level) {
            case PERFECT: 
                return available.pop();

            case DUMB:
                return available.shift();

            case SMART:
            case GOOD:
            case NORMAL:
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
                    if (i == target) return move;
                    i++;
                }
        }

        // no move was successfully made, skip turn
        return null;
    }



}
