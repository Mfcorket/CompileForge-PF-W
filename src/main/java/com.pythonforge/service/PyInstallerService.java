package com.pythonforge.service;

import com.pythonforge.model.PyInstallerInfo;
import com.pythonforge.model.PythonEnvironment;
import com.pythonforge.util.LogUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * PyInstaller 服务。
 *
 * <p>
 * 当前负责：
 * </p>
 *
 * <ul>
 *     <li>检测 PyInstaller</li>
 *     <li>获取 PyInstaller 版本</li>
 *     <li>验证 PyInstaller 是否可以执行</li>
 * </ul>
 *
 * <p>
 * 当前版本只允许通过：
 * </p>
 *
 * <pre>
 * python.exe -m PyInstaller
 * </pre>
 *
 * <p>
 * 执行 PyInstaller。
 * </p>
 */
public final class PyInstallerService {

    private static final Logger LOGGER =
            LogUtils.getLogger(
                    PyInstallerService.class
            );

    private final PythonEnvironmentService
            environmentService;

    /**
     * 默认构造。
     */
    public PyInstallerService() {

        this.environmentService =
                new PythonEnvironmentService();
    }

    /**
     * 使用指定环境服务。
     *
     * @param environmentService 环境服务
     */
    public PyInstallerService(
            PythonEnvironmentService
                    environmentService) {

        if (environmentService == null) {

            throw new IllegalArgumentException(
                    "environmentService must not be null"
            );
        }

        this.environmentService =
                environmentService;
    }

    /**
     * 检测当前 Python 环境中的 PyInstaller。
     *
     * @return PyInstaller 信息
     */
    public Optional<PyInstallerInfo>
    detect() {

        Optional<PythonEnvironment>
                current =
                environmentService
                        .getCurrentEnvironment();

        if (current.isEmpty()) {

            LogUtils.warning(
                    LOGGER,
                    "No current Python environment."
            );

            return Optional.empty();
        }

        return detect(
                current.get()
        );
    }

    /**
     * 检测指定 Python 环境中的 PyInstaller。
     *
     * @param environment Python 环境
     * @return PyInstaller 信息
     */
    public Optional<PyInstallerInfo>
    detect(
            PythonEnvironment environment) {

        if (environment == null) {

            return Optional.empty();
        }

        Path executable =
                environment.getExecutable();

        if (executable == null) {

            return Optional.empty();
        }

        LogUtils.info(
                LOGGER,
                "Checking PyInstaller: "
                        + executable
        );

        String version =
                execute(
                        executable,
                        "-m",
                        "PyInstaller",
                        "--version"
                );

        if (
                version == null
                        ||
                        version.isBlank()
        ) {

            LogUtils.warning(
                    LOGGER,
                    "PyInstaller is not available: "
                            + executable
            );

            return Optional.of(
                    new PyInstallerInfo(
                            environment,
                            false,
                            null,
                            false
                    )
            );
        }

        String cleanVersion =
                cleanVersion(version);

        boolean executableResult =
                testExecution(
                        executable
                );

        boolean installed =
                true;

        PyInstallerInfo info =
                new PyInstallerInfo(
                        environment,
                        installed,
                        cleanVersion,
                        executableResult
                );

        LogUtils.info(
                LOGGER,
                "PyInstaller result: "
                        + info
        );

        return Optional.of(info);
    }

    /**
     * 测试 PyInstaller 是否可以正常启动。
     *
     * <p>
     * 使用 --help，不执行实际构建。
     * </p>
     *
     * @param executable Python
     * @return 是否可以执行
     */
    private boolean testExecution(
            Path executable) {

        String output =
                execute(
                        executable,
                        "-m",
                        "PyInstaller",
                        "--help"
                );

        return output != null
                && !output.isBlank();
    }

    /**
     * 执行 Python 命令。
     *
     * @param executable Python
     * @param arguments 参数
     * @return 输出
     */
    private String execute(
            Path executable,
            String... arguments) {

        try {

            List<String> command =
                    new ArrayList<>();

            command.add(
                    executable.toString()
            );

            for (String argument : arguments) {

                command.add(argument);
            }

            ProcessBuilder builder =
                    new ProcessBuilder(
                            command
                    );

            builder
                    .redirectErrorStream(true);

            Process process =
                    builder.start();

            StringBuilder output =
                    new StringBuilder();

            try (
                    BufferedReader reader =
                            new BufferedReader(
                                    new InputStreamReader(
                                            process.getInputStream(),
                                            StandardCharsets.UTF_8
                                    )
                            )
            ) {

                String line;

                while (
                        (line =
                                reader.readLine())
                                != null
                ) {

                    if (
                            output.length()
                                    > 0
                    ) {

                        output.append('\n');
                    }

                    output.append(line);
                }
            }

            int exitCode =
                    process.waitFor();

            if (exitCode != 0) {

                return null;
            }

            return output
                    .toString()
                    .trim();

        } catch (IOException e) {

            LogUtils.warning(
                    LOGGER,
                    "Failed to execute PyInstaller: "
                            + e.getMessage()
            );

            return null;

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            LogUtils.warning(
                    LOGGER,
                    "PyInstaller process interrupted."
            );

            return null;
        }
    }

    /**
     * 清理版本字符串。
     *
     * @param value 原始版本
     * @return 版本
     */
    private String cleanVersion(
            String value) {

        if (value == null) {

            return null;
        }

        String result =
                value.trim();

        if (
                result
                        .toLowerCase()
                        .startsWith(
                                "pyinstaller"
                        )
        ) {

            result =
                    result.replaceFirst(
                            "(?i)^PyInstaller\\s*",
                            ""
                    ).trim();
        }

        return result;
    }
}
