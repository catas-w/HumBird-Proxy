package com.catas.wicked.proxy.gui;

import com.jfoenix.controls.JFXTreeView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

public class AppController implements Initializable {

    @FXML
    private TreeView<String> reqTreeView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        TreeItem<String> rootNode = new TreeItem<String>("MyCompany Human Resources");
        rootNode.setExpanded(true);
        for (int i = 1; i < 6; i++) {
            TreeItem<String> item = new TreeItem<String> ("Message" + i);
            rootNode.getChildren().add(item);
        }
        ImageView imageView = new ImageView();
        reqTreeView = new TreeView<String>(rootNode);

    }
}
