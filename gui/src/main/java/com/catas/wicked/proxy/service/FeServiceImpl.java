package com.catas.wicked.proxy.service;

import com.catas.wicked.common.bean.FeRequestInfo;
import javafx.scene.web.WebEngine;
import lombok.extern.slf4j.Slf4j;
import netscape.javascript.JSObject;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Slf4j
@Service("feService")
public class FeServiceImpl implements FeService{

    private WebEngine engine;

    private static final String IMG_PREFIX = "data:image/jpeg;base64,";

    @Override
    public void setWebEngine(WebEngine webEngine) {
        if (webEngine != null) {
            this.engine = webEngine;
        }
    }

    @Override
    public void renderData(String id, String content) {
        getWindow().call("renderData", id, content);
    }

    @Override
    public void renderImage(String id, byte[] content) {
        String encode = IMG_PREFIX + Base64.getEncoder().encodeToString(content);
        getWindow().call("renderImage", id, encode);
    }

    @Override
    public void setUrlTitle(FeRequestInfo feRequestInfo) {
        getWindow().call("setUrlTitle", feRequestInfo);
    }

    private JSObject getWindow() {
        return (JSObject) engine.executeScript("window");
    }
}
