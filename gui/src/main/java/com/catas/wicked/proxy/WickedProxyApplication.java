package com.catas.wicked.proxy;

import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.util.AppContextUtil;
import com.catas.wicked.proxy.gui.ApplicationView;
import com.catas.wicked.proxy.gui.CustomLoadingView;
import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Collection;

@Slf4j
@SpringBootApplication(scanBasePackages = {"com.catas.wicked.proxy", "com.catas.wicked.common", "com.catas.wicked.server"})
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

    @Override
    public void stop() throws Exception {
        log.info("---- Stopping ----");
        ApplicationConfig appConfig = AppContextUtil.getBean(ApplicationConfig.class);
        appConfig.shutDownApplication();
    }
}
