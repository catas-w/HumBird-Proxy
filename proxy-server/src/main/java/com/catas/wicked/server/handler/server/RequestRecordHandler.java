package com.catas.wicked.server.handler.server;

import com.catas.wicked.common.bean.ProxyRequestInfo;
import com.catas.wicked.common.bean.RequestMessage;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.pipeline.MessageQueue;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class RequestRecordHandler extends ChannelInboundHandlerAdapter {

    private ApplicationConfig applicationConfig;
    private MessageQueue messageQueue;
    private final AttributeKey<ProxyRequestInfo> requestInfoAttributeKey = AttributeKey.valueOf("requestInfo");

    public RequestRecordHandler(ApplicationConfig applicationConfig, MessageQueue messageQueue) {
        this.applicationConfig = applicationConfig;
        this.messageQueue = messageQueue;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            System.out.println("======= Request start ===========");
            FullHttpRequest request = (FullHttpRequest) msg;
            String uri = request.uri();
            HttpHeaders headers = request.headers();
            HttpMethod method = request.method();
            log.info("-- uri: {}\n-- headers: {}\n-- method: {}", uri, headers, method);
            ByteBuf content = request.content();

            RequestMessage requestMessage = new RequestMessage(uri);
            requestMessage.setMethod(method);

            if (content.isReadable()) {
                String cont = content.toString(StandardCharsets.UTF_8);
                log.info("-- content: {}", cont.length() > 1000 ? cont.substring(0, 1000): cont);
            }
            messageQueue.pushMsg(requestMessage);
            System.out.println("=========== Request end ============");
        } else if (msg instanceof HttpRequest) {
            System.out.println("=========== http request ====================");
        }
        ctx.fireChannelRead(msg);
    }
}
