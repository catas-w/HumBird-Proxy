package com.catas.wicked.server.strategy;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

@Slf4j
@Singleton
public class TailStrategyManager implements StrategyManager{

    @Override
    public void arrange(ChannelPipeline pipeline, StrategyList strategyList) {
        if (pipeline == null || strategyList == null) {
            return;
        }
        Map<String, ChannelHandler> handlerMap = pipeline.toMap();
        List<StrategyModel> targetList = strategyList.getList().stream().filter(StrategyModel::isRequired).toList();
        if (!(targetList.get(targetList.size() - 1) instanceof TailContextStrategy)) {
            targetList = new ArrayList<>(targetList);
            targetList.add(TAIL_CONTEXT);
        }
        String prev = null;
        int x = 0;
        int y = 0;

        // A-B-C-D-E-TAIL
        // A-E-TAIL
        // #-A-B-#-C-TAIL
        // #-#-A-C-#-TAIL
        // X-C-B-A-TAIL
        // X-TAIL
        while (x < targetList.size()) {
            StrategyModel model = targetList.get(x);
            String target = model.getHandlerName();
            List<String> handlerNames = pipeline.names();
            String currentName = handlerNames.get(y);

            if (isTailHandler(target) && isTailHandler(currentName)) {
                break;
            }

            // skip current handler
            if (!isTailHandler(currentName) && model.skip(currentName)) {
                y ++;
                prev = currentName;
                continue;
            }

            if (!model.isEligible(currentName)) {
                if (!isTailHandler(currentName) && !strategyList.isRequired(currentName)) {
                    // remove redundant handler
                    pipeline.remove(currentName);
                    continue;
                }

                ChannelHandler toAdd;
                if (model.getSupplier() != null) {
                    toAdd = model.getSupplier().get();
                } else {
                    toAdd = handlerMap.get(target);
                }
                // System.out.println(toAdd);
                try {
                    pipeline.remove(target);
                } catch (NoSuchElementException ignored) {}
                if (prev == null) {
                    pipeline.addFirst(target, toAdd);
                } else {
                    pipeline.addAfter(prev, target, toAdd);
                }
            }

            prev = target;
            x ++;
            y ++;
        }
        // System.out.println("After Arrange: " + pipeline.names());
    }

    @Override
    public boolean verify(ChannelPipeline pipeline, StrategyList strategyList) {
        if (pipeline == null || strategyList == null) {
            return false;
        }
        List<String> handlerNames = pipeline.names();
        List<StrategyModel> targetList = strategyList.getList().stream().filter(StrategyModel::isRequired).toList();
        if (!(targetList.get(targetList.size() - 1) instanceof TailContextStrategy)) {
            targetList = new ArrayList<>(targetList);
            targetList.add(TAIL_CONTEXT);
        }

        int x = 0;
        int y = 0;
        while (x < targetList.size()) {
            if (y >= handlerNames.size()) {
                return isTailHandler(targetList.get(x).getHandlerName());
            }
            StrategyModel model = targetList.get(x);
            String currentName = handlerNames.get(y);

            if (model.skip(currentName)) {
                y ++;
                continue;
            }
            if (!model.isEligible(currentName)) {
                return false;
            }
            x ++;
            y ++;
        }

        return y == handlerNames.size();
    }

    /**
     * match netty's tailContext handler
     */
    public static boolean isTailHandler(String handlerName) {
        return StringUtils.containsAnyIgnoreCase(handlerName, "TailContext");
    }

    protected static final TailContextStrategy TAIL_CONTEXT = new TailContextStrategy();

    public static class TailContextStrategy extends StrategyModel {

        public TailContextStrategy() {
            super("TailContextStrategy", true, true, null);
            this.setEligiblePredicate(TailStrategyManager::isTailHandler);
        }

        public TailContextStrategy(Predicate<String> skipPredicate) {
            this();
            this.setSkipPredicate(skipPredicate);
        }


    }
}
