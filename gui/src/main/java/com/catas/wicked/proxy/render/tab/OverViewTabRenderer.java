package com.catas.wicked.proxy.render.tab;

import com.catas.wicked.common.bean.OverviewInfo;
import com.catas.wicked.common.bean.PairEntry;
import com.catas.wicked.common.bean.message.RenderMessage;
import com.catas.wicked.common.bean.message.RequestMessage;
import com.catas.wicked.common.bean.message.ResponseMessage;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.util.WebUtils;
import com.catas.wicked.proxy.gui.componet.SelectableNodeBuilder;
import com.catas.wicked.proxy.gui.componet.SelectableTreeTableCell;
import com.catas.wicked.proxy.gui.controller.DetailTabController;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.skin.TableHeaderRow;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.Cache;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Slf4j
@Singleton
public class OverViewTabRenderer extends AbstractTabRenderer {

    @Inject
    private DetailTabController detailTabController;

    @Inject
    private Cache<String, RequestMessage> requestCache;

    @Inject
    private ApplicationConfig appConfig;

    @Inject
    private OverviewInfo overviewInfo;

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void render(RenderMessage renderMsg) {
        // System.out.println("-- render overview --");
        detailTabController.getOverViewMsgLabel().setVisible(renderMsg.isEmpty());
        if (renderMsg.isEmpty()) {
            return;
        }
        RequestMessage request = requestCache.get(renderMsg.getRequestId());
        displayOverView(request);
    }

    public void displayOverView(RequestMessage request) {
        TreeTableView<PairEntry> overviewTable = detailTabController.getOverviewTable();
        if (overviewTable.getColumns() == null || overviewTable.getColumns().isEmpty()) {
            initTreeTable(overviewTable);
        }
        String protocol = request.getProtocol() == null ? "-" : request.getProtocol();
        String url = request.getRequestUrl();
        String method = request.getMethod();
        if (method.contains("UNK")) {
            method = "-";
        }
        ResponseMessage response = request.getResponse();
        String code = response == null ? "Waiting" : response.getStatusStr() + " " + response.getReasonPhrase();

        // basic
        overviewInfo.getUrl().setVal(url);
        overviewInfo.getMethod().setVal(method);
        overviewInfo.getStatus().setVal(code);
        overviewInfo.getProtocol().setVal(protocol);
        overviewInfo.getRemoteHost().setVal(request.getRemoteHost());
        overviewInfo.getRemotePort().setVal(String.valueOf(request.getRemotePort()));
        overviewInfo.getClientHost().setVal(request.getLocalAddress());
        overviewInfo.getClientPort().setVal(String.valueOf(request.getLocalPort()));

        // timing
        overviewInfo.getTimeCost().setVal(response == null ? "-": response.getEndTime() - request.getStartTime() + " ms");
        overviewInfo.getRequestTime().setVal(request.getStartTime() + "-" + request.getEndTime());
        overviewInfo.getRequestStart().setVal(dateFormat.format(new Date(request.getStartTime())));
        overviewInfo.getRequestEnd().setVal(dateFormat.format(new Date(request.getEndTime())));
        overviewInfo.getRespTime().setVal(response == null ? "-": response.getStartTime() + " - " + response.getEndTime());
        overviewInfo.getRespStart().setVal(response == null ? "-": dateFormat.format(new Date(response.getStartTime())));
        overviewInfo.getRespEnd().setVal(response == null ? "-": dateFormat.format(new Date(response.getEndTime())));

        // size
        overviewInfo.getRequestSize().setVal(WebUtils.getHSize(request.getSize()));
        overviewInfo.getResponseSize().setVal(response == null ? "-": WebUtils.getHSize(response.getSize()));
        overviewInfo.getAverageSpeed().setVal(getSpeed(request, response));

        overviewTable.refresh();
    }

    private String getSpeed(RequestMessage request, ResponseMessage response) {
        if (response == null || (request.getSize() == 0 && response.getSize() == 0)) {
            return "-";
        }
        int size = request.getSize() + response.getSize();
        int time = (int) (response.getEndTime() - request.getStartTime());
        return String.format("%.2f KB/s", (double) size/(double) time);
    }

