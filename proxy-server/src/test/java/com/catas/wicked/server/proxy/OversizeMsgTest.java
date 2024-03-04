package com.catas.wicked.server.proxy;

import com.catas.wicked.common.bean.mock.RequestModel;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.server.ProxyServerTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

@Slf4j
public class OversizeMsgTest extends ProxyServerTest {

    @Before
    public void before() {
        // set maxContentSize to 50 kb
        cache.clear();
        appConfig = new ApplicationConfig() {
            @Override
            public int getMaxContentSize() {
                return 50 * 1024;
            }
        };
    }

    @Test
    public void testOversizeRecord() throws Exception {
        List<RequestModel> list = mockDataUtil.loadRequestModel("oversize-data.json");
        RequestModel requestModel = list.get(1);
        System.out.println(requestModel);
        // execute request
        HttpUriRequest request = mockDataUtil.getUriRequest(requestModel);
        CloseableHttpResponse response = httpClient.execute(request);
        System.out.println(new String(response.getEntity().getContent().readAllBytes()));
        log.info("---- Response: {}", response.getStatusLine().getStatusCode());
        response.close();
    }
}
