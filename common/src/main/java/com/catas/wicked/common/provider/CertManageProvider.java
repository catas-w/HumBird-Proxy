package com.catas.wicked.common.provider;

import com.catas.wicked.common.config.CertificateConfig;

import java.io.InputStream;
import java.util.List;

public interface CertManageProvider {

    void importCert(InputStream inputStream);

    void exportCert(String certId);

    List<CertificateConfig> getCertList();

    CertificateConfig getSelectedCert();

    CertificateConfig getCertById(String certId);
}
