package com.catas.wicked.common.config;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class SystemProxyConfig {

    /**
     * Wi-fi, ethernet
     */
    private String networkService;

    /**
     * HTTP, HTTPS
     */
    private String proxyType;

    private boolean enabled;

    private String server;

    private int port;

    private boolean authEnabled;

    private String username;

    private String password;


    /**
     * @param lines
     * Enabled: Yes
     * Server: 127.0.0.1
     * Port: 7890
     * Authenticated Proxy Enabled: 0
     */
    public static SystemProxyConfig parseFromLines(String lines) {
        if (lines == null) {
            return null;
        }
        SystemProxyConfig config = new SystemProxyConfig();
        for (String line : lines.split("\n")) {
            String[] parts = line.split(":", 2);
            if (parts.length != 2) {
                continue;
            }
            // Trim the key and value and add them to the map
            String key = parts[0].trim();
            String value = parts[1].trim();
            switch (key.toLowerCase()) {
                case "enabled" -> config.setEnabled(StringUtils.equalsIgnoreCase(value, "Yes"));
                case "server" -> config.setServer(value);
                case "port" -> config.setPort(Integer.parseInt(value));
                case "authenticated proxy enabled" -> config.setAuthEnabled(value.equals("1"));
            }
        }
        return config;
    }
}
