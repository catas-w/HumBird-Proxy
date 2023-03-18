package com.catas.wicked.proxy.gui.controller;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@FXMLController
public class ButtonBarController implements Initializable {
    @FXML
    private MenuButton mainMenuButton;
    @FXML
    private MenuItem proxySetting;

    private Dialog proxyConfigDialog;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // proxy setting dialog
        bindProxySettingBtn();
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
