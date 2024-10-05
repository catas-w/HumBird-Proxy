package com.catas.wicked.server.cert;

import com.catas.wicked.BaseTest;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.config.CertificateConfig;
import com.catas.wicked.common.config.Settings;
import com.catas.wicked.common.provider.CertManageProvider;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.List;

@Slf4j
public class SimpleCertManagerTest extends BaseTest {

    private CertManageProvider certManager;

    private CertService certService;

    String cert = """
            -----BEGIN CERTIFICATE-----
            MIIDJjCCAg6gAwIBAgIGAYip42nwMA0GCSqGSIb3DQEBCwUAMEcxCzAJBgNVBAYTA
            kNOMQswCQYDVQQIDAJTQzELMAkGA1UEBwwCQ0QxDjAMBgNVBAoMBUNhdGFzMQ4wD
            AYDVQQDDAVDYXRhczAgFw0yMjEyMzExNjAwMDBaGA8yMDUyMTIzMTE2MDAwMFowR
            zELMAkGA1UEBhMCQ04xCzAJBgNVBAgMAlNDMQswCQYDVQQHDAJDRDEOMAwGA1UEC
            gwFQ2F0YXMxDjAMBgNVBAMMBUNhdGFzMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AM
            IIBCgKCAQEArJAEmjwUVgtJi8BE9euB70SZ3WXK0jM2HdqSQoVGj8RneUyZlgd73
            TK1peZEnJ4GH7moS0/s2IUneVtIr55Bmv86dQ1T9167Kr2C+uqYgKb/M0ORmYjTY
            aq8Ys9JlUuADoxpw5orr6wS1Axv/4oXQx7/7hRU1YXyOB0YC85uOlYhsQwB4+Rtr
            /Tk9xMf9rIw2L2S9Gj+ikqTaTTiz+80Za4xiU9Vp2xikUwPUAhNkMkg98ioxFPsi
            eIVI8khgtKs3Qp4oU3vAZ9WYUa8eRSj5Z30DyPZ0AN2I9CjnE+vLhTB5Qv8Y8/7k
            kpsl/cTQO5Kwt9nqUpx6VKuWtgEBEDTRwIDAQABoxYwFDASBgNVHRMBAf8ECDAGA
            QH/AgEAMA0GCSqGSIb3DQEBCwUAA4IBAQA4AIEjk3ivh1g563I+eZOeEFzAIMcti
            DV4UYx7odDMpsa9u2iJ0VzzotrZsDrO38MVF7osUr0CSYzgtlJY9PNTeTbM1ZHMg
            ZfCp9uGIe2W3s4CaPVgAuWNb30W/aMNs3w87Nd3D/ukyq1tBiU34mQbu1VwHD+az
            t3CZdirw3lBAoF+/P2tapX/r4JGO6v8EnYcjRhkFnErZ0iYgESTM6RJgOgoHg6v9
            megi8vb7XfdTo/UkZF/MvG7d7f0Ug51Z44TwMV/3aFUMim5qjGLYLhuMW6KsJ37l
            uFb6Vg1Jc+Nh55XYuUloyMaPYaKoegiICakVb60q+k7lVf0Vl8SioG3
            -----END CERTIFICATE-----
            """;

