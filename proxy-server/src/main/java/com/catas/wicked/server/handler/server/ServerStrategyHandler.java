package com.catas.wicked.server.handler.server;

import com.catas.wicked.common.bean.ProxyRequestInfo;
import com.catas.wicked.common.constant.ProxyConstant;
import com.catas.wicked.common.constant.ServerStatus;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.util.IdUtil;
import com.catas.wicked.common.util.WebUtils;
import com.catas.wicked.server.cert.CertPool;
import com.catas.wicked.server.handler.RearHttpAggregator;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
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
import java.net.SocketAddress;
import java.util.NoSuchElementException;

import static com.catas.wicked.common.constant.NettyConstant.*;

/**
 * Decide which handlers to use for current channel and refresh request-info
 * ---
 * Http: 全部解码
 * Https: 仅在 isRecord && handleSSl && certOK 时解码，其他情况均发送原始数据块
 * ---
 * http record [NORMAL]:    httpCodec - strategyHandler - proxyProcessHandler - [aggregator] - postRecorder
 * http un-record [NORMAL]: httpCodec - strategyHandler - proxyProcessHandler - postRecorder
 * ssl record [NORMAL]:     [sslHandler] - httpCodec - strategyHandler - proxyProcessHandler - [aggregator] - postRecorder
 * ssl record [TUNNEL]:     strategyHandler - proxyProcessHandler - postRecorder
 * ssl un-record [TUNNEL]:  strategyHandler - proxyProcessHandler - postRecorder
 */
@Slf4j
public class ServerStrategyHandler extends ChannelDuplexHandler {

    private byte[] httpTagBuf;

    private final ApplicationConfig appConfig;

    private ServerStatus status;

    private final CertPool certPool;

    private final AttributeKey<ProxyRequestInfo> requestInfoAttributeKey = AttributeKey.valueOf("requestInfo");

    public ServerStrategyHandler(ApplicationConfig applicationConfig, CertPool certPool) {
        this.appConfig = applicationConfig;
        this.certPool = certPool;
        this.status = ServerStatus.INIT;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        // Bug-fix: HttpAggregator removes Transfer-Encoding header
        // if (msg instanceof HttpMessage httpMessage) {
        //     HttpHeaders headers = httpMessage.headers();
        //     log.info("Resp headers: {}", headers.entries().size());
        // }
        super.write(ctx, msg, promise);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            handleHttpRequest(ctx, msg);
        } else if (msg instanceof HttpContent) {
            // 处理 head 后的 LastContent
            if (status == ServerStatus.AFTER_CONNECT) {
                status = ServerStatus.INIT;
                ReferenceCountUtil.release(msg);
                return;
            }
            ctx.fireChannelRead(msg);
        } else if (!(msg instanceof HttpObject)) {
            handleRaw(ctx, msg);
        }
    }

    /**
     * Refresh http request-info when new request arrives
     */
    private ProxyRequestInfo refreshRequestInfo(ChannelHandlerContext ctx, HttpRequest request) {
        Attribute<ProxyRequestInfo> attr = ctx.channel().attr(requestInfoAttributeKey);
        ProxyRequestInfo requestInfo = attr.get();
        if (requestInfo == null && request != null) {
            requestInfo = WebUtils.getRequestProto(request);
            attr.set(requestInfo);
        }
        assert requestInfo != null;
        requestInfo.setUsingExternalProxy(appConfig.getExternalProxyConfig().isUsingExternalProxy());
        requestInfo.setRequestId(IdUtil.getId());
        requestInfo.setRecording(appConfig.isRecording());
        requestInfo.resetBasicInfo();

        SocketAddress remoteAddress = ctx.channel().remoteAddress();
        SocketAddress localAddress = ctx.channel().localAddress();
        if (remoteAddress instanceof InetSocketAddress inetRemoteAddress) {
            requestInfo.setRemoteAddress(inetRemoteAddress.getAddress().getHostAddress());
        }
        if (localAddress instanceof InetSocketAddress inetLocalAddress) {
            requestInfo.setLocalAddress(inetLocalAddress.getAddress().getHostAddress());
            requestInfo.setLocalPort(inetLocalAddress.getPort());
        }
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
            try {
                ctx.channel().pipeline().remove(AGGREGATOR);
            } catch (NoSuchElementException ignored) {}
        } else {
            try {
                ctx.channel().pipeline().addAfter(
                        SERVER_PROCESSOR, AGGREGATOR, new RearHttpAggregator(appConfig.getMaxContentSize()));
            } catch (IllegalArgumentException ignore) {}
        }
        requestInfo.setClientType(ProxyRequestInfo.ClientType.NORMAL);
        // attr.set(requestInfo);

        if (status.equals(ServerStatus.INIT)) {
            if (HttpMethod.CONNECT.name().equalsIgnoreCase(request.method().name())) {
                status = ServerStatus.AFTER_CONNECT;
                // https connect
                HttpResponse response = new DefaultFullHttpResponse(request.protocolVersion(), ProxyConstant.SUCCESS);
                ctx.writeAndFlush(response);
                try {
                    ctx.channel().pipeline().remove(HTTP_CODEC);
                } catch (NoSuchElementException ignore) {}
                ReferenceCountUtil.release(msg);
                return;
            }
        }
        ctx.fireChannelRead(msg);
    }

    private void handleRaw(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 判断是否为新请求
        ByteBuf byteBuf = (ByteBuf) msg;
        if (byteBuf.getByte(0) == 22) {
            // TODO process new request
            ProxyRequestInfo requestInfo = ctx.channel().attr(requestInfoAttributeKey).get();
            assert requestInfo != null;
            requestInfo.setSsl(true);
            if (requestInfo.isRecording() && appConfig.isHandleSsl()) {
                int port = ((InetSocketAddress) ctx.channel().localAddress()).getPort();
                String originHost = requestInfo.getHost();
                SslContext sslCtx = SslContextBuilder.forServer(
                        appConfig.getServerPriKey(), certPool.getCert(port, originHost)).build();

                ctx.pipeline().addFirst(HTTP_CODEC, new HttpServerCodec());
                ctx.pipeline().addFirst(SSL_HANDLER, sslCtx.newHandler(ctx.alloc()));
                ctx.pipeline().fireChannelRead(msg);
                return;
            }
            requestInfo.setClientType(ProxyRequestInfo.ClientType.TUNNEL);
            try {
                ctx.pipeline().remove(AGGREGATOR);
            } catch (NoSuchElementException ignore) {}
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
            ctx.pipeline().addFirst(HTTP_CODEC, new HttpServerCodec());
            ctx.pipeline().fireChannelRead(msg);
            return;
        }
        ctx.fireChannelRead(msg);
    }
}
