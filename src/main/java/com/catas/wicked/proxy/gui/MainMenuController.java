package com.catas.wicked.proxy.gui;

import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXPopup;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

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

    private JFXPopup proxyPopup;

    private Parent proxyScene;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            proxyScene = FXMLLoader.load(getClass().getResource("/fxml/proxy-settings.fxml"));
        } catch (IOException ioExc) {
            ioExc.printStackTrace();
        }
        proxySetting.setOnMouseClicked(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            // alert.getDialogPane().setContent(proxySetting);
            alert.setGraphic(proxyScene);
            alert.setTitle("Information Dialog");
            alert.setHeight(proxySetting.getHeight());
            alert.setWidth(proxySetting.getWidth());
            alert.getButtonTypes().clear();
            alert.showAndWait();
        });
    }
}
