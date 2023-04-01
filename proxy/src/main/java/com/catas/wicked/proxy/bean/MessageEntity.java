package com.catas.wicked.proxy.bean;

import io.netty.handler.codec.http.HttpMethod;
import lombok.Data;

import java.net.MalformedURLException;
import java.net.URL;

@Data
public class MessageEntity {

    private String type;

    private HttpMethod method;

    private String contentType;

    private String requestId;

    private String requestUrl;

    private URL url;

    private String host;

    private String path;

    private String protocol;

    private byte[] body;

    private boolean isEnd;

    public MessageEntity() {}

    public MessageEntity(String requestUrl) throws MalformedURLException {
        this.url = new URL(requestUrl);
        this.requestUrl = requestUrl;
    }
}

