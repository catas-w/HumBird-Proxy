package com.catas.wicked.server.handler.client;

import com.catas.wicked.common.bean.ProxyRequestInfo;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.pipeline.MessageQueue;
import com.catas.wicked.common.util.ProxyHandlerFactory;
import com.catas.wicked.common.util.WebUtils;
import com.catas.wicked.server.handler.RearHttpAggregator;
import com.catas.wicked.server.strategy.StrategyList;
import com.catas.wicked.server.strategy.StrategyManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpClientCodec;
import lombok.extern.slf4j.Slf4j;

import static com.catas.wicked.server.strategy.Handler.*;


@Slf4j
public class ClientChannelInitializer extends ChannelInitializer {

    private ApplicationConfig appConfig;

    private MessageQueue messageQueue;

    private StrategyManager strategyManager;

    private ProxyRequestInfo requestInfo;

    private Channel serverChannel;

    public ClientChannelInitializer(ApplicationConfig appConfig,
                                    MessageQueue messageQueue,
                                    ProxyRequestInfo requestInfo,
                                    StrategyManager strategyManager,
                                    Channel serverChannel) {
        this.appConfig = appConfig;
        this.messageQueue = messageQueue;
        this.strategyManager = strategyManager;
        this.requestInfo = requestInfo;
        this.serverChannel = serverChannel;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        StrategyList strategyList = defaultStrategyList();
        ch.pipeline().addLast(CLIENT_PROCESSOR.name(), strategyList.getSupplier(CLIENT_PROCESSOR.name()).get());
        ch.pipeline().addLast(POST_RECORDER.name(), strategyList.getSupplier(POST_RECORDER.name()).get());
        ch.pipeline().addLast(CLIENT_STRATEGY.name(), strategyList.getSupplier(CLIENT_STRATEGY.name()).get());
    }

    private StrategyList defaultStrategyList() {
        StrategyList list = new StrategyList();
        list.add(EXTERNAL_PROXY.name(), false,
                () -> ProxyHandlerFactory.getExternalProxyHandler(appConfig.getSettings().getExternalProxy(),
                        WebUtils.getHostname(requestInfo)));
        list.add(SSL_HANDLER.name(), false, () -> null);
        list.add(HTTP_CODEC.name(), false, HttpClientCodec::new);
        list.add(CLIENT_PROCESSOR.name(), true, true,
                () -> new ProxyClientHandler(serverChannel));
        list.add(HTTP_AGGREGATOR.name(), false,
                () -> new RearHttpAggregator(appConfig.getMaxContentSize()));
        list.add(POST_RECORDER.name(), true, true,
                () -> new ClientPostRecorder(appConfig, messageQueue));
        list.add(CLIENT_STRATEGY.name(), true, true,
                () -> new ClientStrategyHandler(appConfig, messageQueue, requestInfo, defaultStrategyList(), strategyManager));

        return list;
    }
}
