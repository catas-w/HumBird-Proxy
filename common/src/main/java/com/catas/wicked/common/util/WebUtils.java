package com.catas.wicked.common.util;

import com.catas.wicked.common.bean.ProxyRequestInfo;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
        if (list.size() == 1) {
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
}
