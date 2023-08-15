package com.catas.wicked.server.handler.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.proxy.ProxyHandler;

@Deprecated
public class TunnelProxyInitializer extends ChannelInitializer {

    private final Channel clientChannel;
    private final ProxyHandler proxyHandler;

    public TunnelProxyInitializer(Channel clientChannel,
                                  ProxyHandler proxyHandler) {
        this.clientChannel = clientChannel;
        this.proxyHandler = proxyHandler;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        if (proxyHandler != null) {
            ch.pipeline().addLast(proxyHandler);
        }
        ch.pipeline().addLast("proxyClientHandler",  new ProxyClientHandler(clientChannel));
    }
}
