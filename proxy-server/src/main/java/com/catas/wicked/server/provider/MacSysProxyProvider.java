package com.catas.wicked.server.provider;

import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.config.SystemProxyConfig;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Singleton
@Requires(os = Requires.Family.MAC_OS)
public class MacSysProxyProvider implements SysProxyProvider {

    private static final String NETWORK_SETUP = "networksetup";
    private static final String PROXY_TYPE_WEB = "webproxy";
    private static final String PROXY_TYPE_SECURE_WEB = "securewebproxy";
    private static final String PROXY_TYPE_SOCKS = "socksfirewallproxy";
    private static final List<String> PROXY_TYPES = List.of(PROXY_TYPE_WEB, PROXY_TYPE_SECURE_WEB);

    @Inject
    private ApplicationConfig appConfig;

    @Override
    public List<SystemProxyConfig> getSysProxyConfig() {
        List<String> networkServices = getNetworkServices();
        log.info("All available network services: {}", networkServices);
        if (networkServices.isEmpty()) {
            log.error("No available network services");
            return Collections.emptyList();
        }

        List<SystemProxyConfig> result = new ArrayList<>();
        try {
            for (String networkService: networkServices) {
                for (String proxyType : PROXY_TYPES) {
                    String httpProxySettings = getProxySettings(networkService, proxyType);
                    SystemProxyConfig httpProxyConfig = SystemProxyConfig.parseFromLines(httpProxySettings);
                    httpProxyConfig.setProxyType(proxyType);
                    httpProxyConfig.setNetworkService(networkService);
                    result.add(httpProxyConfig);
                }
            }
        } catch (Exception e) {
            log.error("Error in getting MacOs system proxy settings.", e);
        }
        return result;
    }

    private String getProxySettings(String networkService, String proxyType) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(
                NETWORK_SETUP,
                "-get" + proxyType,
                networkService);
        return runCommand(builder);
    }

    @Override
    public void setSysProxyConfig() {
        List<String> networkServices = getNetworkServices();
        log.info("All available network services: {}", networkServices);
        if (networkServices.isEmpty()) {
            log.error("No available network services");
            return;
        }

        String host = appConfig.getHost();
        int port = appConfig.getSettings().getPort();
        boolean enabled = appConfig.getSettings().isSystemProxy();
        try {
            for (String networkService : networkServices) {
                for (String proxyType : PROXY_TYPES) {
                    if (enabled) {
                        setProxy(networkService, proxyType, host, port);
                    }
                    setProxyState(networkService, proxyType, enabled);
                }
            }
        } catch (Exception e) {
            log.error("Error in setting MacOS system proxy.", e);
        }
    }

    private List<String> getNetworkServices() {
        ProcessBuilder builder = new ProcessBuilder(NETWORK_SETUP, "-listallnetworkservices");
        String res = null;
        try {
            res = runCommand(builder);
        } catch (Exception e) {
            log.error("Error in getting available MacOS network services.", e);
        }
        if (StringUtils.isEmpty(res)) {
            return Collections.emptyList();
        }

        // parse result
        List<String> list = new ArrayList<>();
        for (String line : res.split("\n")) {
            if (line != null && !line.contains("*")) {
                list.add(line.trim());
            }
        }
        return list;
    }

    private void setProxy(String networkService, String proxyType, String proxyAddress, int proxyPort) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(
                NETWORK_SETUP,
                "-set" + proxyType,
                networkService,
                proxyAddress,
                String.valueOf(proxyPort)
        );
        String res = runCommand(builder);
        log.info("Setting MacOs proxy for {}, type: {}, host:{}-{}, res: {}", networkService, proxyType, proxyAddress, proxyPort, res);
    }

    private void setProxyState(String networkService, String proxyType, boolean state) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(
                NETWORK_SETUP,
                "-set" + proxyType + "state",
                networkService,
                state ? "on" : "off"
        );
        String res = runCommand(builder);
        log.info("Setting MacOs proxy for {}, type: {}, state: {}, res: {}", networkService, proxyType, state, res);
    }

    private String runCommand(ProcessBuilder builder) throws IOException, InterruptedException {
        Process process = builder.start();
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
        }

        process.waitFor();
        return result.toString();
    }
}
