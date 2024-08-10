package com.catas.wicked.server.worker;

import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.config.SystemProxyConfig;
import com.catas.wicked.common.constant.ServerStatus;
import com.catas.wicked.common.constant.SystemProxyStatus;
import com.catas.wicked.server.provider.SysProxyProvider;
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
        if (appConfig.getServerStatus() != ServerStatus.RUNNING) {
            log.warn("Server is not running");
            appConfig.setSystemProxyStatus(SystemProxyStatus.DISABLED);
            return;
        }
        if (!appConfig.getSettings().isSystemProxy()) {
            appConfig.setSystemProxyStatus(SystemProxyStatus.OFF);
            return;
        }

        List<SystemProxyConfig> configList = proxyProvider.getSysProxyConfig();
    }

    @Override
    public long getDelay() {
        return 5 * 1000L;
    }
}
