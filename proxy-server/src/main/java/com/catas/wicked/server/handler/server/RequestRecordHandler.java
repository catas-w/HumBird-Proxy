package com.catas.wicked.server.handler.server;

import com.catas.wicked.common.bean.ProxyRequestInfo;
import com.catas.wicked.common.bean.message.RequestMessage;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.pipeline.MessageQueue;
import com.catas.wicked.common.util.ThreadPoolService;
import com.catas.wicked.common.util.WebUtils;
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

@Deprecated
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
                // TODO: 使用异步处理
                if (msg instanceof FullHttpRequest request) {
                    // recordHttpRequest(ctx, request.copy(), requestInfo);
                    FullHttpRequest requestCopy = request.copy();
                    ThreadPoolService.getInstance().run(() -> {
                                try {
                                    recordHttpRequest(ctx, requestCopy, requestInfo);
                                } catch (MalformedURLException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
                } else if (!(msg instanceof HttpObject)){
                    recordUnDecodedRequest(ctx, requestInfo);
                }
            } catch (MalformedURLException e) {
                log.error("Record request error: ", e);
            }
        } else {
            log.info("==== Un-record request: {} ====", requestInfo.getHost());
        }
        ctx.fireChannelRead(msg);
    }


    /**
     * record unparsed http request
     */
    private void recordUnDecodedRequest(ChannelHandlerContext ctx, ProxyRequestInfo requestInfo)
            throws MalformedURLException {
        if (!requestInfo.isNewAndReset()) {
            return;
        }

        String url = getHostname(requestInfo) + "/<Encrypted>";
        RequestMessage requestMessage = new RequestMessage(url);
        requestMessage.setRequestId(requestInfo.getRequestId());
        requestMessage.setMethod("UNKNOWN");
        requestMessage.setHeaders(new HashMap<>());
        requestMessage.setStartTime(requestInfo.getRequestStartTime());
        requestMessage.setEndTime(System.currentTimeMillis());
        messageQueue.pushMsg(requestMessage);

        // log.info("RequestId: " + requestInfo.getRequestId());
        log.info("==== Record request[encrypted]: {} ====", url);
    }


    private String getHostname(ProxyRequestInfo requestInfo) {
        StringBuilder builder = new StringBuilder();
        if (requestInfo.isSsl()) {
            builder.append("https://");
            builder.append(requestInfo.getHost());
            if (requestInfo.getPort() != 443) {
                builder.append(":").append(requestInfo.getPort());
            }
        } else {
            builder.append("http://");
            if (requestInfo.getPort() != 80) {
                builder.append(":").append(requestInfo.getPort());
            }
        }

        // log.info("Get host name from requestInfo: {}", builder);
        return builder.toString();
    }

    /**
     * decode HttpPostRequestDecoder
     * 记录请求信息
     */
    private void recordHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request, ProxyRequestInfo requestInfo)
            throws MalformedURLException {
        String uri = request.uri();
        HttpHeaders headers = request.headers();
        HttpMethod method = request.method();
        // log.info("-- uri: {}\n-- headers: {}\n-- method: {}", uri, headers, method);
        ByteBuf content = request.content();

        if (!uri.startsWith("http")) {
            uri = getHostname(requestInfo) + uri;
        }
        RequestMessage requestMessage = new RequestMessage(uri);
        Map<String, String> headerMap = new HashMap<>();
        headers.entries().forEach(entry -> {
            headerMap.put(entry.getKey(), entry.getValue());
        });
        requestMessage.setMethod(method.name());
        requestMessage.setHeaders(headerMap);

        try {
            if (content.isReadable()) {
                if (content.hasArray()) {
                    requestMessage.setBody(content.array());
                } else {
                    byte[] bytes = new byte[content.readableBytes()];
                    content.getBytes(content.readerIndex(), bytes);
                    requestMessage.setBody(bytes);
                }
            }
        } catch (Exception e) {
            log.error("Error recording request content.", e);
        }

        boolean isFormRequest = WebUtils.isFormRequest(headerMap.get("Content-Type"));
        if (isFormRequest) {
            byte[] body = requestMessage.getBody();
            System.out.println(new String(body));
        }

        // save to request tree
        requestMessage.setRequestId(requestInfo.getRequestId());
        requestMessage.setStartTime(requestInfo.getRequestStartTime());
        requestMessage.setEndTime(System.currentTimeMillis());
        messageQueue.pushMsg(requestMessage);
        request.release();

        log.info("==== Record request[decoded]: {} ====", uri);
    }
}
