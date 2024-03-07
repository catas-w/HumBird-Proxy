package com.catas.wicked.server.strategy;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class StrategyManagerTest {

    @Test
    public void testMockPipeline() {
        TestChannelPipeline pipeline = new TestChannelPipeline();
        Assert.assertTrue(pipeline.names().isEmpty());

        pipeline.addFirst("3rd", new TestChannelHandler());
        pipeline.addFirst("2nd", new TestChannelHandler());
        pipeline.addFirst("1st", new TestChannelHandler());
        Assert.assertEquals("1st", pipeline.names().get(0));
        Assert.assertEquals("3rd", pipeline.names().get(2));

        pipeline.addAfter("1st", "1-1", new TestChannelHandler());
        Assert.assertEquals("1-1", pipeline.names().get(1));

        pipeline.remove("1-1");
        Assert.assertFalse(pipeline.names().contains("1-1"));
        Assert.assertEquals("2nd", pipeline.names().get(1));

        Map<String, ChannelHandler> map = pipeline.toMap();
        Assert.assertNotNull(map.get("1st"));
        Assert.assertNotNull(map.get("3rd"));
        Assert.assertNull(map.get("1-1"));

        pipeline.addLast("4th", new TestChannelHandler());
        pipeline.addLast("5th", new TestChannelHandler());
        Assert.assertEquals("5th", pipeline.names().get(4));

        while (pipeline.names().size() > 0) {
            pipeline.removeLast();
        }
        Assert.assertTrue(pipeline.names().isEmpty());
    }

    @Test
    public void testStrategyManager() {
        DefaultStrategyManager strategyManager = new DefaultStrategyManager();
        List<StrategyModel> list = new ArrayList<>();
        list.add(new StrategyModel("A", true, TestChannelHandler::new));
        list.add(new StrategyModel("A1", false, TestChannelHandler::new));
        list.add(new StrategyModel("B", true, TestChannelHandler::new));
        list.add(new StrategyModel("C", true, TestChannelHandler::new));
        list.add(new StrategyModel("C1", false, TestChannelHandler::new));
        list.add(new StrategyModel("D", true, TestChannelHandler::new));
        list.add(new StrategyModel("E", true, TestChannelHandler::new));

        {
            TestChannelPipeline pipeline = new TestChannelPipeline();
            pipeline.addFirst("A", new TestChannelHandler());
            pipeline.addFirst("B", new TestChannelHandler());
            pipeline.addFirst("C", new TestChannelHandler());

            strategyManager.arrange(pipeline, list);
            Assert.assertTrue(strategyManager.verify(pipeline, list));
        }

        {
            TestChannelPipeline pipeline = new TestChannelPipeline();
            strategyManager.arrange(pipeline, list);
            Assert.assertTrue(strategyManager.verify(pipeline, list));
        }

        {
            TestChannelPipeline pipeline = new TestChannelPipeline();
            pipeline.addFirst("C", new TestChannelHandler());

            strategyManager.arrange(pipeline, list);
            Assert.assertTrue(strategyManager.verify(pipeline, list));
        }

        {
            TestChannelPipeline pipeline = new TestChannelPipeline();
            pipeline.addLast("A", new TestChannelHandler());
            pipeline.addLast("B", new TestChannelHandler());
            pipeline.addLast("C", new TestChannelHandler());
            pipeline.addLast("D", new TestChannelHandler());
            pipeline.addLast("E", new TestChannelHandler());

            strategyManager.arrange(pipeline, list);
            Assert.assertTrue(strategyManager.verify(pipeline, list));
        }

        {
            TestChannelPipeline pipeline = new TestChannelPipeline();
            pipeline.addLast("A", new TestChannelHandler());
            pipeline.addLast("E", new TestChannelHandler());
            pipeline.addLast("C", new TestChannelHandler());

            strategyManager.arrange(pipeline, list);
            Assert.assertTrue(strategyManager.verify(pipeline, list));
        }

        {
            TestChannelPipeline pipeline = new TestChannelPipeline();
            pipeline.addLast("D", new TestChannelHandler());
            pipeline.addLast("E", new TestChannelHandler());

            strategyManager.arrange(pipeline, list);
            Assert.assertTrue(strategyManager.verify(pipeline, list));
        }

        {
            TestChannelPipeline pipeline = new TestChannelPipeline();
            pipeline.addLast("A1", new TestChannelHandler());
            pipeline.addLast("S", new TestChannelHandler());
            pipeline.addLast("F", new TestChannelHandler());
            pipeline.addLast("E", new TestChannelHandler());
            pipeline.addLast("C1", new TestChannelHandler());
            pipeline.addLast("C", new TestChannelHandler());
            pipeline.addLast("B", new TestChannelHandler());

            strategyManager.arrange(pipeline, list);
            Assert.assertTrue(strategyManager.verify(pipeline, list));
        }
    }


    /**
     * for test only
     */
    static class TestChannelHandler implements ChannelHandler {

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {

        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {

        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        }
    }
}


