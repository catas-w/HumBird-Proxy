package com.catas.wicked.common.bean;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class BaseMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private MessageType type;

    private long timestamp;

    public BaseMessage() {
        this.timestamp = System.currentTimeMillis();
    }

    public enum MessageType {
        REQUEST,
        RESPONSE,
        POISON
    }
}
