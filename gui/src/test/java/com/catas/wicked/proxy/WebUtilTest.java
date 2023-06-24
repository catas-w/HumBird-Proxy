package com.catas.wicked.proxy;

import com.catas.wicked.common.util.WebUtils;
import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.util.Arrays;

public class WebUtilTest {

    @Test
    public void testSplit() throws MalformedURLException {
        Assert.assertEquals(
                WebUtils.getPathSplits("http://google.com"),
                Arrays.asList("http://google.com", "<Default>"));
        Assert.assertEquals(
                WebUtils.getPathSplits("http://google.com/"),
                Arrays.asList("http://google.com", "<Default>"));
        Assert.assertEquals(
                WebUtils.getPathSplits("http://google.com/?page=1"),
                Arrays.asList("http://google.com", "?page=1"));
        Assert.assertEquals(
                WebUtils.getPathSplits("http://www.google.com/page/1"),
                Arrays.asList("http://www.google.com", "page", "1"));
        Assert.assertEquals(
                WebUtils.getPathSplits("http://google.com/index/1/page?name=nq&host=111"),
                Arrays.asList("http://google.com", "index", "1", "page?name=nq&host=111"));

    }
}
