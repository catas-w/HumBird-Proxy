package com.catas.wicked.common.util;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.catas.wicked.common.bean.HeaderEntry;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;


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
                if(keyEvent.getSource() instanceof TableView) {
                    // copy to clipboard
                    copySelectedRow((TableView<?>) keyEvent.getSource());

                    // event is handled, consume it
                    keyEvent.consume();
                }
            } else if (selectAllCodeCombination.match(keyEvent)) {
                if (keyEvent.getSource() instanceof TableView) {
                    selectAllTableRows((TableView<?>) keyEvent.getSource());
                    keyEvent.consume();
                }
            }
        }
    }

    public static void selectAllTableRows(TableView<?> table) {
        if (table == null) {
            return;
        }
        table.getSelectionModel().selectAll();
    }

    /**
     * Get table selection and copy it to the clipboard.
     * @param table
     */
    @SuppressWarnings("rawtypes")
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

            ObservableValue<?> observableValue = table.getColumns().get(col).getCellObservableValue(row);
            clipboardString.append(getCellValue(observableValue));

            // remember previous
            prevRow = row;
        }

        // create clipboard content
        final ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(clipboardString.toString());
        // set clipboard content
        Clipboard.getSystemClipboard().setContent(clipboardContent);
    }

    public static void copySelectedRow(TableView<?> tableView) {
        copySelectedRow(tableView, true, true);
    }

    /**
     * copy selected row to clipboard
     * @param table table
     */
    @SuppressWarnings("rawtypes")
    public static void copySelectedRow(TableView<?> table, boolean includeKey, boolean includeVal) {
        if (table == null || (!includeKey && !includeVal)) {
            return;
        }
        Set<Integer> rows = new TreeSet<>();
        ObservableList<TablePosition> positionList = table.getSelectionModel().getSelectedCells();

        for (TablePosition position : positionList) {
            rows.add(position.getRow());
        }
        StringBuilder builder = new StringBuilder();
        boolean firstRow = true;
        for (Integer row : rows) {
            if (!firstRow) {
                builder.append("\n");
            }
            firstRow = false;
            boolean firstColumn = true;
            for (TableColumn<?, ?> column : table.getColumns()) {
                boolean skip = false;
                if (firstColumn && !includeKey) {
                    skip = true;
                } else if (!firstColumn && !includeVal) {
                    skip = true;
                }
                if (!firstColumn && includeKey && includeVal) {
                    builder.append("\t");
                }
                firstColumn = false;
                if (skip) {
                    continue;
                }
                ObservableValue<?> observableValue = column.getCellObservableValue(row);
                builder.append(getCellValue(observableValue));
            }
        }
        final ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(builder.toString());
        Clipboard.getSystemClipboard().setContent(clipboardContent);
    }

    private static String getCellValue(ObservableValue<?> observableValue) {
        String text;
        if (observableValue == null) {
            text = "";
        } else if(observableValue instanceof DoubleProperty doubleProperty) { // TODO: handle boolean etc
            text = numberFormatter.format(doubleProperty.get());
        } else if( observableValue instanceof IntegerProperty integerProperty) {
            text = numberFormatter.format(integerProperty.get());
        } else if( observableValue instanceof StringProperty stringProperty) {
            text = stringProperty.get();
        } else {
            Object value = observableValue.getValue();
            text = value.toString();
        }
        return text;
    }

    public static ObservableList<HeaderEntry> headersConvert(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return FXCollections.emptyObservableList();
        }
        ArrayList<HeaderEntry> list = new ArrayList<>();
        headers.forEach((key, value) -> list.add(new HeaderEntry(key, value)));
        return FXCollections.observableArrayList(list);
    }

    public static void setTableCellFactory(TableColumn<HeaderEntry, String> column, boolean isHeader) {
        column.setCellFactory(tableColumn -> {
            TableCell<HeaderEntry, String> cell = new TableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(column.widthProperty());
            text.textProperty().bind(cell.itemProperty());
            if (isHeader) {
                text.getStyleClass().add("headers-key");
                // text.setFill(Paint.valueOf("#792f22"));
            }
            // cell.setOnMouseClicked(event -> {
            //     Object source = event.getSource();
            //     if (source instanceof TableCell<?,?> tableCell) {
            //         TableRow<?> tableRow = tableCell.getTableRow();
            //         int index = tableRow.getIndex();
            //         tableCell.getTableView().getSelectionModel().clearAndSelect(index);
            //     }
            // });
            return cell ;
        });
    }
}
