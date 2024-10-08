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
import com.jfoenix.controls.JFXButton;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.ByteArrayInputStream;
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
                component.setOperateEvent(actionEvent -> System.out.println("download cert"));
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
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Import Certificate");

        // buttons
        ButtonType okButton = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(okButton, cancelBtn);

        VBox vBox = new VBox();
        vBox.setPrefWidth(500);
        vBox.setPrefHeight(400);

        // private key input
        TextArea certArea = createInputComponent(vBox, "Paste Certificate (PEM):");
        certArea.setWrapText(true);
        certArea.setPromptText("Input certificate starts with: -----BEGIN CERTIFICATE-----");
        TextArea priKeyTextArea = createInputComponent(vBox, "Paste Private Key (PEM):");
        priKeyTextArea.setWrapText(true);
        priKeyTextArea.setPromptText("Input private key starts with: -----BEGIN PRIVATE KEY-----");


        // dialog
        dialog.getDialogPane().setContent(vBox);
        dialog.getDialogPane().getStyleClass().add("cert-dialog");
        dialog.getDialogPane().lookupButton(okButton).getStyleClass().add("ok-btn");
        dialog.getDialogPane().lookupButton(cancelBtn).getStyleClass().add("cancel-btn");
        dialog.getDialogPane().getStylesheets()
                .add(Objects.requireNonNull(getClass().getResource("/css/cert-dialog.css")).toExternalForm());

        // listener on text
        dialog.getDialogPane().lookupButton(okButton).setDisable(true);
        certArea.textProperty().addListener((observable, oldValue, newValue) -> {
            dialog.getDialogPane().lookupButton(okButton).setDisable(newValue.trim().isEmpty()
                    || priKeyTextArea.getText().trim().isEmpty());
        });
        priKeyTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            dialog.getDialogPane().lookupButton(okButton).setDisable(newValue.trim().isEmpty()
                    || certArea.getText().trim().isEmpty());
        });

        // Convert the result to a Pair<String, String> when OK button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButton) {
                return new Pair<>(certArea.getText(), priKeyTextArea.getText());
            }
            return null;
        });

        // Show the dialog and wait for the user's input
        dialog.showAndWait().ifPresent(result -> {
            String certStr = result.getKey();
            String priKeyStr = result.getValue();

            try {
                CertificateConfig config = certManager.importCert(new ByteArrayInputStream(certStr.getBytes(StandardCharsets.UTF_8)),
                        new ByteArrayInputStream(priKeyStr.getBytes(StandardCharsets.UTF_8)));
                log.info("Imported config success: {}", config.getName());
                initValues(appConfig);
            } catch (Exception e) {
                log.error("Error in importing certificate.", e);
                AlertUtils.alertLater(Alert.AlertType.WARNING, e.getMessage());
            }
        });
    }

    private TextArea createInputComponent(VBox vBox, String title) {
        Label label = new Label(title);
        JFXButton selectBtn = new JFXButton("Select");
        FontIcon icon = new FontIcon();
        icon.setIconLiteral("fas-file-upload");
        selectBtn.setGraphic(icon);
        selectBtn.getStyleClass().add("cert-select-btn");

        StackPane.setAlignment(selectBtn, Pos.BOTTOM_LEFT);
        StackPane.setMargin(selectBtn, new Insets(0, 0, 5, 5));

        TextArea textArea = new TextArea();

        StackPane stackPane = new StackPane();
        VBox.setMargin(stackPane, new Insets(0, 0, 10, 0));
        stackPane.getChildren().addAll(textArea, selectBtn);

        vBox.getChildren().addAll(label, stackPane);
        return textArea;
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
}
