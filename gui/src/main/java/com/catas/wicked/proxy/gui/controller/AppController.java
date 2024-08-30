package com.catas.wicked.proxy.gui.controller;

import jakarta.inject.Singleton;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import lombok.Getter;

import java.net.URL;
import java.util.ResourceBundle;

@Singleton
public class AppController implements Initializable {

    @FXML
    private VBox detailTabPane;

    @FXML
    @Getter
    private VBox rootVBox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
