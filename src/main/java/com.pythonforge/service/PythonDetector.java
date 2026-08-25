package com.pythonforge.service;

import com.pythonforge.model.PythonArchitecture;
import com.pythonforge.model.PythonEnvironment;
import com.pythonforge.util.LogUtils;
import com.pythonforge.util.WindowsUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Python 环境检测器。
 *
 * <p>
 * PythonForge-win 当前版本仅支持 Windows。
 * </p>
 *
 * <p>
 * 检测内容：
 * </p>
 *
 * <ul>
 *     <li>Python executable</li>
 *     <li>Python version</li>
 *     <li>Python architecture</li>
 *     <li>pip</li>
 *     <li>site-packages</li>
 *     <li>PyInstaller</li>
 * </ul>
 *
 * <p>
 * Python 发现方式：
 * </p>
 *
 * <ul>
 *     <li>py -0p</li>
 *     <li>where python</li>
 *     <li>where python3</li>
 *     <li>Windows 常见安装目录</li>
 * </ul>
 */
public final class PythonDetector {

    private static final Logger LOGGER =
            LogUtils.getLogger(
                    PythonDetector.class
            );

    /**
     * 检测当前 Windows 系统中的所有 Python 环境。
     *
     * @return Python 环境列表
     */
    public List<PythonEnvironment> detect() {

        List<PythonEnvironment> environments =
                new ArrayList<>();

        /*
         * PythonForge-win 当前版本只支持 Windows。
         */
        if (!WindowsUtils.isWindows()) {

            LogUtils.warning(
                    LOGGER,
                    "PythonForge-win requires Windows."
            );

            return environments;
        }

        /*
         * 收集 Python 候选路径。
         */
        Set<Path> candidates =
                collectCandidates();

        LogUtils.info(
                LOGGER,
                "Python candidates found: "
                        + candidates.size()
        );

        /*
         * 逐个检查候选 Python。
         */
        for (Path candidate : candidates) {

            try {

                PythonEnvironment environment =
                        inspect(candidate);

                if (environment != null) {

                    environments.add(
                            environment
                    );

                    LogUtils.info(
                            LOGGER,
                            "Detected Python: "
                                    + environment
                                    .getExecutable()
                    );

                } else {

                    LogUtils.warning(
                            LOGGER,
                            "Python candidate rejected: "
                                    + candidate
                    );
                }

            } catch (Exception e) {

                LogUtils.warning(
                        LOGGER,
                        "Failed to inspect Python: "
                                + candidate
                                + " - "
                                + e.getMessage()
                );
            }
        }

        LogUtils.info(
                LOGGER,
                "Total Python environments detected: "
                        + environments.size()
        );

        return environments;
    }

    /**
     * 收集 Python 候选路径。
     *
     * <p>
     * 优先使用 py -0p 获取 Windows
     * Python Manager / Launcher 管理的真实
     * Python 路径。
     * </p>
     */
    private Set<Path> collectCandidates() {

        Set<Path> candidates =
                new LinkedHashSet<>();

        /*
         * -------------------------------------------------
         * 1. Windows Python Launcher / Python Manager
         * -------------------------------------------------
         *
         * 例如：
         *
         * -V:3.14 * D:\Application\Compiler\Python\3.14.5\python.exe
         * -V:3.12   D:\Application\Compiler\Python\3.12.6\python.exe
         * -V:3.9    D:\Application\Compiler\Python\3.9.7\python.exe
         */
        findFromPythonLauncher(
                candidates
        );

        /*
         * -------------------------------------------------
         * 2. PATH 中的 python.exe
         * -------------------------------------------------
         */
        findFromCommand(
                candidates,
                "where",
                "python"
        );

        /*
         * -------------------------------------------------
         * 3. PATH 中的 python3.exe
         * -------------------------------------------------
         */
        findFromCommand(
                candidates,
                "where",
                "python3"
        );

        /*
         * -------------------------------------------------
         * 4. Windows 常见 Python 安装目录
         * -------------------------------------------------
         */
        findFromCommonLocations(
                candidates
        );

        /*
         * -------------------------------------------------
         * 5. 过滤 Windows shim / alias
         * -------------------------------------------------
         */
        candidates.removeIf(
                this::isIgnoredPython
        );

        return candidates;
    }

