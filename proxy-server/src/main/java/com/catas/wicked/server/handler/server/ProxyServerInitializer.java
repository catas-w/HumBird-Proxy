package com.catas.wicked.server.handler.server;

import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.pipeline.MessageQueue;
import com.catas.wicked.server.cert.CertPool;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpServerCodec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.catas.wicked.common.constant.NettyConstant.*;


@Slf4j
@Component
public class ProxyServerInitializer extends ChannelInitializer {

    @Resource(name = "applicationConfig")
    private ApplicationConfig appConfig;

    @Resource
    private CertPool certPool;

    @Resource
    private MessageQueue messageQueue;

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline().addLast(HTTP_CODEC, new HttpServerCodec());
        ch.pipeline().addLast(SERVER_STRATEGY, new ServerStrategyHandler(appConfig, certPool));
        ch.pipeline().addLast(SERVER_PROCESSOR, new ProxyProcessHandler(appConfig, messageQueue));
        ch.pipeline().addLast(POST_RECORDER, new ServerPostRecorder(appConfig, messageQueue));
    }
}
