package com.catas.wicked.server.proxy.handler;

import com.catas.wicked.common.bean.ProxyRequestInfo;
import com.catas.wicked.common.config.ApplicationConfig;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.proxy.ProxyHandler;


public class ProxyInitializer extends ChannelInitializer {

    private ApplicationConfig applicationConfig;

    private Channel clientChannel;

    private ProxyRequestInfo requestInfo;

    private ProxyHandler proxyHandler;

    public ProxyInitializer(Channel clientChannel,
                            ProxyRequestInfo requestInfo,
                            ProxyHandler proxyHandler,
                            ApplicationConfig applicationConfig) {
        this.clientChannel = clientChannel;
        this.requestInfo = requestInfo;
        this.proxyHandler = proxyHandler;
        this.applicationConfig = applicationConfig;
    }

    public ProxyInitializer(Channel clientChannel) {
        this.clientChannel = clientChannel;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        if (proxyHandler != null) {
            ch.pipeline().addLast(proxyHandler);
        }
        if (applicationConfig.isHandleSsl()) {
            // TODO
            ch.pipeline().addLast(
                    applicationConfig.getClientSslCtx().newHandler(ch.alloc(),
                    applicationConfig.getHost(),
                    applicationConfig.getPort()));
        }
        ch.pipeline().addLast("httpCodec", new HttpClientCodec());
        ch.pipeline().addLast("proxyClientHandler", new ProxyClientHandler(clientChannel));
        // ch.pipeline().addLast("proxyOutHandler", new ProxyClientOutHandler());
    }
}
