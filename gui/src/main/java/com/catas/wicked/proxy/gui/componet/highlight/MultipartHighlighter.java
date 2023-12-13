package com.catas.wicked.proxy.gui.componet.highlight;

import com.catas.wicked.common.util.WebUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
public class MultipartHighlighter extends HeaderHighlighter implements Formatter {

    @Override
    public String format(String text, ContentType contentType) {
        if (contentType == null) {
            return "";
        }
        try {
            Map<String, String> map = null;
            map = WebUtils.parseMultipartForm(
                    text.getBytes(), contentType.getParameter("boundary"), StandardCharsets.UTF_8);

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
        } catch (Exception e) {
            log.error("Error in parsing multipart-form data.", e);
            return "";
        }
    }
}
