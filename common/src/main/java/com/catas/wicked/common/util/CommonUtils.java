package com.catas.wicked.common.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommonUtils {

    public static String toHexString(byte[] byteArray, char separator) {
        StringBuilder builder = new StringBuilder();
        final int len = 12;
        for (int i = 0; i < byteArray.length; i++) {
            builder.append(String.format("%02X", byteArray[i]));
            if (i > 0 && i % len == 0) {
                builder.append("\n");
            } else {
                builder.append(separator);
            }
        }

        if (builder.charAt(builder.length() - 1) == separator) {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }
}
