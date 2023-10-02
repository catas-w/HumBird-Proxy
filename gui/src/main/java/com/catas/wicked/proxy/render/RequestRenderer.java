package com.catas.wicked.proxy.render;

import com.catas.wicked.common.bean.HeaderEntry;
import javafx.scene.control.TableView;
import org.fxmisc.richtext.GenericStyledArea;

import java.util.Map;

public interface RequestRenderer {

    /**
     * render headers to rich text area
     */
    void renderHeaders(String text, GenericStyledArea area);

    /**
     * render headers to tableView
     */
    void renderHeaders(Map<String, String> headers, TableView<HeaderEntry> tableView);

    void appendHeaders(String text, GenericStyledArea area);

    void renderContent(String text, GenericStyledArea area);

    void renderContent(byte[] content, GenericStyledArea area, String type);

    void appendContent(String text, GenericStyledArea area);

    void appendContent(byte[] content, GenericStyledArea area, String type);
}
