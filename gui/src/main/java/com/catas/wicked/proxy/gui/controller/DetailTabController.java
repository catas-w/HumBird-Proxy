package com.catas.wicked.proxy.gui.controller;

import com.catas.wicked.common.common.DetailArea;
import com.jfoenix.controls.JFXTextArea;
import de.felixroske.jfxsupport.FXMLController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

@FXMLController
public class DetailTabController implements Initializable {

    @FXML
    private TitledPane reqOtherPane;
    @FXML
    private TitledPane reqPayloadPane;
    @FXML
    private TitledPane respHeaderPane;
    @FXML
    private TitledPane respDataPane;
    @FXML
    private TitledPane reqHeaderPane;
    @FXML
    private JFXTextArea reqHeaderArea;
    @FXML
    private JFXTextArea reqPayloadArea;
    @FXML
    private JFXTextArea reqTimingArea;
    @FXML
    private JFXTextArea respHeaderArea;
    @FXML
    private JFXTextArea respContentArea;

    private final Map<DetailArea, Node> detailAreaMap = new HashMap<>();

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
        addTitleListener(reqOtherPane);

        addTitleListener(respHeaderPane);
        addTitleListener(respDataPane);

        initAreaMap();
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
