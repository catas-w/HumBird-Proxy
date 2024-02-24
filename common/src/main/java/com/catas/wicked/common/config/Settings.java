package com.catas.wicked.common.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;

@Slf4j
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Settings {

    public enum CertType {
        BUILT_IN,
        CUSTOM,
    }

    /**
     * General settings
     */
    private String language;
    private boolean recording = true;
    private int maxContentSize = 10;
    private List<String> recordIncludeList;
    private List<String> recordExcludeList;

    /**
     * Server settings
     */
    private int port = 9999;
    private boolean systemProxy = true;
    private List<String> sysProxyExcludeList;

    /**
     * Ssl settings
     */
    private boolean handleSsl = true;
    private CertType certType = CertType.BUILT_IN;
    private File localCertificate;
    private List<String> sslExcludeList;

    /**
     * External proxy settings
     */
    private ExternalProxyConfig externalProxy;

    /**
     * Throttle settings
     */
    private boolean throttle = true;
    private int throttleLevel = 0;

}
