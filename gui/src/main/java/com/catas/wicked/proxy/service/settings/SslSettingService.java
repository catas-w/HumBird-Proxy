package com.catas.wicked.proxy.service.settings;

import com.catas.wicked.common.bean.HeaderEntry;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.config.CertificateConfig;
import com.catas.wicked.common.config.Settings;
import com.catas.wicked.common.provider.CertManageProvider;
import com.catas.wicked.common.util.AlertUtils;
import com.catas.wicked.common.util.TableUtils;
import com.catas.wicked.proxy.gui.componet.CertSelectComponent;
import com.catas.wicked.proxy.gui.componet.SelectableNodeBuilder;
import com.catas.wicked.proxy.gui.componet.SelectableTableCell;
import com.catas.wicked.proxy.gui.componet.dialog.CertImportDialog;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Singleton
public class SslSettingService extends AbstractSettingService {

    private final ToggleGroup certSelectGroup = new ToggleGroup();

    private static final int MAX_CERT_SIZE = 5;

    @Inject
    private CertManageProvider certManager;

    @Inject
    private ApplicationConfig appConfig;

    @Override
    public void init() {
        // bugfix: make disable-listener work
        settingController.getSslBtn().setSelected(true);

        settingController.getSslBtn().selectedProperty().addListener(((observable, oldValue, newValue) -> {
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

        // import cert dialog
        settingController.setImportCertEvent(actionEvent -> displayImportDialog());
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
            component.setPreviewEvent(actionEvent -> displayPreviewDialog(config.getId()));

            if (StringUtils.equals(selectedCertId, config.getId())) {
                component.setSelected(true);
            }
            if (config.isDefault()) {
                if (StringUtils.isBlank(selectedCertId)) {
                    component.setSelected(true);
                }
                component.setOperateEvent(actionEvent -> saveCert(config));
            } else {
                component.setAlertLabel("Certificate is not installed!");
                component.setOperateEvent(actionEvent -> deleteCert(config.getId()));
            }

            certList.add(component);
        }
        settingController.setSelectableCert(certList);
        settingController.setImportCertBtnStatus(certConfigs.size() >= MAX_CERT_SIZE);

        // exclude list
        settingController.getSslExcludeArea().setText(getTextFromList(settings.getSslExcludeList()));
    }

    @Override
    public void update(ApplicationConfig appConfig) {
        Settings settings = appConfig.getSettings();
        settings.setHandleSsl(settingController.getSslBtn().isSelected());

        // update selected cert
        settings.setSslExcludeList(getListFromText(settingController.getSslExcludeArea().getText()));
    }

    /**
     * import cert dialog
     */
    private void displayImportDialog() {
        CertImportDialog dialog = new CertImportDialog();

        dialog.showAndWait().ifPresent(result -> {
            CertImportDialog.CertImportData certData = result.getKey();
            CertImportDialog.CertImportData priKeyData = result.getValue();

            try {
                CertificateConfig config = certManager.importCert(certData.fetchData(), priKeyData.fetchData());
                log.info("Imported config success: {}", config.getName());
                initValues(appConfig);
            } catch (Exception e) {
                log.error("Error in importing certificate.", e);
                AlertUtils.alertLater(Alert.AlertType.WARNING, e.getMessage());
            }
        });
    }

    /**
     * preview dialog
     */
    @SuppressWarnings("unchecked")
    private void displayPreviewDialog(String configId) {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Preview Certificate");

        // buttons
        ButtonType cancelBtn = new ButtonType("OK", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(cancelBtn);

        // label
        Label label = new Label("Certificate");

        // tableView
        TableView<HeaderEntry> tableView = new TableView<>();
        // set key column
        TableColumn<HeaderEntry, String> keyColumn = new TableColumn<>();
        keyColumn.setText("Name");
        keyColumn.getStyleClass().add("table-key");
        keyColumn.setSortable(false);
        keyColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
        keyColumn.setPrefWidth(100);

        // set value column
        TableColumn<HeaderEntry, String> valColumn = new TableColumn<>();
        valColumn.setText("Value");
        valColumn.getStyleClass().add("table-value");
        valColumn.setPrefWidth(150);
        valColumn.setSortable(false);
        valColumn.setEditable(true);
        valColumn.setCellValueFactory(new PropertyValueFactory<>("val"));
        valColumn.setCellFactory((TableColumn<HeaderEntry, String> param) -> {
            return new SelectableTableCell<>(new SelectableNodeBuilder(), valColumn);
        });
        tableView.getColumns().setAll(keyColumn, valColumn);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // add data
        Map<String, String> map = new LinkedHashMap<>();
        try {
            Map<String, String> certInfo = certManager.getCertInfo(configId);
            map.putAll(certInfo);
            label.setText(certInfo.get("CN"));
        } catch (Exception e) {
            log.error("Error in getting certInfo", e);
        }

        ObservableList<HeaderEntry> list = TableUtils.headersConvert(map);
        Platform.runLater(() -> {
            if (!tableView.getColumns().isEmpty()) {
                tableView.setItems(list);
            }
        });

        VBox vBox = new VBox();
        vBox.setPrefWidth(460);
        vBox.setPrefHeight(500);
        vBox.getChildren().addAll(label, tableView);

        // dialog
        dialog.getDialogPane().setContent(vBox);
        dialog.getDialogPane().getStyleClass().add("cert-view-dialog");
        dialog.getDialogPane().lookupButton(cancelBtn).getStyleClass().add("cancel-btn");
        dialog.getDialogPane().getStylesheets()
                .add(Objects.requireNonNull(getClass().getResource("/css/cert-dialog.css")).toExternalForm());

        dialog.showAndWait();
    }

    /**
     * delete cert event
     */
    private void deleteCert(String certId) {
        boolean confirmed = AlertUtils.confirm("Warning", "Delete this certificate?");
        if (!confirmed) {
            return;
        }
        boolean res = certManager.deleteCertConfig(certId);
        if (res) {
            initValues(appConfig);
        }
    }

    /**
     * save cert
     */
    private void saveCert(CertificateConfig certConfig) {
        String certPEM;
        try {
            certPEM = certManager.getCertPEM(certConfig.getId());
        } catch (Exception e) {
            log.error("Error in saving certificate.", e);
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Certificate");
        fileChooser.setInitialFileName(certConfig.getName());
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Certificate", "*.crt", "*.pem"),
            new FileChooser.ExtensionFilter("All files", "*.*")
        );

        List<Window> windows = Stage.getWindows().stream().filter(Window::isShowing).filter(Window::isFocused).toList();
        File file = fileChooser.showSaveDialog(windows.get(0));
        if (file == null) {
            return;
        }

        try {
            FileUtils.writeByteArrayToFile(file, certPEM.getBytes(StandardCharsets.UTF_8));
            log.info("Saved certificate to file: " + file.getAbsoluteFile());
        } catch (Exception ex) {
            log.error("Error in saving certificate.", ex);
        }
    }
}
