package com.catas.wicked.common.util;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

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
                WebUtils.getPathSplits("http://google.com", false),
                Arrays.asList("http://google.com"));
        Assert.assertEquals(
                WebUtils.getPathSplits("http://google.com/", false),
                Arrays.asList("http://google.com"));
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

    @Test
    public void testParseQueryParam() {
        String query = "param1=value1&param2=value2&&name=jack&";
        Map<String, String> params = WebUtils.parseQueryParams(query);
        Assert.assertEquals("value1", params.get("param1"));
        Assert.assertEquals("value2", params.get("param2"));
        Assert.assertEquals("jack", params.get("name"));
    }

    @Test
    public void testHeaderText() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");

        String expected = """
                key1: value1
                key2: value2
                key3: value3
                """;
        String headerText = WebUtils.getHeaderText(map);
        Assert.assertEquals(expected.trim(), headerText);
    }

    @Ignore
    @Test
    public void testPortAvailable() {
        Assert.assertFalse(WebUtils.isPortAvailable(-1));
        Assert.assertFalse(WebUtils.isPortAvailable(655356));
        Assert.assertFalse(WebUtils.isPortAvailable(-2));
        Assert.assertFalse(WebUtils.isPortAvailable(10808));
        Assert.assertFalse(WebUtils.isPortAvailable(10809));

        Assert.assertTrue(WebUtils.isPortAvailable(9999));
        Assert.assertTrue(WebUtils.isPortAvailable(9900));
    }

    @Test
    public void testRemoveProtocol() {
        Assert.assertEquals("test.com", WebUtils.removeProtocol("test.com"));
        Assert.assertEquals("test.com", WebUtils.removeProtocol("http://test.com"));
        Assert.assertEquals("test.com", WebUtils.removeProtocol("https://test.com"));
        Assert.assertEquals("test.com/123", WebUtils.removeProtocol("https://test.com/123"));
        Assert.assertEquals("", WebUtils.removeProtocol("https://"));
        Assert.assertEquals("httpbin.org", WebUtils.removeProtocol("https://httpbin.org"));
        Assert.assertEquals("httpbin.org", WebUtils.removeProtocol("http://httpbin.org"));
        Assert.assertEquals("httpbin.org", WebUtils.removeProtocol("httpbin.org"));
    }
}
