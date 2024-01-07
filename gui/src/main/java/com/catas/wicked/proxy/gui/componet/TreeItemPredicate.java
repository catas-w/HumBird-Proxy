package com.catas.wicked.proxy.gui.componet;

import javafx.scene.control.TreeItem;

import java.util.function.Predicate;

@FunctionalInterface
public interface TreeItemPredicate<T> {

    boolean test(TreeItem<T> parent, T value);

    static <T> TreeItemPredicate<T> create(Predicate<T> predicate) {
        return (parent, value) -> predicate.test(value);
    }
}
