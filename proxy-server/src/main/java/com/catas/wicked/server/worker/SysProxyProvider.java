package com.catas.wicked.server.worker;

import com.catas.wicked.common.config.SystemProxyConfig;

public interface SysProxyProvider {

    SystemProxyConfig getSysProxyConfig();

    void setSysProxyConfig();
}
