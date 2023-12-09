package com.catas.wicked.common.constant;


import org.apache.commons.lang3.StringUtils;

public enum CodeStyle {
    PLAIN,
    JSON,
    HTML,
    XML,
    HEADER,
    JAVASCRIPT,
    CODE,
    PARSED,
    ORIGIN,
    HEX;

    public static CodeStyle valueOfIgnoreCase(String value) {
        if (value == null) {
            return null;
        }

        String strip = value.strip();
        for (CodeStyle codeStyle : CodeStyle.values()) {
            if (StringUtils.equalsIgnoreCase(codeStyle.name(), strip)) {
                return codeStyle;
            }
        }
        return null;
    }
}
