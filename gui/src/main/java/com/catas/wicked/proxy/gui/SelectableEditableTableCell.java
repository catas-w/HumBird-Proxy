package com.catas.wicked.proxy.gui;

import com.jfoenix.controls.cells.editors.base.EditorNodeBuilder;
import com.jfoenix.controls.cells.editors.base.GenericEditableTableCell;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableColumn;
import javafx.scene.text.Text;

/**
 * set graphic to text to enable wrapping, origin graphic is labeled
 */
public class SelectableEditableTableCell<S> extends GenericEditableTableCell<S, String> {

    private final Text text;

    public SelectableEditableTableCell(EditorNodeBuilder builder, Text text) {
        super(builder);
        this.text = text;
        setGraphic(text);
    }

    public SelectableEditableTableCell(EditorNodeBuilder builder, TableColumn<S, String> valColumn) {
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
