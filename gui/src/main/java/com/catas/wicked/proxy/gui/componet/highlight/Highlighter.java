package com.catas.wicked.proxy.gui.componet.highlight;

import org.fxmisc.richtext.model.StyleSpans;

public interface Highlighter<S> {

    StyleSpans<S> computeHighlight(String text);
}
