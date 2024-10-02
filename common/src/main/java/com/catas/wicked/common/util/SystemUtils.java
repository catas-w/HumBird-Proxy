package com.catas.wicked.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @see io.micronaut.context.condition.OperatingSystem
 */
public class SystemUtils {

    public static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    public static final String OS_VERSION = System.getProperty("os.version").toLowerCase();
    public static final String OS_ARCH = System.getProperty("os.arch").toLowerCase();
    public static final String USER_HOME = System.getProperty("user.home");

    public static String getOsInfo() {
        return String.format("%s-%s-%s", OS_NAME, OS_ARCH, OS_VERSION);
    }

    public static String runCommand(ProcessBuilder builder) throws IOException, InterruptedException {
        Process process = builder.start();
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
        }

        process.waitFor();
        return result.toString();
    }
}
