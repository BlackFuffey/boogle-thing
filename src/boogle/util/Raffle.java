package boogle.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * Random draw bag that returns each item at most once.
 *
 * <p>The raffle copies the supplied list and tracks an active prefix. Drawing an
 * item swaps it with the last active entry and shrinks the prefix, giving O(1)
 * draws without replacement.</p>
 *
 * @param <T> item type stored in the raffle
 */
public class Raffle<T> implements Serializable {
    /** Internal item storage, with the active portion at the front. */
    private final List<T> items;
    /** Random source used to choose an active index. */
    private final Random random;
    /** Number of items still available to draw. */
    private int remaining;

    /**
     * Creates a raffle from the supplied items.
     *
     * @param items source items to copy
     * @throws IllegalArgumentException if {@code items} is {@code null}
     */
    public Raffle(List<T> items) {
        if (items == null) {
            throw new IllegalArgumentException("items cannot be null");
        }

        this.items = new ArrayList<>(items);
        this.random = new Random();
        this.remaining = items.size();
    }

    /**
     * Draws one random remaining item and removes it from future draws.
     *
     * @return randomly selected item
     * @throws NoSuchElementException if no items remain
     */
    public T draw() {
        if (remaining == 0) {
            throw new NoSuchElementException("No items left in raffle");
        }

        int index = random.nextInt(remaining);

        T selected = items.get(index);

        T lastActive = items.get(remaining - 1);
        items.set(index, lastActive);

        items.set(remaining - 1, null);

        remaining--;

        return selected;
    }

    /**
     * Checks whether all items have been drawn.
     *
     * @return {@code true} when no items remain
     */
    public boolean isEmpty() {
        return remaining == 0;
    }

    /**
     * Returns the number of items that can still be drawn.
     *
     * @return remaining item count
     */
    public int size() {
        return remaining;
    }
}
