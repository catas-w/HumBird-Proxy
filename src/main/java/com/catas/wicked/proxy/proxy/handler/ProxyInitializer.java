package com.catas.wicked.proxy.proxy.handler;

import com.catas.wicked.proxy.bean.ProxyRequestInfo;
import com.catas.wicked.proxy.config.ProxyConfig;
import com.catas.wicked.proxy.proxy.handler.ProxyClientHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.proxy.ProxyHandler;

public class ProxyInitializer extends ChannelInitializer {

    private Channel clientChannel;

    private ProxyRequestInfo requestInfo;

    private ProxyHandler proxyHandler;

    public ProxyInitializer(Channel clientChannel, ProxyRequestInfo requestInfo, ProxyHandler proxyHandler) {
        this.clientChannel = clientChannel;
        this.requestInfo = requestInfo;
        this.proxyHandler = proxyHandler;
    }

    public ProxyInitializer(Channel clientChannel) {
        this.clientChannel = clientChannel;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        if (proxyHandler != null) {
            ch.pipeline().addLast(proxyHandler);
        }
        if (ProxyConfig.getInstance().isHandleSsl()) {
            // TODO
        }
        ch.pipeline().addLast("httpCodec", new HttpClientCodec());
        ch.pipeline().addLast("proxyClientHandler", new ProxyClientHandler(clientChannel));
        ch.pipeline().addLast("proxyOutHandler", new ProxyClientOutHandler());
    }
}
