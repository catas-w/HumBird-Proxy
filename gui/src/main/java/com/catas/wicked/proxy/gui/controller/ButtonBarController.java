package com.catas.wicked.proxy.gui.controller;

import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.pipeline.MessageQueue;
import com.catas.wicked.proxy.service.RequestMockService;
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
import javafx.stage.Window;
import lombok.SneakyThrows;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
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
    public JFXButton removeAllBtn;
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

    @Inject
    private RequestMockService requestMockService;

    @SneakyThrows
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // proxy setting dialog
        bindProxySettingBtn();
    }

    public void mockTreeItem() {
        markerBtn.setOnAction(event -> {
            requestMockService.mockRequest();
        });
    }

    public void deleteAll() {
        System.out.println("Delete All");
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
