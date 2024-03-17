package com.catas.wicked.proxy.service.settings;

import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.config.Settings;
import com.catas.wicked.common.constant.ThrottlePreset;
import com.catas.wicked.proxy.gui.controller.ButtonBarController;
import com.catas.wicked.proxy.gui.controller.SettingController;
import com.jfoenix.controls.JFXComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThrottleSettingService extends AbstractSettingService{

    private final SettingController settingController;
    private final ButtonBarController buttonBarController;

    public ThrottleSettingService(SettingController settingController,
                                  ButtonBarController buttonBarController) {
        this.settingController = settingController;
        this.buttonBarController = buttonBarController;
    }

    @Override
    public void init() {
        JFXComboBox<Labeled> throttleComboBox = settingController.getThrottleComboBox();
        for (ThrottlePreset preset : ThrottlePreset.values()) {
            throttleComboBox.getItems().add(new Label(preset.name()));
        }

        settingController.getThrottleBtn().selectedProperty().addListener(((observable, oldValue, newValue) -> {
            throttleComboBox.setDisable(!newValue);
        }));

        // settingController.getThrottleBtn().selectedProperty().bindBidirectional(
        //         buttonBarController.throttleBtn.selectedProperty());
    }

    @Override
    public void initValues(ApplicationConfig appConfig) {
        Settings settings = appConfig.getSettings();
        settingController.getThrottleBtn().setSelected(settings.isThrottle());

        ThrottlePreset preset = settings.getThrottlePreset();
        JFXComboBox<Labeled> throttleComboBox = settingController.getThrottleComboBox();
        throttleComboBox.setDisable(!settings.isThrottle());
        if (preset == null) {
            throttleComboBox.getSelectionModel().select(0);
        } else {
            throttleComboBox.getSelectionModel().select(preset.ordinal());
        }
    }

    @Override
    public void update(ApplicationConfig appConfig) {
        Settings settings = appConfig.getSettings();
        settings.setThrottle(settingController.getThrottleBtn().isSelected());

        Labeled selected = settingController.getThrottleComboBox().getSelectionModel().getSelectedItem();
        settings.setThrottlePreset(ThrottlePreset.valueOf(selected.getText()));

        // update throttle toggle button in homepage
        buttonBarController.throttleBtn.setSelected(settings.isThrottle());
    }
}
