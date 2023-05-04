package com.catas.wicked.proxy.service;

import com.catas.wicked.common.bean.RequestMessage;
import com.catas.wicked.common.bean.ResponseMessage;
import com.catas.wicked.common.common.DetailArea;
import com.catas.wicked.proxy.gui.controller.DetailTabController;
import org.ehcache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Update gui of tab-pane
 */
@Service
public class RequestViewService {

    @Autowired
    private DetailTabController detailTabController;

    @Autowired
    private Cache<String, RequestMessage> requestCache;

    private String currentRequestId;

    public void updateView(String requestId) {
        if (requestId == null || requestId.equals(currentRequestId)) {
            return;
        }
        this.currentRequestId = requestId;
        RequestMessage request = requestCache.get(requestId);
        ResponseMessage response = request.getResponse();

        // TODO
        detailTabController.setRequestDetail(DetailArea.REQUEST_HEADER, request.getHeaders().toString());
        detailTabController.setRequestDetail(DetailArea.REQUEST_PAYLOAD, new String(request.getBody()));

        detailTabController.setRequestDetail(DetailArea.RESP_HEADER,
                response.getStatus() + "\n" + response.getHeaders().toString());
        detailTabController.setRequestDetail(DetailArea.RESP_CONTENT, new String(response.getContent()));
    }
}
