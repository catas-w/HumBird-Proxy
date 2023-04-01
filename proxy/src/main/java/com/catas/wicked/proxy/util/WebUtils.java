package com.catas.wicked.proxy.util;

import com.catas.wicked.proxy.bean.ProxyRequestInfo;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import org.apache.commons.lang3.StringUtils;

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

    public static List<String> getPathSplits(URL url) {
        ArrayList<String> list = new ArrayList<>();
        list.add(url.getHost());
        String[] split = url.getPath().split("/");
        for (String item : split) {
            if (StringUtils.isNotBlank(item)) {
                list.add(item);
            }
        }
        if (!list.isEmpty() && StringUtils.isNotBlank(url.getQuery())) {
            String lastPath = list.get(list.size() - 1);
            list.set(list.size() - 1, lastPath + url.getQuery());
        }
        return list;
    }


}
