package com.catas.wicked.proxy.gui.controller;

import com.catas.wicked.common.bean.HeaderEntry;
import com.catas.wicked.common.constant.DetailArea;
import com.catas.wicked.proxy.render.RequestRenderer;
import com.jfoenix.controls.JFXTextArea;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TableView;
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
    private TableView<HeaderEntry> reqHeaderTable;
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
    @FXML
    private TableView<HeaderEntry> respHeaderTable;

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

        HashMap<String, String> map = new HashMap<>();
        map.put("aa", "bb");
        map.put("aa2", "bb");
        map.put("aa3", "bb");
        map.put("aa4", "bb");
        map.put("aa5", "bb");
        map.put("aa6", "bb");
        map.put("aa7", "bb");
        map.put("aa8", "bb");
        map.put("aa9", "bb");
        map.put("aa10", "bb");
        map.put("aa411", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36");
        requestRenderer.renderHeaders(map, reqHeaderTable);
        requestRenderer.renderHeaders(map, respHeaderTable);
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
