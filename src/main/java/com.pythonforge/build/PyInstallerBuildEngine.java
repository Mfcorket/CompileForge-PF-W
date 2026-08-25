package com.pythonforge.build;

import com.pythonforge.history.BuildLogger;
import com.pythonforge.model.BuildStatus;
import com.pythonforge.model.PyInstallerBuildConfig;
import com.pythonforge.model.PythonEnvironment;
import com.pythonforge.service.PyInstallerCommandBuilder;
import com.pythonforge.util.LogUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

/**
 * PyInstaller 构建核心引擎。
 *
 * <p>
 * P4.10.4：
 * </p>
 *
 * <ul>
 *     <li>执行 PyInstaller 构建</li>
 *     <li>实时输出构建日志</li>
 *     <li>保存构建日志文件</li>
 *     <li>自动识别生成的 EXE</li>
 *     <li>支持 ONEFILE / ONEDIR</li>
 *     <li>支持取消构建</li>
 *     <li>BuildResult 携带 EXE、输出目录和日志文件</li>
 * </ul>
 */
public final class PyInstallerBuildEngine {

    private static final Logger LOGGER =
            LogUtils.getLogger(
                    PyInstallerBuildEngine.class
            );


    /**
     * PyInstaller 命令生成器。
     */
    private final PyInstallerCommandBuilder commandBuilder =
            new PyInstallerCommandBuilder();


    /**
     * 外部进程执行器。
     */
    private final BuildProcessExecutor executor =
            new BuildProcessExecutor();


    /**
     * 当前构建日志。
     */
    private BuildLogger buildLogger;


    /**
     * 当前构建是否被取消。
     */
    private volatile boolean cancelled;


    /**
     * 执行构建。
     *
     * @param environment Python 环境
     * @param config 构建配置
     * @return 构建结果
     */
    public BuildResult build(
            PythonEnvironment environment,
            PyInstallerBuildConfig config) {

        return build(
                environment,
                config,
                null
        );
    }


