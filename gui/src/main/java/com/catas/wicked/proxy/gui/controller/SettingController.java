package com.catas.wicked.proxy.gui.controller;

import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.util.AlertUtils;
import com.catas.wicked.common.worker.worker.ScheduledManager;
import com.catas.wicked.proxy.gui.componet.ProxyTypeLabel;
import com.catas.wicked.proxy.service.settings.ExternalProxySettingService;
import com.catas.wicked.proxy.service.settings.GeneralSettingService;
import com.catas.wicked.proxy.service.settings.ProxySettingService;
import com.catas.wicked.proxy.service.settings.SettingService;
import com.catas.wicked.proxy.service.settings.SslSettingService;
import com.catas.wicked.proxy.service.settings.ThrottleSettingService;
import com.catas.wicked.server.proxy.ProxyServer;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

@Getter
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
    public JFXToggleButton throttleBtn;
    public JFXComboBox<Labeled> throttleComboBox;

    public Tab generalSettingTab;
    public Tab proxySettingTab;
    public Tab sslSettingTab;
    public Tab externalSettingTab;
    public Tab throttleSettingTab;
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

    @Inject
    private ApplicationConfig appConfig;
    @Setter
    private ButtonBarController buttonBarController;
    @Inject
    private ProxyServer proxyServer;
    @Inject
    private ScheduledManager scheduledManager;

    private List<SettingService> settingServiceList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initServers();

        // set icons
        configTabStyle(generalSettingTab, "fas-sliders-h");
        configTabStyle(proxySettingTab, "fas-hat-cowboy");
        configTabStyle(sslSettingTab, "fas-key");
        configTabStyle(externalSettingTab, "fas-monument");
        configTabStyle(throttleSettingTab, "fas-hourglass-end");
    }

    private void configTabStyle(Tab tab, String iconCode) {
        if (tab == null) {
            return;
        }

        final String styleClass = "setting-icon-pane";
        FontIcon icon = new FontIcon();
        icon.setIconLiteral(iconCode);
        // icon.setIconSize(36);

        Label label = new Label(tab.getText());

        BorderPane tabPane = new BorderPane();
        tabPane.setPrefWidth(90);
        tabPane.setCenter(icon);
        tabPane.setBottom(label);
        tabPane.getStyleClass().add(styleClass);

        tab.setText(null);
        tab.setGraphic(tabPane);
    }

    /**
     * initialize all components
     */
    public void initServers() {
        settingServiceList = new ArrayList<>();
        settingServiceList.add(new GeneralSettingService(this));
        settingServiceList.add(new ProxySettingService(this));
        settingServiceList.add(new SslSettingService(this));
        settingServiceList.add(new ExternalProxySettingService(this));
        settingServiceList.add(new ThrottleSettingService(this));
        settingServiceList.forEach(SettingService::init);
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
            AlertUtils.alertWarning("Illegal settings!");
            return;
        }

        settingServiceList.forEach(settingService -> settingService.update(appConfig));

        // update config file
        appConfig.updateSettings();
        cancel(event);
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

        settingServiceList.forEach(settingService -> settingService.initValues(appConfig));
    }

    /**
     * reset current page
     */
    public void reset() {
        Tab selectedTab = settingTabPane.getSelectionModel().getSelectedItem();
        List<String> styleList = selectedTab.getStyleClass()
                .stream()
                .filter(style -> style.startsWith("setting-") && !style.startsWith("setting-tab"))
                .toList();
        Class targetServiceType = null;
        switch (styleList.get(0)) {
            case "setting-general" -> targetServiceType = GeneralSettingService.class;
            case "setting-server" -> targetServiceType = ProxySettingService.class;
            case "setting-ssl" -> targetServiceType = SslSettingService.class;
            case "setting-external" -> targetServiceType = ExternalProxySettingService.class;
        }

        if (targetServiceType != null) {
            for (SettingService service : settingServiceList) {
                if (service.getClass().equals(targetServiceType)) {
                    service.initValues(appConfig);
                }
            }
        }
    }

    public void updateThrottleBtn(boolean selected) {
        buttonBarController.updateThrottleBtn(selected);
    }

    public void loadCertFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();

        // Set file chooser initial directory
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        // Set file extension filters
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        // Show the open file dialog
        File selectedFile = null;
        List<Window> windows = Stage.getWindows().stream().filter(Window::isShowing).filter(Window::isFocused).toList();
        for (Window window : windows) {
            if (window instanceof Stage ) {
                selectedFile = fileChooser.showOpenDialog(window);
            }
        }

        // Check if a file was selected
        if (selectedFile != null) {
            System.out.println("File selected: " + selectedFile.getAbsolutePath());
        } else {
            System.out.println("File selection canceled.");
        }
    }
}
