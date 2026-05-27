package boogle.util;

import java.io.Serializable;
import java.util.*;

/*
 * java.util.LinkedHashSet is so ass it doesnt even have reverse iterator
 *
 * so heres our own version 
 * (i definately wasnt too lazy to code this myself and used an llm)
 */
/**
 * A {@link Set} implementation that preserves insertion order and provides
 * constant‑time access to elements as well as the ability to iterate in
 * both forward and reverse order. Unlike {@link java.util.LinkedHashSet},
 * this set exposes {@link #pop()} and {@link #shift()} operations to remove
 * and return the last or first element respectively, as well as random
 * access by index via {@link #get(int)}. Internally the set is backed by a
 * doubly‑linked list and a {@link HashMap} for O(1) membership checks and
 * removals. Duplicate elements are ignored on addition. This class is not
 * thread‑safe; concurrent modifications may produce undefined behaviour.
 *
 * @param <T> the type of elements maintained by this set
 */
public class FastOrderedSet<T> implements Set<T>, Serializable {

    /**
     * Node used in the internal doubly‑linked list. Each node stores a
     * reference to the contained value and pointers to the previous and
     * next nodes in the sequence. Nodes are not exposed outside this class.
     */
    private static class Node<T> {
        /** Value stored at this position in the list. */
        T val;
        /** Pointer to the previous node in the list or {@code null} if this is the head. */
        Node<T> prev;
        /** Pointer to the next node in the list or {@code null} if this is the tail. */
        Node<T> next;

        /**
         * Constructs a new node wrapping the specified value. Both next and
         * previous pointers are initialised to {@code null}.
         *
         * @param val the value to store in the node
         */
        Node(T val) {
            this.val = val;
        }
    }

    /**
     * Pointer to the first element in the list. When the set is empty
     * {@code head} is {@code null}.
     */
    private Node<T> head;
    /**
     * Pointer to the last element in the list. When the set is empty
     * {@code tail} is {@code null}.
     */
    private Node<T> tail;
    /**
     * Map from element values to their corresponding list nodes. Provides
     * constant‑time membership checks and allows removal of arbitrary
     * elements in O(1) time.
     */
    private HashMap<T, Node<T>> map = new HashMap<>();

    /**
     * Removes and returns the most recently added element (the tail) from
     * the set. If the set is empty this method returns {@code null}.
     *
     * @return the last element in insertion order or {@code null} if the set is empty
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
     * Removes and returns the element that was added earliest (the head)
     * from the set. If the set is empty this method returns {@code null}.
     *
     * @return the first element in insertion order or {@code null} if the set is empty
     */
    public T shift() {
        if (map.size() == 0)
            return null;

        T item = head.val;

        map.remove(head.val);
        unlink(head);

        return item;
    }

    /**
     * Adds the specified element to this set if it is not already present.
     * Duplicate elements are ignored and the method returns {@code false}.
     *
     * @param val element to be added to the set
     * @return {@code true} if the set did not already contain the specified element
     */
    @Override
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
     * Returns the element at the specified index in insertion order. If the
     * index is in the first half of the list the method traverses from the
     * head; otherwise it traverses from the tail for efficiency.
     *
     * @param index zero‑based index of the element to return
     * @return the element at the specified position or {@code null} if the
     *         index is out of bounds
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

    /**
     * Removes the specified element from this set if it is present. The
     * underlying node is unlinked from the list and removed from the map.
     *
     * @param o object to be removed from this set, if present
     * @return {@code true} if the set contained the specified element
     */
    @Override
    public boolean remove(Object o) {
        Node<T> node = map.get(o);
        if (node == null) return false;

        unlink(node);
        map.remove(o);
        return true;
    }

    /**
     * Removes the specified node from the doubly‑linked list by updating
     * neighbouring node pointers. This method does not update the map.
     *
     * @param node the node to unlink from the list
     */
    private void unlink(Node<T> node) {
        if (node.prev != null) node.prev.next = node.next;
        else head = node.next;

        if (node.next != null) node.next.prev = node.prev;
        else tail = node.prev;

        node.prev = node.next = null;
    }

    /**
     * Returns {@code true} if this set contains the specified element.
     * Membership checks are O(1).
     *
     * @param o element whose presence in this set is to be tested
     * @return {@code true} if this set contains the specified element
     */
    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }


    /**
     * Returns the number of elements in this set.
     *
     * @return the number of elements in the set
     */
    @Override
    public int size() {
        return map.size();
    }

    /**
     * Returns {@code true} if this set contains no elements.
     *
     * @return {@code true} if the set is empty
     */
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Removes all of the elements from this set. The set will be empty after
     * this call returns.
     */
    @Override
    public void clear() {
        head = tail = null;
        map.clear();
    }

    /**
     * Returns an iterator over the elements in this set in reverse order
     * (from most recently added to earliest). The iterator supports removal
     * of elements via its {@link Iterator#remove()} method.
     *
     * @return an iterator traversing the set in reverse insertion order
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
     * Returns an {@link Iterable} view of this set in reverse order. This
     * convenience method simply returns a lambda invoking {@link #reverseIterator()}.
     *
     * @return an iterable that produces elements from newest to oldest
     */
    public Iterable<T> reverse() {
        return () -> reverseIterator();
    }

    /**
     * Returns an iterator over the elements in this set in insertion order.
     * The iterator supports element removal via its {@link Iterator#remove()}
     * method.
     *
     * @return an iterator traversing the set from oldest to newest element
     */
    @Override
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

    /**
     * Returns an array containing all of the elements in this set in proper
     * sequence (from oldest to newest). The returned array will be "safe"
     * in that no references to it are maintained by this set.
     *
     * @return an array containing all of the elements in this set
     */
    @Override
    public Object[] toArray() {
        Object[] arr = new Object[size()];
        int i = 0;
        for (T val : this) {
            arr[i++] = val;
        }
        return arr;
    }

    /**
     * Returns an array containing all of the elements in this set in proper
     * sequence; the runtime type of the returned array is that of the
     * specified array. If the set fits in the specified array, it is
     * returned therein. Otherwise, a new array is allocated with the
     * runtime type of the specified array and the size of this set.
     *
     * @param a the array into which the elements of the set are to be stored
     * @param <E> the component type of the array to contain the set
     * @return an array containing the elements of the set
     */
    @Override
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

    /**
     * Returns {@code true} if this set contains all of the elements in the
     * specified collection. Membership checks are O(1) per element.
     *
     * @param c collection to be checked for containment in this set
     * @return {@code true} if this set contains all elements of {@code c}
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) return false;
        }
        return true;
    }

    /**
     * Adds all of the elements in the specified collection to this set if
     * they're not already present.
     *
     * @param c collection containing elements to be added to this set
     * @return {@code true} if this set changed as a result of the call
     */
    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean changed = false;
        for (T val : c) {
            if (add(val)) changed = true;
        }
        return changed;
    }

    /**
     * Removes from this set all of its elements that are also contained in the
     * specified collection.
     *
     * @param c collection containing elements to be removed from this set
     * @return {@code true} if this set changed as a result of the call
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for (Object o : c) {
            if (remove(o)) changed = true;
        }
        return changed;
    }

    /**
     * Retains only the elements in this set that are contained in the
     * specified collection. In other words, removes from this set all of
     * its elements that are not contained in the specified collection.
     *
     * @param c collection containing elements to be retained in this set
     * @return {@code true} if this set changed as a result of the call
     */
    @Override
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
