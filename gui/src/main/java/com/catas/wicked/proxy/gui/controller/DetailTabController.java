package com.catas.wicked.proxy.gui.controller;

import com.catas.wicked.common.bean.HeaderEntry;
import com.catas.wicked.common.bean.message.RequestMessage;
import com.catas.wicked.common.bean.message.ResponseMessage;
import com.catas.wicked.common.util.WebUtils;
import com.catas.wicked.proxy.render.RequestRenderer;
import com.jfoenix.controls.JFXTabPane;
import com.jfoenix.controls.JFXTextArea;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import org.fxmisc.richtext.CodeArea;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

@Singleton
public class DetailTabController implements Initializable {

    @FXML
    private SplitPane respSplitPane;
    @FXML
    private SplitPane reqSplitPane;
    @FXML
    private CodeArea overviewArea;
    @FXML
    private TitledPane reqPayloadPane;
    @FXML
    private JFXTabPane reqPayloadTabPane;
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

    private final Map<SplitPane, double[]> dividerPositionMap =new HashMap<>();

    private boolean dividerUpdating;

    private boolean midTitleCollapse;

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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dividerPositionMap.put(reqSplitPane, reqSplitPane.getDividerPositions().clone());
        dividerPositionMap.put(respSplitPane, respSplitPane.getDividerPositions().clone());

        addTitleListener(reqHeaderPane, reqSplitPane);
        addTitleListener(reqPayloadPane, reqSplitPane);
        addTitleListener(respHeaderPane, respSplitPane);
        addTitleListener(respDataPane, respSplitPane);


        Map<String, String> map = new LinkedHashMap<>();
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

        RequestMessage requestMessage = new RequestMessage("http://google.com/page");
        // RequestMessage requestMessage = new RequestMessage("http://google.com/page?name=aa&age=22");

        requestMessage.setHeaders(map);
        requestMessage.setBody(sampleJson.getBytes(StandardCharsets.UTF_8));

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setStatus(200);
        responseMessage.setHeaders(map);
        responseMessage.setContent(sampleCode.getBytes(StandardCharsets.UTF_8));

