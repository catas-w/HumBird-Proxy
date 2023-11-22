package com.catas.wicked.proxy.service;

import com.catas.wicked.common.bean.FeRequestInfo;
import com.catas.wicked.common.bean.message.BaseMessage;
import com.catas.wicked.common.bean.message.RenderMessage;
import com.catas.wicked.common.bean.message.RequestMessage;
import com.catas.wicked.common.bean.message.ResponseMessage;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.util.ThreadPoolService;
import com.catas.wicked.common.util.WebUtils;
import com.catas.wicked.proxy.gui.controller.DetailTabController;
import com.catas.wicked.proxy.gui.controller.DetailWebViewController;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.ehcache.Cache;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;


/**
 * Update gui of tab-pane
 */
@Data
@Slf4j
@Singleton
public class RequestViewService {

    @Inject
    private DetailTabController detailTabController;

    @Inject
    private DetailWebViewController detailWebViewController;

    @Inject
    private Cache<String, RequestMessage> requestCache;

    private String currentRequestId;

    @Inject
    private FeService feService;

    @Inject
    private ApplicationConfig appConfig;
    private BlockingQueue<BaseMessage> queue;

    private Map<RenderMessage.Tab, Consumer<RenderMessage>> renderFuncMap;

    private static final String REQ_HEADER = "requestHeaders";
    private static final String REQ_DETAIL = "requestDetail";
    private static final String RESP_HEADER = "responseHeaders";
    private static final String RESP_DETAIL = "responseDetail";

    private static final String ERROR_DATA = "<Error loading data>";

    public void pushMsg(BaseMessage message) {
        queue.add(message);
    }

    public BaseMessage getMsg() throws InterruptedException {
        return queue.take();
    }

    @PostConstruct
    public void init() {
        this.queue = new LinkedBlockingQueue<>();
        this.renderFuncMap = new HashMap<>();
        renderFuncMap.put(RenderMessage.Tab.REQUEST, this::renderRequest);
        renderFuncMap.put(RenderMessage.Tab.RESPONSE, this::renderResponse);
        renderFuncMap.put(RenderMessage.Tab.OVERVIEW, this::renderOverview);

        ThreadPoolService.getInstance().run(() -> {
            while (!appConfig.getShutDownFlag().get()) {
                try {
                    BaseMessage msg = getMsg();
                    if (msg instanceof RenderMessage renderMsg) {
                        // System.out.println(renderMsg);
                        Consumer<RenderMessage> consumer = renderFuncMap.get(renderMsg.getTab());
                        if (consumer != null) {
                            consumer.accept(renderMsg);
                        } else {
                            log.warn("consumer not exist");
                        }
                    }
                } catch (InterruptedException e) {
                    log.info("-- quit --");
                    break;
                } catch (Exception e) {
                    log.error("Error occurred in message tree thread", e);
                }
            }
        });
    }

    public synchronized void updateRequestTab(String requestId) {
        String curRequestId = appConfig.getCurrentRequestId().get();
        if (StringUtils.equals(curRequestId, requestId)) {
            log.warn("Same requestId");
            return;
        }
        queue.clear();
        // System.out.println("-----requestId: " + requestId + "-----");

        // current requestView tab
        String curTab = detailTabController.getCurrentRequestTab();
        RenderMessage.Tab firstTargetTab = RenderMessage.Tab.valueOf(curTab);

        Queue<RenderMessage> messages = new PriorityQueue<>(Comparator.comparingInt(o -> o.getTab().getOrder()));
        if (requestId == null) {
            // set empty page
            RenderMessage msg = new RenderMessage(requestId, RenderMessage.Tab.EMPTY);
            messages.offer(msg);
        } else {
            messages.offer(new RenderMessage(requestId, RenderMessage.Tab.OVERVIEW));
            messages.offer(new RenderMessage(requestId, RenderMessage.Tab.REQUEST));
            messages.offer(new RenderMessage(requestId, RenderMessage.Tab.RESPONSE));
            messages.offer(new RenderMessage(requestId, RenderMessage.Tab.TIMING));
        }

        // render current tab first
        Iterator<RenderMessage> iterator = messages.iterator();
        while (iterator.hasNext()) {
            RenderMessage msg = iterator.next();
            if (msg.getTab() == firstTargetTab) {
                pushMsg(msg);
                iterator.remove();
            }
        }

        while (!messages.isEmpty()) {
            pushMsg(messages.poll());
        }
        appConfig.getCurrentRequestId().set(requestId);
    }

