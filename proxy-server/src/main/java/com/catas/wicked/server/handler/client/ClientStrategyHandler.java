package com.catas.wicked.server.handler.client;

import com.catas.wicked.common.bean.ProxyRequestInfo;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.pipeline.MessageQueue;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.NoSuchElementException;

import static com.catas.wicked.common.common.NettyConstant.*;

/**
 * decide which handlers to use when sending new request
 * remote <-<-<- this
 * [Normal]: [externalProxyHandler] - [sslHandler] - httpCodec - [httpAggregator] - responseRecorder - (strategyHandler) - proxyClientHandler
 * [Tunnel]: [externalProxyHandler] - (strategyHandler) - proxyClientHandler
 */
@Slf4j
public class ClientStrategyHandler extends ChannelDuplexHandler {

    private ApplicationConfig appConfig;

    private ProxyRequestInfo requestInfo;

    private MessageQueue messageQueue;

    private String currentRequestId;

    public ClientStrategyHandler(ApplicationConfig appConfig,
                                 MessageQueue messageQueue,
                                 ProxyRequestInfo requestInfo) {
        this.appConfig = appConfig;
        this.requestInfo = requestInfo;
        this.messageQueue = messageQueue;
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


    public void refreshStrategy(ChannelHandlerContext ctx) throws Exception {
        if (!StringUtils.equals(currentRequestId, requestInfo.getRequestId())) {
            // new response
            currentRequestId = requestInfo.getRequestId();
            Channel ch = ctx.channel();
            List<String> handlerNames = ch.pipeline().names();

            // update external proxy strategy
            if (requestInfo.isUseExternalProxy()) {
                // TODO: add external proxy handler
            }

            // update record strategy
            if (requestInfo.getClientType() == ProxyRequestInfo.ClientType.NORMAL) {
                // update sslHandler
                if (requestInfo.isSsl()) {
                    if (!handlerNames.contains(SSL_HANDLER)) {
                        ch.pipeline().addBefore(CLIENT_STRATEGY, SSL_HANDLER,
                                appConfig.getClientSslCtx().newHandler(ch.alloc(), appConfig.getHost(), appConfig.getPort()));
                    }
                } else if (handlerNames.contains(SSL_HANDLER)){
                    ch.pipeline().remove(SSL_HANDLER);
                }

                // update httpCodec & responseRecorder
                try {
                    ch.pipeline().addBefore(CLIENT_STRATEGY, HTTP_CODEC, new HttpClientCodec());
                    ch.pipeline().addBefore(CLIENT_STRATEGY, RESP_RECORDER,
                            new ResponseRecordHandler(appConfig, messageQueue, requestInfo));
                } catch (IllegalArgumentException ignored) {}

                // update httpAggregator
                if (requestInfo.isRecording() && !handlerNames.contains(AGGREGATOR)) {
                    ch.pipeline().addBefore(RESP_RECORDER, AGGREGATOR,
                            new HttpObjectAggregator(appConfig.getMaxContentSize()));
                }
            } else {
                try {
                    ch.pipeline().remove(HTTP_CODEC);
                    ch.pipeline().remove(AGGREGATOR);
                    ch.pipeline().remove(RESP_RECORDER);
                } catch (NoSuchElementException ignored) {}
            }

            requestInfo.setResponseStartTime(System.currentTimeMillis());
        }
    }

}
