package com.catas.wicked.proxy.proxy;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProxyClientHandler extends ChannelInboundHandlerAdapter {

    private Channel clientChannel;

    public ProxyClientHandler(Channel clientChannel) {
        this.clientChannel = clientChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        FullHttpResponse response = (FullHttpResponse) msg;
        //修改http响应体返回至客户端
        response.headers().add("test","from proxy");
        log.info("Received msg from remote: {}", msg);
        clientChannel.writeAndFlush(msg);
    }
}
