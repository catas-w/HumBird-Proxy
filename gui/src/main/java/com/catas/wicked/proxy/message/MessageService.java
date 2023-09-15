package com.catas.wicked.proxy.message;

import com.catas.wicked.common.bean.message.BaseMessage;
import com.catas.wicked.common.bean.message.DeleteMessage;
import com.catas.wicked.common.bean.message.PoisonMessage;
import com.catas.wicked.common.bean.message.RequestMessage;
import com.catas.wicked.common.bean.message.ResponseMessage;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.pipeline.MessageQueue;
import com.catas.wicked.common.util.ThreadPoolService;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.Cache;

@Slf4j
@Singleton
public class MessageService {

    @Inject
    private ApplicationConfig appConfig;

    @Inject
    private MessageQueue messageQueue;

    @Inject
    private Cache<String, RequestMessage> requestCache;

    @Inject
    private MessageTree messageTree;

    @PostConstruct
    public void init() {
        // fetch data from queue and add to message tree
        ThreadPoolService.getInstance().run(() -> {
            while (!appConfig.getShutDownFlag().get()) {
                try {
                    BaseMessage msg = messageQueue.getMsg();
                    processMsg(msg);
                } catch (InterruptedException e) {
                    log.info("-- quit --");
                    break;
                } catch (Exception e) {
                    log.error("Error occurred in message tree thread", e);
                }
            }
        });
    }

    private void processMsg(BaseMessage msg) throws Exception {
        if (msg instanceof PoisonMessage) {
            throw new InterruptedException();
        }

        if (msg instanceof RequestMessage requestMessage) {
            switch (requestMessage.getType()) {
                case REQUEST -> {
                    // put to cache
                    requestCache.put(requestMessage.getRequestId(), requestMessage);
                    messageTree.add(requestMessage);
                }
                case REQUEST_CONTENT -> {
                    // 添加请求体
                    RequestMessage contentMsg = (RequestMessage) msg;
                    RequestMessage data = requestCache.get(contentMsg.getRequestId());
                    if (data != null) {
                        data.setBody(contentMsg.getBody());
                        requestCache.put(data.getRequestId(), data);
                    }
                }
            }
        }

        if (msg instanceof ResponseMessage responseMessage) {
            switch (responseMessage.getType()) {
                case RESPONSE -> {
                    ResponseMessage respMessage = (ResponseMessage) msg;
                    RequestMessage data = requestCache.get(respMessage.getRequestId());
                    if (data != null) {
                        data.setResponse(respMessage);
                        requestCache.put(data.getRequestId(), data);
                    }
                }
                case RESPONSE_CONTENT -> {
                    // 添加响应体
                    ResponseMessage respMessage = (ResponseMessage) msg;
                    RequestMessage data = requestCache.get(respMessage.getRequestId());
                    if (data != null && data.getResponse() != null) {
                        data.getResponse().setContent(respMessage.getContent());
                        requestCache.put(data.getRequestId(), data);
                    }
                }
            }
        }

        if (msg instanceof DeleteMessage deleteMessage) {
            messageTree.delete(deleteMessage.getRequestCell());
        }
    }
}
