package com.catas.wicked.proxy.gui.controller;

import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.config.ExternalProxyConfig;
import com.catas.wicked.common.constant.ProxyProtocol;
import com.catas.wicked.common.util.WebUtils;
import com.catas.wicked.proxy.gui.componet.ProxyTypeLabel;
import com.catas.wicked.server.proxy.ProxyServer;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import com.jfoenix.validation.RequiredFieldValidator;
import jakarta.inject.Singleton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
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

    public JFXComboBox<ProxyTypeLabel> proxyComboBox;
    public JFXTextField exProxyHost;
    public JFXTextField exProxyPort;
    public JFXTextField exUsername;
    public JFXTextField exPassword;
    public JFXToggleButton exProxyAuth;
    public Label exUsernameLabel;
    public Label exPasswordLabel;
    public JFXComboBox<Labeled> languageComboBox;
    public JFXRadioButton defaultCertRadio;
    public JFXRadioButton customCertRadio;
    public JFXButton selectCertBtn;
    public JFXToggleButton recordBtn;
    public TextArea recordIncludeArea;
    public TextArea recordExcludeArea;
    public TextArea sysProxyExcludeArea;
    public JFXToggleButton sslBtn;
    public TextArea sslExcludeArea;
    @FXML
    private JFXToggleButton sysProxyBtn;
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

    private ApplicationConfig appConfig;

    private ProxyServer proxyServer;

    private UnaryOperator<TextFormatter.Change> textIntegerFilter;

    public void setAppConfig(ApplicationConfig appConfig) {
        this.appConfig = appConfig;
    }

    public void setProxyServer(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // textField constraint
        textIntegerFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("-?([1-9][0-9]*)?")) {
                return change;
            }
            return null;
        };


        initGeneralSettingsTab();
        initSSlSettingsTab();
        initProxySettingsTab();
        initExSettingsTab();
    }

    private void initGeneralSettingsTab() {
        languageComboBox.getItems().add(new Label("English"));
        languageComboBox.getItems().add(new Label("简体中文"));
        languageComboBox.getSelectionModel().select(0);

        maxSizeField.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 10, textIntegerFilter));
        addRequiredValidator(maxSizeField);

        recordBtn.selectedProperty().addListener(((observable, oldValue, newValue) -> {
            maxSizeField.setDisable(!newValue);
            recordIncludeArea.setDisable(!newValue);
            recordExcludeArea.setDisable(!newValue);

            Pane parent = (Pane) recordBtn.getParent();
            parent.getChildren().stream()
                    .filter(node -> node instanceof Label)
                    .skip(2)
                    .forEach(node -> {
                        Label labeled = (Label) node;
                        labeled.setDisable(!newValue);
                    });
        }));
    }

    private void initSSlSettingsTab() {
        final ToggleGroup group = new ToggleGroup();
        defaultCertRadio.setSelected(true);
        defaultCertRadio.setToggleGroup(group);
        customCertRadio.setToggleGroup(group);

        customCertRadio.selectedProperty().addListener(((observable, oldValue, newValue) -> {
            selectCertBtn.setDisable(!sslBtn.isSelected() || !newValue);
        }));

        sslBtn.selectedProperty().addListener(((observable, oldValue, newValue) -> {
            defaultCertRadio.setDisable(!newValue);
            customCertRadio.setDisable(!newValue);
            selectCertBtn.setDisable(!newValue);
            sslExcludeArea.setDisable(!newValue);

            Pane parent = (Pane) sslBtn.getParent();
            parent.getChildren().stream()
                    .filter(node -> node instanceof Label)
                    .skip(1)
                    .forEach(node -> {
                        Label labeled = (Label) node;
                        labeled.setDisable(!newValue);
                    });
        }));
    }

    /**
     * init server settings
     */
    private void initProxySettingsTab() {
        portField.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 9624, textIntegerFilter));

        // add validator
        addRequiredValidator(portField);

        sysProxyBtn.selectedProperty().addListener(((observable, oldValue, newValue) -> {
            sysProxyExcludeArea.setDisable(!newValue);
            Pane parent = (Pane) sysProxyBtn.getParent();
            parent.getChildren().stream()
                    .filter(node -> node instanceof Label)
                    .skip(2)
                    .forEach(node -> {
                        Label labeled = (Label) node;
                        labeled.setDisable(!newValue);
                    });
        }));
    }

    /**
     * init external proxy settings
     */
    private void initExSettingsTab() {
        exProxyPort.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), 0, textIntegerFilter));
        for (ProxyProtocol proxyType : ProxyProtocol.values()) {
            ProxyTypeLabel label = new ProxyTypeLabel(proxyType.getName()) {
                @Override
                public ProxyProtocol getProxyType() {
                    return proxyType;
                }
            };
            proxyComboBox.getItems().add(label);
        }
        proxyComboBox.getSelectionModel().selectedIndexProperty().addListener(((observable, oldValue, newValue) -> {
            // disable other fields when not using proxy or system proxy
            boolean disableFields = newValue.intValue() < 2;
            ((Pane) proxyComboBox.getParent()).getChildren().stream()
                    .skip(1)
                    .filter(node -> node != proxyComboBox)
                    .forEach(node -> node.setDisable(disableFields));
        }));
        proxyComboBox.getSelectionModel().selectFirst();

        exProxyAuth.selectedProperty().addListener(((observable, oldValue, newValue) -> {
            exUsernameLabel.setVisible(newValue);
            exPasswordLabel.setVisible(newValue);
            exUsername.setVisible(newValue);
            exPassword.setVisible(newValue);
        }));
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

    public void save(ActionEvent event) {
        // valid textFields
        boolean isValidated = true;
        for (Tab tab : settingTabPane.getTabs()) {
            Pane tabContent = (Pane) tab.getContent();
            Set<Node> textFields = tabContent.lookupAll(".jfx-text-field");
            if (isValidated && textFields != null && !textFields.isEmpty()) {
                for (Node textField : textFields) {
                    if (textField instanceof JFXTextField jfxTextField && !jfxTextField.validate()) {
                        isValidated = false;
                        break;
                    }
                }
            }
        }

        if (!isValidated) {
            alert("Illegal settings!");
            return;
        }

        saveGeneralSettings();
        saveServerSettings();
        saveExProxySettings();

        // update config file
        appConfig.updateLocalConfig();
        cancel(event);
    }

    private void saveGeneralSettings() {
        System.out.println("Save general settings");
    }

    private void saveServerSettings() {
        Integer newPort = Integer.valueOf(portField.getText());
        Integer oldPort = appConfig.getPort();

        // restart server if port changed
        if (!oldPort.equals(newPort)) {
            // check pot available
            if (!WebUtils.isPortAvailable(newPort)) {
                alert("Port " + newPort + " is unavailable");
                return;
            }
            appConfig.setPort(newPort);
            try {
                proxyServer.shutdown();
                proxyServer.start();
            } catch (Exception e) {
                log.error("Error in restarting proxy server.", e);
                alert("Port " + newPort + " is unavailable");
                appConfig.setPort(oldPort);
                proxyServer.start();
                return;
            }
        }
        // appConfig.setPort(newPort);
        appConfig.setMaxContentSize(Integer.parseInt(maxSizeField.getText()));
        appConfig.setSystemProxy(sysProxyBtn.isSelected());
    }

    private void saveExProxySettings() {
        // System.out.println("external proxy settings");
        ExternalProxyConfig externalProxy = appConfig.getExternalProxy();
        if (externalProxy == null) {
            externalProxy = new ExternalProxyConfig();
            appConfig.setExternalProxy(externalProxy);
        }
        ProxyProtocol protocol = proxyComboBox.getValue().getProxyType();
        externalProxy.setUsingExternalProxy(protocol != ProxyProtocol.None);
        externalProxy.setProtocol(protocol);
        externalProxy.setHost(exProxyHost.getText());
        externalProxy.setPort(Integer.parseInt(exProxyPort.getText()));
        externalProxy.setProxyAuth(exProxyAuth.isSelected());
        externalProxy.setUsername(exUsername.getText());
        externalProxy.setPassword(exPassword.getText());
    }

    public void cancel(ActionEvent event) {
        if (event == null) {
            return;
        }
        List<Window> windows = Stage.getWindows().stream().filter(Window::isShowing).filter(Window::isFocused).toList();
        for (Window window : windows) {
            Parent root = window.getScene().getRoot();
            if (root instanceof DialogPane dialogPane) {
                window.hide();
            }
        }
    }


    public void apply(ActionEvent event) {
        save(null);
    }

    /**
     * initialize form values
     */
    public void initValues() {
        if (appConfig == null) {
            return;
        }
        // server settings tab
        portField.setText(String.valueOf(appConfig.getPort()));
        maxSizeField.setText(String.valueOf(appConfig.getMaxContentSize()));
        sysProxyBtn.setSelected(appConfig.isSystemProxy());

        // external proxy settings tab
        ExternalProxyConfig externalProxy = appConfig.getExternalProxy();
        if (externalProxy != null) {
            proxyComboBox.getSelectionModel().select(externalProxy.getProtocol() == null ?
                    0 : externalProxy.getProtocol().ordinal());
            exProxyHost.setText(externalProxy.getHost());
            exProxyPort.setText(String.valueOf(externalProxy.getPort()));
            exProxyAuth.setSelected(externalProxy.isProxyAuth());
            exUsername.setText(externalProxy.getUsername());
            exPassword.setText(externalProxy.getPassword());
        } else {
            proxyComboBox.getSelectionModel().select(0);
        }
    }

    private void alert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(msg);
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
}
