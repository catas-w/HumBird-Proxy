package com.catas.wicked.proxy;

import com.catas.wicked.proxy.gui.ApplicationView;
import com.catas.wicked.proxy.gui.CustomLoadingView;
import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Collection;

@SpringBootApplication
public class WickedProxyApplication extends AbstractJavaFxApplicationSupport {

    public static void main(String[] args) {
        launch(WickedProxyApplication.class, ApplicationView.class, new CustomLoadingView(), args);
    }


    @Override
    public void beforeInitialView(Stage stage, ConfigurableApplicationContext ctx) {
        stage.initStyle(StageStyle.DECORATED);
    }

    @Override
    public Collection<Image> loadDefaultIcons() {
        return super.loadDefaultIcons();
    }

}
