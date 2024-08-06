package com.catas.wicked.server.worker;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public abstract class AbstractScheduledWorker implements Worker {

    private enum LOCK_STATUS {
        UN_LOCKED,
        LOCKED
    }

    @Getter
    private final long delay;
    private final long spinDelay;
    private boolean running;
    private final AtomicReference<LOCK_STATUS> lock = new AtomicReference<>(LOCK_STATUS.UN_LOCKED);


    public AbstractScheduledWorker(long delay, long spinDelay) {
        this.delay = delay;
        this.spinDelay = spinDelay;
    }

    public AbstractScheduledWorker(long delay) {
        this.delay = delay;
        this.spinDelay = 100L;
    }

    @Override
    public void start() {
        running = true;
    }

    @Override
    public void pause() {
        running = false;
    }

    @Override
    public void invoke(long timeout) {
        if (!tryLock(timeout)) {
            log.warn("Worker failed to get lock: {}", timeout);
            return;
        }
        try {
            long start = System.currentTimeMillis();
            doWork();
            log.info("Executing scheduled worker, time cost: {}", System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.error("Error in executing worker: ", e);
        } finally {
            lock.set(LOCK_STATUS.UN_LOCKED);
        }
    }

    @Override
    public void run() {
        if (!this.running) {
            log.warn("Task is paused");
            return;
        }
        invoke(0);
    }

    private boolean tryLock(long timeout) {
        if (lock.compareAndSet(LOCK_STATUS.UN_LOCKED, LOCK_STATUS.LOCKED)) {
            return true;
        }
        if (timeout <= spinDelay) {
            return false;
        }
        long endTime = System.currentTimeMillis() + timeout;
        while (System.currentTimeMillis() <= endTime) {
            if (lock.compareAndSet(LOCK_STATUS.UN_LOCKED, LOCK_STATUS.LOCKED)) {
                return true;
            }
            try {
                Thread.sleep(spinDelay);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    protected abstract void doWork();
}
