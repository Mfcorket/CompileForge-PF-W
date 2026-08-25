package com.pythonforge.history;

import com.pythonforge.util.LogUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

/**
 * PyInstaller 构建日志记录器。
 *
 * <p>
 * 每次构建创建一个独立日志文件。
 * </p>
 */
public final class BuildLogger {

    private static final Logger LOGGER =
            LogUtils.getLogger(
                    BuildLogger.class
            );

    /**
     * 日志根目录。
     */
    private static final Path LOG_ROOT =
            Path.of(
                    "logs",
                    "build"
            );

    /**
     * 日志文件。
     */
    private final Path logFile;

    /**
     * 日志写入器。
     */
    private BufferedWriter writer;

    /**
     * 是否已经关闭。
     */
    private boolean closed;


    /**
     * 创建构建日志。
     *
     * @param applicationName 应用名称
     */
    public BuildLogger(
            String applicationName) {

        try {

            Files.createDirectories(
                    LOG_ROOT
            );

            String name =
                    sanitizeFileName(
                            applicationName
                    );

            if (name == null
                    || name.isBlank()) {

                name = "PythonForgeApp";
            }

            String timestamp =
                    LocalDateTime.now()
                            .format(
                                    DateTimeFormatter.ofPattern(
                                            "yyyyMMdd_HHmmss_SSS"
                                    )
                            );

            logFile =
                    LOG_ROOT.resolve(
                                    name
                                            + "_"
                                            + timestamp
                                            + ".log"
                            )
                            .toAbsolutePath()
                            .normalize();

            writer =
                    Files.newBufferedWriter(
                            logFile,
                            StandardCharsets.UTF_8,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.WRITE,
                            StandardOpenOption.APPEND
                    );

            closed = false;

            write(
                    "=================================================="
            );

            write(
                    "PythonForge-win Build Log"
            );

            write(
                    "Application: "
                            + name
            );

            write(
                    "Time: "
                            + LocalDateTime.now()
            );

            write(
                    "=================================================="
            );

        } catch (IOException e) {

            throw new IllegalStateException(
                    "无法创建构建日志文件："
                            + e.getMessage(),
                    e
            );
        }
    }


    /**
     * 写入日志。
     *
     * @param message 日志内容
     */
    public synchronized void write(
            String message) {

        if (closed) {

            return;
        }

        if (message == null) {

            message = "";
        }

        try {

            writer.write(
                    message
            );

            writer.newLine();

            writer.flush();

        } catch (IOException e) {

            LogUtils.warning(
                    LOGGER,
                    "Failed to write build log: "
                            + e.getMessage()
            );
        }
    }


    /**
     * 获取日志文件。
     *
     * @return 日志文件路径
     */
    public Path getLogFile() {

        return logFile;
    }


    /**
     * 判断日志文件是否存在。
     *
     * @return true 表示存在
     */
    public boolean exists() {

        return Files.isRegularFile(
                logFile
        );
    }


    /**
     * 关闭日志。
     */
    public synchronized void close() {

        if (closed) {

            return;
        }

        closed = true;

        if (writer == null) {

            return;
        }

        try {

            writer.flush();

            writer.close();

        } catch (IOException e) {

            LogUtils.warning(
                    LOGGER,
                    "Failed to close build log: "
                            + e.getMessage()
            );
        }
    }


    /**
     * 清理文件名中的非法字符。
     */
    private String sanitizeFileName(
            String name) {

        if (name == null) {

            return null;
        }

        String result =
                name.trim();

        if (result.isEmpty()) {

            return result;
        }

        /*
         * Windows 文件名非法字符：
         *
         * < > : " / \ | ? *
         */
        result =
                result.replaceAll(
                        "[<>:\"/\\\\|?*]",
                        "_"
                );

        /*
         * 避免文件名末尾出现空格或句号。
         */
        result =
                result.replaceAll(
                        "[ .]+$",
                        ""
                );

        return result;
    }
}

