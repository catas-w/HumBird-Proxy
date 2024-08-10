package com.catas.wicked.server.worker;

import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.constant.ServerStatus;
import com.catas.wicked.common.constant.SystemProxyStatus;
import com.catas.wicked.server.provider.SysProxyProvider;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class SystemProxyWorker extends AbstractScheduledWorker{

    @Inject
    private ApplicationConfig appConfig;

    @Inject
    private SysProxyProvider proxyProvider;

    private static final long DEFAULT_DELAY = 15 * 1000L;

    public SystemProxyWorker() {
        super(DEFAULT_DELAY);
    }

    @PostConstruct
    public void init() {
        log.info("Provider: {}", proxyProvider);
        proxyProvider.setSysProxyConfig();
    }

    @Override
    protected void doWork() {
        if (appConfig == null || appConfig.getServerStatus() != ServerStatus.RUNNING) {
            log.warn("Server is not running");
            appConfig.setSystemProxyStatus(SystemProxyStatus.DISABLED);
            return;
        }
        if (!appConfig.getSettings().isSystemProxy()) {
            appConfig.setSystemProxyStatus(SystemProxyStatus.OFF);
            return;
        }

        // SystemProxyConfig sysProxyConfig = proxyProvider.getSysProxyConfig();

    }

}
