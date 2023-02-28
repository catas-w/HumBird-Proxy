package com.catas.wicked.proxy;

import com.catas.wicked.proxy.gui.ApplicationView;
import com.catas.wicked.proxy.gui.CustomLoadingView;
import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class WickedProxyApplication extends AbstractJavaFxApplicationSupport {

    public static void main(String[] args) {
        launch(WickedProxyApplication.class, ApplicationView.class, new CustomLoadingView(), args);
    }


    // @Override
    // public void beforeInitialView(Stage stage, ConfigurableApplicationContext ctx) {
    //     stage.initStyle(StageStyle.UNDECORATED);
    // }

}