    String priKey = """
            -----BEGIN PRIVATE KEY-----
            MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCskASaPBRWC0mLw
            ET164HvRJndZcrSMzYd2pJChUaPxGd5TJmWB3vdMrWl5kScngYfuahLT+zYhSd5W
            0ivnkGa/zp1DVP3XrsqvYL66piApv8zQ5GZiNNhqrxiz0mVS4AOjGnDmiuvrBLUD
            G//ihdDHv/uFFTVhfI4HRgLzm46ViGxDAHj5G2v9OT3Ex/2sjDYvZL0aP6KSpNpN
            OLP7zRlrjGJT1WnbGKRTA9QCE2QySD3yKjEU+yJ4hUjySGC0qzdCnihTe8Bn1ZhR
            rx5FKPlnfQPI9nQA3Yj0KOcT68uFMHlC/xjz/uSSmyX9xNA7krC32epSnHpUq5a2
            AQEQNNHAgMBAAECggEAC8jD/7JloyCfM6N8Mh0UoP3a0hM/AA7OPcis300RrgS4G
            kEAZg35x/fMDtnESrvB6E257/az0m95bCCvPIr+qPKQD+lKmCSIfJk3hiIaKwL8f
            4g4O7dr1AogVlEYkdD3nEa7fEedfyAtovx8c4N/Ji7KRHxv9KryiSF3gGrm/SXC4
            HsCgl+/IwO4rBGK2cwdpiFItLSMz/WTDomwKM3HLKwYfQn6TXJWkI1sLdSVae2xz
            lhp51NS7OoiQW7AxLkfJMz4TzO5lt940vsd6Qe7Irkt2sDRVnFqil1h/oL6C08bo
            fFRXXAHMGafcjtM9wKHxQWhWyeNuYr8bEubcoOnoQKBgQDf9mLio/QujgxCbE9yP
            b+tXVHxviXmyZ3S8uXAJJcWkOEuW7Djqbau1KseTkZnS4LKEQ2v7fWhZuORLNJdd
            yZBb8P5waaopp18y18IFcyXDZBTpc2bvVa3ReogpZTAf5UY5a5dUmCUwds98cPuX
            tZYGDBOaS/Yf6mRNIrnngjNIQKBgQDFP1fenru8cYLWF2+vP1djsXqC3iixnvYyM
            tPM3orExfEgDoD/BdL87k7RMGOl5BXR9ymylOsMkxMnsmFY2oQhL29x2cnV84dCJ
            oLH6c756cvBiMmGRf9fU4I4CZRRpoKx8kxbnUR3LXPo9X5v4jVN5fMAnABZ6jTMi
            cynXEnrZwKBgQDFr73pP78xuMCpgOE/fbHLXSwPrj/WTvC4Wx7hU2rpyEh7mwOwc
            QWHnMDOp1kNpGF160ehmvHN3fvRETc+uQVTjUv3ETACfc9VcE4Z3OQSES7sJtYuO
            hpo+5WF66I5qGP67gHS7fvCUrsrGlRP+/ZHHFLHY6GQAF+0hCR3/c7XAQKBgGVEi
            yl5l2s9X+3o5Gc1/vjpErwDsSLYORA2tjT1gItcENrjd2j3JYbEFuUhkdeGzYkZ+
            2d7O14eWHRGCeKjdbjP8MikBxL4T9YvjW9z49a9KOY5AzYT0/OuHSoGCQcOlQUlH
            /zdNlx1ko/lEkABBFr1FxiV/zwqmWb1zIOUR0M7AoGBAINpnwCHFqKj6LQbASrNS
            A7eQnG7U8VpOR4mvDzoBc/6Mk4Y4X7ggnp72pvBe0i+0dZtz7q0U/zin41Z8sV22
            bgS82fs4/JS6MITgb07K2/KXUuIyjKScVsnt9eC/d3FnXpASQvApSv4wpXTp/svA
            yujYbyIpOP7HNC/PJxVh9On
            -----END PRIVATE KEY-----
            """;

    @Before
    public void setUp() throws Exception {
        certManager = new SimpleCertManager();
        certService = new CertService();
        setPrivateField(certManager, "certService", certService);


        ApplicationConfig appConfig = new ApplicationConfig();
        Settings settings = new Settings();
        appConfig.setSettings(settings);
        setPrivateField(certManager, "appConfig", appConfig);

        ((SimpleCertManager) certManager).init();
    }

    @Test
    public void testCertList() {
        List<CertificateConfig> certList = certManager.getCertList();
        System.out.println(certList);
    }

    @Test
    public void testSelectedCert() {
        CertificateConfig selectedCert = certManager.getSelectedCert();
        System.out.println(selectedCert);
    }

    @Test
    public void testGetById() {
        CertificateConfig defaultCert = certManager.getCertConfigById("_default_");
        Assertions.assertNotNull(defaultCert);

        CertificateConfig cert = certManager.getCertConfigById("not_exist");
        Assertions.assertNull(cert);
    }

    @Test
    public void testImportCert() throws Exception {
        String originSubject = certService.getSubject(new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8)));

        CertificateConfig config = certManager.importCert(new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8)),
                new ByteArrayInputStream(priKey.getBytes(StandardCharsets.UTF_8)));
        Assertions.assertNotNull(config);

        X509Certificate parsedCert = certManager.getCertById(config.getId());
        String subject = certService.getSubject(parsedCert);
        // System.out.println(subject);
        Assertions.assertEquals(originSubject, subject);

        String priKeyPEM = certManager.getPriKeyPEM(config.getId());
        Assertions.assertEquals(priKey, priKeyPEM);

        List<CertificateConfig> certList = certManager.getCertList();
        Assertions.assertFalse(certList.isEmpty());
        Assertions.assertTrue(certList.contains(config));

        // certManager.deleteCertConfig(config.getId());
    }

    @Test
    public void testDeleteCert() {
        CertificateConfig config = certManager.importCert(new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8)),
                new ByteArrayInputStream(priKey.getBytes(StandardCharsets.UTF_8)));
        Assertions.assertNotNull(config);

        {
            List<CertificateConfig> certList = certManager.getCertList();
            Assertions.assertTrue(certList.contains(config));
        }

        certManager.deleteCertConfig(config.getId());
        {
            List<CertificateConfig> certList = certManager.getCertList();
            Assertions.assertFalse(certList.contains(config));
        }

    }
}
