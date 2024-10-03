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

import java.util.List;

@Slf4j
public class SimpleCertManagerTest extends BaseTest {

    private CertManageProvider certManager;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        certManager = new SimpleCertManager();
        setPrivateField(certManager, "certService", new CertService());

        ApplicationConfig appConfig = new ApplicationConfig();
        Settings settings = new Settings();
        appConfig.setSettings(settings);
        setPrivateField(certManager, "appConfig", appConfig);

        ((SimpleCertManager) certManager).init();
    }

    @Test
    public void testCertList() {
        List<CertificateConfig> certList = certManager.getCertList();
        System.out.println(certList.size());
    }

    @Test
    public void testSelectedCert() {
        CertificateConfig selectedCert = certManager.getSelectedCert();
        System.out.println(selectedCert);
    }

    @Test
    public void testGetById() {
        CertificateConfig defaultCert = certManager.getCertById("_default_");
        Assertions.assertNotNull(defaultCert);

        CertificateConfig cert = certManager.getCertById("not_exist");
        Assertions.assertNull(cert);
    }
}
