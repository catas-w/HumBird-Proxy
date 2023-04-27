package com.catas.wicked.server;


import com.catas.wicked.common.bean.RequestMessage;
import org.ehcache.Cache;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.net.MalformedURLException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CacheTest {

    @Autowired
    private Cache<String, RequestMessage> requestCache;

    @Test
    public void testCacheBatch() throws MalformedURLException {
        for (int i=0; i < 1500; i++) {
            String url = "http://test/page=" + i;
            RequestMessage obj = new RequestMessage(url);
            requestCache.put(String.valueOf(i), obj);
        }

        RequestMessage message = requestCache.get("1234");
        Assert.assertEquals(message.getRequestUrl(), "http://test/page=1234");
    }
}