    /**
     * 执行构建。
     *
     * @param environment Python 环境
     * @param config 构建配置
     * @param listener 构建日志监听器
     * @return 构建结果
     */
    public BuildResult build(
            PythonEnvironment environment,
            PyInstallerBuildConfig config,
            BuildLogListener listener) {

        /*
         * 每次构建开始时重置取消状态。
         */
        cancelled = false;

        /*
         * 防止上一次日志对象没有正常关闭。
         */
        closeLogger();

        try {

            /*
             * ====================================================
             * 1. 基础检查
             * ====================================================
             */

            if (environment == null) {

                return failed(
                        null,
                        "Python 环境为空。",
                        listener
                );
            }


            if (!environment.isPyInstallerAvailable()) {

                return failed(
                        null,
                        "当前 Python 环境没有安装 PyInstaller。",
                        listener
                );
            }


            if (config == null) {

                return failed(
                        null,
                        "构建配置为空。",
                        listener
                );
            }


            if (config.getEntryFile() == null) {

                return failed(
                        config,
                        "没有指定 Python 入口文件。",
                        listener
                );
            }


            if (!Files.isRegularFile(
                    config.getEntryFile()
            )) {

                return failed(
                        config,
                        "Python 入口文件不存在："
                                + config.getEntryFile(),
                        listener
                );
            }


            if (config.getOutputDirectory() == null) {

                return failed(
                        config,
                        "没有指定输出目录。",
                        listener
                );
            }


            /*
             * ====================================================
             * 2. 创建构建日志
             * ====================================================
             */

            buildLogger =
                    new BuildLogger(
                            config.getName()
                    );


            log(
                    listener,
                    "PythonForge-win 构建开始。"
            );

            log(
                    listener,
                    "应用名称："
                            + config.getName()
            );

            log(
                    listener,
                    "Python："
                            + environment.getExecutable()
            );

            log(
                    listener,
                    "Python 版本："
                            + environment.getVersion()
            );

            log(
                    listener,
                    "入口文件："
                            + config.getEntryFile()
            );

            log(
                    listener,
                    "输出目录："
                            + config.getOutputDirectory()
            );

            log(
                    listener,
                    "日志文件："
                            + getLogFile()
            );

            log(
                    listener,
                    ""
            );


            /*
             * ====================================================
             * 3. 准备目录
             * ====================================================
             */

            prepareDirectories(
                    config,
                    listener
            );


            /*
             * ====================================================
             * 4. 生成 PyInstaller 命令
             * ====================================================
             */

            log(
                    listener,
                    "正在准备 PyInstaller..."
            );


            List<String> command =
                    commandBuilder.build(
                            environment.getExecutable(),
                            config
                    );


            log(
                    listener,
                    "PyInstaller 命令："
            );


            log(
                    listener,
                    formatCommand(
                            command
                    )
            );


            log(
                    listener,
                    ""
            );


            log(
                    listener,
                    "开始构建..."
            );


            /*
             * ====================================================
             * 5. 执行 PyInstaller
             * ====================================================
             */

            int exitCode =
                    executor.execute(
                            command,
                            message -> {

                                /*
                                 * 外部进程输出同时写入：
                                 *
                                 * 1. JavaFX UI
                                 * 2. BuildLogger
                                 */
                                log(
                                        listener,
                                        message
                                );
                            }
                    );


            /*
             * ====================================================
             * 6. 判断取消
             * ====================================================
             */

            if (cancelled) {

                log(
                        listener,
                        ""
                );

                log(
                        listener,
                        "===== BUILD CANCELLED ====="
                );

                log(
                        listener,
                        "构建已被用户取消。"
                );


                return createResult(
                        BuildStatus.CANCELLED,
                        null,
                        config.getOutputDirectory(),
                        "用户取消了构建。"
                );
            }


            /*
             * ====================================================
             * 7. PyInstaller 构建成功
             * ====================================================
             */

            if (exitCode == 0) {

                log(
                        listener,
                        ""
                );

                log(
                        listener,
                        "PyInstaller 执行成功。"
                );


                log(
                        listener,
                        "正在查找生成的 EXE..."
                );


                Path outputFile =
                        findOutputExecutable(
                                config
                        );


                /*
                 * =================================================
                 * 8. 未找到 EXE
                 * =================================================
                 */

                if (outputFile == null) {

                    log(
                            listener,
                            "未找到生成的 EXE 文件。"
                    );


                    log(
                            listener,
                            "输出目录："
                                    + config
                                    .getOutputDirectory()
                    );


                    log(
                            listener,
                            ""
                    );


                    log(
                            listener,
                            "===== BUILD FAILED ====="
                    );


                    log(
                            listener,
                            "PyInstaller 执行成功，但未找到生成的 EXE 文件。"
                    );


                    return createResult(
                            BuildStatus.FAILED,
                            null,
                            null,
                            "PyInstaller 执行成功，但未找到生成的 EXE 文件。"
                    );
                }


                /*
                 * =================================================
                 * 9. 构建成功
                 * =================================================
                 */

                log(
                        listener,
                        ""
                );


                log(
                        listener,
                        "找到 EXE："
                                + outputFile
                );


                log(
                        listener,
                        ""
                );


                log(
                        listener,
                        "===== BUILD SUCCESS ====="
                );


                log(
                        listener,
                        "输出文件："
                                + outputFile
                );


                log(
                        listener,
                        "输出目录："
                                + config
                                .getOutputDirectory()
                );


                log(
                        listener,
                        "日志文件："
                                + getLogFile()
                );


                return createResult(
                        BuildStatus.SUCCESS,
                        outputFile,
                        config.getOutputDirectory(),
                        "构建成功。"
                );
            }


            /*
             * ====================================================
             * 10. PyInstaller 构建失败
             * ====================================================
             */

            log(
                    listener,
                    ""
            );


            log(
                    listener,
                    "===== BUILD FAILED ====="
            );


            log(
                    listener,
                    "PyInstaller 退出代码："
                            + exitCode
            );


            return createResult(
                    BuildStatus.FAILED,
                    null,
                    config == null
                            ? null
                            : config.getOutputDirectory(),
                    "PyInstaller 执行失败，退出代码："
                            + exitCode
            );


        } catch (Exception e) {

            /*
             * ====================================================
             * 11. 未处理异常
             * ====================================================
             */

            LogUtils.error(
                    LOGGER,
                    "Build failed: "
                            + e.getMessage()
            );


            log(
                    listener,
                    ""
            );


            log(
                    listener,
                    "===== BUILD FAILED ====="
            );


            log(
                    listener,
                    "异常："
                            + (
                            e.getMessage() == null
                                    ? e.getClass()
                                    .getSimpleName()
                                    : e.getMessage()
                    )
            );


            return createResult(
                    BuildStatus.FAILED,
                    null,
                    config == null
                            ? null
                            : config.getOutputDirectory(),
                    e.getMessage()
            );


        } finally {

            /*
             * ====================================================
             * 12. 无论成功、失败还是取消，都关闭日志。
             * ====================================================
             *
             * 注意：
             *
             * getLogFile() 在 BuildLogger 关闭后仍然可以
             * 返回之前保存的 Path。
             */

            closeLogger();
        }
    }


