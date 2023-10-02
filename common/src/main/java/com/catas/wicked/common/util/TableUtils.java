package com.catas.wicked.common.util;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Map;

import com.catas.wicked.common.bean.HeaderEntry;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;


public class TableUtils {
    private static NumberFormat numberFormatter = NumberFormat.getNumberInstance();

    public static void installCopyPasteHandler(TableView<?> table) {
        // install copy/paste keyboard handler
        table.setOnKeyPressed(new TableKeyEventHandler());
    }

    /**
     * Copy/Paste keyboard event handler.
     * The handler uses the keyEvent's source for the clipboard data. The source must be of type TableView.
     */
    public static class TableKeyEventHandler implements EventHandler<KeyEvent> {

        KeyCodeCombination copyKeyCodeCombination = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY);
        KeyCodeCombination pasteKeyCodeCombination = new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_ANY);
        KeyCodeCombination selectAllCodeCombination = new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_ANY);

        @Override
        public void handle(final KeyEvent keyEvent) {
            if (copyKeyCodeCombination.match(keyEvent)) {
                if( keyEvent.getSource() instanceof TableView) {

                    // copy to clipboard
                    copySelectionToClipboard( (TableView<?>) keyEvent.getSource());

                    // event is handled, consume it
                    keyEvent.consume();

                }
            }
        }
    }

    /**
     * Get table selection and copy it to the clipboard.
     * @param table
     */
    public static void copySelectionToClipboard(TableView<?> table) {

        StringBuilder clipboardString = new StringBuilder();

        ObservableList<TablePosition> positionList = table.getSelectionModel().getSelectedCells();

        int prevRow = -1;

        for (TablePosition position : positionList) {

            int row = position.getRow();
            int col = position.getColumn();

            // determine whether we advance in a row (tab) or a column
            // (newline).
            if (prevRow == row) {
                clipboardString.append('\t');
            } else if (prevRow != -1) {
                clipboardString.append('\n');
            }

            // create string from cell
            String text = "";

            ObservableValue<?> observableValue = table.getColumns().get(col).getCellObservableValue(row);

            // null-check: provide empty string for nulls
            if (observableValue == null) {
                text = "";
            }
            else if(observableValue instanceof DoubleProperty doubleProperty) { // TODO: handle boolean etc
                text = numberFormatter.format(doubleProperty.get());
            }
            else if( observableValue instanceof IntegerProperty integerProperty) {
                text = numberFormatter.format(integerProperty.get());
            }
            else if( observableValue instanceof StringProperty stringProperty) {
                text = stringProperty.get();
            } else {
                Object value = observableValue.getValue();
                text = value.toString();
            }

            // add new item to clipboard
            clipboardString.append(text);

            // remember previous
            prevRow = row;
        }

        // create clipboard content
        final ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(clipboardString.toString());

        // set clipboard content
        Clipboard.getSystemClipboard().setContent(clipboardContent);
    }


    public static ObservableList<HeaderEntry> headersConvert(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return FXCollections.emptyObservableList();
        }
        ArrayList<HeaderEntry> list = new ArrayList<>();
        headers.forEach((key, value) -> list.add(new HeaderEntry(key, value)));
        return FXCollections.observableArrayList(list);
    }
}
