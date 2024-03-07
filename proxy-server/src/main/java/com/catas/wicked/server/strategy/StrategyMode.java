package com.catas.wicked.server.strategy;


import io.netty.channel.ChannelHandler;
import lombok.Data;

import java.util.function.Supplier;

@Data
public class StrategyMode {

    private HandlerName handlerName;

    private boolean required;

    // private Function<?, ChannelHandler> creator;

    private Supplier<ChannelHandler> supplier;
}
