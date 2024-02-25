package com.catas.wicked.proxy.service.settings;

import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.config.Settings;
import com.catas.wicked.proxy.gui.controller.SettingController;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

/**
 * setting service for general-page
 */
public class GeneralSettingService extends AbstractSettingService {

    private final SettingController settingController;

    public GeneralSettingService(SettingController settingController) {
        this.settingController = settingController;
    }

    @Override
    public void init() {
        settingController.getLanguageComboBox().getItems().add(new Label("English"));
        settingController.getLanguageComboBox().getItems().add(new Label("简体中文"));
        settingController.getLanguageComboBox().getSelectionModel().select(0);

        setIntegerStringConverter(settingController.getMaxSizeField(), 10);
        addRequiredValidator(settingController.getMaxSizeField());

        settingController.getRecordBtn().selectedProperty().addListener(((observable, oldValue, newValue) -> {
            settingController.getMaxSizeField().setDisable(!newValue);
            settingController.getRecordIncludeArea().setDisable(!newValue);
            settingController.getRecordExcludeArea().setDisable(!newValue);

            Pane parent = (Pane) settingController.getRecordBtn().getParent();
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
        settingController.getLanguageComboBox().getSelectionModel().select(0);
        settingController.getRecordBtn().setSelected(true);
        settingController.getMaxSizeField().setText(String.valueOf(settings.getMaxContentSize()));

        settingController.getRecordIncludeArea().setText(getTextFromList(settings.getRecordIncludeList()));
        settingController.getRecordExcludeArea().setText(getTextFromList(settings.getRecordExcludeList()));
    }

    @Override
    public void update(ApplicationConfig appConfig) {
        Settings settings = appConfig.getSettings();
        settings.setLanguage(settingController.getLanguageComboBox().getSelectionModel().getSelectedItem().getText());
        // settings.setRecording(settingController.getRecordBtn().isSelected());
        settings.setMaxContentSize(Integer.parseInt(settingController.getMaxSizeField().getText()));

        settings.setRecordIncludeList(getListFromText(settingController.getRecordIncludeArea().getText()));
        settings.setRecordExcludeList(getListFromText(settingController.getRecordExcludeArea().getText()));

    }
}
