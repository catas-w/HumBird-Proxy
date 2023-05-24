package com.catas.wicked.common.util;

import org.apache.commons.compress.compressors.brotli.BrotliCompressorInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BrotliUtils {

    public static byte[] decompress(byte[] bytes) throws IOException {
        if (bytes == null || bytes.length == 0) {
            return new byte[0];
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        BrotliCompressorInputStream inputStream = new BrotliCompressorInputStream(input);
        byte[] buffer = new byte[1024];
        int n;
        while ((n = inputStream.read(buffer)) != 0) {
            outputStream.write(buffer, 0, n);
        }
        return outputStream.toByteArray();
    }

    public static String decompressStr(byte[] bytes, String encoding) throws IOException {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        return new String(decompress(bytes), encoding);
    }

    public static String decompressStr(byte[] bytes) throws IOException {
        return decompressStr(bytes, GzipUtils.DEFAULT_ENCODING);
    }
 }
