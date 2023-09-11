package com.catas.wicked.server;

import com.catas.wicked.server.proxy.ProxyServer;
// import io.micronaut.runtime.Micronaut;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class HttpProxyApplication {

    @Inject
    private static ProxyServer proxyServer;

    public static void main(String[] args) {
        // Micronaut.run(HttpProxyApplication.class, args);
    }
}
