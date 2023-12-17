package com.catas.wicked.proxy.gui.componet.highlight;

import com.catas.wicked.common.util.WebUtils;
import org.apache.http.entity.ContentType;

import java.util.Map;

public class QueryHighlighter extends HeaderHighlighter implements Formatter{

    @Override
    public String format(String text, ContentType contentType) {
        Map<String, String> map = WebUtils.parseQueryParams(text);
        if (map == null || map.isEmpty()) {
            return text;
        }
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            builder.append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue())
                    .append("\n");
        }
        if (!builder.isEmpty()) {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }
}
