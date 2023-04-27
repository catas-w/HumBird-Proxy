package com.catas.wicked.common.bean;

import io.netty.handler.codec.http.HttpMethod;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.net.MalformedURLException;
import java.net.URL;

@EqualsAndHashCode(callSuper = true)
@Data
public class RequestMessage extends BaseMessage{

    private String type;

    private String method;

    private String contentType;

    private String requestId;

    private String requestUrl;

    private URL url;

    private String host;

    private String path;

    private String protocol;

    private byte[] body;

    private boolean isEnd;

    public RequestMessage() {}

    public RequestMessage(String requestUrl) throws MalformedURLException {
        this.url = new URL(requestUrl);
        this.requestUrl = requestUrl;
    }
}

