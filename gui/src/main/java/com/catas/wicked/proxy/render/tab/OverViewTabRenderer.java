package com.catas.wicked.proxy.render.tab;

import com.catas.wicked.common.bean.message.RenderMessage;
import com.catas.wicked.common.bean.message.RequestMessage;
import com.catas.wicked.common.bean.message.ResponseMessage;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.util.WebUtils;
import com.catas.wicked.proxy.gui.controller.DetailTabController;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.Cache;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Singleton
public class OverViewTabRenderer extends AbstractTabRenderer {

    @Inject
    private DetailTabController detailTabController;

    @Inject
    private Cache<String, RequestMessage> requestCache;


    @Inject
    private ApplicationConfig appConfig;

    @Override
    public void render(RenderMessage renderMsg) {
        // System.out.println("-- render overview --");
        detailTabController.getOverViewMsgLabel().setVisible(renderMsg.isEmpty());
        if (renderMsg.isEmpty()) {
            return;
        }
        RequestMessage request = requestCache.get(renderMsg.getRequestId());
        displayOverView(request);
    }

    public void displayOverView(RequestMessage request) {
        String protocol = request.getProtocol();
        String url = request.getRequestUrl();
        String method = request.getMethod();
        String title = String.format("%s %s %s", protocol, url, method);

        Map<String, String> map = new LinkedHashMap<>();
        ResponseMessage response = request.getResponse();
        String code = response == null ? "Waiting" : String.valueOf(response.getStatus());
        map.put("Status", code);
        map.put("Protocol", protocol);
        map.put("Method", method);
        map.put("Remote Address", request.getRemoteAddress());
        map.put("Remote Port", String.valueOf(request.getRemotePort()));
        map.put("Local Address", request.getLocalAddress());
        map.put("Local Port", String.valueOf(request.getLocalPort()));
        map.put("Request Size", WebUtils.getHSize(request.getSize()));
        map.put("Response Size", response == null ? "" : WebUtils.getHSize(response.getSize()));
        map.put("Request Start", String.valueOf(request.getStartTime()));
        map.put("Request End", String.valueOf(request.getEndTime()));
        map.put("Response Start", response == null ? "" : String.valueOf(response.getStartTime()));
        map.put("Response End", response == null ? "" : String.valueOf(response.getEndTime()));

        String cont = title + "\n" + code + getContentStr(map);
        renderHeaders(map, detailTabController.getOverviewTable());

        Platform.runLater(() -> {
            detailTabController.getOverviewArea().replaceText(cont);
        });
    }

    private String getContentStr(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        map.forEach((key, value) -> builder.append(key).append(": ").append(value).append("\n"));
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }
}
