package com.catas.wicked.common.provider;

import com.catas.wicked.common.util.CommonUtils;
import io.micronaut.context.annotation.Requires;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.KeyStore;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

@Slf4j
@Singleton
@Requires(os = Requires.Family.WINDOWS)
public class WinCertInstallProvider implements CertInstallProvider {

    @PostConstruct
    public void init() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Override
    public boolean checkCertInstalled(String certName, String sha256) {
        try {
            // Access the Windows-ROOT certificate store using BouncyCastle
            KeyStore keyStore = KeyStore.getInstance("Windows-ROOT", "BC");
            keyStore.load(null, null);

            // Iterate over all certificates in the Windows-ROOT store
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                Certificate cert = keyStore.getCertificate(alias);
                if (alias.equals(certName) && cert instanceof X509Certificate localCert) {
                    // if (x509Cert.equals(certificateToCheck)) {

                    if (StringUtils.equalsIgnoreCase(sha256, CommonUtils.SHA256(localCert.getEncoded()))) {
                        log.info("Certificate is installed in the Windows-ROOT store.");
                        return true;
                    }
                }
            }
            log.info("Certificate is NOT installed in the Windows-ROOT store.");
        } catch (Exception e) {
            log.error("Error in checking cert installation on Win: ", e);
        }
        return false;
    }

    @Override
    public boolean install(String certPath) {
        String[] command = {
            "cmd.exe", "/c", "certutil", "-addstore", "root", certPath
        };

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        try {
            // Start the process
            Process process = processBuilder.start();

            // Wait for the process to complete and check the exit value
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                log.info("Certificate installed successfully.");
            } else {
                throw new RuntimeException("Failed to install the certificate. Exit code: " + exitCode);
            }
        } catch (Exception e) {
            log.error("Error in installing certificate in Windows: ", e);
            throw new RuntimeException(e.getMessage());
        }
        return false;
    }
}
