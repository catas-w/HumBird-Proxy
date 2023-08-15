package com.catas.wicked.server.handler.client;

import com.catas.wicked.common.bean.ProxyRequestInfo;
import com.catas.wicked.common.bean.ResponseMessage;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.pipeline.MessageQueue;
import com.catas.wicked.common.util.ThreadPoolService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Deprecated
@Slf4j
public class ResponseRecordHandler extends ChannelDuplexHandler {

    private ApplicationConfig appConfig;
    private MessageQueue messageQueue;
    private ProxyRequestInfo requestInfo;

    public ResponseRecordHandler(ApplicationConfig appConfig, MessageQueue messageQueue, ProxyRequestInfo requestInfo) {
        this.appConfig = appConfig;
        this.messageQueue = messageQueue;
        this.requestInfo = requestInfo;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (requestInfo.isRecording() && msg instanceof FullHttpResponse response) {
            // TODO: 使用异步处理
            // recordHttpResponse(ctx, response);
            FullHttpResponse respCopy = response.copy();
            ThreadPoolService.getInstance().run(() -> recordHttpResponse(ctx, respCopy));
        }
        ctx.fireChannelRead(msg);
    }

    private void recordHttpResponse(ChannelHandlerContext ctx, FullHttpResponse resp) {
        HttpHeaders headers = resp.headers();
        HttpResponseStatus status = resp.status();

        ResponseMessage responseMessage = new ResponseMessage();
        Map<String, String> map = new HashMap<>();
        headers.entries().forEach(entry -> {
            map.put(entry.getKey(), entry.getValue());
        });

        responseMessage.setStatus(status.code());
        responseMessage.setHeaders(map);
        ByteBuf content = resp.content();
        if (content.isReadable()) {
            if (content.hasArray()) {
                responseMessage.setContent(content.array());
            } else {
                byte[] bytes = new byte[content.readableBytes()];
                content.getBytes(content.readerIndex(), bytes);
                responseMessage.setContent(bytes);
            }
        }

        responseMessage.setRequestId(requestInfo.getRequestId());
        messageQueue.pushMsg(responseMessage);
        resp.release();
        // log.info("-- RequestId: " + requestInfo.getRequestId());
    }
}
