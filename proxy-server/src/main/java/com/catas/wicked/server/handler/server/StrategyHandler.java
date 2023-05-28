package com.catas.wicked.server.handler.server;

import com.catas.wicked.common.bean.ProxyRequestInfo;
import com.catas.wicked.common.common.ProxyConstant;
import com.catas.wicked.common.common.ServerStatus;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.util.IdUtil;
import com.catas.wicked.common.util.WebUtils;
import com.catas.wicked.server.cert.CertPool;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * None recording: httpCodec - strategyHandler - proxyProcessHandler
 * Http recording: httpCodec - strategyHandler - [aggregator] - [recorderHandler] - proxyProcessHandler
 * ssl  recording: httpCodec - [sslHandle] - strategyHandler - [aggregator] - [recorderHandler] - proxyProcessHandler
 */
@Slf4j
public class StrategyHandler extends ChannelInboundHandlerAdapter {

    private byte[] httpTagBuf;

    private final ApplicationConfig appConfig;

    private ServerStatus status;

    private final CertPool certPool;

    private final AttributeKey<ProxyRequestInfo> requestInfoAttributeKey = AttributeKey.valueOf("requestInfo");

    private final String id;

    public StrategyHandler(ApplicationConfig applicationConfig, CertPool certPool) {
        this.appConfig = applicationConfig;
        this.certPool = certPool;
        this.status = ServerStatus.INIT;

        id = IdUtil.getId();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("Handler id = {}", id);
        if (msg instanceof HttpRequest) {
            handleHttpRequest(ctx, msg);
        } else if (msg instanceof HttpContent) {
            ctx.fireChannelRead(msg);
        } else if (!(msg instanceof HttpObject)) {
            handleSsl(ctx, msg);
        }
    }

    private ProxyRequestInfo refreshRequestInfo(ChannelHandlerContext ctx, HttpRequest request) {
        Attribute<ProxyRequestInfo> attr = ctx.channel().attr(requestInfoAttributeKey);
        ProxyRequestInfo requestInfo = attr.get();
        if (requestInfo == null && request != null) {
            requestInfo = WebUtils.getRequestProto(request);
            attr.set(requestInfo);
        }
        requestInfo.setNewRequest(true);
        requestInfo.setRequestId(IdUtil.getId());
        requestInfo.setRecording(appConfig.isRecording());

        return requestInfo;
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, Object msg) {
        HttpRequest request = (HttpRequest) msg;
        DecoderResult result = request.decoderResult();
        Throwable cause = result.cause();

        if (cause instanceof DecoderException) {
            HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
            ctx.writeAndFlush(response);
            ReferenceCountUtil.release(msg);
            return;
        }


        ProxyRequestInfo requestInfo = refreshRequestInfo(ctx, request);
        if (!requestInfo.isRecording()) {
            ctx.channel().pipeline().remove("httpAggregator");
            requestInfo.setClientType(ProxyRequestInfo.ClientType.TUNNEL);
        } else {
            try {
                ctx.channel().pipeline().addAfter(
                        "strategyHandler",
                        "httpAggregator",
                        new HttpObjectAggregator(appConfig.getMaxContentSize()));
            } catch (IllegalArgumentException ignore) {}
            requestInfo.setClientType(ProxyRequestInfo.ClientType.NORMAL);
        }
        // attr.set(requestInfo);

        if (status.equals(ServerStatus.INIT)) {
            status = ServerStatus.RUNNING;
            if (HttpMethod.CONNECT.name().equalsIgnoreCase(request.method().name())) {
                // https connect
                HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, ProxyConstant.SUCCESS);
                ctx.writeAndFlush(response);
                ctx.channel().pipeline().remove("httpCodec");
                ReferenceCountUtil.release(msg);
                return;
            }
        }
        ctx.fireChannelRead(msg);
    }

    private void handleSsl(ChannelHandlerContext ctx, Object msg) throws Exception {
        // TODO: 判断是否为新请求
        ByteBuf byteBuf = (ByteBuf) msg;
        if (byteBuf.getByte(0) == 22) {
            // new request
            if (appConfig.isHandleSsl()) {
                int port = ((InetSocketAddress) ctx.channel().localAddress()).getPort();
                String host = ((InetSocketAddress) ctx.channel().remoteAddress()).getHostName();

                SslContext sslCtx = SslContextBuilder.forServer(
                        appConfig.getServerPriKey(), certPool.getCert(port, host)).build();

                ctx.pipeline().addFirst("httpCodec", new HttpServerCodec());
                ctx.pipeline().addFirst("sslHandle", sslCtx.newHandler(ctx.alloc()));
                ctx.pipeline().fireChannelRead(msg);
                return;
            }
            ProxyRequestInfo requestInfo = refreshRequestInfo(ctx, null);
            requestInfo.setClientType(ProxyRequestInfo.ClientType.TUNNEL);
            ctx.pipeline().remove("httpAggregator");
        }

        if (byteBuf.readableBytes() < 8) {
            httpTagBuf = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(httpTagBuf);
            ReferenceCountUtil.release(msg);
            return;
        }
        if (httpTagBuf != null) {
            byte[] tmp = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(tmp);
            byteBuf.writeBytes(httpTagBuf);
            byteBuf.writeBytes(tmp);
            httpTagBuf = null;
        }

        // 如果connect后面跑的是HTTP报文，也可以抓包处理
        if (WebUtils.isHttp(byteBuf)) {
            ctx.pipeline().addFirst("httpCodec", new HttpServerCodec());
            ctx.pipeline().fireChannelRead(msg);
        }
        ctx.fireChannelRead(msg);
    }
}
