package com.catas.wicked.server.handler.server;

import com.catas.wicked.common.bean.IdGenerator;
import com.catas.wicked.common.bean.ProxyRequestInfo;
import com.catas.wicked.common.constant.ClientStatus;
import com.catas.wicked.common.constant.ProxyConstant;
import com.catas.wicked.common.constant.ServerStatus;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.util.WebUtils;
import com.catas.wicked.server.cert.CertPool;
import com.catas.wicked.server.strategy.Handler;
import com.catas.wicked.server.strategy.StrategyList;
import com.catas.wicked.server.strategy.StrategyManager;
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
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.NoSuchElementException;


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
// @ChannelHandler.Sharable
public class ServerStrategyHandler extends ChannelDuplexHandler {

    private byte[] httpTagBuf;

    private final ApplicationConfig appConfig;

    private ServerStatus status;

    private final CertPool certPool;

    private IdGenerator idGenerator;

    private StrategyList strategyList;

    private StrategyManager strategyManager;

    private final AttributeKey<ProxyRequestInfo> requestInfoAttributeKey = AttributeKey.valueOf("requestInfo");

    public ServerStrategyHandler(ApplicationConfig applicationConfig,
                                 CertPool certPool,
                                 IdGenerator idGenerator,
                                 StrategyList strategyList,
                                 StrategyManager strategyManager) {
        this.appConfig = applicationConfig;
        this.certPool = certPool;
        this.status = ServerStatus.INIT;
        this.idGenerator = idGenerator;
        this.strategyList = strategyList;
        this.strategyManager = strategyManager;
        // System.out.println("*** new channel ***");
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
        requestInfo.setUsingExternalProxy(appConfig.getSettings().getExternalProxy() != null &&
                appConfig.getSettings().getExternalProxy().isUsingExternalProxy());
        requestInfo.setRequestId(idGenerator.nextId());
        requestInfo.setRecording(appConfig.getSettings().isRecording());
        requestInfo.updateClientStatus(ClientStatus.Status.WAITING);
        requestInfo.resetBasicInfo();

        SocketAddress remoteAddress = ctx.channel().remoteAddress();
        SocketAddress localAddress = ctx.channel().localAddress();
        if (remoteAddress instanceof InetSocketAddress inetRemoteAddress) {
            requestInfo.setLocalAddress(inetRemoteAddress.getAddress().getHostAddress());
            requestInfo.setLocalPort(inetRemoteAddress.getPort());
        }
        // if (localAddress instanceof InetSocketAddress inetLocalAddress) {
        //     requestInfo.setLocalAddress(inetLocalAddress.getAddress().getHostAddress());
        //     requestInfo.setLocalPort(inetLocalAddress.getPort());
        // }
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
        // if (!requestInfo.isRecording()) {
        //     try {
        //         ctx.channel().pipeline().remove(AGGREGATOR);
        //     } catch (NoSuchElementException ignored) {}
        // } else {
        //     try {
        //         ctx.channel().pipeline().addAfter(SERVER_PROCESSOR, AGGREGATOR,
        //                 new RearHttpAggregator(appConfig.getMaxContentSize()));
        //     } catch (IllegalArgumentException ignore) {}
        // }
        strategyList.setRequire(Handler.HTTP_AGGREGATOR.name(), requestInfo.isRecording());
        strategyManager.arrange(ctx.pipeline(), strategyList);

        requestInfo.setClientType(ProxyRequestInfo.ClientType.NORMAL);
        // attr.set(requestInfo);

        if (status.equals(ServerStatus.INIT)) {
            if (HttpMethod.CONNECT.name().equalsIgnoreCase(request.method().name())) {
                status = ServerStatus.AFTER_CONNECT;
                // https connect
                HttpResponse response = new DefaultFullHttpResponse(request.protocolVersion(), ProxyConstant.SUCCESS);
                ctx.writeAndFlush(response);
                // try {
                //     ctx.channel().pipeline().remove(HTTP_CODEC);
                // } catch (NoSuchElementException ignore) {}
                strategyList.setRequire(Handler.HTTP_CODEC.name(), false);
                strategyManager.arrange(ctx.pipeline(), strategyList);

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
            System.out.println("********* Handle raw *********");
            ProxyRequestInfo requestInfo = ctx.channel().attr(requestInfoAttributeKey).get();
            assert requestInfo != null;
            requestInfo.setSsl(true);
            if (requestInfo.isRecording() && appConfig.getSettings().isHandleSsl()) {
                int port = ((InetSocketAddress) ctx.channel().localAddress()).getPort();
                String originHost = requestInfo.getHost();
                SslContext sslCtx = SslContextBuilder.forServer(
                        appConfig.getServerPriKey(), certPool.getCert(port, originHost)).build();

                // ctx.pipeline().addFirst(HTTP_CODEC, new HttpServerCodec());
                // ctx.pipeline().addFirst(SSL_HANDLER, sslCtx.newHandler(ctx.alloc()));
                strategyList.setRequire(Handler.HTTP_CODEC.name(), true);
                strategyList.setRequire(Handler.SSL_HANDLER.name(), true);
                strategyList.setSupplier(Handler.SSL_HANDLER.name(), () -> sslCtx.newHandler(ctx.alloc()));
                strategyManager.arrange(ctx.pipeline(), strategyList);

                ctx.pipeline().fireChannelRead(msg);
                return;
            }
            requestInfo.setClientType(ProxyRequestInfo.ClientType.TUNNEL);
            try {
                // ctx.pipeline().remove(AGGREGATOR);
                strategyList.setRequire(Handler.HTTP_CODEC.name(), false);
                strategyList.setRequire(Handler.HTTP_AGGREGATOR.name(), false);
                strategyManager.arrange(ctx.pipeline(), strategyList);
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
            // ctx.pipeline().addFirst(HTTP_CODEC, new HttpServerCodec());
            strategyList.setRequire(Handler.HTTP_CODEC.name(), true);
            strategyManager.arrange(ctx.pipeline(), strategyList);

            ctx.pipeline().fireChannelRead(msg);
            return;
        }
        ctx.fireChannelRead(msg);
    }

    private SslHandler createSslHandler(ChannelHandlerContext ctx) throws Exception {
        ProxyRequestInfo requestInfo = ctx.channel().attr(requestInfoAttributeKey).get();
        int port = ((InetSocketAddress) ctx.channel().localAddress()).getPort();
        String originHost = requestInfo.getHost();
        SslContext sslCtx = SslContextBuilder.forServer(
                appConfig.getServerPriKey(), certPool.getCert(port, originHost)).build();

        return sslCtx.newHandler(ctx.alloc());
    }
}
