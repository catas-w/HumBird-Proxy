package com.catas.wicked.proxy;

import app.supernaut.fx.ApplicationDelegate;
import app.supernaut.fx.FxLauncher;
import app.supernaut.fx.fxml.FxmlLoaderFactory;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.constant.BlurOption;
import com.catas.wicked.common.jna.JnaUtils;
import com.catas.wicked.proxy.gui.controller.AppController;
import com.catas.wicked.proxy.message.MessageService;
import com.catas.wicked.server.proxy.ProxyServer;
import io.micronaut.context.annotation.Any;
import jakarta.inject.Inject;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;
import jakarta.inject.Singleton;

@Slf4j
// @Import(packages = {
//         "com.catas.wicked.server.proxy",
//         "com.catas.wicked.server.cert",
//         "com.catas.wicked.server.cert.spi",
//         "com.catas.wicked.server.handler.server"
// })
@Singleton
public class WickedProxyApplication implements ApplicationDelegate {

    @Inject
    private ApplicationConfig applicationConfig;

    @Inject
    private FxmlLoaderFactory loaderFactory;

    @Inject
    private MessageService messageService;

    @Any
    @Inject
    private ProxyServer proxyServer;

    @Inject
    private AppController appController;

    public static void main(String[] args) {
        FxLauncher.find().launch(args, WickedProxyApplication.class);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = loaderFactory.get(WickedProxyApplication.class.getResource("/fxml/application.fxml"));
        Parent root = loader.load();
        // Scene scene = new Scene(root, 1000, 680);
        Scene scene = new Scene(root, 1100, 750);
        scene.setFill(Color.TRANSPARENT);

        boolean useNative = true;
        if (useNative) {
            primaryStage.initStyle(StageStyle.UNIFIED);
            primaryStage.setOnShown((windowEvent -> {
                JnaUtils.setBlurWindow(JnaUtils.getNativeHandleOfStage(primaryStage), BlurOption.HIGH_CONTRAST_AQUA);
            }));
            primaryStage.fullScreenProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    appController.hideCustomTitleBar();
                } else {
                    appController.showCustomTitleBar();
                }
            });
        } else {
            appController.hideCustomTitleBar();
        }

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        log.info("---- Stopping Application ----");
        proxyServer.shutdown();
        applicationConfig.shutDownApplication();
    }
}
