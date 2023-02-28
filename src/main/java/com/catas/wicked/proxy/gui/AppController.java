package com.catas.wicked.proxy.gui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import de.felixroske.jfxsupport.FXMLController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@FXMLController
public class AppController implements Initializable {
    @FXML
    private MenuButton mainMenuButton;
    @FXML
    private MenuItem proxySetting;
    @FXML
    private Button closeBtn;
    @FXML
    private TitledPane respHeaderPane;
    @FXML
    private TitledPane respDataPane;
    @FXML
    private MenuButton listViewMenuBtn;
    @FXML
    private MenuItem treeViewMenuItem;
    @FXML
    private MenuItem listViewMenuItem;
    @FXML
    private TitledPane reqOtherPane;

    @FXML
    private TitledPane reqPayloadPane;

    @FXML
    private TextField filterInput;

    @FXML
    private Button filterCancelBtn;

    @FXML
    private TitledPane reqHeaderPane;

    @FXML
    private TreeView<String> reqTreeView;

    @FXML
    private JFXTextArea reqHeaderText;

    @FXML
    private JFXTextArea reqPayload;

    @FXML
    private JFXButton menuButton;

    @FXML
    private VBox reqVBox;

    private Dialog proxyConfigDialog;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        reqHeaderText.setText(":authority: www.javaroad.cn\n" +
                ":method: GET\n" +
                ":path: /questions/87133\n" +
                ":scheme: https\n" +
                "accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7\n" +
                "accept-encoding: gzip, deflate, br\n" +
                "accept-language: zh-CN,zh;q=0.9,en;q=0.8\n" +
                "cache-control: max-age=0\n" +
                "cookie: user_device_id=22a5278e40a046558d230b95c023bc4b; user_device_id_timestamp=1676912173861; cf_zaraz_google-analytics_2906=true; google-analytics_2906___ga=ee40da82-db43-421c-b639-7b8db5fd756b\n" +
                "dnt: 1\n" +
                "referer: https://www.bing.com/\n" +
                "sec-ch-ua: \"Chromium\";v=\"110\", \"Not A(Brand\";v=\"24\", \"Google Chrome\";v=\"110\"\n" +
                "sec-ch-ua-mobile: ?0\n" +
                "sec-ch-ua-platform: \"Windows\"\n" +
                "sec-fetch-dest: document\n" +
                "sec-fetch-mode: navigate\n" +
                "sec-fetch-site: cross-site\n" +
                "sec-fetch-user: ?1\n" +
                "upgrade-insecure-requests: 1\n" +
                "user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36");
        reqHeaderText.setEditable(false);

        addTitleListener(reqHeaderPane);
        addTitleListener(reqPayloadPane);
        addTitleListener(reqOtherPane);

        addTitleListener(respHeaderPane);
        addTitleListener(respDataPane);

        // list-view/tree-view
        filterInputEventBind();
        listViewEventBind(listViewMenuItem);
        listViewEventBind(treeViewMenuItem);

        // proxy setting dialog
        bindProxySettingBtn();

        // close btn
        closeBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Platform.exit();
            }
        });
    }

    private void bindProxySettingBtn() {
        try {
            Parent proxyScene = FXMLLoader.load(getClass().getResource("/fxml/proxy-settings.fxml"));
            proxyConfigDialog = new Dialog<>();
            proxyConfigDialog.setTitle("Proxy Config");
            DialogPane dialogPane = proxyConfigDialog.getDialogPane();
            dialogPane.setContent(proxyScene);
            dialogPane.getStylesheets().add(
                    getClass().getResource("/css/dialog.css").toExternalForm());
            dialogPane.getStyleClass().add("myDialog");
            Window window = dialogPane.getScene().getWindow();
            window.setOnCloseRequest(e -> window.hide());
        } catch (IOException ioExc) {
            ioExc.printStackTrace();
        }

        proxySetting.setOnAction(e -> {
            proxyConfigDialog.showAndWait();
        });
    }

    private void listViewEventBind(MenuItem menuItem) {
        menuItem.setOnAction(e -> {
            FontIcon icon = (FontIcon) menuItem.getGraphic();
            FontIcon fontIcon = new FontIcon(icon.getIconCode());
            fontIcon.setIconSize(18);
            fontIcon.setIconColor(Color.web("#616161"));
            listViewMenuBtn.setGraphic(fontIcon);
        });
    }

    private void filterInputEventBind() {
        filterInput.setOnKeyTyped(e -> {
            CharSequence characters = filterInput.getText();
            if (characters.length() > 0) {
                filterCancelBtn.setVisible(true);
            } else {
                filterCancelBtn.setVisible(false);
            }
        });

        filterCancelBtn.setOnMouseClicked(e -> {
            filterInput.clear();
            filterCancelBtn.setVisible(false);
        });
    }

    private void addTitleListener(TitledPane pane) {
        pane.expandedProperty().addListener((observable, oldValue, newValue) -> {
            //make it fill space when expanded but not reserve space when collapsed
            if (newValue) {
                pane.maxHeightProperty().set(Double.POSITIVE_INFINITY);
            } else {
                pane.maxHeightProperty().set(Double.NEGATIVE_INFINITY);
            }
        });
    }
}
