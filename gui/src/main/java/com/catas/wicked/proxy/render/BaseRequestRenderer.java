package com.catas.wicked.proxy.render;

import jakarta.inject.Singleton;
import javafx.scene.control.ContextMenu;
import lombok.extern.slf4j.Slf4j;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
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
