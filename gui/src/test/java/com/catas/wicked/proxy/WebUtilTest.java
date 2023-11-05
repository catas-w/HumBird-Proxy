package com.catas.wicked.proxy;

import com.catas.wicked.common.util.WebUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
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

    @Test
    public void testParseMultiForm() throws IOException {
        String boundary = "--------------------------233744180039889815384832";
        String data = "----------------------------233744180039889815384832\r\n" +
                "Content-Disposition: form-data; name=\"name\"\r\n" +
                "\r\n" +
                "Elon Musk\r\n" +
                "----------------------------233744180039889815384832\r\n" +
                "Content-Disposition: form-data; name=\"age\"\r\n" +
                "\r\n" +
                "33\r\n" +
                "----------------------------233744180039889815384832\r\n" +
                "Content-Disposition: form-data; name=\"Job\"\r\n" +
                "\r\n" +
                "pirate\r\n" +
                "----------------------------233744180039889815384832\r\n" +
                "Content-Disposition: form-data; name=\"file\"; filename=\"pic.png\"; filename*=UTF-8''2345%E6%88%AA%E5%9B%BE20230304221554.png\r\n" +
                "Content-Type: image/png\r\n" +
                "\r\n" +
                "�PNG\r\n" +
                "----------------------------233744180039889815384832--\r\n";
        Map<String, String> map = WebUtils.parseMultipartForm(data.getBytes(), boundary, StandardCharsets.UTF_8);

        Assert.assertNotNull(map);
        Assert.assertEquals("Elon Musk", map.get("name"));
        Assert.assertEquals("<pic.png>", map.get("file"));
    }
}
