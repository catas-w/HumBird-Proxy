package com.catas.wicked.proxy.gui.controller;

import jakarta.inject.Singleton;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

@Singleton
public class AppController implements Initializable {

    public VBox detailTabPane;

    @FXML
    private HBox customTitleBar;

    private double xOffset = 0;
    private double yOffset = 0;
    private double titleBarHeight;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        customTitleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        // 监听ToolBar的鼠标拖动事件
        customTitleBar.setOnMouseDragged(event -> {
            // 更新窗口位置
            Window primaryStage = Stage.getWindows().get(0);
            primaryStage.setX(event.getScreenX() - xOffset);
            primaryStage.setY(event.getScreenY() - yOffset);
        });

        // rightBar.prefWidthProperty().bind(Bindings.createDoubleBinding(
        //         () -> detailTabPane.getWidth(), detailTabPane.widthProperty()
        // ));
    }

    public void hideCustomTitleBar() {
        if (customTitleBar == null) {
            return;
        }
        titleBarHeight = customTitleBar.getHeight();
        // customTitleBar.setPrefHeight(0);
        Timeline timeline = new Timeline();
        KeyValue keyValue = new KeyValue(customTitleBar.prefHeightProperty(), 0);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(200), keyValue);

        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

    public void showCustomTitleBar() {
        if (customTitleBar == null) {
            return;
        }
        // customTitleBar.setPrefHeight(titleBarHeight);
        Timeline timeline = new Timeline();
        KeyValue keyValue = new KeyValue(customTitleBar.prefHeightProperty(), titleBarHeight);
        KeyFrame keyFrame = new KeyFrame(Duration.millis(200), keyValue);

        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }
}
