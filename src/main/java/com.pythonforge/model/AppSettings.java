package com.pythonforge.model;

import java.nio.file.Path;
import java.util.Objects;

/**
 * PythonForge-win 软件设置。
 *
 * <p>
 * 该类只负责保存软件设置数据，
 * 不负责文件读写、不负责 JavaFX UI，
 * 也不负责主题实际加载。
 * </p>
 *
 * <p>
 * 后续由 SettingsService 负责：
 * </p>
 *
 * <ul>
 *     <li>加载设置</li>
 *     <li>保存设置</li>
 *     <li>修改设置</li>
 *     <li>恢复默认设置</li>
 * </ul>
 */
public class AppSettings {

    /**
     * 当前设置模型版本。
     *
     * <p>
     * 用于以后设置文件格式升级。
     * </p>
     */
    private int version = 1;

    /*
     * =========================================================
     * 常规设置
     * =========================================================
     */

    /**
     * 软件界面语言。
     *
     * <p>
     * 默认使用简体中文。
     * </p>
     */
    private LanguageOption language =
            LanguageOption.ZH_CN;

    /*
     * =========================================================
     * 外观设置
     * =========================================================
     */

    /**
     * 当前主题模式。
     *
     * <p>
     * 默认自动跟随系统。
     * </p>
     */
    private ThemeMode themeMode =
            ThemeMode.AUTO;

    /**
     * 自定义主色。
     *
     * <p>
     * 使用 CSS 颜色字符串。
     * 例如：
     *
     * <pre>
     * #0078D4
     * </pre>
     * </p>
     */
    private String customPrimaryColor =
            "#0078D4";

    /**
     * 自定义背景颜色。
     */
    private String customBackgroundColor =
            "#FFFFFF";

    /**
     * 自定义文字颜色。
     */
    private String customTextColor =
            "#1E1E1E";

    /**
     * 自定义强调色。
     */
    private String customAccentColor =
            "#0078D4";

    /**
     * 自定义背景图片。
     *
     * <p>
     * null 表示没有设置背景图片。
     * </p>
     */
    private Path customBackgroundImage;

    /*
     * =========================================================
     * Python 设置
     * =========================================================
     */

    /**
     * 默认 Python 可执行文件。
     *
     * <p>
     * 这里只保存路径，不保存 PythonEnvironment 对象。
     * PythonEnvironment 仍然由 PythonDetector / Manager 管理。
     * </p>
     */
    private Path defaultPythonExecutable;

    /**
     * 启动时是否自动检测 Python 环境。
     *
     * <p>
     * 默认开启。
     * </p>
     */
    private boolean autoDetectPython =
            true;

    /*
     * =========================================================
     * 默认构建设置
     * =========================================================
     */

    /**
     * 默认打包模式。
     *
     * <p>
     * 默认单文件。
     * </p>
     */
    private PyInstallerBuildConfigMode
            defaultBuildMode =
            PyInstallerBuildConfigMode.ONEFILE;

    /**
     * 默认窗口模式。
     *
     * <p>
     * 默认无控制台。
     * </p>
     */
    private PyInstallerConsoleMode
            defaultConsoleMode =
            PyInstallerConsoleMode.WINDOWED;

    /**
     * 默认是否清理构建目录。
     */
    private boolean defaultClean =
            false;

    /**
     * 默认是否不询问覆盖。
     */
    private boolean defaultNoconfirm =
            false;

    /**
     * 默认是否禁用 UPX。
     */
    private boolean defaultNoUpx =
            false;

    /**
     * 默认输出目录。
     *
     * <p>
     * null 表示使用项目自己的输出目录。
     * </p>
     */
    private Path defaultOutputDirectory;

    /**
     * 创建默认设置。
     */
    public AppSettings() {
    }

    /*
     * =========================================================
     * 基础设置
     * =========================================================
     */

    public int getVersion() {

        return version;
    }

    public void setVersion(int version) {

        if (version < 1) {
            this.version = 1;
            return;
        }

        this.version = version;
    }

    /*
     * =========================================================
     * Language
     * =========================================================
     */

    public LanguageOption getLanguage() {

        return language;
    }

    public void setLanguage(
            LanguageOption language) {

        this.language =
                Objects.requireNonNull(
                        language,
                        "language"
                );
    }

    /*
     * =========================================================
     * Theme
     * =========================================================
     */

    public ThemeMode getThemeMode() {

        return themeMode;
    }

    public void setThemeMode(
            ThemeMode themeMode) {

        this.themeMode =
                Objects.requireNonNull(
                        themeMode,
                        "themeMode"
                );
    }

    public String getCustomPrimaryColor() {

        return customPrimaryColor;
    }

    public void setCustomPrimaryColor(
            String customPrimaryColor) {

        this.customPrimaryColor =
                normalizeColor(
                        customPrimaryColor,
                        "#0078D4"
                );
    }

    public String getCustomBackgroundColor() {

        return customBackgroundColor;
    }

    public void setCustomBackgroundColor(
            String customBackgroundColor) {

        this.customBackgroundColor =
                normalizeColor(
                        customBackgroundColor,
                        "#FFFFFF"
                );
    }

    public String getCustomTextColor() {

        return customTextColor;
    }

    public void setCustomTextColor(
            String customTextColor) {

        this.customTextColor =
                normalizeColor(
                        customTextColor,
                        "#1E1E1E"
                );
    }

    public String getCustomAccentColor() {

        return customAccentColor;
    }

    public void setCustomAccentColor(
            String customAccentColor) {

        this.customAccentColor =
                normalizeColor(
                        customAccentColor,
                        "#0078D4"
                );
    }

    public Path getCustomBackgroundImage() {

        return customBackgroundImage;
    }

