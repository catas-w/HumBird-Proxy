package com.catas.wicked.common.executor;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CommonExecutorService {

    public static ExecutorService singleThreadExecutor(String name) {
        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10), r -> new Thread(r, "SingleThread-" + name));
    }
}
