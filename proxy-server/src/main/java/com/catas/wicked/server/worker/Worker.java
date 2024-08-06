package com.catas.wicked.server.worker;

public interface Worker extends Runnable {

    void start();

    void pause();

    void invoke(long timeout);
}
