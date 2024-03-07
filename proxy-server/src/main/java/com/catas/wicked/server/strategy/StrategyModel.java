package com.catas.wicked.server.strategy;


import io.netty.channel.ChannelHandler;
import lombok.Data;

import java.util.function.Supplier;

@Data
public class StrategyModel {

    private String handlerName;

    private boolean required;

    private Supplier<ChannelHandler> supplier;

    public StrategyModel(String handlerName, boolean required, Supplier<ChannelHandler> supplier) {
        this.handlerName = handlerName;
        this.required = required;
        this.supplier = supplier;
    }
}
