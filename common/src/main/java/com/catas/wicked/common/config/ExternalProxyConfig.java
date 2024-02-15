package com.catas.wicked.common.config;

import com.catas.wicked.common.constant.ProxyProtocol;
import lombok.Data;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

@Data
public class ExternalProxyConfig {

    private ProxyProtocol protocol;

    private String host;
    private int port;
    private SocketAddress socketAddress;

    private String username;

    private String password;

    private boolean usingExternalProxy;

    private boolean proxyAuth;

    public void setProxyAddress(String hostname, int port) {
        socketAddress = new InetSocketAddress(hostname, port);
    }

    public void setProxyAddress() {
        setProxyAddress(host, port);
    }

    public SocketAddress getSocketAddress() {
        if (socketAddress != null) {
            return socketAddress;
        }
        setProxyAddress();
        return socketAddress;
    }
}
