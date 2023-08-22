package com.catas.wicked.server.handler.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ProxyClientHandler extends ChannelInboundHandlerAdapter {

    private Channel clientChannel;

    public ProxyClientHandler(Channel clientChannel) {
        this.clientChannel = clientChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!clientChannel.isOpen()) {
            ReferenceCountUtil.release(msg);
            return;
        }

        if (msg instanceof HttpResponse origin) {
            // Bug-fix: HttpAggregator removes Transfer-Encoding header
            DefaultHttpResponse copiedResp = new DefaultHttpResponse(
                    origin.protocolVersion(), origin.status(), origin.headers().copy());
            clientChannel.writeAndFlush(msg);
            ctx.fireChannelRead(copiedResp);
        } else {
            ReferenceCountUtil.retain(msg);
            clientChannel.writeAndFlush(msg);
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
        clientChannel.close();
        // TODO: https://i.pinimg.com/564x/67/6c/ee/676cee55deb99942ef3c46e499fec44f.jpg
        log.error("Error occurred in Proxy client.", cause);
    }
}
