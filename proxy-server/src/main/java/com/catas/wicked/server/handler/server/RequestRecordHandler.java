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
import io.netty.handler.codec.http.HttpObject;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RequestRecordHandler extends ChannelInboundHandlerAdapter {

    private ApplicationConfig applicationConfig;
    private MessageQueue messageQueue;
    private final AttributeKey<ProxyRequestInfo> requestInfoAttributeKey = AttributeKey.valueOf("requestInfo");

    public RequestRecordHandler(ApplicationConfig applicationConfig, MessageQueue messageQueue) {
        this.applicationConfig = applicationConfig;
        this.messageQueue = messageQueue;
    }

    /**
     * 参数解析: org.springframework.web.method.annotation.RequestParamMapMethodArgumentResolver
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Attribute<ProxyRequestInfo> attr = ctx.channel().attr(requestInfoAttributeKey);
        ProxyRequestInfo requestInfo = attr.get();

        if (requestInfo.isRecording()) {
            try {
                if (msg instanceof FullHttpRequest) {
                    FullHttpRequest request = (FullHttpRequest) msg;
                    recordHttpRequest(ctx, request.copy());
                } else if (!(msg instanceof HttpObject)){
                    recordUnDecodedRequest(ctx, requestInfo);
                }
            } catch (MalformedURLException e) {
                log.error("Record request error: ", e);
            }
        }
        ctx.fireChannelRead(msg);
    }


    private void recordUnDecodedRequest(ChannelHandlerContext ctx, ProxyRequestInfo requestInfo)
            throws MalformedURLException {
        if (!requestInfo.isNewAndReset()) {
            return;
        }
        System.out.println("=========== Un-decoded Request start ============");
        StringBuilder builder = new StringBuilder();
        builder.append("https://").append(requestInfo.getHost());
        if (requestInfo.getPort() != 80 || requestInfo.getPort() != 443) {
            builder.append(":").append(requestInfo.getPort());
        }
        builder.append("/Encrypted");
        RequestMessage requestMessage = new RequestMessage(builder.toString());
        requestMessage.setRequestId(requestInfo.getRequestId());
        requestMessage.setMethod("UNKNOWN");
        requestMessage.setHeaders(new HashMap<>());
        messageQueue.pushMsg(requestMessage);

        log.info("RequestId: " + requestInfo.getRequestId());
        System.out.println("=========== Un-decoded Request end ============");
    }

    /**
     * decode HttpPostRequestDecoder
     * 记录请求信息
     */
    private void recordHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) throws MalformedURLException {
        System.out.println("=========== Request start ============");
        String uri = request.uri();
        HttpHeaders headers = request.headers();
        HttpMethod method = request.method();
        log.info("-- uri: {}\n-- headers: {}\n-- method: {}", uri, headers, method);
        ByteBuf content = request.content();

        RequestMessage requestMessage = new RequestMessage(uri);
        Map<String, String> map = new HashMap<>();
        headers.entries().forEach(entry -> {
            map.put(entry.getKey(), entry.getValue());
        });
        requestMessage.setMethod(method.name());
        requestMessage.setHeaders(map);

        if (content.isReadable()) {
            if (content.hasArray()) {
                requestMessage.setBody(content.array());
            } else {
                byte[] bytes = new byte[content.readableBytes()];
                content.getBytes(content.readerIndex(), bytes);
                requestMessage.setBody(bytes);
            }
        }

        // save to request tree
        Attribute<ProxyRequestInfo> attr = ctx.channel().attr(requestInfoAttributeKey);
        ProxyRequestInfo requestInfo = attr.get();
        requestMessage.setRequestId(requestInfo.getRequestId());
        messageQueue.pushMsg(requestMessage);

        log.info("RequestId: " + requestInfo.getRequestId());
        System.out.println("=========== Request end ============");
    }
}
