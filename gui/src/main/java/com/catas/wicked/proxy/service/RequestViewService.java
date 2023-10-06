package com.catas.wicked.proxy.service;

import com.catas.wicked.common.bean.FeRequestInfo;
import com.catas.wicked.common.bean.message.RequestMessage;
import com.catas.wicked.common.bean.message.ResponseMessage;
import com.catas.wicked.common.util.WebUtils;
import com.catas.wicked.proxy.gui.controller.DetailTabController;
import com.catas.wicked.proxy.gui.controller.DetailWebViewController;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.scene.web.WebEngine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.ehcache.Cache;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;


/**
 * Update gui of tab-pane
 */
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

    private static final String REQ_HEADER = "requestHeaders";
    private static final String REQ_DETAIL = "requestDetail";
    private static final String RESP_HEADER = "responseHeaders";
    private static final String RESP_DETAIL = "responseDetail";

    private static final String ERROR_DATA = "<Error loading data>";

    public void updateView(String requestId) {
        if (requestId == null || requestId.equals(currentRequestId)) {
            return;
        }
        this.currentRequestId = requestId;
        RequestMessage request = requestCache.get(requestId);
        ResponseMessage response = request.getResponse();

        // Media-type
        WebEngine webEngine = detailWebViewController.getDetailWebView().getEngine();
        feService.setWebEngine(webEngine);

        updateOverviewTab(request, response);
        updateRequestTab(request);
        updateResponseTab(response);
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
