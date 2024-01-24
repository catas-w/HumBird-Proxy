package com.catas.wicked.proxy.gui.componet;

import com.jfoenix.controls.cells.editors.base.EditorNodeBuilder;
import com.jfoenix.controls.cells.editors.base.GenericEditableTreeTableCell;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.text.Text;

/**
 * selectable table cell for treeTableView
 */
public class SelectableTreeTableCell<S> extends GenericEditableTreeTableCell<S, String> {

    private final Text text;

    public SelectableTreeTableCell(EditorNodeBuilder builder, TreeTableColumn<S, String> valColumn) {
        super(builder);
        this.text = new Text();
        setGraphic(text);
        text.wrappingWidthProperty().bind(valColumn.widthProperty());
        text.textProperty().bind(this.itemProperty());
    }
    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setGraphic(text);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty && !isEditing()) {
            setGraphic(text);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }
    }

    public void addTextStyle(String style) {
        this.text.getStyleClass().add(style);
    }
}
