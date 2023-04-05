package com.catas.wicked.proxy;

import com.catas.wicked.common.util.WebUtils;
import junit.framework.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collections;

public class WebUtilTest {

    @Test
    public void testSplit() throws MalformedURLException {
        Assert.assertEquals(
                WebUtils.getPathSplits("http://google.com"),
                Collections.singletonList("http://google.com"));
        Assert.assertEquals(
                WebUtils.getPathSplits("http://www.google.com/page/1"),
                Arrays.asList("http://www.google.com", "page", "1"));
        Assert.assertEquals(
                WebUtils.getPathSplits("http://google.com/index/1/page?name=nq&host=111"),
                Arrays.asList("http://google.com", "index", "1", "page?name=nq&host=111"));
    }
}
