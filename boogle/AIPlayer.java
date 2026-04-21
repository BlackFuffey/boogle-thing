package boogle;

import java.util.*;

public class AIPlayer implements Player {

    public enum Level{
        PERFECT,    // always makes best move
        SMART,      // randomly select from better half of possibilities
        GOOD,       // randomly select from all possibilities
        NORMAL,     // randomly select from worse half of possibilities
        DUMB        // always makes worst move
    }

    private Level level;

    private FastOrderedSet<String> available;
    private int worstCaseAt = 0;

    private Random random = new Random();

    public AIPlayer(Gameboard board, String[] wordlist, Level level) {
        this.level = level;

        // init logic
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
                for (String move : useUpperHalf ? available : available.reverse()) {
                    if (i == target) return move;
                    i++;
                }
        }

        // no move was successfully made, skip turn
        return null;
    }



}
