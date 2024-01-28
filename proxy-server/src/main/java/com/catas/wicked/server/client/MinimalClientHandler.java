package com.catas.wicked.server.client;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MinimalClientHandler extends ChannelDuplexHandler {

    private MinimalHttpClient client;
    private HttpResponse response;

    public MinimalClientHandler(MinimalHttpClient client) {
        this.client = client;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        // System.out.println("close client");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        if (msg instanceof HttpResponse httpResponse){
            // System.out.println("Receiving: " + httpResponse);
            response = new DefaultFullHttpResponse(httpResponse.protocolVersion(), httpResponse.status());
        } else if (msg instanceof LastHttpContent) {
            // TODO notify to get response
            client.setHttpResponse(response);
            client.close();
            // client.getNotifier().notifyAll();
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);
    }
}
