package com.catas.wicked.proxy.render.tab;

import com.catas.wicked.common.bean.HeaderEntry;
import com.catas.wicked.common.util.TableUtils;
import com.catas.wicked.proxy.gui.componet.SideBar;
import com.catas.wicked.proxy.render.ContextMenuFactory;
import com.catas.wicked.proxy.render.TabRenderer;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.apache.http.entity.ContentType;

import java.util.Map;

public abstract class AbstractTabRenderer implements TabRenderer {

    protected void renderHeaders(Map<String, String> headers, TableView<HeaderEntry> tableView) {
        ObservableList<HeaderEntry> list = TableUtils.headersConvert(headers);

        Platform.runLater(() -> {
            renderHeaders(list, tableView);
        });
        // clearSelection when lose focus
        tableView.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                tableView.getSelectionModel().clearSelection();
            }
        });
        TableUtils.installCopyPasteHandler(tableView);
    }

    protected void renderHeaders(ObservableList<HeaderEntry> list, TableView<HeaderEntry> tableView) {
        if (!tableView.getColumns().isEmpty()) {
            tableView.setItems(list);
            return;
        }
        // set key column
        TableColumn<HeaderEntry, String> keyColumn = new TableColumn<>();
        keyColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
        keyColumn.setPrefWidth(120);
        keyColumn.setMaxWidth(200);
        TableUtils.setTableCellFactory(keyColumn, true);

        // set value column
        TableColumn<HeaderEntry, String> valColumn = new TableColumn<>();
        valColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        TableUtils.setTableCellFactory(valColumn, false);

        tableView.getColumns().setAll(keyColumn, valColumn);

        // ObservableList<HeaderEntry> data = TableUtils.headersConvert(headers);
        tableView.setItems(list);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        // tableView.setFixedCellSize(20);
        tableView.prefHeightProperty()
                .bind(Bindings.size(tableView.getItems()).multiply(tableView.getFixedCellSize()));

        // selection
        // tableView.getSelectionModel().setCellSelectionEnabled(true);
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        // tableView.getSelectionModel().clearAndSelect(0);

        tableView.setContextMenu(ContextMenuFactory.getTableViewContextMenu(tableView));
    }

    protected SideBar.Strategy predictCodeStyle(ContentType contentType) {
        if (contentType == null) {
            return SideBar.Strategy.TEXT;
        }
        String mimeType = contentType.getMimeType();
        if (mimeType.contains("json")) {
            return SideBar.Strategy.JSON;
        } else if (mimeType.contains("xml")) {
            return SideBar.Strategy.XML;
        } else if (mimeType.contains("html")) {
            return SideBar.Strategy.HTML;
        } else if (mimeType.contains("multipart/form-data")) {
            return SideBar.Strategy.MULTIPART_FORM_DATA;
        } else if (mimeType.contains("x-www-form-urlencoded")) {
            return SideBar.Strategy.URLENCODED_FORM_DATA;
        } else if (mimeType.startsWith("image")) {
            return SideBar.Strategy.IMG;
        } else if (mimeType.contains("zip") || mimeType.startsWith("audio") || mimeType.startsWith("video")) {
            return SideBar.Strategy.BINARY;
        }

        return SideBar.Strategy.TEXT;
    }
}
