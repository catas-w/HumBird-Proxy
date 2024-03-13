package com.catas.wicked.server.proxy;

import com.catas.wicked.common.bean.message.ResponseMessage;
import com.catas.wicked.common.bean.mock.ExpectModel;
import com.catas.wicked.common.bean.mock.RequestModel;
import com.catas.wicked.common.constant.ClientStatus;
import com.catas.wicked.common.util.WebUtils;
import com.catas.wicked.server.ProxyServerTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

@Slf4j
public class DecompressTest extends ProxyServerTest {

    @BeforeClass
    public static void init() throws Exception {
        initHttpClient();
        initBeanContext();
    }

    @Before
    public void before() {
        cache.clear();
    }

    @Test
    public void testCompressedData() throws Exception {
        List<RequestModel> list = mockDataUtil.loadRequestModel("compress-data.json");
        Assert.assertNotNull(list);

        for (int i = 0; i < list.size(); i++) {
            String reqId = String.format("test-compressed-id-%d", i);
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
            if (expectModel.getStatus() == ClientStatus.Status.FINISHED && expectModel.getCode() != 0) {
                Assert.assertEquals(assertMsg, expectModel.getCode(), response.getStatusLine().getStatusCode());
                ResponseMessage responseMsg = getRespMessageFromCache(reqId);

                Assert.assertNotNull(assertMsg, responseMsg);
                Assert.assertEquals(assertMsg, (int) responseMsg.getStatus(), expectModel.getCode());

                byte[] parsedContent = WebUtils.parseContent(responseMsg.getHeaders(), responseMsg.getContent());
                // System.out.println(new String(parsedContent));
                if (expectModel.getContainsList() != null) {
                    for (ExpectModel.ContainsItem containsItem : expectModel.getContainsList()) {
                        String decompressed = new String(parsedContent);
                        Assert.assertTrue(assertMsg, decompressed.contains(containsItem.getContent()));
                    }
                }
            }
        }
    }
}
