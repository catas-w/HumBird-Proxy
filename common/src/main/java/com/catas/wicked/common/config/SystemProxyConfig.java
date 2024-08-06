package com.catas.wicked.common.config;

import lombok.Data;

@Data
public class SystemProxyConfig {

    private String protocol;

    private String host;

    private int port;

    private boolean auth;

    private String username;

    private String password;
}
