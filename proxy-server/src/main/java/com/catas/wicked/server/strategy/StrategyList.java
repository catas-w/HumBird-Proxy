package com.catas.wicked.server.strategy;

import io.netty.channel.ChannelHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Container for StrategyModel
 */
@Slf4j
public class StrategyList {

    private final List<StrategyModel> list;
    private final Map<String, StrategyModel> map;

    public StrategyList() {
        this.list = new ArrayList<>();
        this.map = new HashMap<>();
    }

    public void add(String handlerName, boolean required, Supplier<ChannelHandler> supplier) {
        add(new StrategyModel(handlerName, required, supplier));
    }

    public void add(String handlerName, boolean required, boolean anchored, Supplier<ChannelHandler> supplier) {
        add(new StrategyModel(handlerName, required, anchored, supplier));
    }

    public void add(StrategyModel strategyModel) {
        if (map.containsKey(strategyModel.getHandlerName())) {
            throw new IllegalArgumentException("Duplicate strategyModel");
        }
        map.put(strategyModel.getHandlerName(), strategyModel);
        list.add(strategyModel);
    }

    public void delete(String handlerName) {
        StrategyModel model = map.remove(handlerName);
        if (model != null) {
            list.remove(model);
        }
    }

    public void delete(StrategyModel strategyModel) {
        map.remove(strategyModel.getHandlerName());
        list.remove(strategyModel);
    }

    public StrategyModel getStrategyModel(String handlerName) {
        return map.get(handlerName);
    }

    public void setRequire(String handlerName, boolean required) {
        StrategyModel model = getStrategyModel(handlerName);
        if (model == null) {
            throw new IllegalArgumentException("Strategy not exist");
        }
        model.setRequired(required);
    }

    public boolean isRequired(String handlerName) {
        StrategyModel model = getStrategyModel(handlerName);
        return model != null && model.isRequired();
    }

    public void setSupplier(String handlerName, Supplier<ChannelHandler> supplier) {
        StrategyModel model = getStrategyModel(handlerName);
        if (model == null) {
            throw new IllegalArgumentException("Strategy not exist");
        }
        model.setSupplier(supplier);
    }

    public Supplier<ChannelHandler> getSupplier(String handlerName) {
        StrategyModel model = getStrategyModel(handlerName);
        if (model != null) {
            return model.getSupplier();
        }
        return null;
    }

    public List<StrategyModel> getList() {
        return new ArrayList<>(list);
    }
}
