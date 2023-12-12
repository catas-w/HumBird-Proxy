package com.catas.wicked.proxy.render.tab;

import com.catas.wicked.common.bean.message.RenderMessage;
import com.catas.wicked.common.bean.message.RequestMessage;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.util.WebUtils;
import com.catas.wicked.proxy.gui.componet.SideBar;
import com.catas.wicked.proxy.gui.controller.DetailTabController;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.ehcache.Cache;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Singleton
public class RequestTabRenderer extends AbstractTabRenderer {

    @Inject
    private DetailTabController detailTabController;

    @Inject
    private Cache<String, RequestMessage> requestCache;


    @Inject
    private ApplicationConfig appConfig;

    @Override
    public void render(RenderMessage renderMsg) {
        // System.out.println("-- render request --");
        detailTabController.getReqHeaderMsgLabel().setVisible(renderMsg.isEmpty());
        detailTabController.getReqContentMsgLabel().setVisible(renderMsg.isEmpty());
        if (renderMsg.isEmpty()) {
            return;
        }
        RequestMessage request = requestCache.get(renderMsg.getRequestId());
        displayRequest(request);
    }

    /**
     * exhibit request info
     */
    public void displayRequest(RequestMessage request) {
        if (request == null) {
            return;
        }

        // display headers
        Map<String, String> headers = request.getHeaders();
        renderHeaders(headers, detailTabController.getReqHeaderTable());
        detailTabController.getReqHeaderArea().replaceText(WebUtils.getHeaderText(headers));

        // display query-params if exist
        String query = request.getUrl().getQuery();
        Map<String, String> queryParams = WebUtils.parseQueryParams(query);
        if (!queryParams.isEmpty()) {
            StringBuilder queryBuilder = new StringBuilder();
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                queryBuilder.append(entry.getKey());
                queryBuilder.append(": ");
                queryBuilder.append(entry.getValue());
                queryBuilder.append("\n");
            }
            detailTabController.getReqParamArea().replaceText(queryBuilder.toString());
        }

        // display request content
        ContentType contentType = WebUtils.getContentType(headers);
        byte[] content = WebUtils.parseContent(request.getHeaders(), request.getBody());
        Node target = null;
        if (contentType != null && (ContentType.MULTIPART_FORM_DATA.getMimeType().equals(contentType.getMimeType()) ||
                ContentType.APPLICATION_FORM_URLENCODED.getMimeType().equals(contentType.getMimeType()))) {
            target = detailTabController.getReqContentTable();
        } else if (contentType != null && contentType.getMimeType().startsWith("image/")) {
            target = detailTabController.getReqImageView();
        } else {
            target = detailTabController.getReqPayloadCodeArea();
        }
        renderRequestContent(content, contentType, target);


        boolean hasQuery = !queryParams.isEmpty();
        boolean hasContent = content.length > 0;
        // System.out.printf("hasQuery: %s, hasContent: %s\n", hasQuery, hasContent);
        SingleSelectionModel<Tab> selectionModel = detailTabController.getReqPayloadTabPane().getSelectionModel();

        String title = "Payload";
        detailTabController.getReqContentMsgLabel().setVisible(false);
        if (hasQuery && hasContent) {
            detailTabController.getReqPayloadTabPane().setTabMaxHeight(20);
            detailTabController.getReqPayloadTabPane().setTabMinHeight(20);
        } else if (hasQuery) {
            selectionModel.clearAndSelect(1);
            detailTabController.getReqPayloadTabPane().setTabMaxHeight(0);
            title = "Query Parameters";
        } else if (hasContent) {
            selectionModel.clearAndSelect(0);
            detailTabController.getReqPayloadTabPane().setTabMaxHeight(0);
            title = "Content";
        } else {
            detailTabController.getReqPayloadTitlePane().setExpanded(false);
            detailTabController.getReqContentMsgLabel().setVisible(true);
        }

        String finalTitle = title;
        Platform.runLater(() -> {
            detailTabController.getReqPayloadTitlePane().setText(finalTitle);
        });
    }

    private void renderRequestContent(byte[] content, ContentType contentType, Node target) {
        if (target == null) {
            return;
        }
        target.setVisible(true);
        Parent parent = target.getParent();
        for (Node child : ((AnchorPane) parent).getChildren()) {
            if (!(child instanceof SideBar) && child != target) {
                child.setVisible(false);
            }
        }

        Charset charset = contentType != null && contentType.getCharset() != null ?
                contentType.getCharset() : StandardCharsets.UTF_8;
        if (target == detailTabController.getReqPayloadCodeArea()) {
            String contentStr = new String(content, charset);
            detailTabController.getReqPayloadCodeArea().replaceText(contentStr);
        } else if (target == detailTabController.getReqContentTable()) {
            assert contentType != null;
            if (StringUtils.equals(ContentType.APPLICATION_FORM_URLENCODED.getMimeType(), contentType.getMimeType())) {
                // parse url-encode
                Map<String, String> formData = WebUtils.parseQueryParams(new String(content, charset));
                renderHeaders(formData, detailTabController.getReqContentTable());
            } else {
                // parse multipart-form
                try {
                    Map<String, String> formData = WebUtils.parseMultipartForm(
                            content, contentType.getParameter("boundary"), charset);
                    renderHeaders(formData, detailTabController.getReqContentTable());
                } catch (IOException e) {
                    log.error("Error in parsing multipart-form data.", e);
                }
            }
        } else if (target == detailTabController.getReqImageView()) {
            detailTabController.getReqImageView().setImage(new ByteArrayInputStream(content));
        }

        SideBar.Strategy strategy = predictCodeStyle(contentType);
        // log.info("Request predict contentType: {}, strategy: {}", contentType.getMimeType(), strategy);
        detailTabController.getReqContentSideBar().setStrategy(strategy);
    }
}
