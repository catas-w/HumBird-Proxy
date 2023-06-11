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

    private boolean isNewRequest;

    public boolean isNewAndReset() {
        boolean res = this.isNewRequest;
        this.isNewRequest = false;
        return res;
    }

    public enum ClientType {
        /**
         * 隧道代理，不解析 http
         */
        TUNNEL,

        /**
         * 普通代理，解析 http
         */
        NORMAL
    }
}
