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

import static com.catas.wicked.server.strategy.Handler.HTTP_CODEC;
import static com.catas.wicked.server.strategy.Handler.POST_RECORDER;
import static com.catas.wicked.server.strategy.Handler.SERVER_PROCESSOR;
import static com.catas.wicked.server.strategy.Handler.SERVER_STRATEGY;

@Slf4j
@Singleton
public class ServerChannelInitializer extends ChannelInitializer {

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
        StrategyList strategyList = defaultStrategyList();
        ch.pipeline().addLast(HTTP_CODEC.name(), strategyList.getSupplier(HTTP_CODEC.name()).get());
        ch.pipeline().addLast(SERVER_STRATEGY.name(), strategyList.getSupplier(SERVER_STRATEGY.name()).get());
        ch.pipeline().addLast(SERVER_PROCESSOR.name(), strategyList.getSupplier(SERVER_PROCESSOR.name()).get());
        ch.pipeline().addLast(POST_RECORDER.name(), strategyList.getSupplier(POST_RECORDER.name()).get());
    }

    private StrategyList defaultStrategyList() {
        StrategyList list = new StrategyList();
        list.add(Handler.SSL_HANDLER.name(), false, () -> null);
        list.add(HTTP_CODEC.name(), true, HttpServerCodec::new);
        list.add(SERVER_STRATEGY.name(), true, true,
                () -> new ServerStrategyHandler(appConfig, certPool, idGenerator, defaultStrategyList(), strategyManager));
        list.add(SERVER_PROCESSOR.name(), true, true,
                () -> new ProxyProcessHandler(appConfig, messageQueue, strategyManager));
        list.add(Handler.HTTP_AGGREGATOR.name(), false,
                () -> new RearHttpAggregator(appConfig.getMaxContentSize()));
        list.add(POST_RECORDER.name(), true, true,
                () -> new ServerPostRecorder(appConfig, messageQueue));
        return list;
    }
}
