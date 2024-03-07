package com.catas.wicked.server.strategy;

import io.netty.channel.ChannelPipeline;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class DefaultStrategyManager implements StrategyManager{

    @Override
    public void arrange(ChannelPipeline pipeline, List<StrategyMode> modeList) {
        if (pipeline == null || modeList == null) {
            return;
        }
        /**
         * a-b-c-d-e
         * a-d
         * a-v-s-b-c-e
         */

    }

    @Override
    public boolean verify(ChannelPipeline pipeline, List<StrategyMode> modeList) {
        return false;
    }
}
