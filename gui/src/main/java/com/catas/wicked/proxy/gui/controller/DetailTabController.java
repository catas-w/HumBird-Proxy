package com.catas.wicked.proxy.gui.controller;

import com.jfoenix.controls.JFXTextArea;
import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TitledPane;

import java.net.URL;
import java.util.ResourceBundle;

@FXMLController
public class DetailTabController implements Initializable {
    @FXML
    private TitledPane reqOtherPane;
    @FXML
    private TitledPane reqPayloadPane;
    @FXML
    private TitledPane respHeaderPane;
    @FXML
    private TitledPane respDataPane;
    @FXML
    private TitledPane reqHeaderPane;
    @FXML
    private JFXTextArea reqHeaderText;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        reqHeaderText.setText(":authority: www.javaroad.cn\n" +
                ":method: GET\n" +
                ":path: /questions/87133\n" +
                ":scheme: https\n" +
                "accept: text/html,application/xhtml+xml,application/xml;q=0.9d-exchange;v=b3;q=0.7\n" +
                "accept-encoding: gzip, deflate, br\n" +
                "accept-language: zh-CN,zh;q=0.9,en;q=0.8\n" +
                "cache-control: max-age=0\n" +
                "cookie: user_device_id=22a5278e40a046558d230b95c023bc4b; user_device_id_timestamp=16769156b\n" +
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
                "user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/110.0.0.0 Safari/537.36");
        reqHeaderText.setEditable(false);

        addTitleListener(reqHeaderPane);
        addTitleListener(reqPayloadPane);
        addTitleListener(reqOtherPane);

        addTitleListener(respHeaderPane);
        addTitleListener(respDataPane);
    }

    private void addTitleListener(TitledPane pane) {
        pane.expandedProperty().addListener((observable, oldValue, newValue) -> {
            //make it fill space when expanded but not reserve space when collapsed
            if (newValue) {
                pane.maxHeightProperty().set(Double.POSITIVE_INFINITY);
            } else {
                pane.maxHeightProperty().set(Double.NEGATIVE_INFINITY);
            }
        });
    }
}
