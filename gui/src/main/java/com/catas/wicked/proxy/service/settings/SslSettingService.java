package com.catas.wicked.proxy.service.settings;

import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.config.Settings;
import com.catas.wicked.proxy.gui.componet.CertSelectComponent;
import com.catas.wicked.proxy.gui.controller.SettingController;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;

import java.util.List;

public class SslSettingService extends AbstractSettingService {

    private final SettingController settingController;
    private final ToggleGroup certSelectGroup = new ToggleGroup();

    public SslSettingService(SettingController settingController) {
        this.settingController = settingController;
    }

    @Override
    public void init() {
        // bugfix: make disable-listener work
        settingController.getSslBtn().setSelected(true);

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

        // init certificates
        CertSelectComponent defaultCert = new CertSelectComponent("Built-in",  "_default_", "fas-download");
        defaultCert.setToggleGroup(certSelectGroup);

        CertSelectComponent cert1 = new CertSelectComponent("Cert01", "#001", "fas-trash-alt");
        cert1.setAlertLabel("Certificate is not installed!");
        cert1.setOperateEvent(actionEvent -> System.out.println("clicked cert01"));
        cert1.setToggleGroup(certSelectGroup);

        CertSelectComponent cert2 = new CertSelectComponent("Cert02", "#002", "fas-trash-alt");
        cert2.setToggleGroup(certSelectGroup);

        // add cert components
        defaultCert.setSelected(true);
        List<CertSelectComponent> certList = List.of(defaultCert, cert1, cert2);
        settingController.setSelectableCert(certList);

        // exclude list
        settingController.getSslExcludeArea().setText(getTextFromList(settings.getSslExcludeList()));
    }

    @Override
    public void update(ApplicationConfig appConfig) {
        Settings settings = appConfig.getSettings();
        settings.setHandleSsl(settingController.getSslBtn().isSelected());

        // update selected cert
        CertSelectComponent.CertRadioButton selectedToggle = (CertSelectComponent.CertRadioButton) certSelectGroup.getSelectedToggle();
        System.out.println("Selected: " + selectedToggle.getCertId());
        settings.setSslExcludeList(getListFromText(settingController.getSslExcludeArea().getText()));
    }
}
