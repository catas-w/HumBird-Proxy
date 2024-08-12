package com.catas.wicked.common.worker;

public interface ScheduledWorker extends Runnable {

    /**
     * start scheduled task
     */
    void start();

    /**
     * pause task
     */
    void pause();

    /**
     * execute task manually
     */
    void invoke();

    /**
     * time delay for auto execution
     */
    long getDelay();
}
