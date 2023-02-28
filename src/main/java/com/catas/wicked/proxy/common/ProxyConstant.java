package com.catas.wicked.proxy.common;

import io.netty.handler.codec.http.HttpResponseStatus;

public class ProxyConstant {

    public final static HttpResponseStatus SUCCESS = new HttpResponseStatus(200,
            "Connection established");

    public final static HttpResponseStatus UNAUTHORIZED = new HttpResponseStatus(407,
            "Unauthorized");
}
