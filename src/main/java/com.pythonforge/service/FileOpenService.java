package com.pythonforge.service;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * PythonForge-win 文件打开服务。
 *
 * <p>
 * 统一负责 PythonForge-win 中的文件和目录打开操作。
 *
 * <p>
 * Windows 环境下：
 *
 * <ul>
 *     <li>EXE 文件：通过 Windows Shell 启动</li>
 *     <li>普通文件：通过系统默认程序打开</li>
 *     <li>目录：通过 Windows Explorer 打开</li>
 * </ul>
 */
public final class FileOpenService {

    /**
     * Windows Explorer。
     */
    private static final String EXPLORER =
            "explorer.exe";

    /**
     * Windows CMD。
     */
    private static final String CMD =
            "cmd.exe";


    /**
     * 创建文件打开服务。
     */
    public FileOpenService() {
    }


    /**
     * 打开文件。
     *
     * <p>
     * 如果文件是 EXE，则使用 Windows Shell
     * 启动，而不是 Desktop.open()。
     *
     * @param path 文件路径
     * @return true 表示已经提交打开请求
     * @throws IOException 打开失败
     */
    public boolean openFile(
            Path path)
            throws IOException {

        Path normalizedPath =
                validateFile(path);


        /*
         * EXE 文件不能继续使用
         * Desktop.open()。
         *
         * Windows 下直接交给 Shell。
         */
        if (isExecutable(
                normalizedPath
        )) {

            return openExecutable(
                    normalizedPath
            );
        }


        /*
         * 普通文件使用系统默认程序。
         */
        return openWithDesktop(
                normalizedPath
        );
    }


    /**
     * 使用 Windows Shell 启动 EXE。
     *
     * <p>
     * 等价于用户在 Windows Explorer
     * 中双击 EXE。
     *
     * @param path EXE 文件
     * @return true 表示启动请求已经提交
     * @throws IOException 启动失败
     */
    public boolean openExecutable(
            Path path)
            throws IOException {

        Path normalizedPath =
                validateFile(path);


        if (!isExecutable(
                normalizedPath
        )) {

            throw new IOException(
                    "指定文件不是 EXE 文件：\n"
                            + normalizedPath
            );
        }


        /*
         * Windows:
         *
         * cmd.exe /c start "" "A:\Test\dist\test\test.exe"
         *
         * 第一个 "" 是 start 命令的窗口标题。
         *
         * 这是 Windows start 命令的标准写法，
         * 可以正确处理带空格的路径。
         */
        ProcessBuilder processBuilder =
                new ProcessBuilder(
                        CMD,
                        "/c",
                        "start",
                        "",
                        normalizedPath
                                .toString()
                );


        processBuilder
                .redirectErrorStream(true);


        try {

            processBuilder.start();

            return true;

        } catch (IOException exception) {

            throw new IOException(
                    "无法启动 EXE 文件：\n"
                            + normalizedPath
                            + "\n\n"
                            + "错误信息："
                            + getExceptionMessage(
                            exception
                    ),
                    exception
            );
        }
    }


    /**
     * 打开目录。
     *
     * <p>
     * Windows 下直接调用 Explorer。
     *
     * @param path 目录路径
     * @return true 表示已经提交打开请求
     * @throws IOException 打开失败
     */
    public boolean openDirectory(
            Path path)
            throws IOException {

        Path normalizedPath =
                validateDirectory(path);


        ProcessBuilder processBuilder =
                new ProcessBuilder(
                        EXPLORER,
                        normalizedPath.toString()
                );


        processBuilder
                .redirectErrorStream(true);


        try {

            processBuilder.start();

            return true;

        } catch (IOException exception) {

            /*
             * 如果 Explorer 启动失败，
             * 再尝试 Desktop.open()。
             */
            try {

                return openWithDesktop(
                        normalizedPath
                );

            } catch (IOException fallbackException) {

                throw new IOException(
                        "无法打开目录：\n"
                                + normalizedPath
                                + "\n\n"
                                + "错误信息："
                                + getExceptionMessage(
                                exception
                        ),
                        fallbackException
                );
            }
        }
    }


    /**
     * 打开字符串路径对应的文件。
     *
     * @param path 文件路径
     * @return true 表示已经提交打开请求
     * @throws IOException 打开失败
     */
    public boolean openFile(
            String path)
            throws IOException {

        if (path == null
                || path.isBlank()) {

            throw new IOException(
                    "文件路径为空。"
            );
        }


        try {

            return openFile(
                    Path.of(path)
            );

        } catch (Exception exception) {

            if (exception instanceof IOException) {

                throw (IOException) exception;
            }

            throw new IOException(
                    "文件路径无效：\n"
                            + path,
                    exception
            );
        }
    }


    /**
     * 打开字符串路径对应的目录。
     *
     * @param path 目录路径
     * @return true 表示已经提交打开请求
     * @throws IOException 打开失败
     */
    public boolean openDirectory(
            String path)
            throws IOException {

        if (path == null
                || path.isBlank()) {

            throw new IOException(
                    "目录路径为空。"
            );
        }


        try {

            return openDirectory(
                    Path.of(path)
            );

        } catch (Exception exception) {

            if (exception instanceof IOException) {

                throw (IOException) exception;
            }

            throw new IOException(
                    "目录路径无效：\n"
                            + path,
                    exception
            );
        }
    }


