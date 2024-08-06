package com.catas.wicked.common.task;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class ScheduledThreadPoolService {

    private static final int CORE_SIZE = 16;
    private static final String PREFIX = "common-scheduled-thread-";

    private final ScheduledThreadPoolExecutor service;

    private ScheduledThreadPoolService() {
        ThreadFactory threadFactory = new ThreadFactory() {
            private int cnt = 0;
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, PREFIX + cnt ++);
            }
        };
        this.service = new ScheduledThreadPoolExecutor(CORE_SIZE, threadFactory);
    }

    public void submit(Runnable task, long initialDelay, long delay, TimeUnit timeUnit) {
        service.scheduleAtFixedRate(task, initialDelay, delay, timeUnit);
    }

    public void submit(Runnable task, long delay) {
        service.scheduleAtFixedRate(task, 0, delay, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        service.shutdownNow();
    }

    public static boolean owns(String threadName) {
        return threadName != null && threadName.startsWith(PREFIX);
    }

    public static ScheduledThreadPoolService getInstance() {
        return Holder.instance;
    }

    private static class Holder {
        private static final ScheduledThreadPoolService instance = new ScheduledThreadPoolService();
    }
}
