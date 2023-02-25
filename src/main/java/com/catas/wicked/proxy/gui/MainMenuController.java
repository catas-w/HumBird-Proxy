package com.catas.wicked.proxy.gui;

import com.jfoenix.controls.JFXListView;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainMenuController implements Initializable {

    @FXML
    private JFXListView popupList;
    @FXML
    private Label proxySetting;
    @FXML
    private Label appSetting;
    @FXML
    private Label exitBtn;
    @FXML
    private Label aboutBtn;

    private Dialog proxyConfigDialog;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            Parent proxyScene = FXMLLoader.load(getClass().getResource("/fxml/proxy-settings.fxml"));
            proxyConfigDialog = new Dialog<>();
            proxyConfigDialog.setTitle("Proxy Config");
            DialogPane dialogPane = proxyConfigDialog.getDialogPane();
            dialogPane.setContent(proxyScene);
            proxyConfigDialog.setHeight(proxySetting.getHeight());
            proxyConfigDialog.setWidth(proxySetting.getWidth());
            dialogPane.getStylesheets().add(
                    getClass().getResource("/css/dialog.css").toExternalForm());
            dialogPane.getStyleClass().add("myDialog");
            Window window = dialogPane.getScene().getWindow();
            window.setOnCloseRequest(e -> window.hide());
        } catch (IOException ioExc) {
            ioExc.printStackTrace();
        }

        proxySetting.setOnMouseClicked(e -> {
            proxyConfigDialog.showAndWait();
        });
    }
}
