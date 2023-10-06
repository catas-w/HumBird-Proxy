package com.catas.wicked.proxy.gui.controller;

import com.catas.wicked.common.bean.message.RequestMessage;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.pipeline.MessageQueue;
import com.catas.wicked.common.util.IdUtil;
import com.jfoenix.controls.JFXButton;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Window;
import lombok.SneakyThrows;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import static com.catas.wicked.common.constant.StyleConstant.BTN_ACTIVE;
import static com.catas.wicked.common.constant.StyleConstant.BTN_INACTIVE;

@Singleton
public class ButtonBarController implements Initializable {

    public JFXButton markerBtn;
    public JFXButton eyeBtn;
    public JFXButton recordBtn;
    public JFXButton sslBtn;
    @FXML
    private MenuButton mainMenuButton;
    @FXML
    private MenuItem proxySetting;

    private Dialog proxyConfigDialog;

    @Inject
    private MessageQueue queue;

    @Inject
    private ApplicationConfig appConfig;

    private int index = 0;

    @Inject
    private DetailWebViewController webViewController;

    @SneakyThrows
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // proxy setting dialog
        bindProxySettingBtn();
        testTreeItem();
        testWebview();
    }

    private void testWebview() {
        eyeBtn.setOnAction(event -> {
            WebView webView = webViewController.getDetailWebView();
            WebEngine engine = webView.getEngine();
//            engine.executeScript("initTestData()");
            String content = "{   \"name\": \"JacK\",\"age\": 23,\"Num\": 23}";
            String cmd = String.format("initData('collapseDetail', '%s')", content);
            engine.executeScript(cmd);
        });
    }

    private void testTreeItem() {
        ArrayList<String> list = new ArrayList<>();
        list.add("GET https://www.google.com/index/1");
        list.add("GET https://www.google.com/index/2");
        list.add("GET https://www.google.com/index/page/1");
        list.add("POST https://www.google.com/index/page/2");
        list.add("POST https://www.google.com/index/page/3?name=111&age=222&host=333.3");
        list.add("GET https://www.amzaon.com/home");
        list.add("PUT https://www.google.com/page");
        list.add("DELETE https://www.google.com/home/deftail/2");
        list.add("GET https://www.google.com/home/deftail/2?name=jack&host=local");
        list.add("DELETE https://www.amazon.com");
        list.add("PUT https://www.bing.com/index");
        list.add("POST https://www.bing.com/home");
        list.add("POST https://www.microsoft.com/search");
        list.add("GET https://www.microsoft.com/lolo");
        list.add("GET https://www.bing.com");

        markerBtn.setOnAction(event -> {
            String url = list.get(index % (list.size() - 1));
            String[] split = url.split(" ");
            RequestMessage msg = new RequestMessage(split[1]);
            msg.setRequestId(IdUtil.getId());
            msg.setMethod(split[0]);

            queue.pushMsg(msg);
            index ++;
        });
    }

    private void bindProxySettingBtn() {
        try {
            Parent proxyScene = FXMLLoader.load(getClass().getResource("/fxml/proxy-settings.fxml"));
            proxyConfigDialog = new Dialog<>();
            proxyConfigDialog.setTitle("Proxy Config");
            DialogPane dialogPane = proxyConfigDialog.getDialogPane();
            dialogPane.setContent(proxyScene);
            dialogPane.getStylesheets().add(
                    getClass().getResource("/css/dialog.css").toExternalForm());
            dialogPane.getStyleClass().add("myDialog");
            Window window = dialogPane.getScene().getWindow();
            window.setOnCloseRequest(e -> window.hide());
        } catch (IOException ioExc) {
            ioExc.printStackTrace();
        }

        proxySetting.setOnAction(e -> {
            proxyConfigDialog.showAndWait();
        });
    }

    public void handleSSlBtn(ActionEvent actionEvent) {
        Node graphic = sslBtn.getGraphic();
        toggleBtnColor(graphic);
        appConfig.setHandleSsl(!appConfig.isHandleSsl());
    }

    public void handleRecordBtn(ActionEvent actionEvent) {
        Node graphic = recordBtn.getGraphic();
        toggleBtnColor(graphic);
        appConfig.setRecording(!appConfig.isRecording());
    }

    private void toggleBtnColor(Node graphic) {
        FontIcon icon = (FontIcon) graphic;
        Color iconColor = (Color) icon.getIconColor();

        String toColor = BTN_INACTIVE;
        if (iconColor.equals(Color.valueOf(toColor))) {
            toColor = BTN_ACTIVE;
        }
        String finalToColor = toColor;
        Platform.runLater(() -> {
            icon.setIconColor(Color.valueOf(finalToColor));
        });
    }
}
