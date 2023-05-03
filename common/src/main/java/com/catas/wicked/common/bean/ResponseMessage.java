package com.catas.wicked.common.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class ResponseMessage extends BaseMessage implements Serializable {

    private String requestId;

    private int status;

    private Map<String, String> headers;

    private byte[] content;

    public ResponseMessage() {
        this.setType(MessageType.RESPONSE);
    }
}
