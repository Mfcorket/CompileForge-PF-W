package com.pythonforge.model;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Python 项目模型。
 *
 * <p>
 * 表示 PythonForge 当前准备编译/打包的项目。
 * </p>
 *
 * <p>
 * 当前版本支持：
 * .py
 * .pyc
 * Python 项目目录
 * </p>
 */
public final class PythonProject {

    /**
     * 项目入口文件。
     */
    private final Path entryFile;

    /**
     * 项目目录。
     */
    private final Path projectDirectory;

    /**
     * Python 环境。
     */
    private final PythonEnvironment pythonEnvironment;

    /**
     * 是否为 Python 源代码。
     */
    private final boolean sourceFile;

    /**
     * 是否为 Python 字节码。
     */
    private final boolean bytecodeFile;

    /**
     * 构造项目。
     *
     * @param entryFile          入口文件
     * @param projectDirectory   项目目录
     * @param pythonEnvironment  Python 环境
     * @param sourceFile         是否源代码
     * @param bytecodeFile       是否字节码
     */
    public PythonProject(
            Path entryFile,
            Path projectDirectory,
            PythonEnvironment pythonEnvironment,
            boolean sourceFile,
            boolean bytecodeFile) {

        this.entryFile =
                Objects.requireNonNull(
                        entryFile,
                        "entryFile must not be null"
                );

        this.projectDirectory =
                Objects.requireNonNull(
                        projectDirectory,
                        "projectDirectory must not be null"
                );

        this.pythonEnvironment =
                Objects.requireNonNull(
                        pythonEnvironment,
                        "pythonEnvironment must not be null"
                );

        this.sourceFile =
                sourceFile;

        this.bytecodeFile =
                bytecodeFile;
    }

    /**
     * 获取入口文件。
     *
     * @return 入口文件
     */
    public Path getEntryFile() {
        return entryFile;
    }

    /**
     * 获取项目目录。
     *
     * @return 项目目录
     */
    public Path getProjectDirectory() {
        return projectDirectory;
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
     * 是否为 Python 源文件。
     *
     * @return true 表示 .py
     */
    public boolean isSourceFile() {
        return sourceFile;
    }

    /**
     * 是否为 Python 字节码。
     *
     * @return true 表示 .pyc
     */
    public boolean isBytecodeFile() {
        return bytecodeFile;
    }

    /**
     * 获取文件名。
     *
     * @return 文件名
     */
    public String getFileName() {

        Path fileName =
                entryFile.getFileName();

        return fileName == null
                ? entryFile.toString()
                : fileName.toString();
    }

    @Override
    public String toString() {

        return "PythonProject{"
                + "entryFile="
                + entryFile
                + ", projectDirectory="
                + projectDirectory
                + ", python="
                + pythonEnvironment
                .getVersion()
                + ", sourceFile="
                + sourceFile
                + ", bytecodeFile="
                + bytecodeFile
                + '}';
    }
}