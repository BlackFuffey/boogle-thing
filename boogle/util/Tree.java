package boogle.util;

import java.util.HashMap;

public class Tree<T> {
    private HashMap<T, Tree<T>> children = new HashMap<>();
    private final T self;

    public Tree(T self) {
        this.self = self;
    }

    public T self() {
        return self;
    }

    public void addChild(Tree<T> child) {
        children.put(child.self, child);
    }

    public void removeChild(T value) {
        children.remove(value);
    }

    public Tree<T> getChild(T value) {
        return children.get(value);
    }
}
