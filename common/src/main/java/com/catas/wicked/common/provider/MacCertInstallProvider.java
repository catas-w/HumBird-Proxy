package com.catas.wicked.common.provider;

import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Slf4j
@Singleton
@Requires(os = Requires.Family.MAC_OS)
public class MacCertInstallProvider implements CertInstallProvider {

    @Override
    public boolean checkCertInstalled(String certName, String SHA256) {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "security", "find-certificate", "-a", "-c", certName, "-Z", "/Library/Keychains/System.keychain");
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            String res = output.toString();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                // Check if the output contains information about the certificate
                return res.contains(certName) && (res.contains(SHA256.toUpperCase()) || res.contains(SHA256));
            } else {
                throw new RuntimeException(res);
            }
        } catch (Exception e) {
            log.error("Error in checking cert installation in MacOS.", e);
            return false;
        }
    }

    @Override
    public void install(String certPath) {

        // Construct the command to install the certificate
        String[] command = {
                "sudo", "security", "add-trusted-cert", "-d", "-r", "trustRoot",
                "-k", "/Library/Keychains/System.keychain", certPath
        };

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        try {
            // Start the process
            Process process = processBuilder.start();

            // Wait for the process to complete and check the exit value
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Certificate installed successfully.");
            } else {
                System.err.println("Failed to install the certificate. Exit code: " + exitCode);
                throw new RuntimeException("Failed to install the certificate. Exit code: " + exitCode);
            }
        } catch (Exception e) {
            log.error("Error in installing certificate in MacOS: ", e);
            throw new RuntimeException(e.getMessage());
        }
    }
}
