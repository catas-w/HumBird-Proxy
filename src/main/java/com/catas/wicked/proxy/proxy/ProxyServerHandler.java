package com.catas.wicked.proxy.proxy;

import com.catas.wicked.proxy.bean.ProxyRequestInfo;
import com.catas.wicked.proxy.common.ProxyConstant;
import com.catas.wicked.proxy.proxy.handler.ProxyInitializer;
import com.catas.wicked.proxy.proxy.handler.TunnelProxyInitializer;
import com.catas.wicked.proxy.util.WebUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.ReferenceCountUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.LinkedList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProxyServerHandler extends ChannelInboundHandlerAdapter {

    private ChannelFuture channelFuture;

    private ProxyRequestInfo proxyRequest;

    private List<Object> requestList;

    private boolean connected;

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
        setProxyRequest(WebUtils.getRequestProto(request));
        if (HttpMethod.CONNECT.name().equalsIgnoreCase(request.method().name())) {
            // https connect
            HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, ProxyConstant.SUCCESS);
            ctx.writeAndFlush(response);
            ctx.channel().pipeline().remove("httpCodec");
            ReferenceCountUtil.release(msg);
            return;
        }
        handleProxyData(ctx.channel(), msg, true);
    }

    private void handleHttpContent(ChannelHandlerContext ctx, Object msg) {
        handleProxyData(ctx.channel(), msg, true);
    }

    private void handleSsl(ChannelHandlerContext ctx, Object msg) {
        // TODO
    }

    private void handleProxyData(Channel channel, Object msg, boolean isHttp) {
        // TODO: record request
        if (channelFuture == null) {
            if (isHttp && !(msg instanceof HttpRequest)) {
                return;
            }
            ChannelInitializer channelInitializer = isHttp ? new ProxyInitializer(channel, proxyRequest, null)
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
}