    private void renderRequest(RenderMessage renderMsg) {
        System.out.println("-- render request --");
        RequestMessage request = requestCache.get(renderMsg.getRequestId());
        detailTabController.displayRequest(request);
    }

    private void renderResponse(RenderMessage renderMsg) {
        System.out.println("-- render response --");
        RequestMessage request = requestCache.get(renderMsg.getRequestId());
        detailTabController.displayResponse(request.getResponse());
    }

    private void renderOverview(RenderMessage renderMsg) {
        System.out.println("-- render overview --");
        RequestMessage request = requestCache.get(renderMsg.getRequestId());
        detailTabController.displayOverView(request);
    }

    public void updateView(String requestId) {
        if (requestId == null || requestId.equals(currentRequestId)) {
            return;
        }
        this.currentRequestId = requestId;
        RequestMessage request = requestCache.get(requestId);

        if (request != null) {
            detailTabController.displayRequest(request);
        }
    }

    private void updateOverviewTab(RequestMessage request, ResponseMessage response) {
        FeRequestInfo info = new FeRequestInfo();
        info.setUrl(request.getRequestUrl());
        info.setProtocol("Http1.1");
        info.setCode(response == null ? "": String.valueOf(response.getStatus()));
        info.setMethod(request.getMethod());

        feService.setUrlTitle(info);
    }

    private void updateRequestTab(RequestMessage request) {
        try {
            // render request headers
            String headers = parseHeaders(request.getHeaders());
            System.out.println("*** Request headers: " + headers);
            feService.renderData(REQ_HEADER, headers);
        } catch (Exception e) {
            log.error("Error rendering request headers.", e);
            feService.renderData(REQ_HEADER, ERROR_DATA);
        }

        // String contentTypeHeader = request.getHeaders().get("Content-type");
        // ContentType contentType = ContentType.parse(contentTypeHeader);
        // String mimeType = contentType.getMimeType();
        // Charset charset = contentType.getCharset();
        // System.out.println("*** Request MimeType: " + mimeType);
        // System.out.println("*** Request Charset: " + charset);

        try {
            // render request body
            byte[] content = WebUtils.parseContent(request.getHeaders(), request.getBody());

            // String encoding = charset == null ? "UTF-8": charset.name();
            String contentStr = "";
            contentStr = new String(content, StandardCharsets.UTF_8);
            feService.renderData(REQ_DETAIL, contentStr);
        } catch (Exception e) {
            log.error("Error rendering request body.", e);
            feService.renderData(REQ_DETAIL, ERROR_DATA);
        }
    }

    private void updateResponseTab(ResponseMessage response) {
        try {
            // render request headers
            String headers = parseHeaders(response.getHeaders());
            System.out.println("*** Response headers: " + headers);
            feService.renderData(RESP_HEADER, headers);
        } catch (Exception e) {
            log.error("Error rendering response headers.", e);
            feService.renderData(REQ_HEADER, ERROR_DATA);
        }

        try {
            // render parseContent
            String contentTypeHeader = response.getHeaders().get("Content-Type");
            String mimeType = null;
            Charset charset = null;
            if (StringUtils.isNotBlank(contentTypeHeader)) {
                ContentType contentType = ContentType.parse(contentTypeHeader);
                mimeType = contentType.getMimeType();
                charset = contentType.getCharset();
            }

            byte[] parseContent = WebUtils.parseContent(response.getHeaders(), response.getContent());
            if (StringUtils.isNotBlank(mimeType) && mimeType.startsWith("image/")) {
                feService.renderImage(RESP_DETAIL, parseContent);
            } else {
                String contentStr = "";
                contentStr = new String(parseContent, charset == null ? StandardCharsets.UTF_8: charset);
                feService.renderData(RESP_DETAIL, contentStr);
            }
        } catch (Exception e) {
            log.error("Error rendering response content.", e);
            feService.renderData(RESP_DETAIL, ERROR_DATA);
        }
    }


    private String parseHeaders(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        headers.forEach((key, value) -> buffer.append(key).append(":\s").append(value).append("\n"));
        return buffer.toString();
    }
}
