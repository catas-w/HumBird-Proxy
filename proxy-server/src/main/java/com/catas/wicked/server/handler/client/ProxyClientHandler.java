package com.catas.wicked.server.handler.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;

@Slf4j
public class ProxyClientHandler extends ChannelInboundHandlerAdapter {

    private Channel clientChannel;

    private final List<Object> responseList;

    public ProxyClientHandler(Channel clientChannel) {
        this.clientChannel = clientChannel;
        responseList = new LinkedList<>();
    }



    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (clientChannel.isOpen()) {
            ReferenceCountUtil.retain(msg);
            clientChannel.writeAndFlush(msg);
            ctx.fireChannelRead(msg);

            if (!responseList.isEmpty()) {
                synchronized (responseList) {
                    responseList.forEach(resp -> {
                        ReferenceCountUtil.retain(resp);
                        clientChannel.writeAndFlush(resp);
                        ctx.fireChannelRead(resp);
                    });
                }
            }
        } else {
            synchronized (responseList) {
                responseList.add(msg);
            }
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
        if (!responseList.isEmpty()) {
            synchronized (responseList) {
                responseList.forEach(ReferenceCountUtil::release);
            }
        }
    }
}