    /**
     * 准备构建目录。
     */
    private void prepareDirectories(
            PyInstallerBuildConfig config,
            BuildLogListener listener)
            throws IOException {

        if (config.getOutputDirectory() != null) {

            Files.createDirectories(
                    config.getOutputDirectory()
            );
        }


        if (config.getWorkDirectory() != null) {

            Files.createDirectories(
                    config.getWorkDirectory()
            );
        }


        if (config.getSpecDirectory() != null) {

            Files.createDirectories(
                    config.getSpecDirectory()
            );
        }


        log(
                listener,
                "构建目录准备完成。"
        );
    }


    /**
     * 查找 PyInstaller 生成的 EXE。
     *
     * <p>
     * ONEFILE：
     *
     * <pre>
     * dist/MyApp.exe
     * </pre>
     *
     * ONEDIR：
     *
     * <pre>
     * dist/MyApp/MyApp.exe
     * </pre>
     * </p>
     */
    private Path findOutputExecutable(
            PyInstallerBuildConfig config) {

        Path outputDirectory =
                config.getOutputDirectory();


        if (outputDirectory == null) {

            return null;
        }


        String name =
                config.getName();


        if (name == null
                || name.isBlank()) {

            name =
                    removeExtension(
                            config.getEntryFile()
                                    .getFileName()
                                    .toString()
                    );
        }


        /*
         * ========================================================
         * ONEFILE
         * ========================================================
         */

        if (config.getMode()
                == PyInstallerBuildConfig.Mode.ONEFILE) {

            Path exe =
                    outputDirectory.resolve(
                            name + ".exe"
                    );


            if (Files.isRegularFile(exe)) {

                return exe
                        .toAbsolutePath()
                        .normalize();
            }


            return findExeInDirectory(
                    outputDirectory,
                    name
            );
        }


        /*
         * ========================================================
         * ONEDIR
         * ========================================================
         */

        Path applicationDirectory =
                outputDirectory.resolve(
                        name
                );


        Path exe =
                applicationDirectory.resolve(
                        name + ".exe"
                );


        if (Files.isRegularFile(exe)) {

            return exe
                    .toAbsolutePath()
                    .normalize();
        }


        /*
         * 在 dist/name 中查找。
         */
        Path result =
                findExeInDirectory(
                        applicationDirectory,
                        name
                );


        if (result != null) {

            return result;
        }


        /*
         * 最后扫描 dist。
         */
        return findExeInDirectory(
                outputDirectory,
                name
        );
    }


