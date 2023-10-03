package com.catas.wicked.proxy.render.context;

import com.catas.wicked.common.util.TableUtils;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;

public class DefaultTableViewContextMenu extends ContextMenu {

    private MenuItem copy, copyKey, copyValue;


    public DefaultTableViewContextMenu(TableView<?> table) {
        copy = new MenuItem("Copy");
        copyKey = new MenuItem("Copy Key");
        copyValue = new MenuItem("Copy Value");

        copy.setOnAction(event -> TableUtils.copySelectedRow(table));
        copyKey.setOnAction(event -> TableUtils.copySelectedRow(table, true, false));
        copyValue.setOnAction(event -> TableUtils.copySelectedRow(table, false, true));
        getItems().addAll(copy, copyKey, copyValue);
        getStyleClass().add("req-context-menu");
    }
}
