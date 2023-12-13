package com.catas.wicked.proxy.gui.componet.highlight;

import org.apache.http.entity.ContentType;

public interface Formatter {

    String format(String text, ContentType contentType);
}
