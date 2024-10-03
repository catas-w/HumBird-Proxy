package com.catas.wicked.proxy.service.settings;

import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.config.CertificateConfig;
import com.catas.wicked.common.config.Settings;
import com.catas.wicked.common.provider.CertManageProvider;
import com.catas.wicked.proxy.gui.componet.CertSelectComponent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Singleton
public class SslSettingService extends AbstractSettingService {

    private final ToggleGroup certSelectGroup = new ToggleGroup();

    @Inject
    private CertManageProvider certManager;

    @Override
    public void init() {
        // bugfix: make disable-listener work
        settingController.getSslBtn().setSelected(true);

        settingController.getSslBtn().selectedProperty().addListener(((observable, oldValue, newValue) -> {
            // settingController.getDefaultCertRadio().setDisable(!newValue);
            // settingController.getCustomCertRadio().setDisable(!newValue);
            // settingController.getSelectCertBtn().setDisable(!newValue);
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
        List<CertificateConfig> certConfigs = certManager.getCertList();
        List<CertSelectComponent> certList = new ArrayList<>();
        String selectedCertId = appConfig.getSettings().getSelectedCert();
        for (CertificateConfig config : certConfigs) {
            String iconStr = config.isDefault() ? "fas-download": "fas-trash-alt";
            CertSelectComponent component = new CertSelectComponent(config.getName(), config.getId(), iconStr);
            component.setToggleGroup(certSelectGroup);
            component.setPreviewEvent(actionEvent -> System.out.println("preview"));

            if (StringUtils.equals(selectedCertId, config.getId())) {
                component.setSelected(true);
            }
            if (config.isDefault()) {
                if (StringUtils.isBlank(selectedCertId)) {
                    component.setSelected(true);
                }
                component.setOperateEvent(actionEvent -> System.out.println("download cert"));
            } else {
                component.setAlertLabel("Certificate is not installed!");
                component.setOperateEvent(actionEvent -> System.out.println("clicked cert01"));
            }

            certList.add(component);
        }
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
