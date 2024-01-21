package com.catas.wicked.common.util;

import com.catas.wicked.common.bean.ProxyRequestInfo;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.tomcat.util.http.fileupload.MultipartStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class WebUtils {

    public static ProxyRequestInfo getRequestProto(HttpRequest httpRequest) {
        ProxyRequestInfo requestInfo = new ProxyRequestInfo();
        String uri = httpRequest.uri().toLowerCase();

        if (!uri.startsWith("http://")) {
            uri = "http://" + uri;
        }
        URL url;
        try {
            url = new URL(uri);
        } catch (MalformedURLException e) {
            return null;
        }
        requestInfo.setHost(url.getHost().isEmpty() ? httpRequest.headers().get(HttpHeaderNames.HOST) : url.getHost());
        requestInfo.setPort(url.getPort() != -1 ? url.getPort() : url.getDefaultPort());
        return requestInfo;
    }

    public static List<String> getPathSplits(String url) {
        return getPathSplits(url, true);
    }

    public static List<String> getPathSplits(String url, boolean addDefault) {
        List<String> list = new ArrayList<>();
        int len = url.length();
        int slashCount = 0;
        int left = 0;
        for (int i=0; i < len; i++) {
            char chr = url.charAt(i);
            if (chr == '/') {
                if (slashCount < 2) {
                    slashCount ++;
                } else {
                    String path = url.substring(left, i);
                    if (path.length() > 0) {
                        list.add(path);
                    }
                    slashCount ++;
                    left = i + 1;
                }
            } else if (chr == '?') {
                break;
            }
        }
        if (left <= url.length() - 1) {
            list.add(url.substring(left));
        }
        if (addDefault && list.size() == 1) {
            list.add("<Default>");
        }
        return list;
    }

    public static boolean isHttp(ByteBuf byteBuf) {
        byte[] bytes = new byte[8];
        byteBuf.getBytes(0, bytes);
        String methodToken = new String(bytes);
        return methodToken.startsWith("GET ") || methodToken.startsWith("POST ") || methodToken.startsWith("HEAD ")
                || methodToken.startsWith("PUT ") || methodToken.startsWith("DELETE ") || methodToken.startsWith("OPTIONS ")
                || methodToken.startsWith("CONNECT ") || methodToken.startsWith("TRACE ");
    }

    public static String getStoragePath() throws IOException {
        File directory = new File("..");
        return directory.getCanonicalPath();
    }

    /**
     * if request is form-data
     */
    public static boolean isFormRequest(String contentType) {
        return contentType != null &&
                (contentType.contains("application/x-www-form-urlencoded") ||
                        contentType.contains("multipart/form-data"));
    }

    /**
     * parse content when encrypted
     * @param headers headers
     * @param content content
     */
    public static byte[] parseContent(Map<String, String> headers, byte[] content) {
        if (content == null || content.length == 0) {
            return new byte[0];
        }
        String contentEncoding = null;
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if ("content-encoding".equals(entry.getKey().toLowerCase().strip())) {
                contentEncoding = entry.getValue().toLowerCase().strip();
                break;
            }
        }

        // content-encoding gzip,compress,deflate,br
        if (StringUtils.isNotBlank(contentEncoding)) {
            try {
                switch (contentEncoding) {
                    case "gzip" -> content = GzipUtils.decompress(content);
                    case "br" -> content = BrotliUtils.decompress(content);
                    case "deflate" -> content = GzipUtils.inflate(content);
                    default -> {
                    }
                }
            } catch (IOException e) {
                log.error("Content decompressFailed; {}", contentEncoding);
            }
        }
        return content;
    }

    public static Map<String, String> parseQueryParams(String query) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        if (StringUtils.isEmpty(query)) {
            return map;
        }
        String[] queryParamsArray = query.split("&");
        for (String param : queryParamsArray) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2) {
                String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                map.put(key, value);
            }
        }
        return map;
    }

    public static String getHeaderText(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            builder.append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue())
                    .append("\n");
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    public static ContentType getContentType(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return null;
        }
        // String contentTypeHeader = headers.getOrDefault("Content-Type", "");
        String contentTypeHeader = "";
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (StringUtils.equalsIgnoreCase("Content-Type", entry.getKey())) {
                contentTypeHeader = entry.getValue();
                break;
            }
        }
        if (StringUtils.isBlank(contentTypeHeader)) {
            return null;
        }
        try {
            return ContentType.parse(contentTypeHeader);
        } catch (Exception e) {
            log.warn("Content-type parsed error: {}", contentTypeHeader);
            return null;
        }
    }

    /**
     * convert multipart-form data to map
     */
    public static Map<String, String> parseMultipartForm(byte[] content, String boundary, Charset charset)
            throws IOException {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        if (content == null || content.length == 0) {
            return map;
        }

        MultipartStream multipartStream = new MultipartStream(
                new ByteArrayInputStream(content),
                boundary.getBytes(),
                1024,
                null);

        boolean nextPart = multipartStream.skipPreamble();

        Pattern namePattern = Pattern.compile("name=\"(.+?)\"");
        Pattern filenamePattern = Pattern.compile("filename=\"(.+?)\"");
        while (nextPart) {
            String partHeaders = multipartStream.readHeaders();
            Matcher nameMatcher = namePattern.matcher(partHeaders);
            if (nameMatcher.find()) {
                String name = nameMatcher.group(1);
                String value = "";

                Matcher filenameMatcher = filenamePattern.matcher(partHeaders);
                if (filenameMatcher.find()) {
                    value = String.format("<%s>", filenameMatcher.group(1));
                    multipartStream.discardBodyData();

                } else {
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    multipartStream.readBodyData(output);
                    value = output.toString(charset);
                }
                map.put(name, value);
            } else {
                multipartStream.discardBodyData();
            }
            nextPart = multipartStream.readBoundary();
        }
        return map;
    }

    public static String getHSize(int size) {
        if (size < 1024) {
            return String.format("%d B", size);
        }
        return String.format("%.2f KB", size / 1024.0);
    }

    public static int estimateSize(HttpMessage httpMessage) {
        if (httpMessage == null) {
            return 0;
        }
        HttpHeaders headers = null;
        int size = 0;
        if (httpMessage instanceof HttpRequest httpRequest) {
            size = httpRequest.method().name().length()
                    + httpRequest.uri().length()
                    + httpRequest.protocolVersion().protocolName().length()  + 4;
            headers = httpRequest.headers();
        } else if (httpMessage instanceof HttpResponse httpResponse) {
            size = httpResponse.protocolVersion().protocolName().length()
                    + httpResponse.status().codeAsText().length()
                    + httpResponse.status().reasonPhrase().length() + 4;
            headers = httpResponse.headers();
        }

        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entries()) {
                size += entry.getKey().length() + entry.getValue().length() + 3;
            }
        }

        return size;
    }
}
