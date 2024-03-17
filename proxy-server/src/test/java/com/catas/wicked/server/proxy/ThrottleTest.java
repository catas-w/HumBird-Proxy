package com.catas.wicked.server.proxy;

import com.catas.wicked.common.bean.message.RequestMessage;
import com.catas.wicked.common.constant.ThrottlePreset;
import com.catas.wicked.server.ProxyServerTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;

@Slf4j
public class ThrottleTest extends ProxyServerTest {

    @Before
    public void before() {
        cache.clear();
    }

    @BeforeClass
    public static void init() throws Exception {
        initHttpClient();
        initBeanContext();
    }

    @Test
    @Ignore
    public void testThrottle() throws Exception {
        long timeCostNormal = sendAndEstimateTime("https://www.winrar.com.cn/download/winrar-x32-700scp.exe",
                "test-throttle-001");
        log.info("Normal time cost: {} ms", timeCostNormal);

        appConfig.getSettings().setThrottle(true);
        appConfig.getSettings().setThrottlePreset(ThrottlePreset.REGULAR_2G);
        long timeCostThrottle = sendAndEstimateTime("https://www.winrar.com.cn/download/winrar-x32-700scp.exe",
                "test-throttle-002");
        log.info("Throttle time cost: {} ms", timeCostThrottle);

        Assert.assertTrue(timeCostThrottle > 2 * timeCostNormal);
    }

    private long sendAndEstimateTime(String uri, String reqId) throws Exception {
        log.info("==== RequestId: {}, uri: {} ====", reqId, uri);

        long start = System.currentTimeMillis();
        // execute request
        prevIdGenerator.setNextId(reqId);
        // HttpUriRequest request = mockDataUtil.getUriRequest(requestModel);
        HttpGet request = new HttpGet(uri);
        CloseableHttpResponse response = httpClient.execute(request);
        log.info("---- Response: {}", response.getStatusLine().getStatusCode());
        InputStream content = response.getEntity().getContent();
        content.readAllBytes();
        response.close();

        RequestMessage requestMessage = getRequestMessageFromCache(reqId);
        Assert.assertNotNull(requestMessage);
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());

        return System.currentTimeMillis() - start;
    }
}
