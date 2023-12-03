package com.catas.wicked.common.pipeline;

import com.catas.wicked.common.bean.message.BaseMessage;
import com.catas.wicked.common.bean.message.PoisonMessage;
import com.catas.wicked.common.util.ThreadPoolService;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@Singleton
public class MessageQueue {

    private final Map<Topic, MessageChannel> channelMap;

    public MessageQueue() {
        this.channelMap = new HashMap<>();
    }

    @PostConstruct
    public void init() {
        for (Topic topic : Topic.values()) {
            MessageChannel messageChannel = new MessageChannel(topic);
            channelMap.put(topic, messageChannel);
            // TODO 设置线程名称
            ThreadPoolService.getInstance().run(() -> {
                log.info("Start listening to topic: {}", messageChannel.getTopic());
                while (true) {
                    try {
                        BaseMessage msg = messageChannel.getMsg();
                        if (msg instanceof PoisonMessage) {
                            throw new InterruptedException("Quit");
                        }
                        messageChannel.consume(msg);
                    } catch (InterruptedException e) {
                        log.warn("Message listener interrupted: {}", messageChannel.getTopic());
                        break;
                    }
                }
                log.info("End listening to topic: {}", messageChannel.getTopic());
            });
        }
    }

    /**
     * subscribe to a topic
     * @param topic topic
     * @param consumer consumer function
     */
    public void subscribe(Topic topic, Consumer<BaseMessage> consumer) {
        if (consumer == null || topic == null) {
            throw new RuntimeException("topic or consumer cannot be null.");
        }
        MessageChannel messageChannel = channelMap.get(topic);
        messageChannel.addConsumer(consumer);
    }

    public void clearMsg(Topic topic) {
        MessageChannel messageChannel = channelMap.get(topic);
        messageChannel.clear();
    }

    /**
     * push message to a specific queue
     * @param topic topic
     * @param message message
     */
    public void pushMsg(Topic topic, BaseMessage message) {
        MessageChannel messageChannel = channelMap.get(topic);
        messageChannel.pushMsg(message);
    }

}
