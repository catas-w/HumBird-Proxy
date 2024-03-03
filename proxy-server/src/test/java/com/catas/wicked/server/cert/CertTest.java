package com.catas.wicked.server.cert;

import com.catas.wicked.server.cert.CertService;
import com.catas.wicked.server.cert.spi.BouncyCastleCertGenerator;
import com.catas.wicked.server.cert.spi.CertGenerator;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

public class CertTest {

    private static final String START_DATE = "2023-01-01 ";

    private static final String SUBJECT = "C=CN, ST=SC, L=CD, O=Catas, CN=Catas";

    @Test
    public void testGenerate() throws Exception {
        CertGenerator certGenerator = new BouncyCastleCertGenerator();
        CertService certService = new CertService();
        certService.setCertGenerator(certGenerator);

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date notBeforeDate = formatter.parse(START_DATE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(notBeforeDate);
        calendar.add(Calendar.YEAR, 30);
        Date notAfterDate = calendar.getTime();

        KeyPair keyPair = certService.genKeyPair();
        assert keyPair != null;

        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        String privateKeyStr = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        String pKeyFileContent = String.format("""
                -----BEGIN RSA PRIVATE KEY-----
                %s
                -----END RSA PRIVATE KEY-----
                """, privateKeyStr);
        FileUtils.write(new File("cert/private.key"), pKeyFileContent, StandardCharsets.UTF_8);


        X509Certificate cert =
                certGenerator.generateCaCert(SUBJECT, notBeforeDate, notAfterDate, certService.genKeyPair());

        byte[] encoded = cert.getEncoded();
        String certStr = Base64.getEncoder().encodeToString(encoded);
        String certFileContent = String.format("""
                -----BEGIN CERTIFICATE-----
                %s
                -----END CERTIFICATE-----
                """, certStr);
        FileUtils.write(new File("cert.pem"), certFileContent, StandardCharsets.UTF_8);
    }

    @Test
    public void testGenerateCertFile() throws Exception {
        CertGenerator certGenerator = new BouncyCastleCertGenerator();
        CertService certService = new CertService();
        certService.setCertGenerator(certGenerator);

        Path path = Path.of("");
        certService.generateCACertFile(path);
    }
}
