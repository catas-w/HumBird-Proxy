package com.catas.wicked.proxy.render;

import org.fxmisc.richtext.GenericStyledArea;

public interface RequestRenderer {

    void renderHeaders(String text, GenericStyledArea area);

    void appendHeaders(String text, GenericStyledArea area);

    void renderContent(String text, GenericStyledArea area);

    void renderContent(byte[] content, GenericStyledArea area, String type);

    void appendContent(String text, GenericStyledArea area);

    void appendContent(byte[] content, GenericStyledArea area, String type);
}
