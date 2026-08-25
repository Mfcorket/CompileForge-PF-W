package com.pythonforge.service;

import com.pythonforge.model.PyInstallerBuildConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**

 * PyInstaller 命令生成器。
 *
 * <p>
 * 负责将 PyInstallerBuildConfig 转换为 Windows
 * 下可执行的 PyInstaller 命令行参数。
 * </p>

 */
public final class PyInstallerCommandBuilder {

    /**
     * 构建 PyInstaller 命令。
     *
     * <p>
     * 使用：
     *
     * <pre>
     * python.exe -m PyInstaller
     * </pre>
     *
     * 而不是直接调用 pyinstaller.exe。
     *
     * <p>
     * 这样可以确保使用当前 Python 环境中的
     * PyInstaller。
     *
     * @param pythonExecutable Python 可执行文件
     * @param config PyInstaller 构建配置
     * @return 命令参数列表
     */
    public List<String> build(
            Path pythonExecutable,
            PyInstallerBuildConfig config) {

        if (pythonExecutable == null) {

            throw new IllegalArgumentException(
                    "Python executable cannot be null."
            );
        }

        if (config == null) {

            throw new IllegalArgumentException(
                    "Build config cannot be null."
            );
        }

        List<String> command =
                new ArrayList<>();

        /*
         * Python。
         */
        command.add(
                pythonExecutable.toString()
        );

        /*
         * -m PyInstaller
         */
        command.add("-m");
        command.add("PyInstaller");

        /*
         * ========================================================
         * 打包模式
         * ========================================================
         */

        if (
                config.getMode()
                        == PyInstallerBuildConfig.Mode.ONEFILE
        ) {

            command.add("--onefile");

        } else {

            command.add("--onedir");
        }

        /*
         * ========================================================
         * 控制台模式
         * ========================================================
         */

        if (
                config.getConsoleMode()
                        == PyInstallerBuildConfig.ConsoleMode.WINDOWED
        ) {

            command.add("--windowed");

        } else {

            command.add("--console");
        }

        /*
         * ========================================================
         * 应用名称
         * ========================================================
         */

        if (
                config.getName() != null
                        &&
                        !config.getName().isBlank()
        ) {

            command.add("--name");

            command.add(
                    config.getName()
            );
        }

        /*
         * ========================================================
         * 输出目录
         * ========================================================
         */

        addPathOption(
                command,
                "--distpath",
                config.getOutputDirectory()
        );

        /*
         * ========================================================
         * 工作目录
         * ========================================================
         */

        addPathOption(
                command,
                "--workpath",
                config.getWorkDirectory()
        );

        /*
         * ========================================================
         * Spec 目录
         * ========================================================
         */

        addPathOption(
                command,
                "--specpath",
                config.getSpecDirectory()
        );

        /*
         * ========================================================
         * 图标
         * ========================================================
         */

        addPathOption(
                command,
                "--icon",
                config.getIcon()
        );

        /*
         * Windows 版本信息文件。
         */
        addPathOption(
                command,
                "--version-file",
                config.getVersionFile()
        );


        /*
         * Windows Manifest。
         */
        addPathOption(
                command,
                "--manifest",
                config.getManifestFile()
        );

        /*
         * ========================================================
         * 清理
         * ========================================================
         */

        if (config.isClean()) {

            command.add("--clean");
        }

        /*
         * ========================================================
         * 不询问
         * ========================================================
         */

        if (config.isNoconfirm()) {

            command.add("--noconfirm");
        }

        /*
         * ========================================================
         * UPX
         * ========================================================
         */

        if (config.isNoUpx()) {

            command.add("--noupx");
        }

        /*
         * ========================================================
         * Strip
         * ========================================================
         *
         * Windows 下通常不需要。
         * 但保留 PyInstaller 参数能力。
         */

        if (config.isStrip()) {

            command.add("--strip");
        }

        /*
         * ========================================================
         * Debug
         * ========================================================
         */

        if (config.isDebug()) {

            command.add("--debug");
            command.add("all");
        }

        /*
         * ========================================================
         * 详细日志
         * ========================================================
         *
         * PyInstaller 支持：
         *
         * --log-level LEVEL
         *
         * 当前配置开启时使用 DEBUG。
         */

        if (config.isLogLevelDebug()) {

            command.add("--log-level");
            command.add("DEBUG");
        }

        /*
         * ========================================================
         * Windowed traceback
         * ========================================================
         */

        if (
                config.isDisableWindowedTraceback()
        ) {

            command.add(
                    "--disable-windowed-traceback"
            );
        }

        /*
         * ========================================================
         * Python 搜索路径
         * ========================================================
         */

        for (
                String path
                : config.getPaths()
        ) {

            addValueOption(
                    command,
                    "--paths",
                    path
            );
        }

        /*
         * ========================================================
         * Hidden imports
         * ========================================================
         */

        for (
                String module
                : config.getHiddenImports()
        ) {

            addValueOption(
                    command,
                    "--hidden-import",
                    module
            );
        }

        /*
         * Runtime Hooks。
         */
        for (
                String runtimeHook
                : config.getRuntimeHooks()
        ) {

            command.add(
                    "--runtime-hook"
            );

            command.add(
                    runtimeHook
            );
        }

        /*
         * ========================================================
         * Excludes
         * ========================================================
         */

        for (
                String module
                : config.getExcludes()
        ) {

            addValueOption(
                    command,
                    "--exclude-module",
                    module
            );
        }

        /*
         * ========================================================
         * Add Data
         * ========================================================
         *
         * Windows:
         *
         * source;destination
         */

        for (
                String data
                : config.getDataFiles()
        ) {

            addValueOption(
                    command,
                    "--add-data",
                    data
            );
        }

        /*
         * ========================================================
         * Add Binary
         * ========================================================
         */

        for (
                String binary
                : config.getBinaries()
        ) {

            addValueOption(
                    command,
                    "--add-binary",
                    binary
            );
        }

        /*
         * ========================================================
         * Collect Submodules
         * ========================================================
         */

        for (
                String module
                : config.getCollectSubmodules()
        ) {

            addValueOption(
                    command,
                    "--collect-submodules",
                    module
            );
        }

        /*
        *====================
        * 附加添加目录
        * ===================
         */
        addAdditionalDirectories(
                command,
                config
        );

        /*
         * ========================================================
         * Collect Data
         * ========================================================
         */

        for (
                String module
                : config.getCollectData()
        ) {

            addValueOption(
                    command,
                    "--collect-data",
                    module
            );
        }

        /*
         * ========================================================
         * Collect Binaries
         * ========================================================
         */

        for (
                String module
                : config.getCollectBinaries()
        ) {

            addValueOption(
                    command,
                    "--collect-binaries",
                    module
            );
        }

        /*
         * ========================================================
         * Collect All
         * ========================================================
         */

        for (
                String module
                : config.getCollectAll()
        ) {

            addValueOption(
                    command,
                    "--collect-all",
                    module
            );
        }

        /*
         * ========================================================
         * 入口文件
         * ========================================================
         *
         * 必须放在所有选项之后。
         */

        if (config.getEntryFile() != null) {

            command.add(
                    config.getEntryFile()
                            .toString()
            );
        }

        return command;
    }

    /**
     * 添加 Path 类型参数。
     */
    private void addPathOption(
            List<String> command,
            String option,
            Path path) {

        if (path == null) {
            return;
        }

        command.add(option);

        command.add(
                path.toString()
        );
    }

    /**
     * 添加普通字符串参数。
     */
    private void addValueOption(
            List<String> command,
            String option,
            String value) {

        if (
                value == null
                        ||
                        value.isBlank()
        ) {

            return;
        }

        command.add(option);

        command.add(
                value.trim()
        );
    }

    private void addAdditionalDirectories(
            List<String> command,
            PyInstallerBuildConfig config) {

        if (config == null) {

            return;
        }

        for (
                Path directory :
                config.getAdditionalDirectories()
        ) {

            if (
                    directory == null
                            ||
                            !Files.isDirectory(directory)
            ) {

                continue;
            }

            String destination =
                    directory.getFileName()
                            .toString();

            command.add(
                    "--add-data"
            );

            command.add(
                    directory.toAbsolutePath()
                            .normalize()
                            .toString()
                            + ";"
                            + destination
            );
        }
    }


}
