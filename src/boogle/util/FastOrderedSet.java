package boogle.util;

import java.io.Serializable;
import java.util.*;

/**
 * Ordered {@link Set} implementation with efficient membership checks and
 * bidirectional iteration support.
 *
 * <p>The set stores elements in insertion order using a linked list and a hash
 * map from value to list node. It provides normal forward iteration, reverse
 * iteration, and convenience removal from either end. It is used by the board
 * walker to remember visited cells and by the AI to keep possible moves ordered
 * by word length.</p>
 *
 * @param <T> element type
 */
public class FastOrderedSet<T> implements Set<T>, Serializable {

    /**
     * Creates an empty ordered set.
     */
    public FastOrderedSet() {
    }

    private static class Node<T> implements Serializable {
        private T val;
        private Node<T> prev;
        private Node<T> next;

        private Node(T val) {
            this.val = val;
        }
    }

    /** First node in insertion order. */
    private Node<T> head;
    /** Last node in insertion order. */
    private Node<T> tail;
    /** Membership and value-to-node lookup table. */
    private HashMap<T, Node<T>> map = new HashMap<>();

    /**
     * Removes and returns the most recently inserted element.
     *
     * @return last element, or {@code null} when the set is empty
     */
    public T pop() {
        if (map.size() == 0)
            return null;

        T item = tail.val;

        map.remove(tail.val);
        unlink(tail);

        return item;
    }

    /**
     * Removes and returns the oldest inserted element.
     *
     * @return first element, or {@code null} when the set is empty
     */
    public T shift() {
        if (map.size() == 0)
            return null;

        T item = head.val;

        map.remove(head.val);
        unlink(head);

        return item;
    }

    @Override
    /**
     * Adds a value to the end of the insertion order when absent.
     *
     * @param val value to add
     * @return {@code true} if the value was new to the set
     */
    public boolean add(T val) {
        if (map.containsKey(val)) return false;

        Node<T> node = new Node<>(val);
        map.put(val, node);

        if (tail == null) {
            head = tail = node;
        } else {
            tail.next = node;
            node.prev = tail;
            tail = node;
        }
        return true;
    }

    /**
     * Returns the value at an insertion-order index.
     *
     * <p>The lookup walks from whichever end is closer. Invalid indexes are not
     * range-checked and may return {@code null} or fail while traversing.</p>
     *
     * @param index zero-based insertion-order index
     * @return value at the index, or {@code null} if traversal lands off-list
     */
    public T get(int index) {
        Node<T> at;
        int size = map.size();

        if (index < size / 2) {
            at = this.head;
            for (int i = 0; i < index; i++) {
                at = at.next;
            }
        } else {
            at = this.tail;
            for (int i = size - 1; i > index; i--) {
                at = at.prev;
            }
        }

        return at == null ? null : at.val;
    }

    @Override
    /**
     * Removes a value from the set.
     *
     * @param o value to remove
     * @return {@code true} if the value was present
     */
    public boolean remove(Object o) {
        Node<T> node = map.get(o);
        if (node == null) return false;

        unlink(node);
        map.remove(o);
        return true;
    }

    /**
     * Detaches a node from the linked list without touching the hash map.
     *
     * @param node node to unlink
     */
    private void unlink(Node<T> node) {
        if (node.prev != null) node.prev.next = node.next;
        else head = node.next;

        if (node.next != null) node.next.prev = node.prev;
        else tail = node.prev;

        node.prev = node.next = null;
    }

    @Override
    /**
     * Checks whether a value is present.
     */
    public boolean contains(Object o) {
        return map.containsKey(o);
    }


    @Override
    /**
     * Returns the number of stored values.
     */
    public int size() {
        return map.size();
    }

    @Override
    /**
     * Checks whether the set contains no values.
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    /**
     * Removes every value from the set.
     */
    public void clear() {
        head = tail = null;
        map.clear();
    }

    /**
     * Returns an iterator that walks elements from newest to oldest.
     *
     * @return reverse insertion-order iterator with remove support
     */
    public Iterator<T> reverseIterator() {
        return new Iterator<T>() {
            Node<T> current = tail;
            Node<T> lastReturned = null;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public T next() {
                if (current == null) throw new NoSuchElementException();
                lastReturned = current;
                current = current.prev;
                return lastReturned.val;
            }

            @Override
            public void remove() {
                if (lastReturned == null) throw new IllegalStateException();

                unlink(lastReturned);
                map.remove(lastReturned.val);

                lastReturned = null;
            }
        };
    }

    /**
     * Returns an iterable view over reverse insertion order.
     *
     * @return iterable backed by {@link #reverseIterator()}
     */
    public Iterable<T> reverse() {
        return () -> reverseIterator();
    }

    @Override
    /**
     * Returns a forward insertion-order iterator.
     */
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            Node<T> current = head;
            Node<T> lastReturned = null;

            public boolean hasNext() {
                return current != null;
            }

            public T next() {
                if (current == null) throw new NoSuchElementException();
                lastReturned = current;
                current = current.next;
                return lastReturned.val;
            }

            public void remove() {
                if (lastReturned == null) throw new IllegalStateException();
                unlink(lastReturned);
                map.remove(lastReturned.val);
                lastReturned = null;
            }
        };
    }

    @Override
    /**
     * Copies values into a new object array in insertion order.
     */
    public Object[] toArray() {
        Object[] arr = new Object[size()];
        int i = 0;
        for (T val : this) {
            arr[i++] = val;
        }
        return arr;
    }

    @Override
    /**
     * Copies values into the supplied array type in insertion order.
     */
    public <E> E[] toArray(E[] a) {
        int size = size();
        if (a.length < size) {
            a = Arrays.copyOf(a, size);
        }

        int i = 0;
        for (T val : this) {
            a[i++] = (E) val;
        }

        if (a.length > size) {
            a[size] = null;
        }

        return a;
    }

    @Override
    /**
     * Checks whether all values in a collection are present.
     */
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) return false;
        }
        return true;
    }

    @Override
    /**
     * Adds all values from a collection, preserving collection iteration order.
     */
    public boolean addAll(Collection<? extends T> c) {
        boolean changed = false;
        for (T val : c) {
            if (add(val)) changed = true;
        }
        return changed;
    }

    @Override
    /**
     * Removes every value contained in a collection.
     */
    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for (Object o : c) {
            if (remove(o)) changed = true;
        }
        return changed;
    }

    @Override
    /**
     * Keeps only values contained in a collection.
     */
    public boolean retainAll(Collection<?> c) {
        boolean changed = false;

        Iterator<T> it = iterator();
        while (it.hasNext()) {
            T val = it.next();
            if (!c.contains(val)) {
                it.remove();
                changed = true;
            }
        }

        return changed;
    }
}
