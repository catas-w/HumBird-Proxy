package com.catas.wicked.common.util;

/**
 * @deprecated
 * @see io.micronaut.context.condition.OperatingSystem
 */
@Deprecated
public class OSUtils {

    public static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    public static final String OS_VERSION = System.getProperty("os.version").toLowerCase();
    public static final String OS_ARCH = System.getProperty("os.arch").toLowerCase();

    public static String getOsInfo() {
        return String.format("%s-%s-%s", OS_NAME, OS_ARCH, OS_VERSION);
    }

    public static boolean isWindows() {
        return (OS_NAME.contains("win"));
    }

    public static boolean isMacOS() {
        return (OS_NAME.contains("mac"));
    }

    public static boolean isLinux() {
        return (OS_NAME.contains("nix") || OS_NAME.contains("nux") || OS_NAME.contains("aix"));
    }
}
