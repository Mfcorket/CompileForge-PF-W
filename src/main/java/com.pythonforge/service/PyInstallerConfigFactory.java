package com.pythonforge.service;

import com.pythonforge.model.PyInstallerBuildConfig;
import com.pythonforge.model.PythonEnvironment;

import java.nio.file.Path;

/**
 * PyInstaller 构建配置工厂。
 *
 * <p>
 * 负责创建 PythonForge-win 当前版本使用的默认
 * PyInstaller 构建配置。
 * </p>
 */
public final class PyInstallerConfigFactory {

    private PyInstallerConfigFactory() {
    }

    /**
     * 根据当前 Python 环境创建默认配置。
     *
     * <p>
     * 当前 PyInstallerBuildConfig 不保存 Python
     * 可执行文件，因此 PythonEnvironment 主要用于
     * 后续构建阶段。
     * </p>
     *
     * @param environment 当前 Python 环境
     * @return 默认构建配置
     */
    public static PyInstallerBuildConfig createDefault(
            PythonEnvironment environment) {

        PyInstallerBuildConfig config =
                PyInstallerBuildConfig.defaults();

        return config;
    }

    /**
     * 创建默认配置。
     *
     * @return 默认 PyInstaller 配置
     */
    public static PyInstallerBuildConfig createDefault() {

        return PyInstallerBuildConfig.defaults();
    }

    /**
     * 配置构建路径。
     *
     * @param config 构建配置
     * @param entryFile Python 入口文件
     * @param outputDirectory 输出目录
     * @param workDirectory 工作目录
     * @param specDirectory Spec 文件目录
     */
    public static void configurePaths(
            PyInstallerBuildConfig config,
            Path entryFile,
            Path outputDirectory,
            Path workDirectory,
            Path specDirectory) {

        if (config == null) {
            return;
        }

        config.setEntryFile(entryFile);

        config.setOutputDirectory(
                outputDirectory
        );

        config.setWorkDirectory(
                workDirectory
        );

        config.setSpecDirectory(
                specDirectory
        );

        if (entryFile != null
                && config.getName() == null) {

            String fileName =
                    entryFile
                            .getFileName()
                            .toString();

            int dot =
                    fileName.lastIndexOf('.');

            if (dot > 0) {

                fileName =
                        fileName.substring(
                                0,
                                dot
                        );
            }

            config.setName(fileName);
        }
    }

    /**
     * 设置应用名称。
     */
    public static void configureName(
            PyInstallerBuildConfig config,
            String name) {

        if (config == null) {
            return;
        }

        if (name == null
                || name.isBlank()) {

            return;
        }

        config.setName(
                name.trim()
        );
    }

    /**
     * 设置打包模式。
     */
    public static void configureMode(
            PyInstallerBuildConfig config,
            boolean oneFile) {

        if (config == null) {
            return;
        }

        config.setMode(
                oneFile
                        ? PyInstallerBuildConfig.Mode.ONEFILE
                        : PyInstallerBuildConfig.Mode.ONEDIR
        );
    }

    /**
     * 设置控制台模式。
     */
    public static void configureConsoleMode(
            PyInstallerBuildConfig config,
            boolean windowed) {

        if (config == null) {
            return;
        }

        config.setConsoleMode(
                windowed
                        ? PyInstallerBuildConfig.ConsoleMode.WINDOWED
                        : PyInstallerBuildConfig.ConsoleMode.CONSOLE
        );
    }

    /**
     * 设置图标。
     */
    public static void configureIcon(
            PyInstallerBuildConfig config,
            Path icon) {

        if (config == null) {
            return;
        }

        config.setIcon(icon);
    }

    
    /**
     * 添加隐藏导入。
     */
    public static void addHiddenImport(
            PyInstallerBuildConfig config,
            String module) {

        if (config == null) {
            return;
        }

        config.addHiddenImport(module);
    }

    /**
     * 添加数据文件。
     *
     * <p>
     * value 应符合 PyInstaller：
     * source;destination
     * </p>
     */
    public static void addDataFile(
            PyInstallerBuildConfig config,
            String value) {

        if (config == null) {
            return;
        }

        config.addDataFile(value);
    }

    /**
     * 添加二进制文件。
     *
     * <p>
     * value 应符合 PyInstaller：
     * source;destination
     * </p>
     */
    public static void addBinary(
            PyInstallerBuildConfig config,
            String value) {

        if (config == null) {
            return;
        }

        config.addBinary(value);
    }

    /**
     * 添加模块搜索路径。
     */
    public static void addPath(
            PyInstallerBuildConfig config,
            String path) {

        if (config == null) {
            return;
        }

        config.addPath(path);
    }

    /**
     * 添加排除模块。
     */
    public static void addExclude(
            PyInstallerBuildConfig config,
            String module) {

        if (config == null) {
            return;
        }

        config.addExclude(module);
    }

    /**
     * 添加需要收集的子模块。
     */
    public static void addCollectSubmodule(
            PyInstallerBuildConfig config,
            String module) {

        if (config == null) {
            return;
        }

        config.addCollectSubmodule(module);
    }

    /**
     * 添加需要收集的数据。
     */
    public static void addCollectData(
            PyInstallerBuildConfig config,
            String module) {

        if (config == null) {
            return;
        }

        config.addCollectData(module);
    }

    /**
     * 添加需要收集的二进制。
     */
    public static void addCollectBinary(
            PyInstallerBuildConfig config,
            String module) {

        if (config == null) {
            return;
        }

        config.addCollectBinary(module);
    }

    /**
     * 添加 collect-all 模块。
     */
    public static void addCollectAll(
            PyInstallerBuildConfig config,
            String module) {

        if (config == null) {
            return;
        }

        config.addCollectAll(module);
    }


}
