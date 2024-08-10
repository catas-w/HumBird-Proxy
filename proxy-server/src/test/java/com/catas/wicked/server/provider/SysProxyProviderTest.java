package com.catas.wicked.server.provider;


import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.config.Settings;
import com.catas.wicked.common.config.SystemProxyConfig;
import com.catas.wicked.server.BaseTestClass;
import com.catas.wicked.server.ConditionalTest;
import io.micronaut.context.annotation.Requires;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * junit5
 */
public class SysProxyProviderTest extends BaseTestClass {

    @Test
    @ConditionalTest(os = Requires.Family.MAC_OS)
    public void testMacGetSysProxy() throws NoSuchFieldException, IllegalAccessException {
        MacSysProxyProvider proxyProvider = new MacSysProxyProvider();
        setPrivateField(proxyProvider, "appConfig", new ApplicationConfig());
        List<SystemProxyConfig> configList = proxyProvider.getSysProxyConfig();
        System.out.println(configList.size());
        System.out.println(configList);
    }

    @Test
    @ConditionalTest(os = Requires.Family.MAC_OS)
    public void testMacSetSysProxy() throws Exception {
        int port = 19624;
        MacSysProxyProvider proxyProvider = new MacSysProxyProvider();
        ApplicationConfig appConfig = new ApplicationConfig();
        Settings settings = new Settings();
        settings.setSystemProxy(true);
        settings.setPort(port);
        appConfig.setSettings(settings);
        setPrivateField(proxyProvider, "appConfig", appConfig);

        // set system proxy
        proxyProvider.setSysProxyConfig();
        {
            List<SystemProxyConfig> configList = proxyProvider.getSysProxyConfig();
            Assertions.assertFalse(configList.isEmpty());
            configList.forEach(config -> {
                Assertions.assertTrue(config.isEnabled());
                Assertions.assertEquals(port, config.getPort());
                Assertions.assertEquals(appConfig.getHost(), config.getServer());
            });
        }

        // cancel system proxy
        settings.setSystemProxy(false);
        proxyProvider.setSysProxyConfig();
        {
            List<SystemProxyConfig> configList = proxyProvider.getSysProxyConfig();
            Assertions.assertFalse(configList.isEmpty());
            configList.forEach(config -> {
                Assertions.assertFalse(config.isEnabled());
            });
        }
    }

    @Test
    @ConditionalTest(os = Requires.Family.WINDOWS)
    public void test4() {
        System.out.println("test-from-win");
    }

    @Test
    public void testParseSysProxyConfig() {
        {
            String lines = """
                    Enabled: Yes
                    Server: 127.0.0.1
                    Port: 7890
                    Authenticated Proxy Enabled: 0""";
            SystemProxyConfig config = SystemProxyConfig.parseFromLines(lines);
            Assertions.assertNotNull(config);
            Assertions.assertTrue(config.isEnabled());
            Assertions.assertEquals("127.0.0.1", config.getServer());
            Assertions.assertEquals(7890, config.getPort());
        }

        {
            String lines = """
                    Enabled: No
                    Server:\s
                    Port: 0
                    Authenticated Proxy Enabled: 0""";
            SystemProxyConfig config = SystemProxyConfig.parseFromLines(lines);
            Assertions.assertNotNull(config);
            Assertions.assertFalse(config.isEnabled());
            Assertions.assertTrue(config.getServer().isBlank());
            Assertions.assertEquals(0, config.getPort());
        }
    }
}