    public void setCustomBackgroundImage(
            Path customBackgroundImage) {

        this.customBackgroundImage =
                customBackgroundImage;
    }

    /*
     * =========================================================
     * Python
     * =========================================================
     */

    public Path getDefaultPythonExecutable() {

        return defaultPythonExecutable;
    }

    public void setDefaultPythonExecutable(
            Path defaultPythonExecutable) {

        this.defaultPythonExecutable =
                defaultPythonExecutable;
    }

    public boolean isAutoDetectPython() {

        return autoDetectPython;
    }

    public void setAutoDetectPython(
            boolean autoDetectPython) {

        this.autoDetectPython =
                autoDetectPython;
    }

    /*
     * =========================================================
     * Default Build
     * =========================================================
     */

    public PyInstallerBuildConfigMode
    getDefaultBuildMode() {

        return defaultBuildMode;
    }

    public void setDefaultBuildMode(
            PyInstallerBuildConfigMode defaultBuildMode) {

        this.defaultBuildMode =
                Objects.requireNonNull(
                        defaultBuildMode,
                        "defaultBuildMode"
                );
    }

    public PyInstallerConsoleMode
    getDefaultConsoleMode() {

        return defaultConsoleMode;
    }

    public void setDefaultConsoleMode(
            PyInstallerConsoleMode defaultConsoleMode) {

        this.defaultConsoleMode =
                Objects.requireNonNull(
                        defaultConsoleMode,
                        "defaultConsoleMode"
                );
    }

    public boolean isDefaultClean() {

        return defaultClean;
    }

    public void setDefaultClean(
            boolean defaultClean) {

        this.defaultClean =
                defaultClean;
    }

    public boolean isDefaultNoconfirm() {

        return defaultNoconfirm;
    }

    public void setDefaultNoconfirm(
            boolean defaultNoconfirm) {

        this.defaultNoconfirm =
                defaultNoconfirm;
    }

    public boolean isDefaultNoUpx() {

        return defaultNoUpx;
    }

    public void setDefaultNoUpx(
            boolean defaultNoUpx) {

        this.defaultNoUpx =
                defaultNoUpx;
    }

    public Path getDefaultOutputDirectory() {

        return defaultOutputDirectory;
    }

    public void setDefaultOutputDirectory(
            Path defaultOutputDirectory) {

        this.defaultOutputDirectory =
                defaultOutputDirectory;
    }

    /*
     * =========================================================
     * Utility
     * =========================================================
     */

    /**
     * 恢复为默认设置。
     *
     * <p>
     * P3.2 SettingsService 会直接使用该方法。
     * </p>
     */
    public void reset() {

        AppSettings defaults =
                new AppSettings();

        this.version =
                defaults.version;

        this.language =
                defaults.language;

        this.themeMode =
                defaults.themeMode;

        this.customPrimaryColor =
                defaults.customPrimaryColor;

        this.customBackgroundColor =
                defaults.customBackgroundColor;

        this.customTextColor =
                defaults.customTextColor;

        this.customAccentColor =
                defaults.customAccentColor;

        this.customBackgroundImage =
                defaults.customBackgroundImage;

        this.defaultPythonExecutable =
                defaults.defaultPythonExecutable;

        this.autoDetectPython =
                defaults.autoDetectPython;

        this.defaultBuildMode =
                defaults.defaultBuildMode;

        this.defaultConsoleMode =
                defaults.defaultConsoleMode;

        this.defaultClean =
                defaults.defaultClean;

        this.defaultNoconfirm =
                defaults.defaultNoconfirm;

        this.defaultNoUpx =
                defaults.defaultNoUpx;

        this.defaultOutputDirectory =
                defaults.defaultOutputDirectory;
    }

    /**
     * 创建一份当前设置的副本。
     *
     * @return 设置副本
     */
    public AppSettings copy() {

        AppSettings copy =
                new AppSettings();

        copy.version =
                this.version;

        copy.language =
                this.language;

        copy.themeMode =
                this.themeMode;

        copy.customPrimaryColor =
                this.customPrimaryColor;

        copy.customBackgroundColor =
                this.customBackgroundColor;

        copy.customTextColor =
                this.customTextColor;

        copy.customAccentColor =
                this.customAccentColor;

        copy.customBackgroundImage =
                this.customBackgroundImage;

        copy.defaultPythonExecutable =
                this.defaultPythonExecutable;

        copy.autoDetectPython =
                this.autoDetectPython;

        copy.defaultBuildMode =
                this.defaultBuildMode;

        copy.defaultConsoleMode =
                this.defaultConsoleMode;

        copy.defaultClean =
                this.defaultClean;

        copy.defaultNoconfirm =
                this.defaultNoconfirm;

        copy.defaultNoUpx =
                this.defaultNoUpx;

        copy.defaultOutputDirectory =
                this.defaultOutputDirectory;

        return copy;
    }

    /**
     * 规范化 CSS 颜色字符串。
     */
    private static String normalizeColor(
            String value,
            String defaultValue) {

        if (value == null
                || value.isBlank()) {

            return defaultValue;
        }

        String normalized =
                value.trim();

        if (!normalized.startsWith("#")) {

            normalized =
                    "#" + normalized;
        }

        return normalized;
    }

    /**
     * 默认构建模式。
     *
     * <p>
     * 这里独立定义设置层的枚举，
     * 避免 AppSettings 与 PyInstallerBuildConfig
     * 形成不必要的强耦合。
     * </p>
     */
    public enum PyInstallerBuildConfigMode {

        ONEFILE,

        ONEDIR
    }

    /**
     * 默认控制台模式。
     */
    public enum PyInstallerConsoleMode {

        CONSOLE,

        WINDOWED
    }
}