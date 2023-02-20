package com.catas.wicked.proxy.gui;

import com.jfoenix.controls.JFXTextArea;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.TreeView;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.net.URL;
import java.util.ResourceBundle;

public class AppController implements Initializable {

    @FXML
    private TreeView<String> reqTreeView;

    @FXML
    private JFXTextArea reqHeaderText;

    @FXML
    private JFXTextArea reqPayload;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        reqHeaderText.setText(":authority: www.javaroad.cn\n" +
                ":method: GET\n" +
                ":path: /questions/87133\n" +
                ":scheme: https\n" +
                "accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7\n" +
                "accept-encoding: gzip, deflate, br\n" +
                "accept-language: zh-CN,zh;q=0.9,en;q=0.8\n" +
                "cache-control: max-age=0\n" +
                "cookie: user_device_id=22a5278e40a046558d230b95c023bc4b; user_device_id_timestamp=1676912173861; cf_zaraz_google-analytics_2906=true; google-analytics_2906___ga=ee40da82-db43-421c-b639-7b8db5fd756b\n" +
                "dnt: 1\n" +
                "referer: https://www.bing.com/\n" +
                "sec-ch-ua: \"Chromium\";v=\"110\", \"Not A(Brand\";v=\"24\", \"Google Chrome\";v=\"110\"\n" +
                "sec-ch-ua-mobile: ?0\n" +
                "sec-ch-ua-platform: \"Windows\"\n" +
                "sec-fetch-dest: document\n" +
                "sec-fetch-mode: navigate\n" +
                "sec-fetch-site: cross-site\n" +
                "sec-fetch-user: ?1\n" +
                "upgrade-insecure-requests: 1\n" +
                "user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36");
        reqHeaderText.setEditable(false);
    }

    @FXML
    public void onHeaderInput(ActionEvent event) {

    }
}
