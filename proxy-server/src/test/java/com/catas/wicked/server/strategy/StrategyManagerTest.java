package com.catas.wicked.server.strategy;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Slf4j
public class StrategyManagerTest {

    @Test
    public void testMockPipeline() {
        TestChannelPipeline pipeline = new TestChannelPipeline();
        Assert.assertEquals(1, pipeline.names().size());

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

        while (pipeline.names().size() > 1) {
            pipeline.removeLast();
        }
        Assert.assertEquals(1, pipeline.names().size());

        pipeline.addLast("4th", new TestChannelHandler());
        pipeline.addLast("5th", new TestChannelHandler());
        Assert.assertEquals("4th", pipeline.names().get(0));
        Assert.assertEquals("5th", pipeline.names().get(1));
    }

    @Test
    public void testStrategyManager() {
        DefaultStrategyManager strategyManager = new DefaultStrategyManager();
        // List<StrategyModel> list = new ArrayList<>();
        StrategyList list = new StrategyList();
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
            pipeline.addFirst("C", new TestChannelHandler());
            pipeline.addFirst("B", new TestChannelHandler());
            pipeline.addFirst("A", new TestChannelHandler());

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

    @Test
    public void testStrategyEligible() {
        DefaultStrategyManager strategyManager = new DefaultStrategyManager();
        // List<StrategyModel> list = new ArrayList<>();
        StrategyList list = new StrategyList();
        list.add(new StrategyModel("HEAD_ELIGIBLE", true, TestChannelHandler::new,
                name -> name.contains("ELIGIBLE")));
        list.add(new StrategyModel("A", true, TestChannelHandler::new));
        list.add(new StrategyModel("A1", false, TestChannelHandler::new));
        list.add(new StrategyModel("B", true, TestChannelHandler::new));
        list.add(new StrategyModel("C", true, TestChannelHandler::new));
        list.add(new StrategyModel("C1", false, TestChannelHandler::new));

        {
            TestChannelPipeline pipeline = new TestChannelPipeline();
            pipeline.addFirst("AA_ELIGIBLE", new TestChannelHandler());
            pipeline.addFirst("A", new TestChannelHandler());
            pipeline.addFirst("B1", new TestChannelHandler());
            pipeline.addFirst("C", new TestChannelHandler());

            strategyManager.arrange(pipeline, list);
            System.out.println(pipeline.names());
            Assert.assertTrue(strategyManager.verify(pipeline, list));
        }

        {
            list.add(new StrategyModel("TAIL_ELIGIBLE", true, TestChannelHandler::new,
                    name -> name.contains("TAIL")));
            TestChannelPipeline pipeline = new TestChannelPipeline();
            pipeline.addFirst("AA_ELIGIBLE", new TestChannelHandler());
            pipeline.addFirst("A", new TestChannelHandler());
            pipeline.addFirst("B1", new TestChannelHandler());
            pipeline.addFirst("CC_TAIL", new TestChannelHandler());

            strategyManager.arrange(pipeline, list);
            System.out.println(pipeline.names());
            Assert.assertTrue(strategyManager.verify(pipeline, list));
        }
    }


    @Test
    public void testStrategyVerify() {
        StrategyManager strategyManager = new TailStrategyManager();
        StrategyList list = new StrategyList();
        list.add(new StrategyModel("A", true, false, TestChannelHandler::new,
                null, skipPredicate));
        list.add(new StrategyModel("A1", false, false, TestChannelHandler::new,
                null, skipPredicate));
        list.add(new StrategyModel("B", true, false, TestChannelHandler::new,
                null, skipPredicate));
        list.add(new StrategyModel("C", true, false, TestChannelHandler::new,
                null, skipPredicate));
        list.add(new StrategyModel("C1", false, false, TestChannelHandler::new,
                null, skipPredicate));
        list.add(new TailStrategyManager.TailContextStrategy(skipPredicate));

        {
            TestChannelPipeline pipeline = new TestChannelPipeline();
            pipeline.addLast("SKIP#1", new TestChannelHandler());
            pipeline.addLast("A", new TestChannelHandler());
            pipeline.addLast("B", new TestChannelHandler());
            pipeline.addLast("SKIP#2", new TestChannelHandler());
            pipeline.addLast("C", new TestChannelHandler());

            Assert.assertTrue(strategyManager.verify(pipeline, list));
        }

        {
            TestChannelPipeline pipeline = new TestChannelPipeline();
            pipeline.addLast("A", new TestChannelHandler());
            pipeline.addLast("B", new TestChannelHandler());
            pipeline.addLast("C", new TestChannelHandler());

            Assert.assertTrue(strategyManager.verify(pipeline, list));
        }

        {
            TestChannelPipeline pipeline = new TestChannelPipeline();
            pipeline.addLast("SKIP#1", new TestChannelHandler());
            pipeline.addLast("SKIP#2", new TestChannelHandler());
            pipeline.addLast("A", new TestChannelHandler());
            pipeline.addLast("B", new TestChannelHandler());
            pipeline.addLast("C", new TestChannelHandler());
            pipeline.addLast("SKIP#3", new TestChannelHandler());

            Assert.assertTrue(strategyManager.verify(pipeline, list));
        }
        {
            TestChannelPipeline pipeline = new TestChannelPipeline();
            pipeline.addLast("A", new TestChannelHandler());
            pipeline.addLast("B", new TestChannelHandler());
            pipeline.addLast("C", new TestChannelHandler());
            pipeline.addLast("SKIP#3", new TestChannelHandler());
            pipeline.addLast("SKIP#4", new TestChannelHandler());

            Assert.assertTrue(strategyManager.verify(pipeline, list));
        }
        {
            TestChannelPipeline pipeline = new TestChannelPipeline();
            pipeline.addLast("A", new TestChannelHandler());
            pipeline.addLast("C", new TestChannelHandler());
            pipeline.addLast("SKIP#3", new TestChannelHandler());
            pipeline.addLast("SKIP#4", new TestChannelHandler());

            Assert.assertFalse(strategyManager.verify(pipeline, list));
        }
        {
            TestChannelPipeline pipeline = new TestChannelPipeline();
            pipeline.addLast("SKIP#3", new TestChannelHandler());
            pipeline.addLast("A", new TestChannelHandler());
            pipeline.addLast("SKIP#3", new TestChannelHandler());
            pipeline.addLast("C", new TestChannelHandler());

            Assert.assertFalse(strategyManager.verify(pipeline, list));
        }
        {
            TestChannelPipeline pipeline = new TestChannelPipeline();
            pipeline.addLast("A", new TestChannelHandler());
            pipeline.addLast("C", new TestChannelHandler());

            Assert.assertFalse(strategyManager.verify(pipeline, list));
        }
        {
            TestChannelPipeline pipeline = new TestChannelPipeline();
            pipeline.addLast("SKIP#3", new TestChannelHandler());
            pipeline.addLast("SKIP#4", new TestChannelHandler());

            Assert.assertFalse(strategyManager.verify(pipeline, list));
        }
    }

    @Test
    public void testTailStrategyManager() {
        StrategyManager strategyManager = new TailStrategyManager();
        StrategyList list = new StrategyList();
        list.add(new StrategyModel("A", true, false, TestChannelHandler::new,
                null, skipPredicate));
        list.add(new StrategyModel("A1", false, false, TestChannelHandler::new,
                null, skipPredicate));
        list.add(new StrategyModel("B", true, false, TestChannelHandler::new,
                null, skipPredicate));
        list.add(new StrategyModel("C", true, false, TestChannelHandler::new,
                null, skipPredicate));
        list.add(new StrategyModel("D", true, false, TestChannelHandler::new,
                null, skipPredicate));
        list.add(new StrategyModel("E", true, false, TestChannelHandler::new,
                null, skipPredicate));
        list.add(new TailStrategyManager.TailContextStrategy(skipPredicate));

        {
            TestChannelPipeline pipeline = new TestChannelPipeline();
            pipeline.addLast("SKIP#1", new TestChannelHandler());
            pipeline.addLast("A", new TestChannelHandler());
            pipeline.addLast("B", new TestChannelHandler());
            pipeline.addLast("SKIP#2", new TestChannelHandler());
            pipeline.addLast("C", new TestChannelHandler());

            Assert.assertFalse(strategyManager.verify(pipeline, list));
            strategyManager.arrange(pipeline, list);
            System.out.println(pipeline.names());
            Assert.assertTrue(strategyManager.verify(pipeline, list));
        }

        {
            TestChannelPipeline pipeline = new TestChannelPipeline();
            pipeline.addLast("A", new TestChannelHandler());
            pipeline.addLast("B", new TestChannelHandler());
            pipeline.addLast("C", new TestChannelHandler());

            Assert.assertFalse(strategyManager.verify(pipeline, list));
            strategyManager.arrange(pipeline, list);
            Assert.assertTrue(strategyManager.verify(pipeline, list));
        }

        {
            TestChannelPipeline pipeline = new TestChannelPipeline();
            pipeline.addLast("A", new TestChannelHandler());
            pipeline.addLast("E", new TestChannelHandler());

            Assert.assertFalse(strategyManager.verify(pipeline, list));
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

            Assert.assertTrue(strategyManager.verify(pipeline, list));
            strategyManager.arrange(pipeline, list);
            Assert.assertTrue(strategyManager.verify(pipeline, list));
        }

        {
            TestChannelPipeline pipeline = new TestChannelPipeline();
            pipeline.addLast("SKIP#1", new TestChannelHandler());
            pipeline.addLast("SKIP#2", new TestChannelHandler());
            pipeline.addLast("A", new TestChannelHandler());
            pipeline.addLast("C", new TestChannelHandler());
            pipeline.addLast("E", new TestChannelHandler());

            Assert.assertFalse(strategyManager.verify(pipeline, list));
            strategyManager.arrange(pipeline, list);
            Assert.assertTrue(strategyManager.verify(pipeline, list));
        }

        {
            TestChannelPipeline pipeline = new TestChannelPipeline();
            pipeline.addLast("SKIP#1", new TestChannelHandler());
            pipeline.addLast("SKIP#2", new TestChannelHandler());
            pipeline.addLast("A", new TestChannelHandler());
            pipeline.addLast("E", new TestChannelHandler());

            Assert.assertFalse(strategyManager.verify(pipeline, list));
            strategyManager.arrange(pipeline, list);
            Assert.assertTrue(strategyManager.verify(pipeline, list));
        }

        {
            TestChannelPipeline pipeline = new TestChannelPipeline();
            pipeline.addLast("SKIP#1", new TestChannelHandler());
            pipeline.addLast("SKIP#2", new TestChannelHandler());
            pipeline.addLast("A", new TestChannelHandler());
            pipeline.addLast("B", new TestChannelHandler());
            pipeline.addLast("C", new TestChannelHandler());
            pipeline.addLast("D", new TestChannelHandler());
            pipeline.addLast("E", new TestChannelHandler());

            Assert.assertTrue(strategyManager.verify(pipeline, list));
            strategyManager.arrange(pipeline, list);
            Assert.assertTrue(strategyManager.verify(pipeline, list));
        }

        {
            TestChannelPipeline pipeline = new TestChannelPipeline();
            pipeline.addLast("SKIP#1", new TestChannelHandler());
            pipeline.addLast("SKIP#2", new TestChannelHandler());
            pipeline.addLast("E", new TestChannelHandler());
            pipeline.addLast("C", new TestChannelHandler());
            pipeline.addLast("A", new TestChannelHandler());

            Assert.assertFalse(strategyManager.verify(pipeline, list));
            strategyManager.arrange(pipeline, list);
            Assert.assertTrue(strategyManager.verify(pipeline, list));
        }

        {
            TestChannelPipeline pipeline = new TestChannelPipeline();
            pipeline.addLast("SKIP#1", new TestChannelHandler());
            pipeline.addLast("SKIP#2", new TestChannelHandler());
            pipeline.addLast("G", new TestChannelHandler());
            pipeline.addLast("A", new TestChannelHandler());
            pipeline.addLast("H", new TestChannelHandler());
            pipeline.addLast("E", new TestChannelHandler());

            Assert.assertFalse(strategyManager.verify(pipeline, list));
            strategyManager.arrange(pipeline, list);
            Assert.assertTrue(strategyManager.verify(pipeline, list));
        }

        {
            TestChannelPipeline pipeline = new TestChannelPipeline();
            pipeline.addLast("SKIP#1", new TestChannelHandler());
            pipeline.addLast("SKIP#2", new TestChannelHandler());
            pipeline.addLast("A", new TestChannelHandler());
            pipeline.addLast("G", new TestChannelHandler());
            pipeline.addLast("K", new TestChannelHandler());
            pipeline.addLast("C", new TestChannelHandler());
            pipeline.addLast("D", new TestChannelHandler());
            pipeline.addLast("E", new TestChannelHandler());

            strategyManager.arrange(pipeline, list);
            Assert.assertTrue(strategyManager.verify(pipeline, list));
        }

        {
            TestChannelPipeline pipeline = new TestChannelPipeline();
            pipeline.addLast("SKIP#1", new TestChannelHandler());
            pipeline.addLast("SKIP#2", new TestChannelHandler());
            pipeline.addLast("G", new TestChannelHandler());
            pipeline.addLast("K", new TestChannelHandler());
            pipeline.addLast("A", new TestChannelHandler());
            pipeline.addLast("SKIP#3", new TestChannelHandler());

            strategyManager.arrange(pipeline, list);
            Assert.assertTrue(strategyManager.verify(pipeline, list));
        }
    }

    private Predicate<String> skipPredicate = new Predicate<String>() {
        final List<String> unSkipNames = List.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "G", "K");

        @Override
        public boolean test(String s) {
            return !unSkipNames.contains(s);
        }
    };

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


