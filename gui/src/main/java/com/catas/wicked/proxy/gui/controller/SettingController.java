package com.catas.wicked.proxy.gui.controller;

import com.catas.wicked.common.config.ApplicationConfig;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.RequiredFieldValidator;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.converter.IntegerStringConverter;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.UnaryOperator;

@Slf4j
@Singleton
public class SettingController implements Initializable {

    @FXML
    private JFXButton cancelBtn;
    @FXML
    private TabPane settingTabPane;
    @FXML
    private JFXTextField portField;
    @FXML
    private JFXTextField maxSizeField;
    @FXML
    private JFXButton saveBtn;

    @Inject
    private ApplicationConfig appConfig;

    public void setAppConfig(ApplicationConfig appConfig) {
        this.appConfig = appConfig;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        UnaryOperator<TextFormatter.Change> integerFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("-?([1-9][0-9]*)?")) {
                return change;
            }
            return null;
        };

        portField.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 9624, integerFilter));
        maxSizeField.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 10, integerFilter));

        addRequiredValidator(portField);
        addRequiredValidator(maxSizeField);
    }

    private void addRequiredValidator(JFXTextField textField) {
        RequiredFieldValidator validator = new RequiredFieldValidator();
        validator.setMessage("Cannot be empty");
        FontIcon warnIcon = new FontIcon(FontAwesomeSolid.EXCLAMATION_TRIANGLE);
        warnIcon.getStyleClass().add("error");
        validator.setIcon(warnIcon);
        textField.getValidators().add(validator);
        textField.focusedProperty().addListener((o, oldVal, newVal) -> {
            if (!newVal) {
                textField.validate();
            }
        });
    }

    public void save() {
        Tab selectedTab = settingTabPane.getSelectionModel().getSelectedItem();
        AnchorPane content = (AnchorPane) selectedTab.getContent();
        Set<Node> textFields = content.lookupAll(".jfx-text-field");

        boolean isValidated = true;
        if (textFields != null && !textFields.isEmpty()) {
            for (Node textField : textFields) {
                if (textField instanceof JFXTextField jfxTextField && !jfxTextField.validate()) {
                    isValidated = false;
                    break;
                }
            }
        }

        if (!isValidated) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("Invalid settings!");

            alert.showAndWait();

            // JFXDialogLayout layout = new JFXDialogLayout();
            // layout.setBody(new Label("Invalid settings!"));
            //
            // JFXAlert<Void> alert = new JFXAlert<>();
            // alert.setOverlayClose(true);
            // alert.setAnimation(JFXAlertAnimation.NO_ANIMATION);
            // alert.setContent(layout);
            // alert.initModality(Modality.WINDOW_MODAL);
            //
            // alert.showAndWait();
        }

        // TODO
        List<String> styleList = selectedTab.getStyleClass().stream()
                .filter(style -> style.startsWith("setting-tab")).toList();
        String style = styleList.get(0);
        switch (style) {
            case "setting-tab-server" -> {}
            case "setting-tab-ssl" -> {}
            case "setting-tab-external" -> {}
            case "setting-tab-help" -> {}
        }

        // update config file
        appConfig.setPort(9999);
        appConfig.updateLocalConfig();
    }

    public void cancel() {
        List<Window> windows = Stage.getWindows().stream().filter(Window::isShowing).filter(Window::isFocused).toList();
        for (Window window : windows) {
            Parent root = window.getScene().getRoot();
            if (root instanceof DialogPane dialogPane) {
                window.hide();
            }
        }
    }
}
