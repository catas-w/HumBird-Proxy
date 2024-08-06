package com.catas.wicked.server.worker;

import com.catas.wicked.common.config.ApplicationConfig;
import com.catas.wicked.common.config.SystemProxyConfig;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@Requires(os = Requires.Family.WINDOWS)
public class WindowsSystemProxyWorker implements SysProxyProvider{

    @Override
    public SystemProxyConfig getSysProxyConfig() {
        return null;
    }

    @Override
    public void setSysProxyConfig() {
        System.out.println("Set from windows");
    }
}