    /**
     * 通过 Windows Python Launcher / Python Manager
     * 查找真实 Python 解释器。
     *
     * <p>
     * 执行：
     *
     * <pre>
     * py -0p
     * </pre>
     *
     * </p>
     */
    private void findFromPythonLauncher(
            Set<Path> candidates) {

        try {

            ProcessBuilder builder =
                    new ProcessBuilder(
                            "py",
                            "-0p"
                    );

            builder.redirectErrorStream(true);

            Process process =
                    builder.start();

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
                        (line = reader.readLine())
                                != null
                ) {

                    line = line.trim();

                    if (line.isEmpty()) {
                        continue;
                    }

                    /*
                     * py -0p 常见格式：
                     *
                     * -V:3.14 * D:\...\python.exe
                     * -V:3.12   D:\...\python.exe
                     *
                     * 使用最后一个空格之后的内容
                     * 作为 Python 路径。
                     */
                    int separator =
                            line.lastIndexOf(' ');

                    if (separator < 0) {
                        continue;
                    }

                    String pathText =
                            line.substring(
                                    separator + 1
                            ).trim();

                    if (pathText.isEmpty()) {
                        continue;
                    }

                    try {

                        Path path =
                                Path.of(
                                                pathText
                                        )
                                        .toAbsolutePath()
                                        .normalize();

                        if (
                                Files.isRegularFile(path)
                                        &&
                                        isPythonExecutable(path)
                        ) {

                            candidates.add(path);

                            LogUtils.info(
                                    LOGGER,
                                    "Python launcher found: "
                                            + path
                            );
                        }

                    } catch (Exception ignored) {

                        /*
                         * 忽略无效路径。
                         */
                    }
                }
            }

