package com.catas.wicked.server;

import com.catas.wicked.common.bean.IdGenerator;
import com.catas.wicked.common.util.IdUtil;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Singleton
public class PrevIdGenerator implements IdGenerator {

    private BlockingQueue<String> blockingQueue;

    @PostConstruct
    public void init() {
        blockingQueue = new LinkedBlockingQueue<>();
    }

    private String currentId;

    @Override
    public String nextId() {
        // try {
        //     String nextId = blockingQueue.poll(2000, TimeUnit.MILLISECONDS);
        //     return nextId;
        // } catch (InterruptedException e) {
        //     throw new RuntimeException("Get next id error");
        // }
        return currentId == null ? IdUtil.getId() : currentId;
    }

    public void setNextId(String id) {
        // blockingQueue.offer(id);
        currentId = id;
    }
}
