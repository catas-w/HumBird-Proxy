package com.catas.wicked.proxy.render.tab;

import com.catas.wicked.common.bean.message.RenderMessage;
import com.catas.wicked.common.bean.message.RequestMessage;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.proxy.gui.controller.DetailTabController;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.Cache;

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
            System.out.println("--empty overview--");
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
        String code = request.getResponse() == null ? "Waiting" : String.valueOf(request.getResponse().getStatus());

        String cont = title + "\n" + code;

        Platform.runLater(() -> {
            // requestRenderer.renderContent(cont, overviewArea);
            detailTabController.getOverviewArea().replaceText(cont);
        });
    }
}
