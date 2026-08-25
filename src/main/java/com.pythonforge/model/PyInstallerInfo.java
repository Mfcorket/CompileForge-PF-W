package com.pythonforge.model;

import java.util.Objects;

/**
 * PyInstaller 环境信息。
 *
 * <p>
 * 用于描述当前 Python 环境中的 PyInstaller
 * 是否可用以及版本信息。
 * </p>
 */
public final class PyInstallerInfo {

    /**
     * Python 环境。
     */
    private final PythonEnvironment pythonEnvironment;

    /**
     * 是否安装 PyInstaller。
     */
    private final boolean installed;

    /**
     * PyInstaller 版本。
     */
    private final String version;

    /**
     * 是否可以通过当前 Python 执行 PyInstaller。
     */
    private final boolean executable;

    /**
     * 构造。
     *
     * @param pythonEnvironment Python 环境
     * @param installed         是否安装
     * @param version           版本
     * @param executable        是否可以执行
     */
    public PyInstallerInfo(
            PythonEnvironment pythonEnvironment,
            boolean installed,
            String version,
            boolean executable) {

        this.pythonEnvironment =
                Objects.requireNonNull(
                        pythonEnvironment,
                        "pythonEnvironment must not be null"
                );

        this.installed =
                installed;

        this.version =
                version;

        this.executable =
                executable;
    }

    /**
     * 获取 Python 环境。
     *
     * @return Python 环境
     */
    public PythonEnvironment getPythonEnvironment() {
        return pythonEnvironment;
    }

    /**
     * 是否安装。
     *
     * @return true 表示已安装
     */
    public boolean isInstalled() {
        return installed;
    }

    /**
     * 获取版本。
     *
     * @return PyInstaller 版本
     */
    public String getVersion() {
        return version;
    }

    /**
     * 是否可以执行。
     *
     * @return true 表示可以执行
     */
    public boolean isExecutable() {
        return executable;
    }

    /**
     * 是否完全可用。
     *
     * @return true 表示安装并且可以执行
     */
    public boolean isAvailable() {
        return installed && executable;
    }

    @Override
    public String toString() {

        return "PyInstallerInfo{"
                + "python="
                + pythonEnvironment.getExecutable()
                + ", installed="
                + installed
                + ", version='"
                + version
                + '\''
                + ", executable="
                + executable
                + '}';
    }
}
