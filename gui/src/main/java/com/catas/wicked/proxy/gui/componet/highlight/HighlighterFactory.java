package com.catas.wicked.proxy.gui.componet.highlight;

import com.catas.wicked.common.constant.CodeStyle;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HighlighterFactory {

    private static Map<CodeStyle, Highlighter> map;

    private static final Object lock = new Object();

    @SuppressWarnings("unchecked")
    public static <S> Highlighter<S> getHighlightComputer(CodeStyle codeStyle) {
        if (map == null) {
            synchronized (lock) {
                if (map != null) {
                    return map.get(codeStyle);
                }
                map = new HashMap<>();
                map.put(CodeStyle.PLAIN, text -> new StyleSpansBuilder<Collection<String>>()
                                .add(Collections.emptyList(), text.length())
                                .create());
                map.put(CodeStyle.JSON, new JsonHighlighter());
                map.put(CodeStyle.XML, new XmlHighlighter());
                map.put(CodeStyle.HEADER, new HeaderHighlighter());
                map.put(CodeStyle.HTML, new HtmlHighlighter());
            }
        }
        return map.get(codeStyle);
    }
}
