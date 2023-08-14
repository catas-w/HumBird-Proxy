package com.catas.wicked.common.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class RequestMessage extends BaseMessage{

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

    private Map<String, String> headers;

    private ResponseMessage response;

    public RequestMessage() {}

    public RequestMessage(String requestUrl) {
        try {
            this.url = new URL(requestUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        this.requestUrl = requestUrl;
        this.setType(MessageType.REQUEST);
    }
}

