package boogle.util;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

public class Raffle<T> {
    private final List<T> items;
    private final Random random;
    private int remaining;

    public Raffle(List<T> items) {
        if (items == null) {
            throw new IllegalArgumentException("items cannot be null");
        }

        this.items = new ArrayList<>(items);
        this.random = new Random();
        this.remaining = items.size();
    }

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

    public boolean isEmpty() {
        return remaining == 0;
    }

    public int size() {
        return remaining;
    }
}
