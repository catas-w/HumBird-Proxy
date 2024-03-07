package com.catas.wicked.server.strategy;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Only used for StrategyManager unit test
 */
@Slf4j
public abstract class TestAbstractChannelPipeline implements ChannelPipeline {

    List<Pair<String, ChannelHandler>> list = new LinkedList<>();

    int getIndex(String name) {
        for (int i=0; i < list.size(); i++) {
            if (StringUtils.equals(name, list.get(i).getKey())) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public ChannelPipeline addFirst(String name, ChannelHandler handler) {
        list.add(0, new Pair<>(name, handler));
        return this;
    }

    @Override
    public ChannelPipeline addLast(String name, ChannelHandler handler) {
        list.add(new Pair<>(name, handler));
        return this;
    }

    @Override
    public ChannelPipeline addBefore(String baseName, String name, ChannelHandler handler) {
        int index = getIndex(baseName);
        if (index == -1) {
            throw new IllegalArgumentException("Base name not exist");
        }
        int addIndex = index == 0 ? 0 : index - 1;
        list.add(addIndex, new Pair<>(name, handler));
        return this;
    }

    @Override
    public ChannelPipeline addAfter(String baseName, String name, ChannelHandler handler) {
        int index = getIndex(baseName);
        if (index == -1) {
            throw new IllegalArgumentException("Base name not exist");
        }
        list.add(index + 1, new Pair<>(name, handler));
        return this;
    }

    @Override
    public ChannelHandler remove(String name) {
        list.removeIf(item -> StringUtils.equals(name, item.getKey()));
        return null;
    }

    @Override
    public ChannelHandler removeFirst() {
        if (list.size() > 0) {
            list.remove(0);
        }
        return null;
    }

    @Override
    public ChannelHandler removeLast() {
        if (list.size() > 0) {
            list.remove(list.size() - 1);
        }
        return null;
    }



    @Override
    public List<String> names() {
        List<String> names = list.stream().map(Pair::getKey).toList();
        log.info("Pipeline names: {}", names);
        return names;
    }

    @Override
    public Map<String, ChannelHandler> toMap() {
        Map<String, ChannelHandler> map = list.stream().collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        log.info("Pipeline map: {}", map);
        return map;
    }
}
