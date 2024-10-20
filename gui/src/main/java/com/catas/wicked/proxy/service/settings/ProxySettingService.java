package com.catas.wicked.proxy.service.settings;

import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.config.Settings;
import com.catas.wicked.common.constant.WorkerConstant;
import com.catas.wicked.common.util.AlertUtils;
import com.catas.wicked.common.util.WebUtils;
import jakarta.inject.Singleton;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class ProxySettingService extends AbstractSettingService{

    @Override
    public void init() {
        setIntegerStringConverter(settingController.getPortField(), 9624);
        addRequiredValidator(settingController.getPortField());

        // bugfix: make disable-listener work
        settingController.getSysProxyBtn().setSelected(true);
        settingController.getSysProxyBtn().selectedProperty().addListener(((observable, oldValue, newValue) -> {
            settingController.getSysProxyExcludeArea().setDisable(!newValue);
            Pane parent = (Pane) settingController.getSysProxyBtn().getParent();
            parent.getChildren().stream()
                    .filter(node -> node instanceof Label)
                    .skip(2)
                    .forEach(node -> {
                        Label labeled = (Label) node;
                        labeled.setDisable(!newValue);
                    });
        }));
    }

    @Override
    public void initValues(ApplicationConfig appConfig) {
        Settings settings = appConfig.getSettings();
        settingController.getPortField().setText(String.valueOf(settings.getPort()));
        settingController.getSysProxyBtn().setSelected(settings.isSystemProxy());
        settingController.getSysProxyExcludeArea().setText(getTextFromList(settings.getSysProxyBypassList()));
    }

    @Override
    public void update(ApplicationConfig appConfig) {
        Settings settings = appConfig.getSettings();
        int newPort = Integer.parseInt(settingController.getPortField().getText());
        int oldPort = settings.getPort();

        // restart server if port changed
        if (oldPort != newPort) {
            // check pot available
            if (!WebUtils.isPortAvailable(newPort)) {
                AlertUtils.alertWarning("Port " + newPort + " is unavailable");
                return;
            }
            settings.setPort(newPort);
            try {
                settingController.getProxyServer().shutdown();
                settingController.getProxyServer().start();
            } catch (Exception e) {
                log.error("Error in restarting proxy server.", e);
                AlertUtils.alertWarning("Port " + newPort + " is unavailable");
                settings.setPort(oldPort);
                settingController.getProxyServer().start();
                return;
            }
        }

        settings.setSystemProxy(settingController.getSysProxyBtn().isSelected());
        settings.setSysProxyBypassList(getListFromText(settingController.getSysProxyExcludeArea().getText()));

        // manually invoke systemProxyWorker
        settingController.getScheduledManager().invoke(WorkerConstant.SYS_PROXY_WORKER);
    }
}
