package com.catas.wicked.server.handler.server;

import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.pipeline.MessageQueue;
import com.catas.wicked.server.cert.CertPool;
import com.catas.wicked.server.handler.ClientInitializerFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpServerCodec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.catas.wicked.common.common.NettyConstant.HTTP_CODEC;
import static com.catas.wicked.common.common.NettyConstant.REQUEST_RECORDER;
import static com.catas.wicked.common.common.NettyConstant.SERVER_PROCESSOR;
import static com.catas.wicked.common.common.NettyConstant.SERVER_STRATEGY;


@Slf4j
@Component
public class ProxyServerInitializer extends ChannelInitializer {

    @Autowired
    private ApplicationConfig appConfig;

    @Autowired
    private CertPool certPool;

    @Autowired
    private MessageQueue messageQueue;

    @Autowired
    private ClientInitializerFactory clientInitializerFactory;

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline().addLast(HTTP_CODEC, new HttpServerCodec());
        ch.pipeline().addLast(SERVER_STRATEGY, new StrategyHandler(appConfig, certPool));
        ch.pipeline().addLast(REQUEST_RECORDER, new RequestRecordHandler(appConfig, messageQueue));
        ch.pipeline().addLast(SERVER_PROCESSOR, new ProxyProcessHandler(appConfig, clientInitializerFactory, messageQueue));
    }
}
