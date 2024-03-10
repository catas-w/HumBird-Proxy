package com.catas.wicked.server.strategy;

import java.util.function.Predicate;

public class DefaultSkipPredicate implements Predicate<String> {

    private DefaultSkipPredicate() {
    }

    public static final DefaultSkipPredicate INSTANCE = new DefaultSkipPredicate();

    @Override
    public boolean test(String string) {
        for (Handler value : Handler.values()) {
            if (value.name().equals(string)) {
                return false;
            }
        }
        return true;
    }
}
