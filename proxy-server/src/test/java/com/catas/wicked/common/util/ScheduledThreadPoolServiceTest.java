package com.catas.wicked.common.util;

import com.catas.wicked.common.executor.ScheduledThreadPoolService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class ScheduledThreadPoolServiceTest {

    @Test
    public void testCancelTask() throws InterruptedException {
        AtomicInteger cnt = new AtomicInteger();
        RunnableScheduledFuture<?> task = (RunnableScheduledFuture<?>) ScheduledThreadPoolService.getInstance()
                .submit(() -> cnt.addAndGet(1), 1000);
        Thread.sleep(2500);
        ScheduledThreadPoolService.getInstance().cancel(task);
        Thread.sleep(2000);
        Assertions.assertEquals(3, cnt.get());
    }
}
