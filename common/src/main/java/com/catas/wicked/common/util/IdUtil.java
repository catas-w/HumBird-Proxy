package com.catas.wicked.common.util;

import java.util.UUID;

public class IdUtil {

    public static String getId() {
        return UUID.randomUUID().toString();
    }

    public static String getSimpleId() {
        return getId().replace("-", "");
    }
}
