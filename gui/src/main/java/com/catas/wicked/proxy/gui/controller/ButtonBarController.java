package com.catas.wicked.proxy.gui.controller;

import com.catas.wicked.common.bean.MessageEntity;
import com.catas.wicked.common.pipeline.MessageQueue;
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
        list.add("GET https://www.google.com/index/page/1");
        list.add("GET https://www.google.com/index/page/2");
        list.add("POST https://www.google.com/index/page/3");
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
            try {
                String[] split = url.split(" ");
                MessageEntity msg = new MessageEntity(split[1]);
                msg.setMethod(new HttpMethod(split[0]));

                queue.pushMsg(msg);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
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
}
