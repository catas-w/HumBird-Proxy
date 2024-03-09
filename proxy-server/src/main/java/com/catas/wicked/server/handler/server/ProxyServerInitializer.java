package com.catas.wicked.server.handler.server;

import com.catas.wicked.common.bean.IdGenerator;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.pipeline.MessageQueue;
import com.catas.wicked.server.cert.CertPool;
import com.catas.wicked.server.handler.RearHttpAggregator;
import com.catas.wicked.server.strategy.Handler;
import com.catas.wicked.server.strategy.StrategyList;
import com.catas.wicked.server.strategy.StrategyManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpServerCodec;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;



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

    @Inject
    private StrategyManager strategyManager;

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline().addLast(Handler.HTTP_CODEC.name(), new HttpServerCodec());
        ch.pipeline().addLast(Handler.SERVER_STRATEGY.name(),
                new ServerStrategyHandler(appConfig, certPool, idGenerator, defaultStrategyList(), strategyManager));
        ch.pipeline().addLast(Handler.SERVER_PROCESSOR.name(), new ProxyProcessHandler(appConfig, messageQueue));
        ch.pipeline().addLast(Handler.POST_RECORDER.name(), new ServerPostRecorder(appConfig, messageQueue));
    }

    private StrategyList defaultStrategyList() {
        StrategyList strategyList = new StrategyList();
        strategyList.add(Handler.SSL_HANDLER.name(), false, () -> null);
        strategyList.add(Handler.HTTP_CODEC.name(), true, HttpServerCodec::new);
        strategyList.add(Handler.SERVER_STRATEGY.name(), true, true,
                () -> new ServerStrategyHandler(appConfig, certPool, idGenerator, defaultStrategyList(), strategyManager));
        strategyList.add(Handler.SERVER_PROCESSOR.name(), true, true,
                () -> new ProxyProcessHandler(appConfig, messageQueue));
        strategyList.add(Handler.HTTP_AGGREGATOR.name(), false,
                () -> new RearHttpAggregator(appConfig.getMaxContentSize()));
        strategyList.add(Handler.POST_RECORDER.name(), true, true,
                () -> new ServerPostRecorder(appConfig, messageQueue));
        return strategyList;
    }
}
