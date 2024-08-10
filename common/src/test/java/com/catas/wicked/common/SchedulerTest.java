package com.catas.wicked.common;

import com.catas.wicked.common.task.ScheduledThreadPoolService;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class SchedulerTest {

    @Test
    public void testSchedulerTest() throws InterruptedException {
        ScheduledThreadPoolService.getInstance().submit(() -> System.out.println("Task 1"), 0, 2, TimeUnit.SECONDS);
        ScheduledThreadPoolService.getInstance().submit(() -> System.out.println("Task 2"), 1, 2, TimeUnit.SECONDS);
        ScheduledThreadPoolService.getInstance().submit(() -> {
            System.out.println("Task 3");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, 0, 1, TimeUnit.SECONDS);
        Thread.sleep(10 * 1000);
    }
}
