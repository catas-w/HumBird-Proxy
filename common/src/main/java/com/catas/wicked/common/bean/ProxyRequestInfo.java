package com.catas.wicked.common.bean;

import lombok.Data;

/**
 * request-info of a channel
 */
@Data
public class ProxyRequestInfo {

    private String host;

    private int port;

    private boolean ssl;

    private String path;

    private boolean isRecording;

    private String requestId;

    private ClientType clientType;

    private long requestStartTime;

    private long requestEndTime;

    private long responseStartTime;

    private long responseEndTime;
    private boolean isNewRequest;

    private boolean usingExternalProxy;

    private boolean isOversize;

    public boolean isNewAndReset() {
        boolean res = this.isNewRequest;
        this.isNewRequest = false;
        return res;
    }

    public synchronized void setRequestTime() {
        long timestamp = System.currentTimeMillis();
        if (requestStartTime == 0L) {
            requestStartTime = timestamp;
        }
        requestEndTime = timestamp;
    }

    public synchronized void setResponseTime() {
        long timestamp = System.currentTimeMillis();
        if (responseStartTime == 0L) {
            responseStartTime = timestamp;
        }
        responseEndTime = timestamp;
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
