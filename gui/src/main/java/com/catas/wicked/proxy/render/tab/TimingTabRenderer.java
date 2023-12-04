package com.catas.wicked.proxy.render.tab;

import com.catas.wicked.common.bean.message.RenderMessage;
import com.catas.wicked.common.bean.message.RequestMessage;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.proxy.gui.controller.DetailTabController;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.ehcache.Cache;

@Singleton
public class TimingTabRenderer extends AbstractTabRenderer {

    @Inject
    private DetailTabController detailTabController;

    @Inject
    private Cache<String, RequestMessage> requestCache;


    @Inject
    private ApplicationConfig appConfig;

    @Override
    public void render(RenderMessage renderMsg) {
        // System.out.println("--render timing --");
        detailTabController.getTimingMsgLabel().setVisible(renderMsg.isEmpty());
        if (renderMsg.isEmpty()) {
            return;
        }
        RequestMessage request = requestCache.get(renderMsg.getRequestId());
        // TODO
    }
}
