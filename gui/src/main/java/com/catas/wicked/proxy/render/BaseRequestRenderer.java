package com.catas.wicked.proxy.render;

import com.catas.wicked.common.bean.HeaderEntry;
import com.catas.wicked.common.util.TableUtils;
import jakarta.inject.Singleton;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import lombok.extern.slf4j.Slf4j;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Singleton
public class BaseRequestRenderer implements RequestRenderer{

    private static final String[] KEYWORDS = new String[] {
            "abstract", "assert", "boolean", "Url", "Request"
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String HEADER_PATTERN = "^.+?:";
    private static final String JSON_KEY_PATTERN = "\".+\":";
    private static final String JSON_VAL_STR_PATTERN = "";
    private static final String JSON_VAL_NUM_PATTERN = "";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<HEADER>" + HEADER_PATTERN + ")"
                    + "|(?<JSONKEY>" + JSON_KEY_PATTERN + ")"
    );

    private static final ContextMenu defaultContextMenu = new DefaultContextMenu();

    @Override
    public void renderHeaders(String text, GenericStyledArea area) {
        area.setContextMenu(defaultContextMenu);
        area.textProperty().addListener((obs, oldText, newText) -> {
            area.setStyleSpans(0, computeHighlighting((String) newText));
        });

        area.replaceText(0, 0, text);
    }

    @Override
    public void renderHeaders(Map<String, String> headers, TableView<HeaderEntry> tableView) {
        if (!tableView.getColumns().isEmpty()) {
            ObservableList<HeaderEntry> list = TableUtils.headersConvert(headers);
            tableView.setItems(list);
            return;
        }
        // set key column
        TableColumn<HeaderEntry, String> keyColumn = new TableColumn<>();
        keyColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
        keyColumn.setPrefWidth(120);
        keyColumn.setMaxWidth(200);
        keyColumn.setCellFactory(tableColumn -> {
            TableCell<HeaderEntry, String> cell = new TableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.getStyleClass().add("headers-key");
            text.setFill(Paint.valueOf("#792f22"));
            text.wrappingWidthProperty().bind(keyColumn.widthProperty());
            text.textProperty().bind(cell.itemProperty());
            return cell ;
        });

        // set value column
        TableColumn<HeaderEntry, String> valColumn = new TableColumn<>();
        valColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        valColumn.setCellFactory(tableColumn -> {
            TableCell<HeaderEntry, String> cell = new TableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(valColumn.widthProperty());
            text.textProperty().bind(cell.itemProperty());
            return cell ;
        });

        tableView.getColumns().setAll(keyColumn, valColumn);

        ObservableList<HeaderEntry> data = TableUtils.headersConvert(headers);
        tableView.setItems(data);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        // tableView.setFixedCellSize(20);
        tableView.prefHeightProperty()
                .bind(Bindings.size(tableView.getItems()).multiply(tableView.getFixedCellSize()).add(10));

        // selection
        tableView.getSelectionModel().setCellSelectionEnabled(true);
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        TableUtils.installCopyPasteHandler(tableView);
    }

    @Override
    public void appendHeaders(String text, GenericStyledArea area) {

    }

    @Override
    public void renderContent(String text, GenericStyledArea area) {
        area.setParagraphGraphicFactory(LineNumberFactory.get(area));
        area.setContextMenu(defaultContextMenu);
        area.textProperty().addListener((obs, oldText, newText) -> {
            area.setStyleSpans(0, computeHighlighting((String) newText));
        });

        area.replaceText(0, 0, text);
    }

    @Override
    public void renderContent(byte[] content, GenericStyledArea area, String type) {

    }

    @Override
    public void appendContent(String text, GenericStyledArea area) {

    }

    @Override
    public void appendContent(byte[] content, GenericStyledArea area, String type) {

    }

    private StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while(matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                    matcher.group("STRING") != null ? "string" :
                    matcher.group("HEADER") != null ? "keyword" :
                    matcher.group("JSONKEY") != null ? "keyword" :
                    null; /* never happens */ assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }
}
