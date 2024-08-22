package com.catas.wicked.common.config;

import lombok.Data;

@Data
public class SystemProxyConfig {

    /**
     * Wi-fi, ethernet
     * on macos
     */
    private String networkService;

    /**
     * HTTP, HTTPS
     */
    private String proxyType;

    private boolean enabled;

    private String server;

    private int port;

    private boolean authEnabled;

    private String username;

    private String password;
}
