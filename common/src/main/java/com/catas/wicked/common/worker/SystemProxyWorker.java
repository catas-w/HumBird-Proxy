package com.catas.wicked.common.worker;

import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.config.SystemProxyConfig;
import com.catas.wicked.common.constant.ServerStatus;
import com.catas.wicked.common.constant.SystemProxyStatus;
import com.catas.wicked.common.provider.SysProxyProvider;
import io.micronaut.core.util.CollectionUtils;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


@Slf4j
@Singleton
public class SystemProxyWorker extends AbstractScheduledWorker{

    @Inject
    private ApplicationConfig appConfig;

    @Inject
    private SysProxyProvider proxyProvider;

    @PostConstruct
    public void init() {
        log.info("Provider: {}", proxyProvider);
    }

    @Override
    protected void doWork(boolean manually) {
        // server is not running
        if (appConfig.getServerStatus() != ServerStatus.RUNNING) {
            log.warn("Server is not running");
            appConfig.setSystemProxyStatus(SystemProxyStatus.DISABLED);
            return;
        }

        if (manually) {
            forceUpdateSysProxy();
        } else {
            autoUpdateSysProxy();
        }
    }

    private void autoUpdateSysProxy() {
        // sysProxy off
        if (!appConfig.getSettings().isSystemProxy()) {
            appConfig.setSystemProxyStatus(SystemProxyStatus.OFF);
            return;
        }

        // sysProxy on
        boolean isConsistent = true;
        List<SystemProxyConfig> configList = proxyProvider.getSysProxyConfig();
        for (SystemProxyConfig config : configList) {
            if (!config.isEnabled() || !appConfig.getHost().equals(config.getServer())
                    || config.getPort() != appConfig.getSettings().getPort()) {
                isConsistent = false;
                break;
            }
        }
        if (!isConsistent) {
            log.warn("Os system proxy is not consistent with settings.");
            appConfig.setSystemProxyStatus(SystemProxyStatus.SUSPENDED);
        }
    }

    private void forceUpdateSysProxy() {
        proxyProvider.setSysProxyConfig();
        SystemProxyStatus status = appConfig.getSettings().isSystemProxy() ? SystemProxyStatus.ON : SystemProxyStatus.OFF;
        appConfig.setSystemProxyStatus(status);

        // update bypass domains
        List<String> targetList = appConfig.getSettings().getSysProxyBypassList();
        if (!CollectionUtils.isEmpty(targetList)) {
            // List<String> originList = proxyProvider.getBypassDomains();
            // List<String> finalList = new ArrayList<>(originList);
            // finalList.addAll(targetList);
            proxyProvider.setBypassDomains(targetList);
        }
    }

    @Override
    public long getDelay() {
        return 5 * 1000L;
    }
}
