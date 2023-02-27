package com.catas.wicked.proxy.proxy.handler;

import com.catas.wicked.proxy.bean.ProxyRequestInfo;
import com.catas.wicked.proxy.cert.CertPool;
import com.catas.wicked.proxy.cert.CertService;
import com.catas.wicked.proxy.common.ProxyConstant;
import com.catas.wicked.proxy.common.ServerStatus;
import com.catas.wicked.proxy.config.ProxyConfig;
import com.catas.wicked.proxy.util.WebUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.TooLongHttpContentException;
import io.netty.handler.codec.http.TooLongHttpHeaderException;
import io.netty.handler.codec.http.TooLongHttpLineException;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.util.ReferenceCountUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProxyServerHandler extends ChannelInboundHandlerAdapter {

    private ChannelFuture channelFuture;

    private ProxyRequestInfo proxyRequest;

    private List<Object> requestList;

    private boolean connected;

    private byte[] httpTagBuf;

    private ProxyConfig proxyConfig;

    private CertService certService;

    private CertPool certPool;

    private ServerStatus status;

    public ProxyServerHandler(ProxyConfig proxyConfig, CertService certService, CertPool certPool) {
        this.proxyConfig = proxyConfig;
        this.certService = certService;
        this.certPool = certPool;
        this.status = ServerStatus.INIT;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            handleHttpRequest(ctx, msg);
        } else if (msg instanceof HttpContent) {
            handleHttpContent(ctx, msg);
        } else {
            handleSsl(ctx, msg);
        }
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, Object msg) {
        HttpRequest request = (HttpRequest) msg;

        if (status.equals(ServerStatus.INIT)) {
            setStatus(ServerStatus.RUNNING);
            setProxyRequest(WebUtils.getRequestProto(request));
            if (HttpMethod.CONNECT.name().equalsIgnoreCase(request.method().name())) {
                // https connect
                HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, ProxyConstant.SUCCESS);
                ctx.writeAndFlush(response);
                ctx.channel().pipeline().remove("httpCodec");
                ReferenceCountUtil.release(msg);
                return;
            }
        }
        handleProxyData(ctx.channel(), msg, true);
    }

    private void handleHttpContent(ChannelHandlerContext ctx, Object msg) {
        ByteBuf content = ((HttpContent) msg).content();
        if (content.isReadable()) {
            String s = content.toString(StandardCharsets.UTF_8);
            System.out.println(s);
        }
        handleProxyData(ctx.channel(), msg, true);
    }

    private void handleSsl(ChannelHandlerContext ctx, Object msg) throws Exception {
        // TODO
        ByteBuf byteBuf = (ByteBuf) msg;
        if (proxyConfig.isHandleSsl() && byteBuf.getByte(0) == 22) {
            getProxyRequest().setSsl(true);
            int port = ((InetSocketAddress) ctx.channel().localAddress()).getPort();
            SslContext sslCtx = SslContextBuilder
                    .forServer(proxyConfig.getServerPriKey(), certPool.getCert(port, getProxyRequest().getHost())).build();
            ctx.pipeline().addFirst("httpCodec", new HttpServerCodec());
            ctx.pipeline().addFirst("sslHandle", sslCtx.newHandler(ctx.alloc()));
            // 重新过一遍pipeline，拿到解密后的的http报文
            ctx.pipeline().fireChannelRead(msg);

            ByteBuf data = (ByteBuf) msg;
            if (data.isReadable()) {
                String s = data.toString(StandardCharsets.UTF_8);
                System.out.println(s);
            }
            return;
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
        if (isHttp(byteBuf)) {
            ctx.pipeline().addFirst("httpCodec", new HttpServerCodec());
            ctx.pipeline().fireChannelRead(msg);
            return;
        }
        handleProxyData(ctx.channel(), msg, false);
    }

    private void handleProxyData(Channel channel, Object msg, boolean isHttp) {
        // TODO: record request
        if (channelFuture == null) {
            if (isHttp && !(msg instanceof HttpRequest)) {
                return;
            }
            ChannelInitializer channelInitializer = isHttp ? new ProxyInitializer(channel, proxyRequest, null, proxyConfig)
                    : new TunnelProxyInitializer(channel, null);
            Bootstrap bootstrap = new Bootstrap();

            bootstrap.group(channel.eventLoop())
                    .channel(NioSocketChannel.class)
                    .handler(channelInitializer);

            // bootstrap.resolver(NoopAddressResolverGroup.INSTANCE);
            setRequestList(new LinkedList<Object>());
            setChannelFuture(bootstrap.connect(proxyRequest.getHost(), proxyRequest.getPort()));
            getChannelFuture().addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    future.channel().writeAndFlush(msg);
                    synchronized (getRequestList()) {
                        getRequestList().forEach(obj -> future.channel().writeAndFlush(obj));
                        getRequestList().clear();
                        setConnected(true);
                    }
                } else {
                    synchronized (getRequestList()) {
                        getRequestList().forEach(ReferenceCountUtil::release);
                        getRequestList().clear();
                    }
                    // TODO
                    // getExceptionHandle().beforeCatch(channel, future.cause());
                    future.channel().close();
                    channel.close();
                }
            });
        } else {
            synchronized (getRequestList()) {
                if (isConnected()) {
                    getChannelFuture().channel().writeAndFlush(msg);
                } else {
                    getRequestList().add(msg);
                }
            }
        }
    }

    private boolean isHttp(ByteBuf byteBuf) {
        byte[] bytes = new byte[8];
        byteBuf.getBytes(0, bytes);
        String methodToken = new String(bytes);
        return methodToken.startsWith("GET ") || methodToken.startsWith("POST ") || methodToken.startsWith("HEAD ")
                || methodToken.startsWith("PUT ") || methodToken.startsWith("DELETE ") || methodToken.startsWith("OPTIONS ")
                || methodToken.startsWith("CONNECT ") || methodToken.startsWith("TRACE ");
    }
}
