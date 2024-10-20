package com.catas.wicked.common.provider;

import com.catas.wicked.BaseTest;
import com.catas.wicked.ConditionalTest;
import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.config.CertificateConfig;
import com.catas.wicked.common.config.Settings;
import com.catas.wicked.server.cert.CertService;
import com.catas.wicked.server.cert.SimpleCertManager;
import io.micronaut.context.annotation.Requires;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class CertInstallProviderTest extends BaseTest {

    private CertManager certManager;

    private CertService certService;

    @BeforeEach
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
    @ConditionalTest(os = Requires.Family.MAC_OS)
    public void testMacCertInstallCheck() throws Exception {
        MacCertInstallProvider provider = new MacCertInstallProvider();

        List<CertificateConfig> certList = certManager.getCertList();
        for (CertificateConfig config : certList) {
            // X509Certificate cert = certManager.getCertById(config.getId());

            Map<String, String> certInfo = certManager.getCertInfo(config.getId());
            boolean res = provider.checkCertInstalled(certInfo.get("CN"), certInfo.get("SHA256"));
            System.out.println("name: " + config.getName() + " installed: " + res);
        }
    }

    @Test
    @ConditionalTest(os = Requires.Family.WINDOWS)
    public void testWinCertInstallCheck() throws Exception {
        WinCertInstallProvider provider = new WinCertInstallProvider();
        provider.init();

        List<CertificateConfig> certList = certManager.getCertList();
        for (CertificateConfig config : certList) {
            Map<String, String> certInfo = certManager.getCertInfo(config.getId());
            boolean res = provider.checkCertInstalled(certInfo.get("CN"), certInfo.get("SHA256"));
            System.out.println("name: " + config.getName() + " installed: " + res);
        }
    }
}
