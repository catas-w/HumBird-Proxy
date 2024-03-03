package com.catas.wicked.server;

import com.catas.wicked.common.bean.message.RequestMessage;
import com.catas.wicked.common.bean.message.ResponseMessage;
import com.catas.wicked.common.bean.test.ExpectModel;
import com.catas.wicked.common.bean.test.RequestModel;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.constant.ClientStatus;
import com.catas.wicked.common.util.MockDataUtil;
import com.catas.wicked.common.util.SslUtils;
import com.catas.wicked.server.proxy.ProxyServer;
import io.micronaut.context.BeanContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.ehcache.Cache;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

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

    private final MockDataUtil mockDataUtil = new MockDataUtil();

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

    private RequestMessage getRequestMessageFromCache(String requestId) {
        return getRequestMessageFromCache(requestId, 0);
    }

    private RequestMessage getRequestMessageFromCache(String requestId, int waitTime) {
        if (waitTime >= 2000) {
            return null;
        }
        RequestMessage requestMessage = cache.get(requestId);
        if (requestMessage != null) {
            return requestMessage;
        }

        waitTime += 250;
        log.info("Waiting for requestMessage, waitTime={}", waitTime);
        try {
            Thread.sleep(250);
        } catch (InterruptedException ignored) {}
        return getRequestMessageFromCache(requestId, waitTime);
    }

    private ResponseMessage getRespMessageFromCache(String requestId) {
        return getRespMessageFromCache(requestId, 0);
    }

    private ResponseMessage getRespMessageFromCache(String requestId, int waitTime) {
        if (waitTime >= 2000) {
            return null;
        }
        RequestMessage requestMessage = cache.get(requestId);
        if (requestMessage != null && requestMessage.getResponse() != null) {
            return requestMessage.getResponse();
        }

        waitTime += 250;
        log.info("Waiting for respMessage: {}", waitTime);
        try {
            Thread.sleep(250);
        } catch (InterruptedException ignored) {}
        return getRespMessageFromCache(requestId, waitTime);
    }

    @Test
    public void testRecordHttp() throws IOException {
        List<RequestModel> list = null;
        try {
            list = mockDataUtil.loadRequestModel("http-data.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(list);

        for (int i = 0; i < list.size(); i++) {
            String reqId = String.format("testHttp-id-%d", i);
            RequestModel requestModel = list.get(i);
            log.info("==== RequestId: {}, uri: {} ====", reqId, requestModel.getUrl());

            // execute request
            HttpUriRequest request = mockDataUtil.getUriRequest(requestModel);
            prevIdGenerator.setNextId(reqId);
            CloseableHttpResponse response = httpClient.execute(request);
            log.info("---- Response: {}", response.getStatusLine().getStatusCode());
            response.close();

            // validate result
            ExpectModel expectModel = requestModel.getExpect();
            if (expectModel.getStatus() == ClientStatus.FINISHED && expectModel.getCode() != 0) {
                ResponseMessage responseMessage = getRespMessageFromCache(reqId);
                Assert.assertNotNull(responseMessage);
                Assert.assertEquals((int) responseMessage.getStatus(), expectModel.getCode());
            }
        }
    }

    @Test
    public void testRecordHttps() throws Exception {
        List<RequestModel> list = null;
        try {
            list = mockDataUtil.loadRequestModel("https-data.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(list);

        for (int i = 0; i < list.size(); i++) {
            String reqId = String.format("testHttps-id-%d", i);
            RequestModel requestModel = list.get(i);
            log.info("==== RequestId: {}, uri: {} ====", reqId, requestModel.getUrl());

            // execute request
            HttpUriRequest request = mockDataUtil.getUriRequest(requestModel);
            prevIdGenerator.setNextId(reqId);
            CloseableHttpResponse response = httpClient.execute(request);
            log.info("---- Response: {}", response.getStatusLine().getStatusCode());
            response.close();

            // validate result
            ExpectModel expectModel = requestModel.getExpect();
            if (expectModel.getStatus() == ClientStatus.FINISHED && expectModel.getCode() != 0) {
                ResponseMessage responseMessage = getRespMessageFromCache(reqId);
                Assert.assertNotNull(responseMessage);
                Assert.assertEquals((int) responseMessage.getStatus(), expectModel.getCode());
            }
        }
    }

    @Test
    public void testRecordUnhandledHttps() throws Exception {
        List<RequestModel> list = null;
        list = mockDataUtil.loadRequestModel("https-data.json");
        Assert.assertNotNull(list);
        appConfig.getSettings().setHandleSsl(false);

        for (int i = 0; i < list.size(); i++) {
            RequestModel requestModel = list.get(i);
            String reqId = String.format("testHttps-unhandled-id-%d", i);
            String assertMsg = "Error: id=" + reqId + ", url=" + requestModel.getUrl();
            log.info("==== RequestId: {}, uri: {} ====", reqId, requestModel.getUrl());

            // execute request
            HttpUriRequest request = mockDataUtil.getUriRequest(requestModel);
            prevIdGenerator.setNextId(reqId);
            CloseableHttpResponse response = httpClient.execute(request);
            log.info("---- Response of {}, code: {}", reqId, response.getStatusLine().getStatusCode());
            response.close();

            // validate result
            ExpectModel expectModel = requestModel.getExpect();
            if (expectModel.getStatus() == ClientStatus.FINISHED) {
                RequestMessage requestMessage = getRequestMessageFromCache(reqId);
                Assert.assertNotNull(assertMsg, requestMessage);
                Assert.assertTrue(assertMsg, requestMessage.getRequestUrl().endsWith("<Encrypted>"));
                Assert.assertTrue(assertMsg, requestMessage.isEncrypted());
            }
        }
    }

}
