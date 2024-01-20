package com.catas.wicked.proxy.render.tab;

import com.catas.wicked.common.bean.message.RenderMessage;
import com.catas.wicked.common.bean.message.RequestMessage;
import com.catas.wicked.common.bean.message.ResponseMessage;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.util.ImageUtils;
import com.catas.wicked.common.util.WebUtils;
import com.catas.wicked.proxy.gui.componet.SideBar;
import com.catas.wicked.proxy.gui.controller.DetailTabController;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.ehcache.Cache;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
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
            setEmptyMsgLabel(detailTabController.getRespHeaderMsgLabel());
            setEmptyMsgLabel(detailTabController.getRespContentMsgLabel());
            return;
        }
        RequestMessage request = requestCache.get(renderMsg.getRequestId());
        displayResponse(request.getResponse());
    }

    public void displayResponse(ResponseMessage response) {
        if (response == null) {
            detailTabController.getRespContentArea().replaceText("<Waiting For Response...>");
            setMsgLabel(detailTabController.getRespContentMsgLabel(), "<Waiting for response...>");
            return;
        }
        // headers
        Map<String, String> headers = response.getHeaders();
        renderHeaders(headers, detailTabController.getRespHeaderTable());
        detailTabController.getRespHeaderArea().replaceText(WebUtils.getHeaderText(headers), true);

        ContentType contentType = WebUtils.getContentType(headers);
        SideBar.Strategy strategy = predictCodeStyle(contentType);
        // log.info("Response predict contentType: {}, strategy: {}", contentType.getMimeType(), strategy);
        detailTabController.getRespSideBar().setStrategy(strategy);

        byte[] parsedContent = WebUtils.parseContent(response.getHeaders(), response.getContent());
        if (parsedContent.length == 0) {
            detailTabController.getRespContentMsgLabel().setVisible(true);
            // detailTabController.getRespDataPane().setExpanded(false);
            return;
        }

        if (strategy == SideBar.Strategy.IMG) {
            detailTabController.getRespContentArea().setVisible(false);
            detailTabController.getRespImageView().setVisible(true);
            InputStream inputStream = new ByteArrayInputStream(parsedContent);
            try {
                // webp format
                if (StringUtils.equals(contentType.getMimeType(), "image/webp")) {
                    BufferedImage encodeWebpImage = ImageUtils.encodeWebpImage(inputStream);
                    detailTabController.getRespImageView().setImage(ImageUtils.getJFXImage(encodeWebpImage));
                } else {
                    detailTabController.getRespImageView().setImage(inputStream);
                }
            } catch (Exception e) {
                setMsgLabel(detailTabController.getRespContentMsgLabel(),
                        "Image load error, type: " + contentType.getMimeType());
            }
        } else {
            detailTabController.getRespContentArea().setVisible(true);
            detailTabController.getRespImageView().setVisible(false);
            Charset charset = contentType != null && contentType.getCharset() != null ?
                    contentType.getCharset() : StandardCharsets.UTF_8;
            String contentStr = new String(parsedContent, charset);
            detailTabController.getRespContentArea().replaceText(contentStr, true);
        }
    }
}
