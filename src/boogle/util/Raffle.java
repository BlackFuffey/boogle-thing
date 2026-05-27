package boogle.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * Simple utility representing a raffle of items that can be drawn without
 * replacement. A {@code Raffle} is initialised with a list of items and
 * allows random draws until the collection is exhausted. Items are not
 * returned to the pool once drawn. This class is not thread‑safe and
 * should not be accessed concurrently from multiple threads.
 *
 * @param <T> the type of items contained in the raffle
 */
public class Raffle<T> implements Serializable {
    /** Internal list of remaining items. Elements drawn are replaced by
     * {@code null} at the end of the active range but never physically
     * removed from this list. */
    private final List<T> items;
    /** Random number generator used to select the next item. */
    private final Random random;
    /** Number of items that have not yet been drawn. This value decreases
     * with each call to {@link #draw()}. */
    private int remaining;

    /**
     * Constructs a new raffle from the provided list of items. The input list
     * is defensively copied so subsequent modifications to {@code items}
     * will not affect the raffle. Passing {@code null} will result in an
     * {@link IllegalArgumentException}.
     *
     * @param items list of items from which draws will occur
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
     * Removes and returns a random item from the raffle. Each item can only be
     * drawn once; subsequent calls will return the remaining items until the
     * raffle is exhausted. Internally this method uses a swap‑with‑last
     * technique to avoid O(n) removals. When no items remain a
     * {@link NoSuchElementException} is thrown.
     *
     * @return a randomly selected item from the remaining pool
     * @throws NoSuchElementException if no items are left in the raffle
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
     * Returns {@code true} if all items have been drawn from this raffle.
     *
     * @return {@code true} when no items remain, {@code false} otherwise
     */
    public boolean isEmpty() {
        return remaining == 0;
    }

    /**
     * Returns the number of items still available to draw.
     *
     * @return the count of undrawn items remaining
     */
    public int size() {
        return remaining;
    }
}
