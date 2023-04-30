package com.catas.wicked.server.handler.server;

import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.pipeline.MessageQueue;
import com.catas.wicked.server.cert.CertPool;
import com.catas.wicked.server.handler.ClientInitializerFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


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
        ch.pipeline().addLast("httpCodec", new HttpServerCodec());
        ch.pipeline().addLast("strategyHandler", new StrategyHandler(appConfig, certPool));
        if (appConfig.isRecording()) {
            ch.pipeline().addLast("httpAggregator",
                    new HttpObjectAggregator(appConfig.getMaxContentSize()) {
                        @Override
                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                            // TODO process maxContent
                            if (cause.getCause() instanceof TooLongFrameException) {
                                // TODO
                            }
                            super.exceptionCaught(ctx, cause);
                        }
                    });
            ch.pipeline().addLast("requestRecorder", new RequestRecordHandler(appConfig, messageQueue));
        }
        ch.pipeline().addLast("proxyProcessor", new ProxyProcessHandler(appConfig, clientInitializerFactory));
    }
}
