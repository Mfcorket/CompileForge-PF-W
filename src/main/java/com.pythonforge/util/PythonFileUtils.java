package com.pythonforge.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Python 文件工具。
 */
public final class PythonFileUtils {

    private PythonFileUtils() {
    }

    /**
     * 判断是否为 Python 源文件。
     *
     * @param path 文件
     * @return 是否为 .py
     */
    public static boolean isPythonSource(
            Path path) {

        return hasExtension(
                path,
                ".py"
        );
    }

    /**
     * 判断是否为 Python 字节码。
     *
     * @param path 文件
     * @return 是否为 .pyc
     */
    public static boolean isPythonBytecode(
            Path path) {

        return hasExtension(
                path,
                ".pyc"
        );
    }

    /**
     * 判断是否为 Python 文件。
     *
     * @param path 文件
     * @return 是否为 .py 或 .pyc
     */
    public static boolean isPythonFile(
            Path path) {

        return isPythonSource(path)
                || isPythonBytecode(path);
    }

    /**
     * 获取文件扩展名。
     *
     * @param path 文件
     * @return 扩展名
     */
    public static String getExtension(
            Path path) {

        if (path == null) {
            return "";
        }

        Path fileName =
                path.getFileName();

        if (fileName == null) {
            return "";
        }

        String name =
                fileName.toString();

        int index =
                name.lastIndexOf('.');

        if (
                index < 0
                        ||
                        index == name.length() - 1
        ) {

            return "";
        }

        return name
                .substring(index)
                .toLowerCase(
                        Locale.ROOT
                );
    }

    /**
     * 判断指定扩展名。
     */
    private static boolean hasExtension(
            Path path,
            String extension) {

        if (
                path == null
                        ||
                        extension == null
        ) {

            return false;
        }

        if (!Files.isRegularFile(path)) {
            return false;
        }

        return getExtension(path)
                .equalsIgnoreCase(
                        extension
                );
    }
}