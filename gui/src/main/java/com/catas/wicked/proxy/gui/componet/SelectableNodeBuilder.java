package com.catas.wicked.proxy.gui.componet;

import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.cells.editors.base.EditorNodeBuilder;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;

/**
 * Table cell: selectable when double-clicked
 */
public class SelectableNodeBuilder implements EditorNodeBuilder<String> {

    protected TextField textField;

    public SelectableNodeBuilder() {}

    @Override
    public void startEdit() {
        Platform.runLater(() -> {
            textField.selectAll();
            textField.requestFocus();
        });
    }

    @Override
    public void cancelEdit() {

    }

    @Override
    public void updateItem(String item, boolean empty) {
        Platform.runLater(() -> {
            textField.selectAll();
            textField.requestFocus();
        });
    }

    @Override
    public Region createNode(String value, EventHandler<KeyEvent> keyEventsHandler, ChangeListener<Boolean> focusChangeListener) {
        textField = value == null ? new JFXTextField() : new JFXTextField(value);
        // textField = value == null ? new TextField() : new TextField(value);

        textField.setEditable(false);
        textField.setOnKeyPressed(keyEventsHandler);
        // textField.getValidators().addAll(validators);
        textField.focusedProperty().addListener(focusChangeListener);
        return textField;
    }

    @Override
    public void setValue(String value) {
        textField.setText(value);
    }

    @Override
    public String getValue() {
        return textField.getText();
    }

    @Override
    public void validateValue() throws Exception {
    }

    @Override
    public void nullEditorNode() {
        textField = null;
    }
}
