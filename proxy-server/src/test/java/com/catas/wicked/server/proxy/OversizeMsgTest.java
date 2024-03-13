package com.catas.wicked.server.proxy;

import com.catas.wicked.common.bean.message.RequestMessage;
import com.catas.wicked.common.bean.message.ResponseMessage;
import com.catas.wicked.common.bean.mock.ExpectModel;
import com.catas.wicked.common.bean.mock.RequestModel;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.constant.ClientStatus;
import com.catas.wicked.server.PrevIdGenerator;
import com.catas.wicked.server.ProxyServerTest;
import com.catas.wicked.server.TestMessageService;
import io.micronaut.context.BeanContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.ehcache.Cache;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

@Slf4j
public class OversizeMsgTest extends ProxyServerTest {

    @Before
    public void before() {
        cache.clear();
    }

    @BeforeClass
    public static void init() throws Exception {
        initHttpClient();

        ProxyServer.standalone = true;
        context = BeanContext.build();
        context.start();

        // set maxContentSize to 50 kb
        appConfig = new ApplicationConfig() {
            @Override
            public int getMaxContentSize() {
                return 50 * 1024;
            }
        };
        context.registerSingleton(ApplicationConfig.class, appConfig);

        // appConfig = context.getBean(ApplicationConfig.class);
        proxyServer = context.getBean(ProxyServer.class);
        testMessageService = context.getBean(TestMessageService.class);
        prevIdGenerator = context.getBean(PrevIdGenerator.class);
        cache = context.getBean(Cache.class);
    }

    @Test
    public void testOversizeHttp() throws Exception {
        List<RequestModel> list = mockDataUtil.loadRequestModel("oversize-data.json");
        Assert.assertNotNull(list);

        for (int i = 0; i < list.size(); i++) {
            RequestModel requestModel = list.get(i);
            String reqId = String.format("test-oversize-id-%d", i);
            String assertMsg = "Error: id=" + reqId + ", url=" + requestModel.getUrl();
            log.info("==== RequestId: {}, uri: {} ====", reqId, requestModel.getUrl());

            // execute request
            HttpUriRequest request = mockDataUtil.getUriRequest(requestModel);
            prevIdGenerator.setNextId(reqId);
            CloseableHttpResponse response = httpClient.execute(request);
            log.info("---- Response of {}, code: {}", reqId, response.getStatusLine().getStatusCode());
            InputStream content = response.getEntity().getContent();
            content.readAllBytes();

            // validate result
            ExpectModel expectModel = requestModel.getExpect();
            if (expectModel.getStatus() == ClientStatus.Status.FINISHED && expectModel.getCode() != 0) {
                Assert.assertEquals(assertMsg, expectModel.getCode(), response.getStatusLine().getStatusCode());

                // check request oversize
                RequestMessage requestMessage = getRequestMessageFromCache(reqId);
                Assert.assertNotNull(assertMsg, requestMessage);
                Assert.assertEquals(assertMsg, expectModel.isRequestOversize(), requestMessage.isOversize());

                // check response oversize
                if (expectModel.isRespOversize()) {
                    ResponseMessage responseMessage = getRespMessageFromCache(reqId);
                    Assert.assertNotNull(assertMsg, responseMessage);
                    // System.out.println(new String(responseMessage.getContent()));
                    Assert.assertTrue(assertMsg, new String(responseMessage.getContent()).contains("Content Oversize"));
                }
            }
            response.close();
        }
    }

    @Test
    public void testOversizeHttps() throws Exception {
        List<RequestModel> list = mockDataUtil.loadRequestModel("oversize-data.json");
        Assert.assertNotNull(list);

        for (int i = 0; i < list.size(); i++) {
            RequestModel requestModel = list.get(i);
            requestModel.setUrl(requestModel.getUrl().replace("http://", "https://"));
            String reqId = String.format("test-oversize-id-%d", i);
            String assertMsg = "Error: id=" + reqId + ", url=" + requestModel.getUrl();
            log.info("==== RequestId: {}, uri: {} ====", reqId, requestModel.getUrl());

            // execute request
            HttpUriRequest request = mockDataUtil.getUriRequest(requestModel);
            prevIdGenerator.setNextId(reqId);
            CloseableHttpResponse response = httpClient.execute(request);
            log.info("---- Response of {}, code: {}", reqId, response.getStatusLine().getStatusCode());
            InputStream content = response.getEntity().getContent();
            content.readAllBytes();

            // validate result
            ExpectModel expectModel = requestModel.getExpect();
            if (expectModel.getStatus() == ClientStatus.Status.FINISHED && expectModel.getCode() != 0) {
                Assert.assertEquals(assertMsg, expectModel.getCode(), response.getStatusLine().getStatusCode());

                // check request oversize
                RequestMessage requestMessage = getRequestMessageFromCache(reqId);
                Assert.assertNotNull(assertMsg, requestMessage);
                Assert.assertEquals(assertMsg, expectModel.isRequestOversize(), requestMessage.isOversize());

                // check response oversize
                if (expectModel.isRespOversize()) {
                    ResponseMessage responseMessage = getRespMessageFromCache(reqId);
                    Assert.assertNotNull(assertMsg, responseMessage);
                    Assert.assertTrue(assertMsg, new String(responseMessage.getContent()).contains("Content Oversize"));
                }
            }
            response.close();
        }
    }
}
