package com.catas.wicked.server.handler.server;

import com.catas.wicked.common.bean.ProxyRequestInfo;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.pipeline.MessageQueue;
import com.catas.wicked.server.handler.ClientInitializerFactory;
import com.catas.wicked.server.handler.client.ClientStrategyHandler;
import com.catas.wicked.server.handler.client.ProxyClientHandler;
import com.catas.wicked.server.handler.client.ProxyClientInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;

import static com.catas.wicked.common.common.NettyConstant.CLIENT_PROCESSOR;
import static com.catas.wicked.common.common.NettyConstant.CLIENT_STRATEGY;

@Slf4j
public class ProxyProcessHandler extends ChannelInboundHandlerAdapter {

    private boolean isConnected;

    private ApplicationConfig applicationConfig;

    private ChannelFuture channelFuture;

    private List<Object> requestList;

    private ClientInitializerFactory initializerFactory;

    private ProxyRequestInfo requestInfo;

    private final MessageQueue messageQueue;

    private final AttributeKey<ProxyRequestInfo> requestInfoAttributeKey = AttributeKey.valueOf("requestInfo");

    public ProxyProcessHandler(ApplicationConfig applicationConfig,
                               ClientInitializerFactory initializerFactory,
                               MessageQueue messageQueue) {
        this.applicationConfig = applicationConfig;
        this.initializerFactory = initializerFactory;
        this.messageQueue = messageQueue;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ProxyRequestInfo curRequestInfo = ctx.channel().attr(requestInfoAttributeKey).get();
        if (curRequestInfo == null) {
            log.error("Request info is null");
            ReferenceCountUtil.release(msg);
            return;
        }

        handleProxyData(ctx.channel(), msg, curRequestInfo);
    }

    private void handleProxyData(Channel channel, Object msg, ProxyRequestInfo requestInfo) {
        if (channelFuture == null) {
            if (requestInfo.getClientType() == ProxyRequestInfo.ClientType.NORMAL
                    && (!(msg instanceof HttpObject))) {
                return;
            }

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(channel.eventLoop())
                    .channel(NioSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(CLIENT_STRATEGY, new ClientStrategyHandler(applicationConfig, messageQueue, requestInfo));
                            ch.pipeline().addLast(CLIENT_PROCESSOR, new ProxyClientHandler(channel));
                        }
                    });

            // bootstrap.resolver(NoopAddressResolverGroup.INSTANCE);
            requestList = new LinkedList<>();
            channelFuture = bootstrap.connect(requestInfo.getHost(), requestInfo.getPort());
            channelFuture.addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    future.channel().writeAndFlush(msg);
                    synchronized (requestList) {
                        requestList.forEach(obj -> future.channel().writeAndFlush(obj));
                        requestList.clear();
                        isConnected = true;
                    }
                } else {
                    synchronized (requestList) {
                        requestList.forEach(ReferenceCountUtil::release);
                        requestList.clear();
                    }
                    // TODO
                    // getExceptionHandle().beforeCatch(channel, future.cause());
                    future.channel().close();
                    channel.close();
                }
            });
        } else {
            synchronized (requestList) {
                if (isConnected) {
                    channelFuture.channel().writeAndFlush(msg);
                } else {
                    requestList.add(msg);
                }
            }
        }
    }
}
