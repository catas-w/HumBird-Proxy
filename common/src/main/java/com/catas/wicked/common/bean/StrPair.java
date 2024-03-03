package com.catas.wicked.common.bean;

import javafx.util.Pair;

public class StrPair extends Pair<String, String> {

    /**
     * Creates a new pair
     *
     * @param key   The key for this pair
     * @param value The value to use for this pair
     */
    public StrPair(String key, String value) {
        super(key, value);
    }

    public StrPair() {
        this(null, null);
    }
}
