package com.pythonforge.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PyInstaller 构建配置。
 *
 * <p>
 * 用于描述一次 Python -> EXE 构建所需要的全部核心参数。
 * </p>
 *
 * <p>
 * 当前版本仅针对 Windows。
 * </p>
 */
public final class PyInstallerBuildConfig {

    /**
     * 打包模式。
     */
    public enum Mode {

        /**
         * 单目录模式。
         */
        ONEDIR,

        /**
         * 单文件模式。
         */
        ONEFILE
    }

    /**
     * 控制台模式。
     */
    public enum ConsoleMode {

        /**
         * 显示控制台窗口。
         */
        CONSOLE,

        /**
         * 不显示控制台窗口。
         */
        WINDOWED
    }

    /**
     * Python入口文件。
     */
    private Path entryFile;

    /**
     * 输出目录。
     */
    private Path outputDirectory;

    /**
     * 临时构建目录。
     */
    private Path workDirectory;

    /**
     * Spec 文件目录。
     */
    private Path specDirectory;

    /**
     * 应用名称。
     */
    private String name;

    /**
     * 打包模式。
     */
    private Mode mode = Mode.ONEFILE;

    /**
     * 控制台模式。
     */
    private ConsoleMode consoleMode =
            ConsoleMode.CONSOLE;

    /**
     * 图标。
     */
    private Path icon;

    /**
     * 是否清理临时文件。
     */
    private boolean clean = true;

    /**
     * 是否覆盖已有输出。
     */
    private boolean overwrite = true;

    /**
     * 是否确认无误后再构建。
     */
    private boolean noconfirm = true;

    /**
     * 是否关闭 UPX。
     */
    private boolean noUpx = false;

    /**
     * 是否关闭高级模块依赖分析。
     */
    private boolean strip = false;

    /**
     * 是否启用调试模式。
     */
    private boolean debug = false;

    /**
     * 是否启用 bootloader debug。
     */
    private boolean bootloaderIgnoreSignals = false;

    /**
     * 隐藏导入。
     */
    private final List<String> hiddenImports =
            new ArrayList<>();

    /**
     * 添加数据文件。
     */
    private final List<String> dataFiles =
            new ArrayList<>();

    /**
     * 添加二进制文件。
     */
    private final List<String> binaries =
            new ArrayList<>();

    /**
     * 添加模块路径。
     */
    private final List<String> paths =
            new ArrayList<>();

    /**
     * 排除模块。
     */
    private final List<String> excludes =
            new ArrayList<>();

    /**
     * 收集子模块。
     */
    private final List<String> collectSubmodules =
            new ArrayList<>();

    /**
     * 收集数据。
     */
    private final List<String> collectData =
            new ArrayList<>();

    /**
     * 收集二进制。
     */
    private final List<String> collectBinaries =
            new ArrayList<>();

    /**
     * 收集全部。
     */
    private final List<String> collectAll =
            new ArrayList<>();

    /**
     * 是否启用递归依赖。
     */
    private boolean collectAllPackages = false;

    /**
     * 是否禁用 Windows UPX。
     */
    private boolean disableWindowedTraceback = false;

    /**
     * 是否启用详细日志。
     */
    private boolean logLevelDebug = false;

    /**
     * Windows 版本信息文件。
     *
     * <p>
     * 对应 PyInstaller：
     *
     * <pre>
     * --version-file
     * </pre>
     */
    private Path versionFile;


    /**
     * Windows Manifest 文件。
     *
     * <p>
     * 对应 PyInstaller：
     *
     * <pre>
     * --manifest
     * </pre>
     */
    private Path manifestFile;


    /**
     * Runtime Hook 文件。
     *
     * <p>
     * 对应 PyInstaller：
     *
     * <pre>
     * --runtime-hook
     * </pre>
     */
    private final List<String> runtimeHooks =
            new ArrayList<>();

    private final List<Path> additionalDirectories =
            new ArrayList<>();



    public Path getEntryFile() {
        return entryFile;
    }

    public void setEntryFile(Path entryFile) {
        this.entryFile = entryFile;
    }

