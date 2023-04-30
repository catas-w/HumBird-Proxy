package com.catas.wicked.server.handler.server;

import com.catas.wicked.common.bean.ProxyRequestInfo;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.server.handler.ClientInitializerFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;

import java.util.LinkedList;
import java.util.List;

@Slf4j
public class ProxyProcessHandler extends ChannelInboundHandlerAdapter {

    private boolean isConnected;

    private ApplicationConfig applicationConfig;

    private ChannelFuture channelFuture;

    private List<Object> requestList;

    private ClientInitializerFactory initializerFactory;

    private final AttributeKey<ProxyRequestInfo> requestInfoAttributeKey = AttributeKey.valueOf("requestInfo");

    public ProxyProcessHandler(ApplicationConfig applicationConfig, ClientInitializerFactory initializerFactory) {
        this.applicationConfig = applicationConfig;
        this.initializerFactory = initializerFactory;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ProxyRequestInfo requestInfo = ctx.channel().attr(requestInfoAttributeKey).get();
        if (!applicationConfig.isHandleSsl() && requestInfo != null && BooleanUtils.isTrue(requestInfo.isSsl())) {
            handleProxyData(ctx.channel(), msg, false);
        } else  {
            handleProxyData(ctx.channel(), msg, true);
        }
    }

    private void handleProxyData(Channel channel, Object msg, boolean isHttp) {
        // TODO: record request
        if (channelFuture == null) {
            if (isHttp && (!(msg instanceof FullHttpRequest))) {
                return;
            }
            Attribute<ProxyRequestInfo> attr = channel.attr(requestInfoAttributeKey);
            ProxyRequestInfo requestInfo = attr.get();
            ChannelInitializer channelInitializer = initializerFactory.getChannelInitializer(
                    isHttp, channel, null, requestInfo);

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(channel.eventLoop())
                    .channel(NioSocketChannel.class)
                    .handler(channelInitializer);

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
