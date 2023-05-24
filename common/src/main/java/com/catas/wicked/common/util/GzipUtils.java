package com.catas.wicked.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.*;

public class GzipUtils {

    public static final String DEFAULT_ENCODING = "UTF-8";

    public static byte[] compress(String str, String encoding) throws IOException {
        if (str == null || str.length() == 0) {
            return new byte[0];
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        GZIPOutputStream outputStream = new GZIPOutputStream(out);
        outputStream.write(str.getBytes(encoding));
        outputStream.close();

        return out.toByteArray();
    }

    public static byte[] compress(String str) throws IOException {
        return compress(str, DEFAULT_ENCODING);
    }

    private static ByteArrayOutputStream decompressOutput(byte[] bytes) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream input = new ByteArrayInputStream(bytes);

        GZIPInputStream gzipInputStream = new GZIPInputStream(input);
        byte[] buffer = new byte[1024];
        int n;
        while ((n = gzipInputStream.read(buffer)) > 0) {
            out.write(buffer, 0, n);
        }
        return out;
    }

    public static byte[] decompress(byte[] bytes) throws IOException {
        if (bytes == null || bytes.length == 0) {
            return new byte[0];
        }
        return decompressOutput(bytes).toByteArray();
    }

    public static String decompressStr(byte[] bytes, String encoding) throws IOException {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        return decompressOutput(bytes).toString(encoding);
    }

    public static String decompressStr(byte[] bytes) throws IOException {
        return decompressStr(bytes, DEFAULT_ENCODING);
    }

    /**
     * Deflate decompress
     */
    public static byte[] inflate(byte[] bytes) throws IOException {
        if (bytes == null || bytes.length == 0) {
            return new byte[0];
        }
        Inflater inflater = new Inflater();
        inflater.setInput(bytes);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (outputStream) {
            byte[] buffer = new byte[1024];
            int n;
            while ((n = inflater.inflate(buffer)) > 0) {
                outputStream.write(buffer, 0, n);
            }
        } catch (DataFormatException e) {
            throw new RuntimeException(e);
        }
        return outputStream.toByteArray();
    }

    public static String inflateStr(byte[] bytes, String encoding) throws IOException {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        return new String(inflate(bytes), encoding);
    }

    public static String inflateStr(byte[] bytes) throws IOException {
        return inflateStr(bytes, DEFAULT_ENCODING);
    }

    /**
     * Deflate compress
     */
    public static byte[] deflate(byte[] bytes) throws IOException {
        if (bytes == null || bytes.length == 0) {
            return new byte[0];
        }
        Deflater deflater = new Deflater();
        deflater.setInput(bytes);
        deflater.finish();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (outputStream) {
            byte[] buffer = new byte[1024];
            int n;
            while ((n = deflater.deflate(buffer)) > 0) {
                outputStream.write(buffer, 0, n);
            }
        }
        return outputStream.toByteArray();
    }

    public static byte[] deflateStr(String str, String encoding) throws IOException {
        if (str == null || str.length() == 0) {
            return new byte[0];
        }
        return deflate(str.getBytes(encoding));
    }

    public static byte[] deflateStr(String str) throws IOException {
        return deflateStr(str, DEFAULT_ENCODING);
    }
}
