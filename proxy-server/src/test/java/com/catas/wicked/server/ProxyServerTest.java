package com.catas.wicked.server;

import com.catas.wicked.common.bean.message.RequestMessage;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.util.SslUtils;
import com.catas.wicked.server.proxy.ProxyServer;
import io.micronaut.context.BeanContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.ehcache.Cache;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@MicronautTest
public class ProxyServerTest {

    private static CloseableHttpClient httpClient;

    // @Inject
    private static ProxyServer proxyServer;

    // @Inject
    private static ApplicationConfig appConfig;
    private static TestMessageService testMessageService;
    private static Cache<String, RequestMessage> cache;
    private static PrevIdGenerator prevIdGenerator;

    @BeforeClass
    public static void init() throws Exception {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(10 * 1000)
                .setConnectionRequestTimeout(10 * 1000)
                .setProxy(new HttpHost("127.0.0.1", 9999))
                .build();

        httpClient = HttpClients.custom()
                .setDefaultHeaders(Collections.emptyList())
                .setDefaultRequestConfig(requestConfig)
                .setSSLSocketFactory(SslUtils.getSocketFactory(false, null, null))
                .build();

        ProxyServer.standalone = true;
        BeanContext context = BeanContext.build();
        context.start();

        appConfig = context.getBean(ApplicationConfig.class);
        proxyServer = context.getBean(ProxyServer.class);
        testMessageService = context.getBean(TestMessageService.class);
        prevIdGenerator = context.getBean(PrevIdGenerator.class);
        cache = context.getBean(Cache.class);
    }

    @Test
    public void testBasicHttpRequest() throws IOException {
        String reqId = "basic-test-001";
        prevIdGenerator.setNextId(reqId);

        HttpGet httpGet = new HttpGet("https://httpbin.org/get?name=jack&age=32");
        httpGet.addHeader("accept", "application/json");
        CloseableHttpResponse getResp = httpClient.execute(httpGet);
        Assert.assertEquals(200, getResp.getStatusLine().getStatusCode());
        getResp.close();

        RequestMessage requestMessage = cache.get(reqId);
        System.out.println(requestMessage);
        Assert.assertEquals("https://httpbin.org/get?name=jack&age=32", requestMessage.getRequestUrl());
        // cache.forEach(entry -> {
        //     System.out.println("Key: " + entry.getKey());
        //     System.out.println("Value: " + entry.getValue());
        // });
    }

    @Test
    public void getResponse() {
        CloseableHttpResponse httpResponse = null;
        final HttpGet httpGet = new HttpGet("https://httpbin.org/get?name=jack&age=32");
        try {
            httpResponse = httpClient.execute(httpGet);
            System.out.println(httpResponse.getStatusLine().getStatusCode());
            Header[] headers = httpResponse.getAllHeaders();
            for (Header header : headers) {
                System.out.println(header.getName() + ":" + header.getValue());
            }
            HttpEntity entity = httpResponse.getEntity();
            if(null != entity){
                //3.1 得到返回结果并关闭流，与下面的只能执行一个，因为流只能读取一次
                String content = EntityUtils.toString(entity);
                System.out.println(content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != httpResponse) {
                //4.归还连接到连接池
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //如果复用 httpGet ，则重置其状态使其可以重复使用
            httpGet.releaseConnection();
        }

        try {
            httpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
