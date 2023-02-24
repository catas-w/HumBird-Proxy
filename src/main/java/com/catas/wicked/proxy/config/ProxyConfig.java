package com.catas.wicked.proxy.config;

import com.catas.wicked.proxy.common.ProxyType;
import lombok.Data;

@Data
public class ProxyConfig {

    private String host = "127.0.0.1";

    private int port = 9919;

    private ProxyType proxyType = ProxyType.HTTP;

    private boolean handleSsl;

    private boolean recording = true;

    private int throttleLevel = 0;

    private ProxyConfig() {}

    public static ProxyConfig getInstance() {
        return Holder.instance;
    }

    public static class Holder {
        public static final ProxyConfig instance = new ProxyConfig();
    }
}