    /**
     * 在目录中查找指定名称的 EXE。
     */
    private Path findExeInDirectory(
            Path directory,
            String expectedName) {

        if (directory == null
                || !Files.isDirectory(directory)) {

            return null;
        }


        try {

            try (
                    var stream =
                            Files.list(
                                    directory
                            )
            ) {

                return stream
                        .filter(
                                Files::isRegularFile
                        )
                        .filter(
                                path ->
                                        path.getFileName()
                                                .toString()
                                                .toLowerCase()
                                                .endsWith(".exe")
                        )
                        .filter(
                                path ->
                                        expectedName == null
                                                ||
                                                path.getFileName()
                                                        .toString()
                                                        .equalsIgnoreCase(
                                                                expectedName
                                                                        + ".exe"
                                                        )
                        )
                        .findFirst()
                        .map(
                                path ->
                                        path.toAbsolutePath()
                                                .normalize()
                        )
                        .orElse(null);
            }

        } catch (IOException e) {

            LogUtils.warning(
                    LOGGER,
                    "Failed to scan output directory: "
                            + e.getMessage()
            );

            return null;
        }
    }


    /**
     * 删除文件扩展名。
     */
    private String removeExtension(
            String fileName) {

        if (fileName == null
                || fileName.isBlank()) {

            return "PythonForgeApp";
        }


        int index =
                fileName.lastIndexOf('.');


        if (index > 0) {

            return fileName.substring(
                    0,
                    index
            );
        }


        return fileName;
    }


    /**
     * 构建失败结果。
     */
    private BuildResult failed(
            PyInstallerBuildConfig config,
            String message,
            BuildLogListener listener) {

        log(
                listener,
                ""
        );


        log(
                listener,
                "===== BUILD FAILED ====="
        );


        log(
                listener,
                message
        );


        return new BuildResult(
                BuildStatus.FAILED,
                null,
                config == null
                        ? null
                        : config.getOutputDirectory(),
                getLogFile(),
                message
        );
    }


    /**
     * 将命令转换为日志文本。
     *
     * <p>
     * 包含空格的参数使用双引号包裹。
     * </p>
     */
    private String formatCommand(
            List<String> command) {

        StringBuilder result =
                new StringBuilder();


        for (String argument : command) {

            if (result.length() > 0) {

                result.append(' ');
            }


            if (argument.contains(" ")
                    || argument.contains("\t")) {

                result.append('"');

                result.append(
                        argument
                );

                result.append('"');

            } else {

                result.append(
                        argument
                );
            }
        }


        return result.toString();
    }


    /**
     * 同时输出到：
     *
     * <ul>
     *     <li>界面日志</li>
     *     <li>构建日志文件</li>
     * </ul>
     */
    private void log(
            BuildLogListener listener,
            String message) {

        /*
         * 输出到 UI。
         */
        if (listener != null) {

            listener.onLog(
                    message
            );
        }


        /*
         * 输出到日志文件。
         */
        if (buildLogger != null) {

            try {

                buildLogger.write(
                        message
                );

            } catch (Exception e) {

                LogUtils.warning(
                        LOGGER,
                        "Failed to write build log: "
                                + e.getMessage()
                );
            }
        }
    }


    /**
     * 获取当前构建日志文件。
     *
     * @return 日志文件路径；尚未创建日志时返回 null
     */
    public Path getLogFile() {

        if (buildLogger == null) {

            return null;
        }


        return buildLogger.getLogFile();
    }


    /**
     * 取消构建。
     */
    public void cancel() {

        cancelled = true;

        executor.cancel();
    }


    /**
     * 判断当前是否正在执行构建。
     *
     * @return true 表示正在执行
     */
    public boolean isRunning() {

        return executor.isRunning();
    }


    /**
     * 关闭当前日志。
     */
    private void closeLogger() {

        if (buildLogger == null) {

            return;
        }


        try {

            buildLogger.close();

        } catch (Exception e) {

            LogUtils.warning(
                    LOGGER,
                    "Failed to close build logger: "
                            + e.getMessage()
            );

        } finally {

            /*
             * 注意：
             *
             * 这里不能把 buildLogger 设为 null，
             * 否则 finally 中无法取得 logFile。
             *
             * 因此这里只关闭，不清空引用。
             */
        }
    }

    private BuildResult createResult(
            BuildStatus status,
            Path outputFile,
            Path outputDirectory,
            String message) {

        Path logFile = null;

        if (buildLogger != null) {
            logFile = buildLogger.getLogFile();
        }

        return new BuildResult(
                status,
                outputFile,
                outputDirectory,
                logFile,
                message
        );
    }
}

