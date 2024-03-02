package com.catas.wicked.server.handler.server;

import com.catas.wicked.common.bean.IdGenerator;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.pipeline.MessageQueue;
import com.catas.wicked.server.cert.CertPool;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpServerCodec;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import static com.catas.wicked.common.constant.NettyConstant.*;


@Slf4j
@Singleton
public class ProxyServerInitializer extends ChannelInitializer {

    @Inject
    private ApplicationConfig appConfig;

    @Inject
    private CertPool certPool;

    @Inject
    private MessageQueue messageQueue;

    @Inject
    private IdGenerator idGenerator;

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline().addLast(HTTP_CODEC, new HttpServerCodec());
        ch.pipeline().addLast(SERVER_STRATEGY, new ServerStrategyHandler(appConfig, certPool, idGenerator));
        ch.pipeline().addLast(SERVER_PROCESSOR, new ProxyProcessHandler(appConfig, messageQueue));
        ch.pipeline().addLast(POST_RECORDER, new ServerPostRecorder(appConfig, messageQueue));
    }
}
