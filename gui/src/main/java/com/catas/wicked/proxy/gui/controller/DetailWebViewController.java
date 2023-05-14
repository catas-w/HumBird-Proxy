package com.catas.wicked.proxy.gui.controller;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.Initializable;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.ResourceBundle;

import static org.apache.commons.lang3.Validate.notNull;

@FXMLController
public class DetailWebViewController implements Initializable {
    public WebView detailWebView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        WebEngine engine = detailWebView.getEngine();
        URL link = getClass().getResource("/html/request-detail.html");
        notNull(link);
        engine.load(link.toExternalForm());
//        engine.setUserStyleSheetLocation();
//        engine.load("http://v4.bootcss.com/docs/components/pagination/");
    }
}
