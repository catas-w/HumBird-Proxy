package com.catas.wicked.proxy.gui.componet;

import com.jfoenix.controls.cells.editors.base.EditorNodeBuilder;
import com.jfoenix.controls.cells.editors.base.GenericEditableTableCell;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableColumn;
import javafx.scene.text.Text;

/**
 * selectable table cell for tableView
 * set graphic to text to enable wrapping, origin graphic is labeled
 */
public class SelectableTableCell<S> extends GenericEditableTableCell<S, String> {

    private final Text text;

    public SelectableTableCell(EditorNodeBuilder builder, Text text) {
        super(builder);
        this.text = text;
        setGraphic(text);
    }

    public SelectableTableCell(EditorNodeBuilder builder, TableColumn<S, String> valColumn) {
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
