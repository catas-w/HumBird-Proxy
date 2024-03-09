package com.catas.wicked.server.strategy;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@Singleton
public class DefaultStrategyManager implements StrategyManager{

    @Override
    public void arrange(ChannelPipeline pipeline, StrategyList strategyList) {
        if (pipeline == null || strategyList == null) {
            return;
        }
        Map<String, ChannelHandler> handlerMap = pipeline.toMap();
        String prev = null;
        int x = 0;
        List<StrategyModel> targetList = strategyList.getList().stream().filter(StrategyModel::isRequired).toList();

         // A-B-C-D-E
         // X-A-B-C-TAIL
         // X-TAIL
        while (x < targetList.size()) {
            StrategyModel strategyMode = targetList.get(x);
            String target = strategyMode.getHandlerName();

            List<String> handlerNames = pipeline.names();
            String currentHandler = handlerNames.get(x);
            if (!target.equals(currentHandler)) {
                if (!isTailHandler(currentHandler) && !strategyList.isRequired(currentHandler)) {
                    pipeline.remove(currentHandler);
                    continue;
                }
                // try to create first
                ChannelHandler toAdd;
                if (strategyMode.getSupplier() != null) {
                    toAdd = strategyMode.getSupplier().get();
                } else {
                    toAdd = handlerMap.get(target);
                }
                try {
                    pipeline.remove(target);
                } catch (NoSuchElementException ignored) {}
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

        int toTrim = pipeline.names().size() - targetList.size() - 1;
        while (toTrim > 0) {
            pipeline.removeLast();
            toTrim --;
        }
        // System.out.println("After Arrange: " + pipeline.names());
    }

    private boolean isTailHandler(String handlerName) {
        return handlerName != null && StringUtils.containsAnyIgnoreCase(handlerName, "TailContext");
    }

    @Override
    public boolean verify(ChannelPipeline pipeline, StrategyList strategyList) {
        if (pipeline == null || strategyList == null) {
            return false;
        }
        List<String> handlerNames = pipeline.names();
        List<String> targetNames = strategyList.getList().stream()
                .filter(StrategyModel::isRequired)
                .map(StrategyModel::getHandlerName)
                .toList();
        if (handlerNames.size() != targetNames.size() + 1) {
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