            process.waitFor();

        } catch (Exception ignored) {

            /*
             * py 命令不存在时忽略。
             */
        }
    }

    /**
     * 使用 Windows where 命令查找 Python。
     */
    private void findFromCommand(
            Set<Path> candidates,
            String... command) {

        try {

            ProcessBuilder builder =
                    new ProcessBuilder(command);

            builder.redirectErrorStream(true);

            Process process =
                    builder.start();

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
                        (line = reader.readLine())
                                != null
                ) {

                    line = line.trim();

                    if (line.isEmpty()) {
                        continue;
                    }

                    Path path;

                    try {

                        path =
                                Path.of(line)
                                        .toAbsolutePath()
                                        .normalize();

                    } catch (Exception ignored) {

                        continue;
                    }

                    if (
                            Files.isRegularFile(path)
                                    &&
                                    isPythonExecutable(path)
                    ) {

                        candidates.add(path);

                        LogUtils.info(
                                LOGGER,
                                "PATH Python candidate: "
                                        + path
                        );
                    }
                }
            }

            process.waitFor();

        } catch (Exception ignored) {

            /*
             * where 命令不存在或执行失败时忽略。
             */
        }
    }

    /**
     * 从 Windows 常见目录寻找 Python。
     */
    private void findFromCommonLocations(
            Set<Path> candidates) {

        String userProfile =
                System.getenv(
                        "USERPROFILE"
                );

        String localAppData =
                System.getenv(
                        "LOCALAPPDATA"
                );

        String programFiles =
                System.getenv(
                        "ProgramFiles"
                );

        String programFilesX86 =
                System.getenv(
                        "ProgramFiles(x86)"
                );

        List<Path> locations =
                new ArrayList<>();

        /*
         * 用户级 Python：
         *
         * C:\Users\<User>\AppData\Local\Programs\Python
         */
        if (userProfile != null) {

            locations.add(
                    Path.of(
                            userProfile,
                            "AppData",
                            "Local",
                            "Programs",
                            "Python"
                    )
            );
        }

        /*
         * LOCALAPPDATA：
         */
        if (localAppData != null) {

            locations.add(
                    Path.of(
                            localAppData,
                            "Programs",
                            "Python"
                    )
            );
        }

        /*
         * Program Files：
         */
        if (programFiles != null) {

            locations.add(
                    Path.of(
                            programFiles,
                            "Python"
                    )
            );
        }

        /*
         * Program Files (x86)：
         */
        if (programFilesX86 != null) {

            locations.add(
                    Path.of(
                            programFilesX86,
                            "Python"
                    )
            );
        }

        /*
         * 扫描目录。
         */
        for (Path location : locations) {

            if (!Files.isDirectory(location)) {
                continue;
            }

            try (
                    var stream =
                            Files.walk(
                                    location,
                                    3
                            )
            ) {

                stream
                        .filter(
                                Files::isRegularFile
                        )
                        .filter(
                                this::isPythonExecutable
                        )
                        .map(
                                path ->
                                        path
                                                .toAbsolutePath()
                                                .normalize()
                        )
                        .forEach(
                                candidates::add
                        );

            } catch (IOException ignored) {

                /*
                 * 无法访问目录时忽略。
                 */
            }
        }
    }

    /**
     * 判断是否为 Python 可执行文件。
     */
    private boolean isPythonExecutable(
            Path path) {

        if (path == null) {
            return false;
        }

        Path fileNamePath =
                path.getFileName();

        if (fileNamePath == null) {
            return false;
        }

        String fileName =
                fileNamePath.toString();

        return fileName.equalsIgnoreCase(
                "python.exe"
        )
                ||
                fileName.equalsIgnoreCase(
                        "python3.exe"
                );
    }

    /**
     * 判断是否应该忽略 Python shim。
     *
     * <p>
     * 以下路径不会作为真正的 Python 环境：
     * </p>
     *
     * <ul>
     *     <li>WindowsApps</li>
     *     <li>PyManager</li>
     * </ul>
     */
    private boolean isIgnoredPython(
            Path path) {

        if (path == null) {
            return true;
        }

        String normalized =
                path.toAbsolutePath()
                        .normalize()
                        .toString()
                        .toLowerCase();

        /*
         * Windows App Execution Alias。
         *
         * 例如：
         *
         * C:\Users\...\AppData\Local\Microsoft\
         * WindowsApps\python.exe
         */
        if (
                normalized.contains(
                        "\\windowsapps\\"
                )
        ) {

            LogUtils.info(
                    LOGGER,
                    "Ignoring WindowsApps Python: "
                            + path
            );

            return true;
        }

        /*
         * Python Manager shim。
         *
         * 例如：
         *
         * C:\Program Files\PyManager\python.exe
         */
        if (
                normalized.contains(
                        "\\pymanager\\"
                )
        ) {

            LogUtils.info(
                    LOGGER,
                    "Ignoring PyManager Python: "
                            + path
            );

            return true;
        }

        return false;
    }

    /**
     * 检查单个 Python 环境。
     */
    private PythonEnvironment inspect(
            Path executable)
            throws IOException, InterruptedException {

        if (!Files.isRegularFile(executable)) {
            return null;
        }

        /*
         * -------------------------------------------------
         * Python version
         * -------------------------------------------------
         */
        String version =
                execute(
                        executable,
                        "--version"
                );

        LogUtils.info(
                LOGGER,
                "Inspecting Python: "
                        + executable
                        + " -> "
                        + version
        );

        /*
         * 无法获取 Python 版本，
         * 则认为不是有效 Python。
         */
        if (
                version == null
                        ||
                        version.isBlank()
                        ||
                        !version
                                .toLowerCase()
                                .startsWith("python")
        ) {

            return null;
        }

        /*
         * -------------------------------------------------
         * Python architecture
         * -------------------------------------------------
         */
        String architectureOutput =
                execute(
                        executable,
                        "-c",
                        "import platform; "
                                + "print("
                                + "platform.architecture()[0]"
                                + ")"
                );

        PythonArchitecture architecture =
                parseArchitecture(
                        architectureOutput
                );

        /*
         * -------------------------------------------------
         * pip
         * -------------------------------------------------
         */
        String pipVersion =
                detectPipVersion(
                        executable
                );

        Path pipExecutable = null;

        if (pipVersion != null) {

            Path parent =
                    executable.getParent();

            if (parent != null) {

                Path candidate =
                        parent
                                .resolve("Scripts")
                                .resolve("pip.exe");

                if (
                        Files.isRegularFile(
                                candidate
                        )
                ) {

                    pipExecutable =
                            candidate
                                    .toAbsolutePath()
                                    .normalize();
                }
            }
        }

        /*
         * -------------------------------------------------
         * site-packages
         * -------------------------------------------------
         */
        String sitePackagesOutput =
                execute(
                        executable,
                        "-c",
                        "import site; "
                                + "print("
                                + "site.getsitepackages()[0]"
                                + ")"
                );

        Path sitePackages = null;

        if (
                sitePackagesOutput != null
                        &&
                        !sitePackagesOutput.isBlank()
        ) {

            try {

                Path candidate =
                        Path.of(
                                        sitePackagesOutput
                                                .trim()
                                )
                                .toAbsolutePath()
                                .normalize();

                if (
                        Files.isDirectory(
                                candidate
                        )
                ) {

                    sitePackages =
                            candidate;
                }

            } catch (Exception ignored) {

                /*
                 * 无效路径时忽略。
                 */
            }
        }

        /*
         * -------------------------------------------------
         * PyInstaller
         * -------------------------------------------------
         */
        String pyInstallerVersion =
                detectPyInstallerVersion(
                        executable
                );

        boolean pyInstallerAvailable =
                pyInstallerVersion != null;

        /*
         * -------------------------------------------------
         * 输出检测结果
         * -------------------------------------------------
         */
        LogUtils.info(
                LOGGER,
                "Python inspection result: "
                        + executable
                        + " | version="
                        + cleanVersion(version)
                        + " | architecture="
                        + architecture
                        + " | pip="
                        + (
                        pipVersion != null
                                ? pipVersion
                                : "Not Installed"
                )
                        + " | PyInstaller="
                        + (
                        pyInstallerVersion != null
                                ? pyInstallerVersion
                                : "Not Installed"
                )
        );

        /*
         * -------------------------------------------------
         * 创建 PythonEnvironment
         * -------------------------------------------------
         */
        return new PythonEnvironment(
                executable
                        .toAbsolutePath()
                        .normalize(),

                cleanVersion(
                        version
                ),

                architecture,

                pipExecutable,

                sitePackages,

                pyInstallerAvailable,

                pyInstallerVersion
        );
    }

    /**
     * 检测 pip 版本。
     *
     * <p>
     * 使用当前 Python：
     *
     * <pre>
     * python.exe -c
     * "import pip; print(pip.__version__)"
     * </pre>
     *
     * 确保 pip 属于当前 Python 环境。
     */
    private String detectPipVersion(
            Path executable) {

        try {

            String output =
                    execute(
                            executable,
                            "-c",
                            "import pip; "
                                    + "print("
                                    + "pip.__version__"
                                    + ")"
                    );

            if (
                    output == null
                            ||
                            output.isBlank()
            ) {

                return null;
            }

            return output.trim();

        } catch (Exception e) {

            return null;
        }
    }

    /**
     * 检测 PyInstaller 版本。
     *
     * <p>
     * 使用当前 Python 解释器直接导入
     * PyInstaller。
     * </p>
     */
    private String detectPyInstallerVersion(
            Path executable) {

        try {

            String output =
                    execute(
                            executable,
                            "-c",
                            "import PyInstaller; "
                                    + "print("
                                    + "PyInstaller.__version__"
                                    + ")"
                    );

            if (
                    output == null
                            ||
                            output.isBlank()
            ) {

                return null;
            }

            return output.trim();

        } catch (Exception e) {

            return null;
        }
    }

    /**
     * 执行 Python 命令。
     */
    private String execute(
            Path executable,
            String... arguments)
            throws IOException, InterruptedException {

        List<String> command =
                new ArrayList<>();

        command.add(
                executable
                        .toAbsolutePath()
                        .normalize()
                        .toString()
        );

        for (String argument : arguments) {

            command.add(argument);
        }

        ProcessBuilder builder =
                new ProcessBuilder(command);

        /*
         * 当前检测阶段合并 stdout/stderr。
         */
        builder.redirectErrorStream(true);

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
                    (line = reader.readLine())
                            != null
            ) {

                if (output.length() > 0) {

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
    }

    /**
     * 解析 Python 架构。
     */
    private PythonArchitecture parseArchitecture(
            String value) {

        if (value == null) {

            return PythonArchitecture.UNKNOWN;
        }

        String normalized =
                value
                        .trim()
                        .toLowerCase();

        if (
                normalized.contains(
                        "64bit"
                )
        ) {

            return PythonArchitecture.X64;

        } else if (
                normalized.contains(
                        "32bit"
                )
        ) {

            return PythonArchitecture.X86;
        }

        return PythonArchitecture.UNKNOWN;
    }

    /**
     * 清理 Python 版本字符串。
     *
     * <p>
     * 例如：
     *
     * <pre>
     * Python 3.14.5
     * </pre>
     *
     * 转换为：
     *
     * <pre>
     * 3.14.5
     * </pre>
     */
    private String cleanVersion(
            String value) {

        if (value == null) {

            return "Unknown";
        }

        return value
                .replaceFirst(
                        "(?i)^python\\s+",
                        ""
                )
                .trim();
    }
}
