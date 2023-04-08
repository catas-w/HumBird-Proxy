package com.catas.wicked.common.pipeline;

import com.catas.wicked.common.bean.BaseMessage;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class MessageQueue {

    private final BlockingQueue<BaseMessage> queue;

    public MessageQueue() {
        this.queue = new LinkedBlockingQueue<>();
    }

    public void pushMsg(BaseMessage message) {
        queue.add(message);
    }

    public BaseMessage getMsg() throws InterruptedException {
        return queue.take();
    }
}
