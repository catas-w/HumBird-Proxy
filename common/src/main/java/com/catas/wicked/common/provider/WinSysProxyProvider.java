package com.catas.wicked.common.provider;

import com.catas.wicked.common.config.SystemProxyConfig;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Singleton
@Requires(os = Requires.Family.WINDOWS)
public class WinSysProxyProvider implements SysProxyProvider {

    @Override
    public List<SystemProxyConfig> getSysProxyConfig() {
        return null;
    }

    @Override
    public void setSysProxyConfig() {
    }

    @Override
    public List<String> getBypassDomains() {
        return null;
    }

    @Override
    public void setBypassDomains(List<String> domains) {

    }
}
