package com.catas.wicked.server.proxy;

import com.catas.wicked.common.bean.message.RequestMessage;
import com.catas.wicked.common.bean.mock.ExpectModel;
import com.catas.wicked.common.bean.mock.RequestModel;
import com.catas.wicked.common.constant.ClientStatus;
import com.catas.wicked.common.util.SslUtils;
import com.catas.wicked.server.ProxyServerTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

@Slf4j
public class ErrorMsgTest extends ProxyServerTest {

    @Before
    public void before() {
        cache.clear();
    }

    @BeforeClass
    public static void init() throws Exception {
        // initHttpClient();
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(2 * 1000)
                .setConnectionRequestTimeout(2 * 1000)
                .setProxy(new HttpHost("127.0.0.1", 9999))
                .build();

        httpClient = HttpClients.custom()
                .setDefaultHeaders(Collections.emptyList())
                .setDefaultRequestConfig(requestConfig)
                .setSSLSocketFactory(SslUtils.getSocketFactory(false, null, null))
                .build();

        initBeanContext();
    }

    @Test
    public void testConnectionTimeout() throws Exception {
        List<RequestModel> data = mockDataUtil.loadRequestModel("error-data.json");
        Assert.assertNotNull(data);

        List<RequestModel> list = data.stream()
                .filter(model -> model.getExpect().getStatus() == ClientStatus.Status.TIMEOUT)
                .toList();

        for (int i = 0; i < list.size(); i++) {
            String reqId = String.format("test-error-id-%d", i);
            RequestModel requestModel = list.get(i);
            String assertMsg = "Error: id=" + reqId + ", url=" + requestModel.getUrl();
            log.info("==== RequestId: {}, uri: {} ====", reqId, requestModel.getUrl());

            // execute request
            HttpUriRequest request = mockDataUtil.getUriRequest(requestModel);
            prevIdGenerator.setNextId(reqId);
            CloseableHttpResponse response = httpClient.execute(request);
            log.info("---- Response: {}", response.getStatusLine().getStatusCode());
            response.close();

            // validate result
            ExpectModel expectModel = requestModel.getExpect();
            RequestMessage requestMessage = getRequestMessageFromCache(reqId);
            Assert.assertNotNull(assertMsg, requestMessage);
            Assert.assertEquals(assertMsg, ClientStatus.Status.CONNECT_ERR, requestMessage.getClientStatus().getStatus());
            Assert.assertTrue(assertMsg, requestMessage.getClientStatus().getMsg().contains("Connection timed out"));
        }
    }

    @Test
    public void testAddrNotFound() throws Exception {
        List<RequestModel> data = mockDataUtil.loadRequestModel("error-data.json");
        Assert.assertNotNull(data);

        List<RequestModel> list = data.stream()
                .filter(model -> model.getExpect().getStatus() == ClientStatus.Status.ADDR_NOTFOUND)
                .toList();

        for (int i = 0; i < list.size(); i++) {
            String reqId = String.format("test-error-id-%d", i);
            RequestModel requestModel = list.get(i);
            String assertMsg = "Error: id=" + reqId + ", url=" + requestModel.getUrl();
            log.info("==== RequestId: {}, uri: {} ====", reqId, requestModel.getUrl());

            // execute request
            HttpUriRequest request = mockDataUtil.getUriRequest(requestModel);
            prevIdGenerator.setNextId(reqId);
            CloseableHttpResponse response = httpClient.execute(request);
            log.info("---- Response: {}", response.getStatusLine().getStatusCode());
            response.close();

            // wait for update msg
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {

            }
            ExpectModel expectModel = requestModel.getExpect();
            RequestMessage requestMessage = getRequestMessageFromCache(reqId);
            Assert.assertNotNull(assertMsg, requestMessage);
            Assert.assertEquals(assertMsg, expectModel.getStatus(), requestMessage.getClientStatus().getStatus());
        }
    }

    @Test
    public void testServerRejected() throws Exception {
        List<RequestModel> data = mockDataUtil.loadRequestModel("error-data.json");
        Assert.assertNotNull(data);

        List<RequestModel> list = data.stream()
                .filter(model -> model.getExpect().getStatus() == ClientStatus.Status.REJECTED)
                .toList();

        for (int i = 0; i < list.size(); i++) {
            String reqId = String.format("test-error-id-%d", i);
            RequestModel requestModel = list.get(i);
            String assertMsg = "Error: id=" + reqId + ", url=" + requestModel.getUrl();
            log.info("==== RequestId: {}, uri: {} ====", reqId, requestModel.getUrl());

            // execute request
            HttpUriRequest request = mockDataUtil.getUriRequest(requestModel);
            prevIdGenerator.setNextId(reqId);
            CloseableHttpResponse response = httpClient.execute(request);
            log.info("---- Response: {}", response.getStatusLine().getStatusCode());
            response.close();

            // wait for update msg
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {

            }
            ExpectModel expectModel = requestModel.getExpect();
            RequestMessage requestMessage = getRequestMessageFromCache(reqId);
            Assert.assertNotNull(assertMsg, requestMessage);
            Assert.assertEquals(assertMsg, ClientStatus.Status.CONNECT_ERR, requestMessage.getClientStatus().getStatus());
            Assert.assertTrue(assertMsg, requestMessage.getClientStatus().getMsg().contains("Connection refused"));
        }
    }
}
