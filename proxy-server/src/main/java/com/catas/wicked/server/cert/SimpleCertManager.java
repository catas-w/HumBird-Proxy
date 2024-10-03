package com.catas.wicked.server.cert;

import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.config.CertificateConfig;
import com.catas.wicked.common.provider.CertManageProvider;
import com.catas.wicked.common.util.SystemUtils;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Singleton
public class SimpleCertManager implements CertManageProvider {

    @Inject
    private CertService certService;

    @Inject
    private ApplicationConfig appConfig;

    private final List<CertificateConfig> customCertList = new ArrayList<>();

    private final CertificateConfig defaultCert = DefaultCertHolder.INSTANCE;

    @PostConstruct
    public void init() {
        // TODO: load custom certs
        File certFile = Paths.get(SystemUtils.USER_HOME, ".wkproxy", "certs.bin").toFile();
        if (!certFile.exists()) {
            log.warn("custom certs not exist");
        }

        CertificateConfig cert1 = CertificateConfig.builder()
                .id("cert01")
                .name("Cert01")
                .cert("DEFAULT_CERT")
                .privateKey("DEFAULT_PRI_KEY")
                .isDefault(false)
                .build();
        CertificateConfig cert2 = CertificateConfig.builder()
                .id("cert02")
                .name("Cert02")
                .cert("DEFAULT_CERT")
                .privateKey("DEFAULT_PRI_KEY")
                .isDefault(false)
                .build();
        customCertList.add(cert1);
        customCertList.add(cert2);
    }

    @Override
    public void importCert(InputStream inputStream) {

    }

    @Override
    public void exportCert(String certId) {

    }

    @Override
    public List<CertificateConfig> getCertList() {
        List<CertificateConfig> list = new ArrayList<>();
        list.add(defaultCert);
        list.addAll(customCertList);
        return list;
    }

    @Override
    public CertificateConfig getSelectedCert() {
        String id = appConfig.getSettings().getSelectedCert();
        CertificateConfig selectedCert = getCertById(id);
        if (selectedCert == null) {
            log.warn("Selected cert is null: {}", id);
            return defaultCert;
        }
        return selectedCert;
    }

    @Override
    public CertificateConfig getCertById(String certId) {
        if (certId == null) {
            return null;
        }
        if (certId.equals(defaultCert.getId())) {
            return defaultCert;
        }
        return customCertList.stream().filter(cert -> cert.getId().equals(certId)).findFirst().orElse(null);
    }

    static class DefaultCertHolder {
        private final static String DEFAULT_CERT = """
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

        private final static String DEFAULT_PRI_KEY = """
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

        public static final CertificateConfig INSTANCE = CertificateConfig.builder()
                .id("_default_")
                .name("Built-in")
                .cert(DEFAULT_CERT)
                .privateKey(DEFAULT_PRI_KEY)
                .isDefault(true)
                .build();
    }
}
