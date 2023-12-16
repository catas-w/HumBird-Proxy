package com.catas.wicked.proxy.gui.componet.highlight;

import org.apache.http.entity.ContentType;

import java.io.ByteArrayInputStream;

public class HexHighlighter extends  OriginHighlighter implements Formatter{

    private static final char[] CHARS = new char[]{
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    @Override
    public String format(String text, ContentType contentType) {
        if (text == null || text.length() == 0) {
            return "";
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(text.getBytes());
        StringBuilder builder = new StringBuilder();
        char[] array = new char[2];
        while (true) {
            int data = inputStream.read();
            if (data == -1) {
                break;
            }
            array[1] = CHARS[data % 16];
            array[0] = CHARS[data / 16];
            builder.append(array).append("\t");
        }
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }

}
