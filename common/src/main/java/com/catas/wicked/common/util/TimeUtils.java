package com.catas.wicked.common.util;

public class TimeUtils {

    private static long lastTime;

    public static long count() {
        long curTime = System.currentTimeMillis();
        long delta = lastTime == 0 ? 0 : curTime - lastTime;
        lastTime = curTime;
        return delta;
    }

    public static String countOnStr() {
        return count() + " ms";
    }

    public static long countAndStop() {
        long delta = System.currentTimeMillis() - lastTime;
        lastTime = 0;
        return delta;
    }

    public static String countAndStopOnStr() {
        return countAndStop() + " ms";
    }
}
