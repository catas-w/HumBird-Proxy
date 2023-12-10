package com.catas.wicked.proxy.gui.componet.highlight;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;

public class OriginHighlighter implements Highlighter<Collection<String>>{
    @Override
    public StyleSpans<Collection<String>> computeHighlight(String text) {
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        spansBuilder.add(Collections.emptyList(), text.length());
        return spansBuilder.create();
    }
}
