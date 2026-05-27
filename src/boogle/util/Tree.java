package boogle.util;

import java.io.Serializable;

/**
 * Generic tree node used to build simple hierarchical structures. Each
 * {@code Tree} stores a reference to its own value ({@link #self}) and a
 * mapping of children keyed by those children’s values. This class is
 * appropriate for representing tries, prefix trees or other simple graphs
 * where nodes can be looked up by value. It does not store parent
 * references and is not thread‑safe.
 *
 * @param <T> type of the value stored in each node
 */

import java.util.HashMap;

public class Tree<T> implements Serializable {
    /** Map of this node’s children keyed by their values. Children are
     * accessible via {@link #getChild(Object)}. */
    private HashMap<T, Tree<T>> children = new HashMap<>();
    /** The value stored at this node. Immutable once set. */
    private final T self;

    /**
     * Constructs a new tree node containing the specified value and no
     * children.
     *
     * @param self the value to store in this node
     */
    public Tree(T self) {
        this.self = self;
    }

    /**
     * Returns the value stored at this node.
     *
     * @return the node’s value
     */
    public T self() {
        return self;
    }

    /**
     * Inserts the given {@code child} node into this node’s children map.
     * If a child with the same value already exists it will be replaced.
     *
     * @param child the child node to add
     */
    public void addChild(Tree<T> child) {
        children.put(child.self, child);
    }

    /**
     * Removes the child whose value equals {@code value} from this node’s
     * children map. If no such child exists this method does nothing.
     *
     * @param value the value of the child to remove
     */
    public void removeChild(T value) {
        children.remove(value);
    }

    /**
     * Retrieves the child node whose value equals {@code value}.
     *
     * @param value the key of the child to retrieve
     * @return the child node or {@code null} if no such child exists
     */
    public Tree<T> getChild(T value) {
        return children.get(value);
    }
}
