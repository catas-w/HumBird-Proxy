package com.catas.wicked.proxy.gui;

import com.jfoenix.controls.JFXProgressBar;
import de.felixroske.jfxsupport.SplashScreen;
import javafx.scene.Parent;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class CustomLoadingView extends SplashScreen {

    @Override
    public String getImagePath() {
        return "/image/start.jpg";
    }

    @Override
    public Parent getParent() {
        final ImageView imageView = new ImageView(getClass().getResource(getImagePath()).toExternalForm());
        final ProgressBar splashProgressBar = new ProgressBar();
        splashProgressBar.setPrefWidth(imageView.getImage().getWidth());
        splashProgressBar.getStyleClass().add("start-progress-bar");
        splashProgressBar.setProgress(-1.0f);
        final VBox vbox = new VBox();
        vbox.getChildren().addAll(imageView, splashProgressBar);
        vbox.getStylesheets().add(getClass().getResource("/css/loading-view.css").toExternalForm());

        return vbox;
    }

}
