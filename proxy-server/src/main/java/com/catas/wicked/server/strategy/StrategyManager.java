package com.catas.wicked.server.strategy;

import io.netty.channel.ChannelPipeline;

import java.util.List;

public interface StrategyManager {

    void arrange(ChannelPipeline pipeline, List<StrategyMode> modeList);

    boolean verify(ChannelPipeline pipeline, List<StrategyMode> modeList);
}
