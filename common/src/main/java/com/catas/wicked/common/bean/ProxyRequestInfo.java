package com.catas.wicked.common.bean;

import lombok.Data;

@Data
public class ProxyRequestInfo {

    private String host;

    private int port;

    private boolean ssl;

    private String path;

    private boolean isRecording;

    private String requestId;

    private ClientType clientType;

    public enum ClientType {
        TUNNEL,
        NORMAL
    }
}
