package com.catas.wicked.server.strategy;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class DefaultStrategyManager implements StrategyManager{

    @Override
    public void arrange(ChannelPipeline pipeline, List<StrategyModel> modelList) {
        if (pipeline == null || modelList == null) {
            return;
        }
        Map<String, ChannelHandler> handlerMap = pipeline.toMap();
        String prev = null;
        int x = 0;
        List<StrategyModel> targetLis = modelList.stream().filter(StrategyModel::isRequired).toList();

        while (x < targetLis.size()) {
            StrategyModel strategyMode = targetLis.get(x);
            String target = strategyMode.getHandlerName();

            List<String> handlerNames = pipeline.names();
            if (x >= handlerNames.size() || !target.equals(handlerNames.get(x))) {
                // add
                ChannelHandler toAdd;
                if (handlerMap.containsKey(target)) {
                    toAdd = handlerMap.get(target);
                    pipeline.remove(target);
                } else {
                    toAdd = strategyMode.getSupplier().get();
                }
                if (prev == null) {
                    // System.out.println(toAdd);
                    pipeline.addFirst(target, toAdd);
                } else {
                    pipeline.addAfter(prev, target, toAdd);
                }
            }

            prev = target;
            x ++;
        }

        while (targetLis.size() < pipeline.names().size()) {
            pipeline.removeLast();
        }
    }

    @Override
    public boolean verify(ChannelPipeline pipeline, List<StrategyModel> modelList) {
        if (pipeline == null || modelList == null) {
            return false;
        }
        List<String> handlerNames = pipeline.names();
        List<String> targetNames = modelList.stream()
                .filter(StrategyModel::isRequired)
                .map(StrategyModel::getHandlerName)
                .toList();
        if (handlerNames.size() != targetNames.size()) {
            return false;
        }
        int index = 0;
        while (index < targetNames.size()) {
            if (!targetNames.get(index).equals(handlerNames.get(index))) {
                return false;
            }
            index ++;
        }

        return true;
    }
}
