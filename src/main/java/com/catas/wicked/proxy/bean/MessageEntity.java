package com.catas.wicked.proxy.bean;

import io.netty.handler.codec.http.HttpMethod;
import lombok.Data;

@Data
public class MessageEntity {

    private String type;

    private HttpMethod method;

    private String contentType;

    private String requestId;

    private String requestUrl;

    private String host;

    private String path;

    private byte[] body;

    private boolean isEnd;
}

