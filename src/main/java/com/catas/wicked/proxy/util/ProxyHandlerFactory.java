package com.catas.wicked.proxy.util;

import com.catas.wicked.proxy.config.ProxyConfig;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;

import java.net.InetSocketAddress;

public class ProxyHandlerFactory {

    private ProxyConfig config;

    public ProxyHandler build() {
        ProxyHandler proxyHandler = null;
        if (config != null) {
            InetSocketAddress inetSocketAddress = new InetSocketAddress(config.getHost(),
                    config.getPort());
            switch (config.getProxyType()) {
                case HTTP:
                    proxyHandler = new HttpProxyHandler(inetSocketAddress);
                    break;
                case SOCKS4:
                    proxyHandler = new Socks4ProxyHandler(inetSocketAddress);
                    break;
                case SOCKS5:
                    proxyHandler = new Socks5ProxyHandler(inetSocketAddress);
            }
        }
        return proxyHandler;
    }
}
