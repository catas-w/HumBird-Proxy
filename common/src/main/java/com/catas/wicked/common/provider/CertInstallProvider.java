package com.catas.wicked.common.provider;

public interface CertInstallProvider {


    boolean checkCertInstalled(String certName, String SHA256);

    /**
     * install certificate on local system
     * @param certPath path to x509certificate
     */
    void install(String certPath);
}
