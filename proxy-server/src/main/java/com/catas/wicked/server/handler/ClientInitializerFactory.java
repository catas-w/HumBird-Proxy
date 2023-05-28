package com.catas.wicked.server.handler;

import com.catas.wicked.common.bean.ProxyRequestInfo;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.pipeline.MessageQueue;
import com.catas.wicked.server.handler.client.ProxyClientInitializer;
import com.catas.wicked.server.handler.client.TunnelProxyInitializer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.proxy.ProxyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class ClientInitializerFactory {

    @Autowired
    private ApplicationConfig appConfig;

    @Autowired
    private MessageQueue messageQueue;

    public ChannelInitializer getChannelInitializer(Channel channel,
                                                    ProxyHandler proxyHandler,
                                                    ProxyRequestInfo requestInfo) {
        if (requestInfo.getClientType() == ProxyRequestInfo.ClientType.NORMAL) {
            ProxyClientInitializer httpInitializer = new ProxyClientInitializer(channel, proxyHandler, appConfig);
            httpInitializer.setAppConfig(appConfig);
            httpInitializer.setMessageQueue(messageQueue);
            httpInitializer.setRequestInfo(requestInfo);
            return httpInitializer;
        } else {
            TunnelProxyInitializer tunnelProxyInitializer = new TunnelProxyInitializer(channel, proxyHandler);
            return tunnelProxyInitializer;
        }
    }
}