    private String getContentStr(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        map.forEach((key, value) -> builder.append(key).append(": ").append(value).append("\n"));
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    /**
     * initialize treeTableView
     * @param tableView treeTableView
     */
    public void initTreeTable(TreeTableView<PairEntry> tableView) {
        TreeTableColumn<PairEntry, String> nameColumn = new TreeTableColumn<>("Name");
        nameColumn.setPrefWidth(130);
        nameColumn.setMaxWidth(200);
        nameColumn.setMinWidth(100);
        nameColumn.setSortable(false);
        nameColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<PairEntry, String> param) ->
                new ReadOnlyStringWrapper(param.getValue().getValue().getKey()));
        final String titleStyle = "tree-table-key";
        nameColumn.getStyleClass().add(titleStyle);

        TreeTableColumn<PairEntry, String> valueColumn = new TreeTableColumn<>("Value");
        valueColumn.setSortable(false );
        valueColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<PairEntry, String> param) ->
                new ReadOnlyStringWrapper(param.getValue().getValue().getVal()));
        valueColumn.setCellFactory((TreeTableColumn<PairEntry, String> param) ->
                new SelectableTreeTableCell<>(new SelectableNodeBuilder(), valueColumn));

        TreeItem<PairEntry> root = new TreeItem<>();
        TreeItem<PairEntry> reqNode = new TreeItem<>(new PairEntry("Request", null));
        TreeItem<PairEntry> sizeNode = new TreeItem<>(new PairEntry("Size", null));
        TreeItem<PairEntry> timingNode = new TreeItem<>(new PairEntry("Timing", null));

        // basic info
        reqNode.getChildren().add(new TreeItem<>(overviewInfo.getUrl()));
        reqNode.getChildren().add(new TreeItem<>(overviewInfo.getMethod()));
        reqNode.getChildren().add(new TreeItem<>(overviewInfo.getProtocol()));
        reqNode.getChildren().add(new TreeItem<>(overviewInfo.getStatus()));
        reqNode.getChildren().add(new TreeItem<>(overviewInfo.getRemoteHost()));
        reqNode.getChildren().add(new TreeItem<>(overviewInfo.getRemotePort()));
        reqNode.getChildren().add(new TreeItem<>(overviewInfo.getClientHost()));
        reqNode.getChildren().add(new TreeItem<>(overviewInfo.getClientPort()));

        // timing info
        timingNode.getChildren().add(new TreeItem<>(overviewInfo.getTimeCost()));
        timingNode.getChildren().add(new TreeItem<>(overviewInfo.getRequestTime()));
        timingNode.getChildren().add(new TreeItem<>(overviewInfo.getRequestStart()));
        timingNode.getChildren().add(new TreeItem<>(overviewInfo.getRequestEnd()));
        timingNode.getChildren().add(new TreeItem<>(overviewInfo.getRespTime()));
        timingNode.getChildren().add(new TreeItem<>(overviewInfo.getRespStart()));
        timingNode.getChildren().add(new TreeItem<>(overviewInfo.getRespEnd()));

        // size info
        sizeNode.getChildren().add(new TreeItem<>(overviewInfo.getRequestSize()));
        sizeNode.getChildren().add(new TreeItem<>(overviewInfo.getResponseSize()));
        sizeNode.getChildren().add(new TreeItem<>(overviewInfo.getAverageSpeed()));

        root.setExpanded(true);
        reqNode.setExpanded(true);
        sizeNode.setExpanded(true);
        timingNode.setExpanded(true);
        root.getChildren().add(reqNode);
        root.getChildren().add(timingNode);
        root.getChildren().add(sizeNode);

        Platform.runLater(() -> {
            tableView.setRoot(root);
            tableView.setShowRoot(false);
            tableView.setEditable(true);
            tableView.getColumns().addAll(nameColumn, valueColumn);
            tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            tableView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        });

        tableView.widthProperty().addListener((source, oldWidth, newWidth) -> {
            TableHeaderRow header = (TableHeaderRow) tableView.lookup("TableHeaderRow");
            header.reorderingProperty().addListener((observable, oldValue, newValue) -> header.setReordering(false));
        });
        tableView.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                tableView.getSelectionModel().clearSelection();
            }
        });
    }
}
