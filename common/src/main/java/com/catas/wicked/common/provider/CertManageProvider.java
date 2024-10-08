package com.catas.wicked.common.provider;

import com.catas.wicked.common.config.CertificateConfig;

import java.io.File;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

public interface CertManageProvider {

    CertificateConfig importCert(InputStream inputStream, InputStream priKeyInputStream);

    void exportCert(String certId, File file);

    List<CertificateConfig> getCertList();

    CertificateConfig getSelectedCert();

    boolean deleteCertConfig(String certId);

    CertificateConfig getCertConfigById(String certId);

    X509Certificate getCertById(String certId) throws Exception;

    PrivateKey getPriKeyById(String certId) throws Exception;

    String getCertPEM(String id) throws Exception;

    String getPriKeyPEM(String id) throws Exception;

    Map<String, String> getCertInfo(String certId) throws Exception;

    boolean isCertMatchingPriKey(X509Certificate certificate, PrivateKey privateKey);
}
