package com.catas.wicked.server.provider;

import com.catas.wicked.common.config.SystemProxyConfig;

import java.util.List;

public interface SysProxyProvider {

    /**
     * get system proxy config in current os
     */
    List<SystemProxyConfig> getSysProxyConfig();

    /**
     * set system proxy
     */
    void setSysProxyConfig();

    /**
     * get system proxy bypass domains in current os
     */
    List<String> getBypassDomains();

    /**
     * set system proxy bypass domains
     */
    void setBypassDomains(List<String> domains);
}
