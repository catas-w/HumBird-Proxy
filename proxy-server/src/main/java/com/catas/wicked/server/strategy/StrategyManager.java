package com.catas.wicked.server.strategy;

import io.netty.channel.ChannelPipeline;

import java.util.List;

public interface StrategyManager {

    /**
     * arrange channelHandlers in pipeline according to the order defined by modeList
     * @param pipeline ChannelPipeline
     * @param strategyList strategyModel list
     */
    void arrange(ChannelPipeline pipeline, StrategyList strategyList);

    /**
     * verify order consistency
     * @param pipeline ChannelPipeline
     * @param strategyList strategyModel list
     * @return boolean
     */
    boolean verify(ChannelPipeline pipeline, StrategyList strategyList);
}
