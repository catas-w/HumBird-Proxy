package com.catas.wicked.proxy.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpVersion;

public class ProxyServerHandler extends ChannelInboundHandlerAdapter {

    private ChannelFuture channelFuture;

    private int port;

    private String host;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            String host = request.headers().get("host");
            String[] temp = host.split(":");
            int port = 80;
            if (temp.length > 1) {
                port = Integer.parseInt(temp[1]);
            } else {
                if (request.uri().indexOf("https") == 0) {
                    port = 443;
                }
            }
            this.host = temp[0];
            this.port = port;
            if ("CONNECT".equalsIgnoreCase(request.method().name())) {//HTTPS建立代理握手
                HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, ProxyServer.SUCCESS);
                ctx.writeAndFlush(response);
                ctx.pipeline().remove("httpCodec");
                ctx.pipeline().remove("httpObject");
                return;
            }
            //连接至目标服务器
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(ctx.channel().eventLoop())
                    .channel(ctx.channel().getClass())
                    .handler(new ProxyInitializer(ctx.channel()));

            ChannelFuture cf = bootstrap.connect(temp[0], port);
            cf.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        future.channel().writeAndFlush(msg);
                    } else {
                        ctx.channel().close();
                    }
                }
            });
        } else { // https 只转发数据，不做处理
            if (channelFuture == null) {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(ctx.channel().eventLoop())
                        .channel(ctx.channel().getClass())
                        .handler(new ChannelInitializer() {

                            @Override
                            protected void initChannel(Channel ch) throws Exception {
                                ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx0, Object msg) throws Exception {
                                        ctx.channel().writeAndFlush(msg);
                                    }
                                });
                            }
                        });
                channelFuture = bootstrap.connect(host, port);
                channelFuture.addListener(new ChannelFutureListener() {
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            future.channel().writeAndFlush(msg);
                        } else {
                            ctx.channel().close();
                        }
                    }
                });
            } else {
                channelFuture.channel().writeAndFlush(msg);
            }
        }

    }
}
