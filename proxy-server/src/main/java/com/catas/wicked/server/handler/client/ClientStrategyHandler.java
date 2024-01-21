package com.catas.wicked.server.handler.client;

import com.catas.wicked.common.bean.ProxyRequestInfo;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.constant.ProxyConstant;
import com.catas.wicked.common.pipeline.MessageQueue;
import com.catas.wicked.server.handler.RearHttpAggregator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.NoSuchElementException;

import static com.catas.wicked.common.constant.NettyConstant.*;

/**
 * decide which handlers to use when sending new request
 * remote <-<-<- local
 * [Normal]: [externalProxyHandler] - [sslHandler] - [httpCodec] - strategyHandler - proxyClientHandler -
 *           [aggregator] - postRecorder
 * [Tunnel]: [externalProxyHandler] - strategyHandler - proxyClientHandler - postRecorder
 *
 * [Normal]: [externalProxyHandler] - [sslHandler] - [httpCodec] - proxyClientHandler -
 *           [aggregator] - postRecorder - strategyHandler
 * [Tunnel]: [externalProxyHandler] - proxyClientHandler - postRecorder - strategyHandler
 */
@Slf4j
public class ClientStrategyHandler extends ChannelDuplexHandler {

    private ApplicationConfig appConfig;

    private ProxyRequestInfo requestInfo;

    private MessageQueue messageQueue;

    private String currentRequestId;

    private final AttributeKey<ProxyRequestInfo> requestInfoKey = AttributeKey.valueOf(ProxyConstant.REQUEST_INFO);

    public ClientStrategyHandler(ApplicationConfig appConfig,
                                 MessageQueue messageQueue,
                                 ProxyRequestInfo requestInfo) {
        this.appConfig = appConfig;
        this.requestInfo = requestInfo;
        this.messageQueue = messageQueue;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (ctx.channel().hasAttr(requestInfoKey) && ctx.channel().attr(requestInfoKey).get() != null) {
            // log.info("** Client inactive: {} **", ctx.channel().attr(requestInfoKey).get().getRequestId());
            ctx.channel().attr(requestInfoKey).get().setClientConnected(true);
        }
        super.channelInactive(ctx);
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        super.disconnect(ctx, promise);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        super.close(ctx, promise);
    }

    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        super.read(ctx);
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        super.flush(ctx);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        refreshStrategy(ctx);
        super.write(ctx, msg, promise);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (ctx.channel().attr(requestInfoKey).get() != null) {
            ctx.channel().attr(requestInfoKey).get().updateResponseTime();
        }
        super.channelRead(ctx, msg);
    }

    public void refreshStrategy(ChannelHandlerContext ctx) throws Exception {
        if (!StringUtils.equals(currentRequestId, requestInfo.getRequestId())) {
            // new response
            if (!ctx.channel().hasAttr(requestInfoKey)) {
                ctx.channel().attr(requestInfoKey).set(requestInfo);
            }
            currentRequestId = requestInfo.getRequestId();
            Channel ch = ctx.channel();

            // update record strategy
            if (requestInfo.getClientType() == ProxyRequestInfo.ClientType.NORMAL) {
                // update sslHandler
                if (requestInfo.isSsl()) {
                    try {
                        ch.pipeline().addBefore(CLIENT_PROCESSOR, SSL_HANDLER,
                                appConfig.getClientSslCtx().newHandler(ch.alloc(), appConfig.getHost(), appConfig.getPort()));
                    } catch (IllegalArgumentException ignored) {
                    } catch (Exception e) {
                        log.error("Error establish Ssl context");
                    }
                } else {
                    try {
                        ch.pipeline().remove(SSL_HANDLER);
                    } catch (NoSuchElementException ignored) {}
                }

                // update httpCodec & responseRecorder
                try {
                    ch.pipeline().addBefore(CLIENT_PROCESSOR, HTTP_CODEC, new HttpClientCodec());
                } catch (IllegalArgumentException ignored) {}

                // update httpAggregator
                if (requestInfo.isRecording()) {
                    try {
                        ch.pipeline().addBefore(POST_RECORDER, AGGREGATOR,
                                new RearHttpAggregator(appConfig.getMaxContentSize()));
                    } catch (IllegalArgumentException ignored) {}
                }
            } else {
                try {
                    ch.pipeline().remove(HTTP_CODEC);
                    ch.pipeline().remove(AGGREGATOR);
                    ch.pipeline().remove(SSL_HANDLER);
                } catch (NoSuchElementException ignored) {}
            }
        }
        log.info(">> Client send data: {} >>", requestInfo.getRequestId());
    }

}
