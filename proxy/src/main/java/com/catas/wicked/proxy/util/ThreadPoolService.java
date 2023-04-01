package com.catas.wicked.proxy.util;

import java.util.concurrent.*;

public class ThreadPoolService {

    private int corePoolSize = 8;

    private int maxPoolSize = 20;

    private long aliveTime = 0L;

    private ExecutorService service;

    private ThreadPoolService() {
        service = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                aliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1024)
                );
    }

    public void run(Runnable task) {
        service.execute(task);
    }

    public Future<Object> submit(Callable<Object> task) {
        return service.submit(task);
    }

    public static ThreadPoolService getInstance() {
        return Holder.instance;
    }

    public static class Holder {
        private static final ThreadPoolService instance = new ThreadPoolService();
    }
}
