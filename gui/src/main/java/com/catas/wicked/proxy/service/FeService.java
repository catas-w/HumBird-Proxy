package com.catas.wicked.proxy.service;

import com.catas.wicked.common.bean.FeRequestInfo;
import javafx.scene.web.WebEngine;

public interface FeService {

    void setWebEngine(WebEngine webEngine);

    void renderData(String id, String content);

    void renderImage(String id, byte[] content);

    void setUrlTitle(FeRequestInfo feRequestInfo);
}