    /**
     * 判断指定文件是否可以打开。
     *
     * @param path 文件路径
     * @return true 表示文件存在且为普通文件
     */
    public boolean isFileOpenable(
            Path path) {

        if (path == null) {

            return false;
        }


        try {

            Path normalizedPath =
                    path.toAbsolutePath()
                            .normalize();


            return Files.exists(
                    normalizedPath
            )
                    &&
                    Files.isRegularFile(
                            normalizedPath
                    );

        } catch (Exception exception) {

            return false;
        }
    }


    /**
     * 判断指定目录是否可以打开。
     *
     * @param path 目录路径
     * @return true 表示目录存在且为目录
     */
    public boolean isDirectoryOpenable(
            Path path) {

        if (path == null) {

            return false;
        }


        try {

            Path normalizedPath =
                    path.toAbsolutePath()
                            .normalize();


            return Files.exists(
                    normalizedPath
            )
                    &&
                    Files.isDirectory(
                            normalizedPath
                    );

        } catch (Exception exception) {

            return false;
        }
    }


    /**
     * 判断文件是否为 EXE。
     *
     * @param path 文件路径
     * @return true 表示为 EXE 文件
     */
    private boolean isExecutable(
            Path path) {

        if (path == null) {

            return false;
        }


        String fileName =
                path.getFileName()
                        .toString();


        return fileName
                .toLowerCase()
                .endsWith(".exe");
    }


    /**
     * 使用 Java Desktop 打开普通文件。
     *
     * <p>
     * 注意：
     * 这里明确不用于 EXE。
     *
     * @param path 文件路径
     * @return true 表示已经提交打开请求
     * @throws IOException 打开失败
     */
    private boolean openWithDesktop(
            Path path)
            throws IOException {

        if (!Desktop.isDesktopSupported()) {

            throw new IOException(
                    "当前系统不支持 Desktop 文件操作。"
            );
        }


        Desktop desktop;

        try {

            desktop =
                    Desktop.getDesktop();

        } catch (
                UnsupportedOperationException exception
        ) {

            throw new IOException(
                    "当前系统不支持 Desktop 文件操作。",
                    exception
            );
        }


        if (!desktop.isSupported(
                Desktop.Action.OPEN
        )) {

            throw new IOException(
                    "当前系统不支持打开文件。"
            );
        }


        try {

            desktop.open(
                    path.toFile()
            );

            return true;

        } catch (IOException exception) {

            throw new IOException(
                    "无法打开文件：\n"
                            + path
                            + "\n\n"
                            + "错误信息："
                            + getExceptionMessage(
                            exception
                    ),
                    exception
            );
        }
    }


    /**
     * 验证文件路径。
     *
     * @param path 文件路径
     * @return 规范化后的绝对路径
     * @throws IOException 路径无效
     */
    private Path validateFile(
            Path path)
            throws IOException {

        if (path == null) {

            throw new IOException(
                    "文件路径为空。"
            );
        }


        final Path normalizedPath;

        try {

            normalizedPath =
                    path.toAbsolutePath()
                            .normalize();

        } catch (Exception exception) {

            throw new IOException(
                    "文件路径无效：\n"
                            + path,
                    exception
            );
        }


        if (!Files.exists(
                normalizedPath
        )) {

            throw new IOException(
                    "文件不存在：\n"
                            + normalizedPath
            );
        }


        if (!Files.isRegularFile(
                normalizedPath
        )) {

            throw new IOException(
                    "指定路径不是普通文件：\n"
                            + normalizedPath
            );
        }


        return normalizedPath;
    }


    /**
     * 验证目录路径。
     *
     * @param path 目录路径
     * @return 规范化后的绝对路径
     * @throws IOException 路径无效
     */
    private Path validateDirectory(
            Path path)
            throws IOException {

        if (path == null) {

            throw new IOException(
                    "目录路径为空。"
            );
        }


        final Path normalizedPath;

        try {

            normalizedPath =
                    path.toAbsolutePath()
                            .normalize();

        } catch (Exception exception) {

            throw new IOException(
                    "目录路径无效：\n"
                            + path,
                    exception
            );
        }


        if (!Files.exists(
                normalizedPath
        )) {

            throw new IOException(
                    "目录不存在：\n"
                            + normalizedPath
            );
        }


        if (!Files.isDirectory(
                normalizedPath
        )) {

            throw new IOException(
                    "指定路径不是目录：\n"
                            + normalizedPath
            );
        }


        return normalizedPath;
    }


    /**
     * 获取异常信息。
     *
     * @param exception 异常
     * @return 可显示的异常信息
     */
    private String getExceptionMessage(
            Exception exception) {

        if (exception == null) {

            return "未知错误";
        }


        String message =
                exception.getMessage();


        if (message == null
                || message.isBlank()) {

            return exception
                    .getClass()
                    .getSimpleName();
        }


        return message;
    }
}