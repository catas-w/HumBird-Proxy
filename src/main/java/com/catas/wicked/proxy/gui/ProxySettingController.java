package com.catas.wicked.proxy.gui;

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class ProxySettingController implements Initializable {

    public JFXButton proxyCancelBtn;
    public JFXButton proxySaveBtn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        proxySaveBtn.setOnAction(e -> {
            // TODO: save settings
        });

        proxyCancelBtn.setOnAction(e -> {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    ((JFXButton)e.getSource()).getScene().getWindow().hide();
                }
            });
        });
    }
}
