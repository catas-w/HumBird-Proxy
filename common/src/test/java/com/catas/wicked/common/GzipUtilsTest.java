package com.catas.wicked.common;

import com.catas.wicked.common.util.GzipUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Paths;

public class GzipUtilsTest {

    private String str = """
                Standards Tree requests made through IETF
                documents will be reviewed and approved by the IESG, while requests made by
                other recognized standards organizations will be reviewed by the Designated
                Expert in accordance with the Specification Required policy. IANA will verify
                that this organization is recognized as a standards organization by the
                IESG.""";

    @Test
    public void testGzip() throws IOException {
        byte[] compress = GzipUtils.compress(str);
        System.out.println("Before compressed length: " + str.length());
        System.out.println("After compressed length: " + compress.length);
        String decompressStr = GzipUtils.decompressStr(compress);
        Assert.assertEquals(str, decompressStr);
    }

    @Test
    public void testDeflate() throws IOException {
        byte[] compress = GzipUtils.deflateStr(str);
        System.out.println("Before compressed length: " + str.length());
        System.out.println("After compressed length: " + compress.length);
        String decompress = GzipUtils.inflateStr(compress);
        Assert.assertEquals(str, decompress);
    }

    @Test
    public void testPath() throws IOException {
        System.out.println(System.getProperty("user.dir"));
        System.out.println(Paths.get("").toAbsolutePath().normalize());
        System.out.println(FileSystems.getDefault().getPath(""));
        System.out.println(getClass().getResource("").getPath());
        System.out.println(new File("").getCanonicalPath());
        System.out.println(new File("").getAbsoluteFile());
    }
}
