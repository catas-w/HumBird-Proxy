package com.catas.wicked.proxy.bean;

import lombok.Data;

@Data
public class ProxyRequestInfo {
    private String host;

    private int port;

    private boolean ssl;

    private String path;
}
