package com.catas.wicked.server.handler.server;

import com.catas.wicked.common.bean.ProxyRequestInfo;
import com.catas.wicked.common.bean.message.RequestMessage;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.constant.ProxyConstant;
import com.catas.wicked.common.pipeline.MessageQueue;
import com.catas.wicked.common.pipeline.Topic;
import com.catas.wicked.common.util.WebUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;


@Slf4j
public class ServerPreRecorder extends ChannelInboundHandlerAdapter {

    private final ApplicationConfig appConfig;

    private final MessageQueue messageQueue;

    private final AttributeKey<ProxyRequestInfo> requestInfoAttributeKey =
            AttributeKey.valueOf(ProxyConstant.REQUEST_INFO);

    private AtomicReference<String> curRequestId;

    public ServerPreRecorder(ApplicationConfig appConfig, MessageQueue messageQueue) {
        this.appConfig = appConfig;
        this.messageQueue = messageQueue;
        curRequestId = new AtomicReference<>("initId");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ProxyRequestInfo requestInfo = ctx.channel().attr(requestInfoAttributeKey).get();

        boolean newRequest = false;
        String reqId = curRequestId.get();
        if (!StringUtils.equals(reqId, requestInfo.getRequestId())) {
            newRequest = curRequestId.compareAndSet(reqId, requestInfo.getRequestId());
        }

        if (msg instanceof HttpRequest httpRequest) {
            requestInfo.updateRequestSize(WebUtils.estimateSize(httpRequest));
            // record
            recordHttpRequest(requestInfo, httpRequest, newRequest);
        } else if (msg instanceof HttpContent content) {
            requestInfo.updateRequestSize(content.content().readableBytes());
        } else {
            try {
                ByteBuf cont = (ByteBuf) msg;
                requestInfo.updateRequestSize(cont.readableBytes());
            } catch (Exception e) {
                log.warn("Unable to catch request size.", e);
            }
            recordHttpRequest(requestInfo, msg, newRequest);
        }
        requestInfo.updateRequestTime();

        super.channelRead(ctx, msg);
    }

    private void recordHttpRequest(ProxyRequestInfo requestInfo, HttpRequest request, boolean newRequest) {
        if (!requestInfo.isRecording()) {
            return;
        }

        String uri = request.uri();
        HttpHeaders headers = request.headers();
        HttpMethod method = request.method();

        if (!uri.startsWith("http")) {
            uri = WebUtils.getHostname(requestInfo) + uri;
        }
        RequestMessage requestMessage = new RequestMessage(uri);
        Map<String, String> headerMap = new LinkedHashMap<>();
        headers.entries().forEach(entry -> {
            headerMap.put(entry.getKey(), entry.getValue());
        });
        requestMessage.setMethod(method.name());
        requestMessage.setHeaders(headerMap);
        requestMessage.setProtocol(request.protocolVersion().text());
        setRequestMsgInfo(requestInfo, requestMessage);

        messageQueue.pushMsg(Topic.RECORD, requestMessage);

        requestInfo.setHasSentRequestMsg(true);
        log.info(">>>> Request Prev-Send[decoded]: {} ID: {} >>>>", uri, requestInfo.getRequestId());
    }

    private void recordHttpRequest(ProxyRequestInfo requestInfo, Object msg, boolean newRequest) {
        if (!requestInfo.isRecording() || !newRequest) {
            return;
        }
        String url = WebUtils.getHostname(requestInfo) + "/<Encrypted>";
        RequestMessage requestMessage = new RequestMessage(url);
        requestMessage.setMethod("-");
        requestMessage.setHeaders(new HashMap<>());
        requestMessage.setEncrypted(true);
        setRequestMsgInfo(requestInfo, requestMessage);
        messageQueue.pushMsg(Topic.RECORD, requestMessage);

        requestInfo.setHasSentRequestMsg(true);
        log.info(">>>> Request Prev-Send[encrypted]: {} ID: {} >>>>", url, requestInfo.getRequestId());
    }

    private void setRequestMsgInfo(ProxyRequestInfo requestInfo, RequestMessage requestMsg) {
        requestMsg.setRequestId(requestInfo.getRequestId());
        requestMsg.setStartTime(requestInfo.getRequestStartTime());
        requestMsg.setEndTime(requestInfo.getRequestEndTime());
        requestMsg.setSize(requestInfo.getRequestSize());
        requestMsg.setRemoteHost(requestInfo.getHost());
        requestMsg.setRemotePort(requestInfo.getPort());
        // requestMsg.setRemoteAddress(requestInfo.getRemoteAddress());
        requestMsg.setLocalAddress(requestInfo.getLocalAddress());
        requestMsg.setLocalPort(requestInfo.getLocalPort());
        requestMsg.setClientStatus(requestInfo.getClientStatus().copy());
    }
}
