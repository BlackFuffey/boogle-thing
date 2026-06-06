/*
 * File: Tree.java
 * Author: Ethan Ding
 * Description: Implements a serializable generic tree node used as a trie for board word searches.
 */

package boogle.util;

import java.io.Serializable;

import java.util.HashMap;

/**
 * Minimal serializable tree node keyed by child value.
 *
 * <p>The AI uses this as a trie: each node represents one character, children
 * are looked up by the next character, and a child keyed by {@code null} marks a
 * terminating word.</p>
 *
 * @param <T> value type stored at each node
 */
public class Tree<T> implements Serializable {
    /** Child nodes keyed by child value. */
    private HashMap<T, Tree<T>> children = new HashMap<>();
    /** Value represented by this node. */
    private final T self;

    /**
     * Creates a node with the supplied value.
     *
     * @param self value represented by this node
     */
    public Tree(T self) {
        this.self = self;
    }

    /**
     * Returns the value represented by this node.
     *
     * @return node value
     */
    public T self() {
        return self;
    }

    /**
     * Adds or replaces a child, keyed by the child's own value.
     *
     * @param child child node to attach
     */
    public void addChild(Tree<T> child) {
        children.put(child.self, child);
    }

    /**
     * Removes the child associated with a value.
     *
     * @param value child key to remove
     */
    public void removeChild(T value) {
        children.remove(value);
    }

    /**
     * Looks up a child by value.
     *
     * @param value child key
     * @return matching child, or {@code null} when absent
     */
    public Tree<T> getChild(T value) {
        return children.get(value);
    }
}
