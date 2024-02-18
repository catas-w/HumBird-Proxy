package com.catas.wicked.common.constant;

public enum ProxyProtocol {
    None("None"),
    System("System Proxy"),
    HTTP("HTTP"),
    SOCKS4("SOCKS4"),
    SOCKS5("SOCKS5");

    private String name;

    ProxyProtocol(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
