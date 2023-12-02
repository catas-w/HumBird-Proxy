package com.catas.wicked.proxy.service;

import com.catas.wicked.common.bean.message.BaseMessage;
import com.catas.wicked.common.bean.message.RenderMessage;
import com.catas.wicked.common.bean.message.RequestMessage;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.util.ThreadPoolService;
import com.catas.wicked.proxy.gui.controller.DetailTabController;
import com.catas.wicked.proxy.gui.controller.DetailWebViewController;
import com.catas.wicked.proxy.render.TabRenderer;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.ehcache.Cache;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


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

    @Named("request")
    @Inject
    private TabRenderer requestTabRenderer;

    @Named("response")
    @Inject
    private TabRenderer responseTabRenderer;

    @Named("overView")
    @Inject
    private TabRenderer overViewTabRenderer;

    @Named("timing")
    @Inject
    private TabRenderer timingTabRenderer;

    private Map<RenderMessage.Tab, TabRenderer> renderFuncMap;

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
        renderFuncMap.put(RenderMessage.Tab.REQUEST, requestTabRenderer);
        renderFuncMap.put(RenderMessage.Tab.RESPONSE, responseTabRenderer);
        renderFuncMap.put(RenderMessage.Tab.OVERVIEW, overViewTabRenderer);
        renderFuncMap.put(RenderMessage.Tab.TIMING, timingTabRenderer);

        ThreadPoolService.getInstance().run(() -> {
            while (!appConfig.getShutDownFlag().get()) {
                try {
                    BaseMessage msg = getMsg();
                    if (msg instanceof RenderMessage renderMsg) {
                        // System.out.println(renderMsg);
                        TabRenderer renderer = renderFuncMap.get(renderMsg.getTab());
                        if (renderer != null) {
                            renderer.render(renderMsg);
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
        System.out.println("-----requestId: " + requestId + "-----");

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
}
