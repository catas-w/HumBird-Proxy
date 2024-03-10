package com.catas.wicked.server.strategy;


import io.netty.channel.ChannelHandler;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Predicate;
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

    /**
     * predicate if given handlerName is eligible
     */
    private Predicate<String> eligiblePredicate;

    /**
     * predicate if skip current handlerName
     */
    private Predicate<String> skipPredicate;

    public StrategyModel() {
    }

    public StrategyModel(String handlerName, boolean required, Supplier<ChannelHandler> supplier) {
        this.handlerName = handlerName;
        this.required = required;
        this.supplier = supplier;
    }

    public StrategyModel(String handlerName, boolean required, Supplier<ChannelHandler> supplier, Predicate<String> eligiblePredicate) {
        this(handlerName, required, supplier);
        this.eligiblePredicate = eligiblePredicate;
    }

    public StrategyModel(String handlerName, boolean required, boolean anchored, Supplier<ChannelHandler> supplier) {
        this.handlerName = handlerName;
        this.required = required;
        this.anchored = anchored;
        this.supplier = supplier;
    }

    public StrategyModel(String handlerName, boolean required, boolean anchored, Supplier<ChannelHandler> supplier,
                         Predicate<String> eligiblePredicate, Predicate<String> skipPredicate) {
        this(handlerName, required, anchored, supplier);
        this.eligiblePredicate = eligiblePredicate;
        this.skipPredicate = skipPredicate;
    }

    public boolean isRequired() {
        return anchored || required;
    }

    public boolean isEligible(String name) {
        if (eligiblePredicate != null) {
            return eligiblePredicate.test(name);
        }
        return StringUtils.equalsIgnoreCase(this.handlerName, name);
    }

    public boolean skip(String name) {
        return skipPredicate != null && name != null && skipPredicate.test(name);
    }
}
