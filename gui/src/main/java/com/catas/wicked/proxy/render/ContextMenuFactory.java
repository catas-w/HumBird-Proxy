package com.catas.wicked.proxy.render;

import com.catas.wicked.proxy.render.context.DefaultRichTextContextMenu;
import com.catas.wicked.proxy.render.context.DefaultTableViewContextMenu;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableView;

public class ContextMenuFactory {

    private static ContextMenu richTextContextMenu;

    private static ContextMenu tableViewContextMenu;

    public static ContextMenu getRichTextContextMenu() {
        if (richTextContextMenu != null) {
            return richTextContextMenu;
        }

        richTextContextMenu = new DefaultRichTextContextMenu();
        return richTextContextMenu;
    }

    public static ContextMenu getTableViewContextMenu(TableView<?> table) {
        if (tableViewContextMenu != null) {
            return tableViewContextMenu;
        }

        tableViewContextMenu = new DefaultTableViewContextMenu(table);
        return tableViewContextMenu;
    }
}
