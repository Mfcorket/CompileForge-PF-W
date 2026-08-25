package com.pythonforge.util;

/**
 * Windows平台工具。
 */
public final class WindowsUtils {

    private WindowsUtils() {
    }

    public static boolean isWindows() {
        String osName = System.getProperty("os.name");

        return osName != null
                && osName.toLowerCase()
                .contains("win");
    }

    public static String getOsName() {
        return System.getProperty("os.name", "Unknown");
    }

    public static String getOsArchitecture() {
        return System.getProperty("os.arch", "Unknown");
    }
}

