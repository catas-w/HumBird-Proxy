package com.catas.wicked.proxy.gui.controller;

import com.catas.wicked.proxy.WickedProxyApplication;
import jakarta.inject.Singleton;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.ResourceBundle;


@Singleton
public class DetailWebViewController implements Initializable {

    @FXML
    private WebView detailWebView;

    public WebView getDetailWebView() {
        return detailWebView;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        WebEngine engine = detailWebView.getEngine();

        // page loading
        engine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
            @Override
            public void changed(ObservableValue<? extends State> observableValue, State state, State t1) {

            }
        });

        URL link = WickedProxyApplication.class.getResource("/html/request-detail.html");
        engine.load(link.toExternalForm());
    }
}
