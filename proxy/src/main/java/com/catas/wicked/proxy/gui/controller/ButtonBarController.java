package com.catas.wicked.proxy.gui.controller;

import com.catas.wicked.proxy.bean.MessageEntity;
import com.catas.wicked.proxy.message.MessageQueue;
import com.jfoenix.controls.JFXButton;
import de.felixroske.jfxsupport.FXMLController;
import io.netty.handler.codec.http.HttpMethod;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.stage.Window;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

@FXMLController
public class ButtonBarController implements Initializable {

    public JFXButton markerBtn;
    public JFXButton eyeBtn;
    @FXML
    private MenuButton mainMenuButton;
    @FXML
    private MenuItem proxySetting;

    private Dialog proxyConfigDialog;

    @Autowired
    private MessageQueue queue;

    private int index = 0;

    @SneakyThrows
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // proxy setting dialog
        bindProxySettingBtn();
        testTreeItem();
    }

    private void testTreeItem() {
        ArrayList<String> list = new ArrayList<>();
        list.add("https://www.google.com/index/page/1");
        list.add("https://www.google.com/index/page/2");
        list.add("https://www.google.com/index/page/3");
        list.add("https://www.amzaon.com/home");
        list.add("https://www.google.com/page");
        list.add("https://www.google.com/home/deftail/2");
        list.add("https://www.google.com/home/deftail/2?name=jack&host=local");
        list.add("https://www.amazon.com");
        list.add("https://www.bing.com");

        markerBtn.setOnAction(event -> {
            String url = list.get(index++);
            try {
                MessageEntity msg = new MessageEntity(url);
                msg.setContentType(HttpMethod.POST.name());
                queue.pushMsg(msg);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            if (index >= list.size()) {
                index = 0;
            }
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
}
