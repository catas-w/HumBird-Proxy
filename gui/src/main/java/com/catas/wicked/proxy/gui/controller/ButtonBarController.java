package com.catas.wicked.proxy.gui.controller;

import com.catas.wicked.common.bean.message.DeleteMessage;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.pipeline.MessageQueue;
import com.catas.wicked.common.pipeline.Topic;
import com.catas.wicked.proxy.service.RequestMockService;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXToggleNode;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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
    public JFXToggleNode recordBtn;
    public JFXToggleNode sslBtn;
    @FXML
    public JFXButton removeAllBtn;
    @FXML
    private MenuButton mainMenuButton;
    @FXML
    private MenuItem proxySetting;

    private Dialog proxyConfigDialog;

    @Inject
    private MessageQueue messageQueue;

    @Inject
    private ApplicationConfig appConfig;

    @Inject
    private RequestMockService requestMockService;

    @SneakyThrows
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // proxy setting dialog
        bindProxySettingBtn();

        // toggle record button
        recordBtn.selectedProperty().addListener(((observable, oldValue, newValue) -> {
            FontIcon icon = (FontIcon) recordBtn.getGraphic();
            if (newValue) {
                icon.setIconLiteral("fas-record-vinyl");
                icon.setIconColor(Color.valueOf("#ec2222"));
            } else {
                icon.setIconLiteral("far-play-circle");
                icon.setIconColor(Color.valueOf("#616161"));
            }
            appConfig.setRecording(newValue);
        }));

        // toggle handle ssl button
        sslBtn.selectedProperty().addListener(((observable, oldValue, newValue) -> {
            FontIcon icon = (FontIcon) sslBtn.getGraphic();
            String color = newValue ? BTN_ACTIVE : BTN_INACTIVE;
            icon.setIconColor(Color.valueOf(color));
            appConfig.setHandleSsl(newValue);
        }));
    }

    public void mockTreeItem() {
        markerBtn.setOnAction(event -> {
            requestMockService.mockRequest();
        });
    }

    /**
     * delete all requests
     */
    public void deleteAll() {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setRemoveAll(true);
        messageQueue.pushMsg(Topic.RECORD, deleteMessage);
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

}
