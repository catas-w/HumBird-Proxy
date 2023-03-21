package com.catas.wicked.proxy.cert;

import com.catas.wicked.proxy.config.ApplicationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

@Component
public class CertPool {
    private final Map<Integer, Map<String, X509Certificate>> certCache = new WeakHashMap<>();

    @Autowired
    private ApplicationConfig applicationConfig;

    @Autowired
    private CertService certService;

    public X509Certificate getCert(Integer port, String host)
            throws Exception {
        X509Certificate cert = null;
        if (host != null) {
            Map<String, X509Certificate> portCertCache = certCache.get(port);
            if (portCertCache == null) {
                portCertCache = new HashMap<>();
                certCache.put(port, portCertCache);
            }
            String key = host.trim().toLowerCase();
            if (portCertCache.containsKey(key)) {
                return portCertCache.get(key);
            } else {
                cert = certService.genCert(applicationConfig.getIssuer(), applicationConfig.getCaPriKey(),
                        applicationConfig.getCaNotBefore(), applicationConfig.getCaNotAfter(),
                        applicationConfig.getServerPubKey(), key);
                portCertCache.put(key, cert);
            }
        }
        return cert;
    }

    public void clear() {
        certCache.clear();
    }
}
