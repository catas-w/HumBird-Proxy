package com.catas.wicked.proxy.gui.componet.highlight;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonHighlighter implements Highlighter<Collection<String>> {

    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String KEY_PATTERN = "\".+\":";
    private static final String VAL_STR_PATTERN = ":\s*(.+)";
    private static final String VAL_NUM_PATTERN = ":\s*(\\d+)";
    private static final String BRACKET_PATTERN = "[{}]";

    private static final Pattern PATTERN = Pattern.compile(
                    "(?<KEY>" + KEY_PATTERN + ")"
                    + "|(?<NUM>" + VAL_NUM_PATTERN + ")"
                    + "|(?<STR>" + VAL_STR_PATTERN + ")"
                    + "|(?<BRACKET>" + BRACKET_PATTERN + ")",
            Pattern.MULTILINE
    );

    @Override
    public StyleSpans<Collection<String>> computeHighlight(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while(matcher.find()) {
            String styleClass =
                    // matcher.group("STRING") != null ? "string" :
                    matcher.group("KEY") != null ? "keyword" :
                    matcher.group("STR") != null ? "keyword" :
                    matcher.group("NUM") != null ? "keyword" :
                    matcher.group("BRACKET") != null ? "keyword" :
                    null;
            assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }
}
