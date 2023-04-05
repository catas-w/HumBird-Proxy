package com.catas.wicked.server.proxy.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.HttpContent;
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

        if (msg instanceof HttpResponse) {
            DecoderResult decoderResult = ((HttpResponse) msg).decoderResult();
            Throwable cause = decoderResult.cause();
            if(cause != null){
                ReferenceCountUtil.release(msg);
                this.exceptionCaught(ctx, cause);
                return;
            }
            // TODO: record resp & throttle
        } else if (msg instanceof HttpContent) {
            // TODO: record
        }
        clientChannel.writeAndFlush(msg);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
        clientChannel.close();
        // TODO: exception handle
    }
}
