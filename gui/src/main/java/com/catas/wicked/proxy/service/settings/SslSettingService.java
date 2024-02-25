package com.catas.wicked.proxy.service.settings;

import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.config.Settings;
import com.catas.wicked.proxy.gui.controller.SettingController;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;

public class SslSettingService extends AbstractSettingService {

    private final SettingController settingController;
    private final ToggleGroup certTypeGroup = new ToggleGroup();

    public SslSettingService(SettingController settingController) {
        this.settingController = settingController;
    }

    @Override
    public void init() {
        // final ToggleGroup group = new ToggleGroup();
        settingController.getDefaultCertRadio().setSelected(true);
        settingController.getDefaultCertRadio().setToggleGroup(certTypeGroup);
        settingController.getCustomCertRadio().setToggleGroup(certTypeGroup);

        settingController.getCustomCertRadio().selectedProperty().addListener(((observable, oldValue, newValue) -> {
            settingController.getSelectCertBtn().setDisable(!settingController.getSslBtn().isSelected() || !newValue);
        }));

        settingController.getSslBtn().selectedProperty().addListener(((observable, oldValue, newValue) -> {
            settingController.getDefaultCertRadio().setDisable(!newValue);
            settingController.getCustomCertRadio().setDisable(!newValue);
            settingController.getSelectCertBtn().setDisable(!newValue);
            settingController.getSslExcludeArea().setDisable(!newValue);

            Pane parent = (Pane) settingController.getSslBtn().getParent();
            parent.getChildren().stream()
                    .filter(node -> node instanceof Label)
                    .skip(1)
                    .forEach(node -> {
                        Label labeled = (Label) node;
                        labeled.setDisable(!newValue);
                    });
        }));
    }

    @Override
    public void initValues(ApplicationConfig appConfig) {
        Settings settings = appConfig.getSettings();
        settingController.getSslBtn().setSelected(settings.isHandleSsl());
        if (settings.getCertType() == Settings.CertType.BUILT_IN) {
            settingController.getDefaultCertRadio().setSelected(true);
        }  else {
            settingController.getCustomCertRadio().setSelected(true);
        }

        settingController.getSslExcludeArea().setText(getTextFromList(settings.getSslExcludeList()));
    }

    @Override
    public void update(ApplicationConfig appConfig) {
        Settings settings = appConfig.getSettings();
        settings.setHandleSsl(settingController.getSslBtn().isSelected());
        if (settingController.getDefaultCertRadio().isSelected()) {
            settings.setCertType(Settings.CertType.BUILT_IN);
        } else {
            settings.setCertType(Settings.CertType.CUSTOM);
        }
        settings.setSslExcludeList(getListFromText(settingController.getSslExcludeArea().getText()));
    }
}
