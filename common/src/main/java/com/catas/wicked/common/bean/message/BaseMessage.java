package com.catas.wicked.common.bean.message;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class BaseMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private MessageType type;

    private long startTime;

    private long endTime;

    private int size;

    public enum MessageType {
        REQUEST,
        REQUEST_CONTENT,
        RESPONSE,
        RESPONSE_CONTENT,
        POISON,
        UPDATE,
        DELETE,
    }
}
