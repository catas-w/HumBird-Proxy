package com.catas.wicked.proxy.gui.controller;

import com.catas.wicked.common.constant.DetailArea;
import com.catas.wicked.proxy.render.RequestRenderer;
import com.jfoenix.controls.JFXTextArea;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import org.fxmisc.richtext.CodeArea;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

@Singleton
public class DetailTabController implements Initializable {

    @FXML
    private CodeArea overviewArea;
    @FXML
    private TitledPane reqPayloadPane;
    @FXML
    private TitledPane respHeaderPane;
    @FXML
    private TitledPane reqParamPane;
    @FXML
    private TitledPane respDataPane;
    @FXML
    private TitledPane reqHeaderPane;
    @FXML
    private CodeArea reqHeaderArea;
    @FXML
    private CodeArea reqParamArea;
    @FXML
    private CodeArea reqPayloadArea;
    @FXML
    private JFXTextArea reqTimingArea;
    @FXML
    private CodeArea respHeaderArea;
    @FXML
    private CodeArea respContentArea;

    @Inject
    private RequestRenderer requestRenderer;

    private final Map<DetailArea, Node> detailAreaMap = new HashMap<>();

    private static final String sampleCode = String.join("\n", new String[] {
            "Request Url:    http://google.com/path/index/1?query=aa&time=bb",
            "Request Method:    POST",
            "Status Code:    200",
            "Remote Address:    192.168.1.234:80",
            "Refer Policy:   cross-origin boolean",
    });

    private static final String sampleQueryParams = """
            name: Jack
            age: 32
            from: Google Chrome""";

    private static final String sampleJson = """
            {
                "key1": "Val1",
                "key2": "Val2",
                "key3": 334,
                "key4": 23
            }""";

    private void initAreaMap() {
        detailAreaMap.put(DetailArea.REQUEST_HEADER, reqHeaderArea);
        detailAreaMap.put(DetailArea.REQUEST_PAYLOAD, reqPayloadArea);
        detailAreaMap.put(DetailArea.RESP_HEADER, respHeaderArea);
        detailAreaMap.put(DetailArea.RESP_CONTENT, respContentArea);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        addTitleListener(reqHeaderPane);
        addTitleListener(reqPayloadPane);
        addTitleListener(reqParamPane);
        addTitleListener(respHeaderPane);
        addTitleListener(respDataPane);

        initAreaMap();

        requestRenderer.renderHeaders(sampleCode, reqHeaderArea);
        requestRenderer.renderHeaders(sampleQueryParams, reqParamArea);
        requestRenderer.renderContent(sampleJson, reqPayloadArea);
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

    /**
     * set request/response detail
     */
    public void setRequestDetail(DetailArea area, String content) {
        Node node = detailAreaMap.get(area);
        if (node == null) {
            return;
        }
        TextArea textArea = (TextArea) node;
        Platform.runLater(() -> {
            textArea.setText(content);
            textArea.setEditable(false);
        });
    }
}
