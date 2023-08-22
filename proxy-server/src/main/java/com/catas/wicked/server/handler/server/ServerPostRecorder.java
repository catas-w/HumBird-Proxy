package com.catas.wicked.server.handler.server;

import com.catas.wicked.common.bean.ProxyRequestInfo;
import com.catas.wicked.common.bean.RequestMessage;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.constant.ProxyConstant;
import com.catas.wicked.common.pipeline.MessageQueue;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 代理服务器请求记录器
 * record requests
 */
@Slf4j
public class ServerPostRecorder extends ChannelInboundHandlerAdapter {

    private ApplicationConfig appConfig;
    private MessageQueue messageQueue;
    private final AttributeKey<ProxyRequestInfo> requestInfoKey = AttributeKey.valueOf(ProxyConstant.REQUEST_INFO);

    public ServerPostRecorder(ApplicationConfig applicationConfig, MessageQueue messageQueue) {
        this.appConfig = applicationConfig;
        this.messageQueue = messageQueue;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ProxyRequestInfo requestInfo = ctx.channel().attr(requestInfoKey).get();
        if (!requestInfo.isRecording()) {
            ReferenceCountUtil.release(msg);
            return;
        }

        try {
            if (msg instanceof FullHttpRequest fullHttpRequest) {
                recordHttpRequest(ctx, fullHttpRequest, requestInfo);
            } else if (!(msg instanceof HttpObject)) {
                recordUnDecodedRequest(ctx, requestInfo);
            } else {
                log.error("?????");
            }
        } catch (Exception e) {
            log.error("Error in recording http request.", e);
        } finally {
            ReferenceCountUtil.release(msg);
        }
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
     * record unparsed http request
     */
    private void recordUnDecodedRequest(ChannelHandlerContext ctx, ProxyRequestInfo requestInfo) {
        if (!requestInfo.isNewAndReset()) {
            return;
        }

        String url = getHostname(requestInfo) + "/<Encrypted>";
        RequestMessage requestMessage = new RequestMessage(url);
        requestMessage.setRequestId(requestInfo.getRequestId());
        requestMessage.setMethod("UNKNOWN");
        requestMessage.setHeaders(new HashMap<>());
        requestMessage.setStartTime(requestInfo.getRequestStartTime());
        requestMessage.setEndTime(requestInfo.getRequestEndTime());
        messageQueue.pushMsg(requestMessage);

        log.info(">>>> Request send[encrypted]: {} ID: {} >>>>", url, requestInfo.getRequestId());
    }

    /**
     * decode HttpPostRequestDecoder
     * 记录请求信息
     */
    private void recordHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request, ProxyRequestInfo requestInfo) {
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

        // boolean isFormRequest = WebUtils.isFormRequest(headerMap.get("Content-Type"));
        // if (isFormRequest) {
        //     byte[] body = requestMessage.getBody();
        //     System.out.println(new String(body));
        // }

        // save to request tree
        requestMessage.setRequestId(requestInfo.getRequestId());
        requestMessage.setStartTime(requestInfo.getRequestStartTime());
        requestMessage.setEndTime(requestInfo.getRequestEndTime());
        messageQueue.pushMsg(requestMessage);

        log.info(">>>> Request send[decoded]: {} ID: {} >>>>", uri, requestInfo.getRequestId());
    }
}