        requestMessage.setResponse(responseMessage);
        displayRequest(requestMessage);
    }

    /**
     * synchronized dividers
     * @deprecated
     */
    private void bindDividerPosition(SplitPane splitPane) {
        if (splitPane.getDividers().size() < 2) {
            return;
        }
        ObservableList<SplitPane.Divider> dividers = splitPane.getDividers();
        dividers.get(0).positionProperty().addListener(((observable, oldValue, newValue) -> {
            if (dividerUpdating || splitPane.getDividers().size() < 2 || reqParamPane.isExpanded()) {
                return;
            }
            // System.out.println("Divider-0: " + newValue);
            if (newValue.doubleValue() > 0.95) {
                dividers.get(0).setPosition(0.95);
                dividers.get(1).setPosition(1.0);
                return;
            }
            dividerUpdating = true;
            double delta = newValue.doubleValue() - oldValue.doubleValue();
            dividers.get(1).setPosition(dividers.get(1).positionProperty().doubleValue() + delta);
            dividerUpdating = false;
        }));

        dividers.get(1).positionProperty().addListener(((observable, oldValue, newValue) -> {
            if (dividerUpdating || splitPane.getDividers().size() < 2 || reqParamPane.isExpanded()) {
                return;
            }
            // System.out.println("Divider-1: " + newValue);
            if (!midTitleCollapse) {
                return;
            }
            if (newValue.doubleValue() < 0.05) {
                dividers.get(0).setPosition(0.0);
                dividers.get(1).setPosition(0.05);
                return;
            }
            dividerUpdating = true;
            double delta = newValue.doubleValue() - oldValue.doubleValue();
            dividers.get(0).setPosition(dividers.get(0).positionProperty().doubleValue() + delta);
            dividerUpdating = false;
        }));
    }

    private void addTitleListener(TitledPane pane, SplitPane splitPane) {
        pane.expandedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // open
                pane.maxHeightProperty().set(Double.POSITIVE_INFINITY);
                if (splitPane.getItems().size() == 2) {
                    splitPane.setDividerPositions(dividerPositionMap.get(splitPane));
                } else if (splitPane.getItems().size() == 3) {
                    // TODO bug: titledPane-1 expanded, titledPane-2,3 closed, tiledPane-3 cannot expand
                    int expandedNum = getExpandedNum(splitPane);
                    System.out.println("Expanded num: " + expandedNum);
                    if (expandedNum != 2) {
                        midTitleCollapse = false;
                        splitPane.setDividerPositions(0.33333, 0.66666);
                        return;
                    }
                    if (!reqParamPane.isExpanded()) {
                        ObservableList<SplitPane.Divider> dividers = splitPane.getDividers();
                        dividers.get(0).setPosition(0.5);
                        dividers.get(1).setPosition(0.5);
                    }
                }
            } else {
                // close
                pane.maxHeightProperty().set(Double.NEGATIVE_INFINITY);
                dividerPositionMap.put(splitPane, splitPane.getDividerPositions().clone());
                if (splitPane.getItems().size() == 3) {
                    if (getExpandedNum(splitPane) == 2 && !reqParamPane.isExpanded()) {
                        midTitleCollapse = true;
                    }
                }
            }
        });
    }
    private int getExpandedNum(SplitPane splitPane) {
        int res = 0;
        for (Node item : splitPane.getItems()) {
            if (item instanceof TitledPane titledPane) {
                res += titledPane.isExpanded() ? 1 : 0;
            }
        }
        return res;
    }

    /**
     * exhibit default info
     */
    public void reset() {

    }

    /**
     * exhibit request info
     */
    public void displayRequest(RequestMessage request) {
        if (request == null) {
            return;
        }

        // display headers
        Map<String, String> headers = request.getHeaders();
        requestRenderer.renderHeaders(headers, reqHeaderTable);

        // display query-params if exist
        String query = request.getUrl().getQuery();
        Map<String, String> queryParams = WebUtils.parseQueryParams(query);
        if (!queryParams.isEmpty()) {
            StringBuilder queryBuilder = new StringBuilder();
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                queryBuilder.append(entry.getKey());
                queryBuilder.append(": ");
                queryBuilder.append(entry.getValue());
                queryBuilder.append("\n");
            }
            requestRenderer.renderHeaders(queryBuilder.toString(), reqParamArea);
        }

        // display request content
        // TODO images
        byte[] content = WebUtils.parseContent(request.getHeaders(), request.getBody());
        String contentStr = new String(content, StandardCharsets.UTF_8);
        requestRenderer.renderContent(contentStr, reqPayloadArea);

        boolean hasQuery = !queryParams.isEmpty();
        boolean hasContent = content.length > 0;
        // System.out.printf("hasQuery: %s, hasContent: %s\n", hasQuery, hasContent);
        SingleSelectionModel<Tab> selectionModel = reqPayloadTabPane.getSelectionModel();
        String title = "";
        if (hasQuery) {
            selectionModel.clearAndSelect(1);
            title = "Query Parameters";
        }
        if (hasContent) {
            selectionModel.clearAndSelect(0);
            // TODO form-data
            title = "Content";
        }
        if (hasQuery && hasContent) {
            reqPayloadTabPane.setTabMaxHeight(20);
            reqPayloadTabPane.setTabMinHeight(20);
            title = "Payload";
        } else {
            reqPayloadTabPane.setTabMaxHeight(0);
        }
        reqPayloadPane.setText(title);

        displayOverView(request);
        displayResponse(request.getResponse());
    }

    public void displayResponse(ResponseMessage response) {
        if (response == null) {
            requestRenderer.renderContent("<Waiting For Response...>", respContentArea);
            return;
        }
        // headers
        Map<String, String> headers = response.getHeaders();
        requestRenderer.renderHeaders(headers, respHeaderTable);

        // content TODO images
        byte[] content = WebUtils.parseContent(headers, response.getContent());
        String contentStr = new String(content, StandardCharsets.UTF_8);
        requestRenderer.renderContent(contentStr, respContentArea);
    }

    public void displayOverView(RequestMessage request) {
        String protocol = request.getProtocol();
        String url = request.getRequestUrl();
        String method = request.getMethod();
        String title = String.format("%s %s %s", protocol, url, method);
        String code = request.getResponse() == null ? "Waiting" : String.valueOf(request.getResponse().getStatus());

        String cont = title + "\n" + code;
        requestRenderer.renderContent(cont, overviewArea);
    }
}
