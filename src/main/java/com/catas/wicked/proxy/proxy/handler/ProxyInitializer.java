package com.catas.wicked.proxy.proxy.handler;

import com.catas.wicked.proxy.bean.ProxyRequestInfo;
import com.catas.wicked.proxy.config.ProxyConfig;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.proxy.ProxyHandler;


public class ProxyInitializer extends ChannelInitializer {

    private ProxyConfig proxyConfig;

    private Channel clientChannel;

    private ProxyRequestInfo requestInfo;

    private ProxyHandler proxyHandler;

    public ProxyInitializer(Channel clientChannel,
                            ProxyRequestInfo requestInfo,
                            ProxyHandler proxyHandler,
                            ProxyConfig proxyConfig) {
        this.clientChannel = clientChannel;
        this.requestInfo = requestInfo;
        this.proxyHandler = proxyHandler;
        this.proxyConfig = proxyConfig;
    }

    public ProxyInitializer(Channel clientChannel) {
        this.clientChannel = clientChannel;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        if (proxyHandler != null) {
            ch.pipeline().addLast(proxyHandler);
        }
        if (proxyConfig.isHandleSsl()) {
            // TODO
            ch.pipeline().addLast(
                    proxyConfig.getClientSslCtx().newHandler(ch.alloc(),
                    proxyConfig.getHost(),
                    proxyConfig.getPort()));
        }
        ch.pipeline().addLast("httpCodec", new HttpClientCodec());
        ch.pipeline().addLast("proxyClientHandler", new ProxyClientHandler(clientChannel));
        ch.pipeline().addLast("proxyOutHandler", new ProxyClientOutHandler());
    }
}
