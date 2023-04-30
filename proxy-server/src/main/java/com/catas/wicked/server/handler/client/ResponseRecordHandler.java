package com.catas.wicked.server.handler.client;

import com.catas.wicked.common.bean.ProxyRequestInfo;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.pipeline.MessageQueue;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class ResponseRecordHandler extends ChannelInboundHandlerAdapter {

    private ApplicationConfig appConfig;
    private MessageQueue messageQueue;
    private ProxyRequestInfo requestInfo;

    public ResponseRecordHandler(ApplicationConfig appConfig, MessageQueue messageQueue, ProxyRequestInfo requestInfo) {
        this.appConfig = appConfig;
        this.messageQueue = messageQueue;
        this.requestInfo = requestInfo;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            recordHttpResponse(ctx, response);
        }
        ctx.fireChannelRead(msg);
    }

    private void recordHttpResponse(ChannelHandlerContext ctx, FullHttpResponse resp) {
        System.out.println("=========== Response start ============");

        HttpHeaders headers = resp.headers();
        log.info("-- headers: {}", headers);

        ByteBuf content = resp.content();
        if (content.isReadable()) {
            String cont = content.toString(StandardCharsets.UTF_8);
            log.info("-- cont: {}", cont.length() > 1000 ? cont.substring(0, 1000): cont);
        }

        log.info("RequestId: " + requestInfo.getRequestId());

        System.out.println("=========== Response end ============");
    }
}
