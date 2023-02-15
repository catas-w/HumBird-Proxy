package com.catas.wicked.proxy.proxy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpProxyApplication {

    public static void main(String[] args) {
        log.info("Server starting...");
        int port = 12005;
        ProxyServer proxyServer = new ProxyServer(port);
        proxyServer.start();
    }
}
