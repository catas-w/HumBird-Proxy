package com.catas.wicked.server.strategy;


import io.netty.channel.ChannelHandler;
import lombok.Data;

import java.util.function.Supplier;

@Data
public class StrategyModel {

    /**
     * handler name
     */
    private String handlerName;

    /**
     * is this handler required
     */
    private boolean required;

    /**
     * set true to make handler always required
     */
    private boolean anchored;

    /**
     * supply specified handler
     */
    private Supplier<ChannelHandler> supplier;

    public StrategyModel(String handlerName, boolean required, Supplier<ChannelHandler> supplier) {
        this.handlerName = handlerName;
        this.required = required;
        this.supplier = supplier;
    }

    public StrategyModel(String handlerName, boolean required, boolean anchored, Supplier<ChannelHandler> supplier) {
        this.handlerName = handlerName;
        this.required = required;
        this.anchored = anchored;
        this.supplier = supplier;
    }

    public boolean isRequired() {
        return anchored || required;
    }
}
