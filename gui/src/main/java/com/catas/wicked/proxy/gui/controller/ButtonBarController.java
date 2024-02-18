package com.catas.wicked.proxy.gui.controller;

import com.catas.wicked.common.bean.message.DeleteMessage;
import com.catas.wicked.common.bean.message.RequestMessage;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.config.ExternalProxyConfig;
import com.catas.wicked.common.constant.ProxyProtocol;
import com.catas.wicked.common.pipeline.MessageQueue;
import com.catas.wicked.common.pipeline.Topic;
import com.catas.wicked.common.util.ThreadPoolService;
import com.catas.wicked.proxy.service.RequestMockService;
import com.catas.wicked.server.client.MinimalHttpClient;
import com.catas.wicked.server.proxy.ProxyServer;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXToggleNode;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Window;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.ehcache.Cache;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

import static com.catas.wicked.common.constant.StyleConstant.BTN_ACTIVE;
import static com.catas.wicked.common.constant.StyleConstant.BTN_INACTIVE;

@Slf4j
@Singleton
public class ButtonBarController implements Initializable {

    public JFXButton markerBtn;
    public JFXToggleNode recordBtn;
    public JFXToggleNode sslBtn;
    @FXML
    public JFXButton removeAllBtn;
    @FXML
    public JFXButton locateBtn;
    @FXML
    public JFXButton resendBtn;
    @FXML
    private MenuButton mainMenuButton;
    @FXML
    private MenuItem proxySetting;

    private Dialog<Node> settingPage;

    @Inject
    private MessageQueue messageQueue;

    @Inject
    private ApplicationConfig appConfig;

    @Inject
    private Cache<String, RequestMessage> requestCache;

    @Inject
    private RequestMockService requestMockService;

    @Inject
    private RequestViewController requestViewController;

    @Inject
    private ProxyServer proxyServer;

    private SettingController settingController;

    @SneakyThrows
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // proxy setting dialog
        // bindProxySettingBtn();

        // toggle record button
        recordBtn.selectedProperty().addListener(((observable, oldValue, newValue) -> {
            FontIcon icon = (FontIcon) recordBtn.getGraphic();
            if (newValue) {
                icon.setIconLiteral("fas-record-vinyl");
                icon.setIconColor(Color.valueOf("#ec2222"));
            } else {
                icon.setIconLiteral("far-play-circle");
                icon.setIconColor(Color.valueOf("#616161"));
            }
            appConfig.setRecording(newValue);
        }));

        // toggle handle ssl button
        sslBtn.selectedProperty().addListener(((observable, oldValue, newValue) -> {
            FontIcon icon = (FontIcon) sslBtn.getGraphic();
            String color = newValue ? BTN_ACTIVE : BTN_INACTIVE;
            icon.setIconColor(Color.valueOf(color));
            appConfig.setHandleSsl(newValue);
        }));
    }

    public void mockTreeItem() {
        markerBtn.setOnAction(event -> {
            requestMockService.mockRequest();
        });
    }

    /**
     * delete all requests
     */
    public void deleteAll() {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setRemoveAll(true);
        messageQueue.pushMsg(Topic.RECORD, deleteMessage);
    }

    public void displaySettingPage() {
        if (settingPage == null || settingController == null) {
            try {
                // Parent settingScene = FXMLLoader.load(getClass().getResource("/fxml/setting-page/settings.fxml"));
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/setting-page/settings.fxml"));
                Parent settingScene = fxmlLoader.load();
                settingController = fxmlLoader.getController();
                settingController.setAppConfig(appConfig);
                settingController.setProxyServer(proxyServer);

                settingPage = new Dialog<>();
                settingPage.setTitle("Settings");
                settingPage.initModality(Modality.APPLICATION_MODAL);
                DialogPane dialogPane = settingPage.getDialogPane();
                dialogPane.setContent(settingScene);
                dialogPane.getStylesheets().add(
                        getClass().getResource("/css/dialog.css").toExternalForm());
                dialogPane.getStyleClass().add("myDialog");
                Window window = dialogPane.getScene().getWindow();
                window.setOnCloseRequest(e -> window.hide());
            } catch (IOException e) {
                log.error("Error loading settings-page.", e);
            }
        }

        settingController.initValues();
        settingPage.showAndWait();
    }

    /**
     * scroll to selected item
     */
    public void locateToSelectedItem() {
        int selectedTreeItem = requestViewController.getReqTreeView().getSelectionModel().getSelectedIndex();
        requestViewController.getReqTreeView().scrollTo(selectedTreeItem);

        int selectedListItem = requestViewController.getReqListView().getSelectionModel().getSelectedIndex();
        requestViewController.getReqListView().scrollTo(selectedListItem);
    }

    /**
     * resend selected request
     */
    public void resendRequest() {
        String requestId = appConfig.getCurrentRequestId().get();
        if (StringUtils.isBlank(requestId)) {
            return;
        }
        RequestMessage requestMessage = requestCache.get(requestId);
        if (requestMessage == null || requestMessage.isEncrypted() || requestMessage.isOversize()) {
            log.warn("Not integrated http request, unable to resend");
            return;
        }

        ThreadPoolService.getInstance().run(() -> {
            String url = requestMessage.getRequestUrl();
            String method = requestMessage.getMethod();
            String protocol = requestMessage.getProtocol();
            Map<String, String> headers = requestMessage.getHeaders();
            byte[] content = requestMessage.getBody();

            ExternalProxyConfig proxyConfig = new ExternalProxyConfig();
            proxyConfig.setProtocol(ProxyProtocol.HTTP);
            proxyConfig.setProxyAddress(appConfig.getHost(), appConfig.getPort());

            MinimalHttpClient client = MinimalHttpClient.builder()
                    .uri(url)
                    .method(HttpMethod.valueOf(method))
                    .httpVersion(protocol)
                    .headers(headers)
                    .content(content)
                    .proxyConfig(proxyConfig)
                    .build();
            try {
                client.execute();
                HttpResponse response = client.response();
                log.info("Get response in resending: {}", response);
            } catch (Exception e) {
                log.error("Error in resending request: {}", requestMessage.getRequestUrl());
            } finally {
                client.close();
            }
        });
    }
}
