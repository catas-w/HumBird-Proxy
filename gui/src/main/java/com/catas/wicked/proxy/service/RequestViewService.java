package com.catas.wicked.proxy.service;

import com.catas.wicked.common.bean.RequestMessage;
import com.catas.wicked.common.bean.ResponseMessage;
import com.catas.wicked.common.util.BrotliUtils;
import com.catas.wicked.common.util.GzipUtils;
import com.catas.wicked.proxy.gui.controller.DetailTabController;
import com.catas.wicked.proxy.gui.controller.DetailWebViewController;
import javafx.scene.web.WebEngine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.ehcache.Cache;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;


/**
 * Update gui of tab-pane
 */
@Slf4j
@Service
public class RequestViewService {

    @Resource
    private DetailTabController detailTabController;

    @Resource
    private DetailWebViewController webViewController;

    @Resource
    private Cache<String, RequestMessage> requestCache;

    private String currentRequestId;

    public void updateView(String requestId) {
        if (requestId == null || requestId.equals(currentRequestId)) {
            return;
        }
        this.currentRequestId = requestId;
        RequestMessage request = requestCache.get(requestId);
        ResponseMessage response = request.getResponse();

        // Media-type
        WebEngine webEngine = webViewController.getDetailWebView().getEngine();

        String reqHeaderStr = parseHeaders(request.getHeaders());
        byte[] reqBody = parseContent(request.getHeaders(), request.getBody());
        System.out.println("*** Request headers: " + reqHeaderStr);
        System.out.println("*** Request body: " + new String(reqBody));

        if (response == null) {
            System.out.println("=== Response waiting...");
        } else {
            String respHeaderStr = parseHeaders(response.getHeaders());
            byte[] respContent = parseContent(response.getHeaders(), response.getContent());
            System.out.println("*** Resp headers: " + respHeaderStr);
            System.out.println("*** Resp body: " + new String(respContent));
        }
    }

    private String parseHeaders(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        headers.entrySet().forEach(entry -> {
            buffer.append(entry.getKey()).append(":\s").append(entry.getValue());
        });
        return buffer.toString();
    }

    private byte[] parseContent(Map<String, String> headers, byte[] content) {
        if (content == null || content.length == 0) {
            return new byte[0];
        }
        String contentEncoding = null;
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if ("content-encoding".equals(entry.getKey().toLowerCase().strip())) {
                contentEncoding = entry.getValue().toLowerCase().strip();
                break;
            }
        }

        // content-encoding gzip,compress,deflate,br
        if (StringUtils.isNotBlank(contentEncoding)) {
            try {
                switch (contentEncoding) {
                    case "gzip" -> content = GzipUtils.decompress(content);
                    case "br" -> content = BrotliUtils.decompress(content);
                    case "deflate" -> content = GzipUtils.inflate(content);
                    default -> {
                    }
                }
            } catch (IOException e) {
                log.error("Content decompressFailed; {}", contentEncoding);
            }
        }
        return content;
    }
}
