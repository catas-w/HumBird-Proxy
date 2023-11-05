package com.catas.api;

import jdk.jfr.ContentType;
import org.apache.tomcat.util.http.fileupload.MultipartStream;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MultiPartTest {

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
            "Content-Disposition: form-data; name=\"file\"; filename=\"2345截图20230304221554.png\"; filename*=UTF-8''2345%E6%88%AA%E5%9B%BE20230304221554.png\\n" +
            "Content-Type: image/png\r\n" +
            "\r\n" +
            "�PNG\r\n" +
            "----------------------------233744180039889815384832--\r\n";


    @Test
    public void test() throws IOException {
        MultipartStream multipartStream = new MultipartStream(
                new ByteArrayInputStream(data.getBytes()),
                boundary.getBytes(),
                1024,
                null);

        boolean nextPart = multipartStream.skipPreamble();
        while (nextPart) {

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            String partHeaders = multipartStream.readHeaders();
            multipartStream.readBodyData(output);

            System.out.println("------------");
            System.out.println(partHeaders);
            // System.out.println(multipartStream.getHeaderEncoding());
            System.out.println(output.toString(StandardCharsets.UTF_8));

            // do something with the multi-line part headers
            // do something with the part 'output' byte array

            nextPart = multipartStream.readBoundary();
        }

    }
}
