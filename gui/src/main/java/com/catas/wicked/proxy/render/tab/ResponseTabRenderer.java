package com.catas.wicked.proxy.render.tab;

import com.catas.wicked.common.bean.message.RenderMessage;
import com.catas.wicked.common.bean.message.RequestMessage;
import com.catas.wicked.common.bean.message.ResponseMessage;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.util.WebUtils;
import com.catas.wicked.proxy.gui.componet.SideBar;
import com.catas.wicked.proxy.gui.controller.DetailTabController;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.ehcache.Cache;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Singleton
public class ResponseTabRenderer extends AbstractTabRenderer {

    @Inject
    private DetailTabController detailTabController;

    @Inject
    private Cache<String, RequestMessage> requestCache;


    @Inject
    private ApplicationConfig appConfig;

    @Override
    public void render(RenderMessage renderMsg) {
        // System.out.println("-- render response --");
        detailTabController.getRespHeaderMsgLabel().setVisible(renderMsg.isEmpty());
        detailTabController.getRespContentMsgLabel().setVisible(renderMsg.isEmpty());
        if (renderMsg.isEmpty()) {
            // System.out.println("--Empty response--");
            return;
        }
        RequestMessage request = requestCache.get(renderMsg.getRequestId());
        displayResponse(request.getResponse());
    }

    public void displayResponse(ResponseMessage response) {
        if (response == null) {
            detailTabController.getRespContentArea().replaceText("<Waiting For Response...>");
            return;
        }
        // headers
        Map<String, String> headers = response.getHeaders();
        renderHeaders(headers, detailTabController.getRespHeaderTable());
        detailTabController.getRespHeaderArea().replaceText(WebUtils.getHeaderText(headers));

        ContentType contentType = WebUtils.getContentType(headers);
        byte[] parsedContent = WebUtils.parseContent(response.getHeaders(), response.getContent());
        if (parsedContent.length == 0) {
            detailTabController.getRespContentMsgLabel().setVisible(true);
            detailTabController.getRespDataPane().setExpanded(false);
            return;
        }
        // detailTabController.getRespContentMsgLabel().setVisible(false);
        if (contentType != null && contentType.getMimeType().startsWith("image/")) {
            detailTabController.getRespContentArea().setVisible(false);
            detailTabController.getRespImageView().setVisible(true);
            // TODO webp格式
            detailTabController.getRespImageView().setImage(new ByteArrayInputStream(parsedContent));
        } else {
            detailTabController.getRespContentArea().setVisible(true);
            detailTabController.getRespImageView().setVisible(false);
            Charset charset = contentType != null && contentType.getCharset() != null ?
                    contentType.getCharset() : StandardCharsets.UTF_8;
            String contentStr = new String(parsedContent, charset);
            detailTabController.getRespContentArea().replaceText(contentStr);
        }
        SideBar.Strategy strategy = predictCodeStyle(contentType);
        log.info("Response predict contentType: {}, strategy: {}", contentType.getMimeType(), strategy);
        detailTabController.getRespSideBar().setStrategy(strategy);
    }
}
