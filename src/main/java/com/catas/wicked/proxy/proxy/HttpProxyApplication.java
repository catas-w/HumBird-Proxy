package com.catas.wicked.proxy.proxy;

import com.catas.wicked.proxy.config.ProxyConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpProxyApplication {

    public static void main(String[] args) {
        log.info("Server starting...");
        ProxyConfig proxyConfig = ProxyConfig.getInstance();
        proxyConfig.setPort(9999);
        ProxyServer proxyServer = new ProxyServer();
        proxyServer.start();
    }
}
