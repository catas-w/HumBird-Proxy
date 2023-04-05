package com.catas.wicked.common.pipeline;

import com.catas.wicked.common.bean.MessageEntity;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class MessageQueue {

    private final BlockingQueue<MessageEntity> queue;

    public MessageQueue() {
        this.queue = new LinkedBlockingQueue<>();
    }

    public void pushMsg(MessageEntity message) {
        queue.add(message);
    }

    public MessageEntity getMsg() throws InterruptedException {
        return queue.take();
    }
}
