package com.catas.wicked.server.provider;

import com.catas.wicked.common.config.SystemProxyConfig;

import java.util.List;

public interface SysProxyProvider {

    List<SystemProxyConfig> getSysProxyConfig();

    void setSysProxyConfig();
}
