package com.catas.wicked.server.proxy;

import com.catas.wicked.common.bean.message.RequestMessage;
import com.catas.wicked.common.bean.message.ResponseMessage;
import com.catas.wicked.common.bean.mock.ExpectModel;
import com.catas.wicked.common.bean.mock.RequestModel;
import com.catas.wicked.common.constant.ClientStatus;
import com.catas.wicked.server.ProxyServerTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
public class HttpProxyTest extends ProxyServerTest {

    @Before
    public void before() {
        // reset all config
        cache.clear();
    }

    @BeforeClass
    public static void init() throws Exception {
        initHttpClient();
        initBeanContext();
    }

    @Test
    public void testRecordHttp() throws Exception {
        List<RequestModel> list = mockDataUtil.loadRequestModel("http-data.json");
        Assert.assertNotNull(list);

        for (int i = 0; i < list.size(); i++) {
            String reqId = String.format("testHttp-id-%d", i);
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
            if (expectModel.getStatus() == ClientStatus.FINISHED && expectModel.getCode() != 0) {
                Assert.assertEquals(assertMsg, expectModel.getCode(), response.getStatusLine().getStatusCode());
                ResponseMessage responseMessage = getRespMessageFromCache(reqId);
                Assert.assertNotNull(assertMsg, responseMessage);
                Assert.assertEquals(assertMsg, (int) responseMessage.getStatus(), expectModel.getCode());
            }
        }
    }

    @Test
    public void testUnRecordHttp() throws Exception {
        List<RequestModel> list = mockDataUtil.loadRequestModel("http-data.json");
        Assert.assertNotNull(list);

        appConfig.getSettings().setRecording(false);
        Set<String> reqIdList = new HashSet<>();
        for (int i = 0; i < list.size(); i++) {
            String reqId = String.format("testHttp-id-%d", i);
            reqIdList.add(reqId);
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
            if (expectModel.getStatus() == ClientStatus.FINISHED && expectModel.getCode() != 0) {
                Assert.assertEquals(assertMsg, expectModel.getCode(), response.getStatusLine().getStatusCode());
            }
        }

        List<RequestMessage> values = cache.getAll(reqIdList).values().stream().filter(Objects::nonNull).toList();
        Assert.assertTrue(values.isEmpty());
    }
}
