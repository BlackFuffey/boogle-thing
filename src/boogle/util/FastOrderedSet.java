package boogle.util;

import java.util.*;

/*
 * java.util.LinkedHashSet is so ass it doesnt even have reverse iterator
 *
 * so heres our own version 
 * (i definately wasnt too lazy to code this myself and used an llm)
*/
public class FastOrderedSet<T> implements Set<T> {

    private static class Node<T> {
        T val;
        Node<T> prev, next;

        Node(T val) {
            this.val = val;
        }
    }

    private Node<T> head, tail;
    private HashMap<T, Node<T>> map = new HashMap<>();

    public T pop() {
        if (map.size() == 0)
            return null;

        T item = tail.val;

        map.remove(tail.val);
        unlink(tail);

        return item;
    }

    public T shift() {
        if (map.size() == 0)
            return null;

        T item = head.val;

        map.remove(head.val);
        unlink(head);

        return item;
    }

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
    public boolean remove(Object o) {
        Node<T> node = map.get(o);
        if (node == null) return false;

        unlink(node);
        map.remove(o);
        return true;
    }

    private void unlink(Node<T> node) {
        if (node.prev != null) node.prev.next = node.next;
        else head = node.next;

        if (node.next != null) node.next.prev = node.prev;
        else tail = node.prev;

        node.prev = node.next = null;
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }


    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public void clear() {
        head = tail = null;
        map.clear();
    }

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

    public Iterable<T> reverse() {
        return () -> reverseIterator();
    }

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

    @Override
    public Object[] toArray() {
        Object[] arr = new Object[size()];
        int i = 0;
        for (T val : this) {
            arr[i++] = val;
        }
        return arr;
    }

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

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) return false;
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean changed = false;
        for (T val : c) {
            if (add(val)) changed = true;
        }
        return changed;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for (Object o : c) {
            if (remove(o)) changed = true;
        }
        return changed;
    }

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
