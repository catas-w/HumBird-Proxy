package com.catas.wicked.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

@Slf4j
public class SslUtils {

    /**
     * @param needVerifyCa need verify certificate
     * @param caInputStream CA
     * @param alias unique alias for certificate
     * @return SSLConnectionSocketFactory
     */
    public static SSLConnectionSocketFactory getSocketFactory(boolean needVerifyCa,
                                                               InputStream caInputStream,
                                                               String alias) throws Exception {
        X509TrustManager x509TrustManager;
        if (needVerifyCa) {
            KeyStore keyStore = getKeyStore(caInputStream, alias);
            TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
            }
            x509TrustManager = (X509TrustManager) trustManagers[0];
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{x509TrustManager}, new SecureRandom());
            return new SSLConnectionSocketFactory(sslContext);
        }

        x509TrustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) {
            }
            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) {
            }
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{x509TrustManager}, new SecureRandom());
        return new SSLConnectionSocketFactory(sslContext);
    }

    /**
     * @param caInputStream ca
     * @param alias unique alias for certificate
     * @return keyStore
     */
    public static KeyStore getKeyStore(InputStream caInputStream, String alias) throws Exception {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null);
        keyStore.setCertificateEntry(alias, certificateFactory.generateCertificate(caInputStream));
        return keyStore;
    }
}
