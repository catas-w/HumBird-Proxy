package com.catas.wicked.server.handler.client;

import com.catas.wicked.common.bean.ProxyRequestInfo;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.pipeline.MessageQueue;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.proxy.ProxyHandler;
import lombok.Setter;

@Setter
public class ProxyClientInitializer extends ChannelInitializer {

    private ApplicationConfig appConfig;

    private Channel clientChannel;

    private ProxyRequestInfo requestInfo;

    private ProxyHandler proxyHandler;

    private MessageQueue messageQueue;

    public ProxyClientInitializer(Channel clientChannel,
                                  ProxyHandler proxyHandler,
                                  ApplicationConfig applicationConfig) {
        this.clientChannel = clientChannel;
        this.proxyHandler = proxyHandler;
        this.appConfig = applicationConfig;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        if (proxyHandler != null) {
            ch.pipeline().addLast(proxyHandler);
        }
        if (appConfig.isHandleSsl()) {
            // TODO
            ch.pipeline().addLast(appConfig.getClientSslCtx().newHandler(ch.alloc(),
                            appConfig.getHost(),
                            appConfig.getPort()));
        }
        ch.pipeline().addLast("httpCodec", new HttpClientCodec());
        ch.pipeline().addLast("httpAggregator", new HttpObjectAggregator(appConfig.getMaxContentSize()));
        ch.pipeline().addLast("responseRecorder", new ResponseRecordHandler(appConfig, messageQueue, requestInfo));
        ch.pipeline().addLast("proxyClientHandler", new ProxyClientHandler(clientChannel));
    }
}