    public Path getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(Path outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public Path getWorkDirectory() {
        return workDirectory;
    }

    public void setWorkDirectory(Path workDirectory) {
        this.workDirectory = workDirectory;
    }

    public Path getSpecDirectory() {
        return specDirectory;
    }

    public void setSpecDirectory(Path specDirectory) {
        this.specDirectory = specDirectory;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public ConsoleMode getConsoleMode() {
        return consoleMode;
    }

    public void setConsoleMode(ConsoleMode consoleMode) {
        this.consoleMode = consoleMode;
    }

    public Path getIcon() {
        return icon;
    }

    public void setIcon(Path icon) {
        this.icon = icon;
    }

    public boolean isClean() {
        return clean;
    }

    public void setClean(boolean clean) {
        this.clean = clean;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public boolean isNoconfirm() {
        return noconfirm;
    }

    public void setNoconfirm(boolean noconfirm) {
        this.noconfirm = noconfirm;
    }

    public boolean isNoUpx() {
        return noUpx;
    }

    public void setNoUpx(boolean noUpx) {
        this.noUpx = noUpx;
    }

    public boolean isStrip() {
        return strip;
    }

    public void setStrip(boolean strip) {
        this.strip = strip;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isBootloaderIgnoreSignals() {
        return bootloaderIgnoreSignals;
    }

    public void setBootloaderIgnoreSignals(
            boolean bootloaderIgnoreSignals) {

        this.bootloaderIgnoreSignals =
                bootloaderIgnoreSignals;
    }

    public List<String> getHiddenImports() {
        return Collections.unmodifiableList(
                hiddenImports
        );
    }

    public void addHiddenImport(
            String value) {

        if (value != null && !value.isBlank()) {
            hiddenImports.add(value.trim());
        }
    }

    public void removeHiddenImport(
            String value) {

        hiddenImports.remove(value);
    }

    public void clearHiddenImports() {
        hiddenImports.clear();
    }


    public void removeDataFile(
            String value) {

        dataFiles.remove(value);
    }

    public void clearDataFiles() {
        dataFiles.clear();
    }





    public void removeBinary(
            String value) {

        binaries.remove(value);
    }

    public void clearBinaries() {
        binaries.clear();
    }

    public List<String> getPaths() {
        return Collections.unmodifiableList(
                paths
        );
    }

    public void addPath(
            String value) {

        if (value != null && !value.isBlank()) {
            paths.add(value.trim());
        }
    }

    public void removePath(
            String value) {

        paths.remove(value);
    }

    public void clearPaths() {
        paths.clear();
    }

    public List<String> getExcludes() {
        return Collections.unmodifiableList(
                excludes
        );
    }

    public void addExclude(
            String value) {

        if (value != null && !value.isBlank()) {
            excludes.add(value.trim());
        }
    }

    public void removeExclude(
            String value) {

        excludes.remove(value);
    }

    public void clearExcludes() {
        excludes.clear();
    }

    public List<String> getCollectSubmodules() {
        return Collections.unmodifiableList(
                collectSubmodules
        );
    }

    public void addCollectSubmodule(
            String value) {

        if (value != null && !value.isBlank()) {
            collectSubmodules.add(value.trim());
        }
    }

    public void clearCollectSubmodules() {
        collectSubmodules.clear();
    }

    public List<String> getCollectData() {
        return Collections.unmodifiableList(
                collectData
        );
    }

    public void addCollectData(
            String value) {

        if (value != null && !value.isBlank()) {
            collectData.add(value.trim());
        }
    }

    public void clearCollectData() {
        collectData.clear();
    }

    public List<String> getCollectBinaries() {
        return Collections.unmodifiableList(
                collectBinaries
        );
    }

    public void addCollectBinary(
            String value) {

        if (value != null && !value.isBlank()) {
            collectBinaries.add(value.trim());
        }
    }

    public void clearCollectBinaries() {
        collectBinaries.clear();
    }

    public List<String> getCollectAll() {
        return Collections.unmodifiableList(
                collectAll
        );
    }

    public void addCollectAll(
            String value) {

        if (value != null && !value.isBlank()) {
            collectAll.add(value.trim());
        }
    }

    public void clearCollectAll() {
        collectAll.clear();
    }

    public boolean isCollectAllPackages() {
        return collectAllPackages;
    }

    public void setCollectAllPackages(
            boolean collectAllPackages) {

        this.collectAllPackages =
                collectAllPackages;
    }

    public boolean isDisableWindowedTraceback() {
        return disableWindowedTraceback;
    }

    public void setDisableWindowedTraceback(
            boolean disableWindowedTraceback) {

        this.disableWindowedTraceback =
                disableWindowedTraceback;
    }

    public boolean isLogLevelDebug() {
        return logLevelDebug;
    }

    public void setLogLevelDebug(
            boolean logLevelDebug) {

        this.logLevelDebug =
                logLevelDebug;
    }

    /**
     * 创建默认配置。
     */
    public static PyInstallerBuildConfig defaults() {

        PyInstallerBuildConfig config =
                new PyInstallerBuildConfig();

        config.setMode(
                Mode.ONEFILE
        );

        config.setConsoleMode(
                ConsoleMode.CONSOLE
        );

        config.setClean(true);
        config.setOverwrite(true);
        config.setNoconfirm(true);
        config.setNoUpx(false);
        config.setStrip(false);
        config.setDebug(false);

        return config;
    }

    @Override
    public String toString() {

        return "PyInstallerBuildConfig{" +
                "entryFile=" + entryFile +
                ", outputDirectory=" + outputDirectory +
                ", workDirectory=" + workDirectory +
                ", specDirectory=" + specDirectory +
                ", name='" + name + '\'' +
                ", mode=" + mode +
                ", consoleMode=" + consoleMode +
                ", icon=" + icon +
                ", clean=" + clean +
                ", overwrite=" + overwrite +
                ", noconfirm=" + noconfirm +
                ", noUpx=" + noUpx +
                ", hiddenImports=" + hiddenImports.size() +
                ", dataFiles=" + dataFiles.size() +
                ", binaries=" + binaries.size() +
                ", paths=" + paths.size() +
                ", excludes=" + excludes.size() +
                '}';
    }

    public Path getVersionFile() {

        return versionFile;
    }


    public void setVersionFile(
            Path versionFile) {

        this.versionFile =
                versionFile;
    }


    public Path getManifestFile() {

        return manifestFile;
    }


    public void setManifestFile(
            Path manifestFile) {

        this.manifestFile =
                manifestFile;
    }


    public List<String> getRuntimeHooks() {

        return List.copyOf(
                runtimeHooks
        );
    }


    public void setRuntimeHooks(
            List<String> runtimeHooks) {

        this.runtimeHooks.clear();

        if (runtimeHooks != null) {

            this.runtimeHooks.addAll(
                    runtimeHooks
            );
        }
    }


    public void addRuntimeHook(
            String runtimeHook) {

        if (
                runtimeHook == null
                        ||
                        runtimeHook.isBlank()
        ) {

            return;
        }

        runtimeHooks.add(
                runtimeHook
        );
    }

    /**
     * 获取数据文件。
     *
     * <p>
     * 对应 PyInstaller：
     * <pre>
     * --add-data source;destination
     * </pre>
     * </p>
     */
    public List<String> getDataFiles() {

        return List.copyOf(dataFiles);
    }

    /**
     * 设置数据文件。
     *
     * @param dataFiles 数据文件列表
     */
    public void setDataFiles(
            List<String> dataFiles) {

        this.dataFiles.clear();

        if (dataFiles != null) {

            this.dataFiles.addAll(
                    dataFiles
            );
        }
    }

    /**
     * 添加一个数据文件。
     *
     * @param dataFile 数据文件
     */
    public void addDataFile(
            String dataFile) {

        if (
                dataFile == null
                        ||
                        dataFile.isBlank()
        ) {
            return;
        }

        this.dataFiles.add(
                dataFile
        );
    }

    /**
     * 获取二进制文件。
     *
     * <p>
     * 对应 PyInstaller：
     * <pre>
     * --add-binary source;destination
     * </pre>
     * </p>
     */
    public List<String> getBinaries() {

        return List.copyOf(binaries);
    }

    /**
     * 设置二进制文件。
     *
     * @param binaries 二进制文件列表
     */
    public void setBinaries(
            List<String> binaries) {

        this.binaries.clear();

        if (binaries != null) {

            this.binaries.addAll(
                    binaries
            );
        }
    }

    /**
     * 添加一个二进制文件。
     *
     * @param binary 二进制文件
     */
    public void addBinary(
            String binary) {

        if (
                binary == null
                        ||
                        binary.isBlank()
        ) {
            return;
        }

        this.binaries.add(
                binary
        );
    }

    /**
     * 获取附加目录。
     */
    public List<Path> getAdditionalDirectories() {

        return List.copyOf(
                additionalDirectories
        );
    }

    /**
     * 设置附加目录。
     */
    public void setAdditionalDirectories(
            List<Path> directories) {

        additionalDirectories.clear();

        if (directories == null) {

            return;
        }

        for (Path directory : directories) {

            if (
                    directory != null
                            &&
                            !additionalDirectories.contains(
                                    directory
                            )
            ) {

                additionalDirectories.add(
                        directory
                );
            }
        }
    }

    /**
     * 添加一个附加目录。
     */
    public void addAdditionalDirectory(
            Path directory) {

        if (directory == null) {

            return;
        }

        if (
                !additionalDirectories.contains(
                        directory
                )
        ) {

            additionalDirectories.add(
                    directory
            );
        }
    }

    /**
     * 删除附加目录。
     */
    public void removeAdditionalDirectory(
            Path directory) {

        if (directory == null) {

            return;
        }

        additionalDirectories.remove(
                directory
        );
    }

}
