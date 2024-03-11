package com.catas.wicked.server.handler.server;

import com.catas.wicked.common.bean.ProxyRequestInfo;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.constant.ProxyConstant;
import com.catas.wicked.common.pipeline.MessageQueue;
import com.catas.wicked.common.util.ProxyHandlerFactory;
import com.catas.wicked.common.util.WebUtils;
import com.catas.wicked.server.handler.client.ClientChannelInitializer;
import com.catas.wicked.server.strategy.StrategyManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.resolver.NoopAddressResolverGroup;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.ConnectException;
import java.util.LinkedList;
import java.util.List;


/**
 * send data to target server
 */
@Slf4j
public class ServerProcessHandler extends ChannelInboundHandlerAdapter {

    private boolean isConnected;

    private ApplicationConfig appConfig;

    private ChannelFuture channelFuture;

    // TODO: ConcurrentLinkedQueue
    private final List<Object> requestList;

    private final MessageQueue messageQueue;

    private StrategyManager strategyManager;

    private final AttributeKey<ProxyRequestInfo> requestInfoAttributeKey =
            AttributeKey.valueOf(ProxyConstant.REQUEST_INFO);

    private String curRequestId;
    public ServerProcessHandler(ApplicationConfig applicationConfig,
                                MessageQueue messageQueue,
                                StrategyManager strategyManager) {
        this.appConfig = applicationConfig;
        this.messageQueue = messageQueue;
        this.strategyManager = strategyManager;
        requestList = new LinkedList<>();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ProxyRequestInfo curRequestInfo = ctx.channel().attr(requestInfoAttributeKey).get();
        if (curRequestInfo == null) {
            log.error("Request info is null");
            ReferenceCountUtil.release(msg);
            return;
        }

        if (msg instanceof HttpRequest httpRequest) {
            curRequestInfo.updateRequestSize(WebUtils.estimateSize(httpRequest));
        } else if (msg instanceof HttpContent content) {
            curRequestInfo.updateRequestSize(content.content().readableBytes());
        } else {
            try {
                ByteBuf cont = (ByteBuf) msg;
                curRequestInfo.updateRequestSize(cont.readableBytes());
            } catch (Exception e) {
                log.warn("Unable to catch request size.", e);
            }
        }
        curRequestInfo.updateRequestTime();
        // System.out.println("Handlers: " + ctx.channel().pipeline().names());
        handleProxyData(ctx, msg, curRequestInfo);
    }

    private void handleProxyData(ChannelHandlerContext ctx, Object msg, ProxyRequestInfo requestInfo)  throws Exception {
        if (channelFuture == null || !StringUtils.equals(curRequestId, requestInfo.getRequestId())) {
            if (requestInfo.getClientType() == ProxyRequestInfo.ClientType.NORMAL
                    && (!(msg instanceof HttpRequest))) {
                return;
            }

            // TODO: thread safe
            curRequestId = requestInfo.getRequestId();
            isConnected = false;
            requestInfo.setClientConnected(false);
            Bootstrap bootstrap = new Bootstrap();

            // set external proxyHandler if needed
            ProxyHandler proxyHandler = null;
            if (requestInfo.isUsingExternalProxy()) {
                proxyHandler = ProxyHandlerFactory.getExternalProxyHandler(
                        appConfig.getSettings().getExternalProxy(), WebUtils.getHostname(requestInfo));
                if (proxyHandler != null) {
                    // TODO: bugfix HTTP proxy error - UnresolvedAddressException
                    bootstrap.resolver(NoopAddressResolverGroup.INSTANCE);
                }
            }

            bootstrap.group(appConfig.getProxyLoopGroup())
                    .channel(NioSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .handler(new ClientChannelInitializer(appConfig, messageQueue, requestInfo,
                            strategyManager, proxyHandler, ctx.channel()));
                    // .handler(new ChannelInitializer<NioSocketChannel>() {
                    //     @Override
                    //     protected void initChannel(NioSocketChannel ch) throws Exception {
                    //         if (requestInfo.isUsingExternalProxy()) {
                    //             // add external proxy handler
                    //             ProxyHandler proxyHandler = ProxyHandlerFactory.getExternalProxyHandler(
                    //                     appConfig.getSettings().getExternalProxy(), WebUtils.getHostname(requestInfo));
                    //             if (proxyHandler != null) {
                    //                 // TODO: bugfix HTTP proxy error - UnresolvedAddressException
                    //                 ch.pipeline().addFirst(EXTERNAL_PROXY, proxyHandler);
                    //                 bootstrap.resolver(NoopAddressResolverGroup.INSTANCE);
                    //             }
                    //         } else {
                    //             bootstrap.resolver(DefaultAddressResolverGroup.INSTANCE);
                    //         }
                    //         // ch.pipeline().addLast(CLIENT_STRATEGY,
                    //         //         new ClientStrategyHandler(appConfig, messageQueue, requestInfo));
                    //         ch.pipeline().addLast(CLIENT_PROCESSOR, new ProxyClientHandler(ctx.channel()));
                    //         ch.pipeline().addLast(POST_RECORDER, new ClientPostRecorder(appConfig, messageQueue));
                    //         ch.pipeline().addLast(CLIENT_STRATEGY,
                    //                 new ClientStrategyHandler(appConfig, messageQueue, requestInfo));
                    //     }
                    // });

            requestList.clear();
            channelFuture = bootstrap.connect(requestInfo.getHost(), requestInfo.getPort());
            channelFuture.addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    ReferenceCountUtil.retain(msg);
                    future.channel().writeAndFlush(msg);
                    ctx.fireChannelRead(msg);

                    if (!requestList.isEmpty()) {
                        synchronized (requestList) {
                            requestList.forEach(obj -> {
                                ReferenceCountUtil.retain(obj);
                                future.channel().writeAndFlush(obj);
                                ctx.fireChannelRead(obj);
                            });
                            requestList.clear();
                        }
                    }
                    isConnected = true;
                } else {
                    // TODO 添加错误记录
                    Throwable cause = future.cause();
                    log.error("Error in creating proxy client channel", cause);
                    if (cause instanceof ConnectException connectException) {
                        // TODO: add error msg, send requestList to postRecorder
                        String host = requestInfo.getHost();
                    } else {
                        System.out.println(cause);
                    }
                    synchronized (requestList) {
                        requestList.forEach(ReferenceCountUtil::release);
                        requestList.clear();
                    }
                    HttpResponse response = new DefaultFullHttpResponse(
                            HttpVersion.HTTP_1_1, HttpResponseStatus.GATEWAY_TIMEOUT);
                    ctx.writeAndFlush(response);
                    future.channel().close();
                    ctx.channel().close();
                }
            });
        } else {
            synchronized (requestList) {
                if (isConnected) {
                    ReferenceCountUtil.retain(msg);
                    channelFuture.channel().writeAndFlush(msg);
                    ctx.fireChannelRead(msg);
                } else {
                    requestList.add(msg);
                }
            }
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        log.info("Server channel closing.");
        if (channelFuture != null) {
            channelFuture.channel().close();
        }
        ctx.channel().close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // TODO channel close - log
        log.error("Server channel unexpected error, closing...", cause);
        cause.printStackTrace();
        if (channelFuture != null) {
            channelFuture.channel().close();
        }
        ctx.channel().close();
    }
}
