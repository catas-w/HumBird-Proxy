package com.catas.wicked.common;

import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.config.Settings;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class ApplicationConfigTest {

    @Test
    public void testLoadSettings() throws IOException {
        ApplicationConfig appConfig = new ApplicationConfig();
        appConfig.init();
        appConfig.setSettingPath("D:\\PY_Projects\\config\\config.json");
        appConfig.loadSettings();
        assert appConfig.getSettings() != null;
    }

    @Test
    public void testUpdateSettings() throws IOException {
        ApplicationConfig appConfig = new ApplicationConfig();
        appConfig.init();
        appConfig.setSettingPath("D:\\PY_Projects\\config\\config.json");
        appConfig.loadSettings();

        appConfig.getSettings().setLanguage("English");
        appConfig.getSettings().setCertType(Settings.CertType.CUSTOM);
        appConfig.getSettings().setLocalCertificate(new File("D:\\PY_Projects\\config\\test.cert"));
        appConfig.updateSettings();
    }
}
