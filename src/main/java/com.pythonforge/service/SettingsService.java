package com.pythonforge.service;

import com.pythonforge.model.AppSettings;
import com.pythonforge.model.LanguageOption;
import com.pythonforge.model.ThemeMode;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

/**
 * PythonForge-win 设置服务。
 *
 * <p>
 * 负责软件设置的加载、保存、恢复默认以及设置文件管理。
 * </p>
 *
 * <p>
 * 设置文件使用 UTF-8 编码的 properties 格式保存，
 * 不依赖额外 JSON/XML 库。
 * </p>
 */
public class SettingsService {

    /**
     * 设置文件版本。
     *
     * <p>
     * 用于以后升级设置文件格式。
     * </p>
     */
    private static final int SETTINGS_VERSION = 1;

    /**
     * 软件设置目录名称。
     */
    private static final String APPLICATION_DIRECTORY =
            "PythonForge-win";

    /**
     * 设置文件名称。
     */
    private static final String SETTINGS_FILE_NAME =
            "settings.properties";

    /**
     * 当前设置。
     */
    private AppSettings settings;

    /**
     * 创建设置服务。
     *
     * <p>
     * 创建后自动加载设置。
     * 如果设置文件不存在或读取失败，
     * 则使用默认设置。
     * </p>
     */
    public SettingsService() {

        this.settings =
                load();
    }

    /**
     * 获取当前设置。
     *
     * @return 当前设置对象
     */
    public synchronized AppSettings getSettings() {

        return settings;
    }

    /**
     * 替换当前设置。
     *
     * <p>
     * 此方法只修改内存中的设置，
     * 不自动写入磁盘。
     * </p>
     *
     * @param settings 新设置
     */
    public synchronized void setSettings(
            AppSettings settings) {

        if (settings == null) {

            throw new IllegalArgumentException(
                    "settings cannot be null"
            );
        }

        this.settings =
                settings;
    }

    /**
     * 获取设置文件路径。
     *
     * @return 设置文件路径
     */
    public Path getSettingsFile() {

        return getSettingsDirectory()
                .resolve(SETTINGS_FILE_NAME);
    }

    /**
     * 获取设置目录。
     *
     * <p>
     * Windows 优先使用：
     *
     * <pre>
     * %APPDATA%\PythonForge-win
     * </pre>
     *
     * 如果 APPDATA 不存在，
     * 则使用：
     *
     * <pre>
     * ${user.home}\PythonForge-win
     * </pre>
     * </p>
     *
     * @return 设置目录
     */
    public Path getSettingsDirectory() {

        String appData =
                System.getenv("APPDATA");

        if (appData != null
                && !appData.isBlank()) {

            return Paths.get(appData)
                    .resolve(APPLICATION_DIRECTORY);
        }

        return Paths.get(
                System.getProperty("user.home")
        ).resolve(
                APPLICATION_DIRECTORY
        );
    }

    /**
     * 加载设置。
     *
     * <p>
     * 如果设置文件不存在，
     * 返回默认设置。
     * </p>
     *
     * <p>
     * 如果设置文件损坏，
     * 同样返回默认设置，
     * 避免设置文件导致整个 PF 无法启动。
     * </p>
     *
     * @return 加载后的设置
     */
    public synchronized AppSettings load() {

        Path file =
                getSettingsFile();

        if (!Files.exists(file)) {

            return createDefaultSettings();
        }

        Properties properties =
                new Properties();

        try (Reader reader =
                     Files.newBufferedReader(
                             file,
                             StandardCharsets.UTF_8
                     )) {

            properties.load(reader);

            return fromProperties(
                    properties
            );

        } catch (Exception exception) {

            /*
             * 设置文件损坏时，
             * 不应该影响 PF 启动。
             */
            return createDefaultSettings();
        }
    }

    /**
     * 重新从磁盘加载设置。
     *
     * @return 最新设置
     */
    public synchronized AppSettings reload() {

        this.settings =
                load();

        return this.settings;
    }

    /**
     * 保存当前设置。
     *
     * @throws IOException 保存失败
     */
    public synchronized void save()
            throws IOException {

        save(this.settings);
    }

    /**
     * 保存指定设置。
     *
     * @param settings 要保存的设置
     *
     * @throws IOException 保存失败
     */
    public synchronized void save(
            AppSettings settings)
            throws IOException {

        if (settings == null) {

            throw new IllegalArgumentException(
                    "settings cannot be null"
            );
        }

        Path directory =
                getSettingsDirectory();

        Files.createDirectories(
                directory
        );

        Path target =
                getSettingsFile();

        Path temporary =
                directory.resolve(
                        SETTINGS_FILE_NAME
                                + ".tmp"
                );

        Properties properties =
                toProperties(
                        settings
                );

        try (Writer writer =
                     Files.newBufferedWriter(
                             temporary,
                             StandardCharsets.UTF_8
                     )) {

            properties.store(
                    writer,
                    "PythonForge-win Settings"
            );
        }

        moveAtomically(
                temporary,
                target
        );

        this.settings =
                settings;
    }

    /**
     * 保存设置的副本。
     *
     * <p>
     * 用于避免调用方在保存过程中继续修改
     * 当前 AppSettings 对象。
     * </p>
     *
     * @throws IOException 保存失败
     */
    public synchronized void saveCopy()
            throws IOException {

        save(
                this.settings.copy()
        );
    }

    /**
     * 恢复默认设置。
     *
     * <p>
     * 此操作只修改内存，
     * 不自动保存。
     * </p>
     *
     * @return 默认设置
     */
    public synchronized AppSettings reset() {

        this.settings =
                createDefaultSettings();

        return this.settings;
    }

    /**
     * 恢复默认设置并立即保存。
     *
     * @return 默认设置
     *
     * @throws IOException 保存失败
     */
    public synchronized AppSettings resetAndSave()
            throws IOException {

        AppSettings defaults =
                createDefaultSettings();

        save(defaults);

        return this.settings;
    }

    /**
     * 判断设置文件是否存在。
     *
     * @return true 表示存在
     */
    public boolean exists() {

        return Files.exists(
                getSettingsFile()
        );
    }

    /**
     * 删除设置文件。
     *
     * <p>
     * 删除后不会立即修改当前内存设置。
     * </p>
     *
     * @return 是否成功删除
     *
     * @throws IOException 删除失败
     */
    public boolean deleteSettingsFile()
            throws IOException {

        return Files.deleteIfExists(
                getSettingsFile()
        );
    }

    /**
     * 创建默认设置。
     *
     * @return 默认设置
     */
    private AppSettings createDefaultSettings() {

        AppSettings defaults =
                new AppSettings();

        defaults.setVersion(
                SETTINGS_VERSION
        );

        defaults.setLanguage(
                LanguageOption.ZH_CN
        );

        defaults.setThemeMode(
                ThemeMode.AUTO
        );

        defaults.setCustomPrimaryColor(
                "#0078D4"
        );

        defaults.setCustomBackgroundColor(
                "#FFFFFF"
        );

        defaults.setCustomTextColor(
                "#1E1E1E"
        );

        defaults.setCustomAccentColor(
                "#0078D4"
        );

        defaults.setCustomBackgroundImage(
                null
        );

        defaults.setDefaultPythonExecutable(
                null
        );

        defaults.setAutoDetectPython(
                true
        );

        defaults.setDefaultBuildMode(
                AppSettings
                        .PyInstallerBuildConfigMode
                        .ONEFILE
        );

        defaults.setDefaultConsoleMode(
                AppSettings
                        .PyInstallerConsoleMode
                        .WINDOWED
        );

        defaults.setDefaultClean(
                false
        );

        defaults.setDefaultNoconfirm(
                false
        );

        defaults.setDefaultNoUpx(
                false
        );

        defaults.setDefaultOutputDirectory(
                null
        );

        return defaults;
    }

    /**
     * 将 AppSettings 转换成 Properties。
     */
    private Properties toProperties(
            AppSettings settings) {

        Properties properties =
                new Properties();

        /*
         * =====================================================
         * 基础
         * =====================================================
         */

        properties.setProperty(
                "version",
                String.valueOf(
                        settings.getVersion()
                )
        );

        /*
         * =====================================================
         * Language
         * =====================================================
         */

        properties.setProperty(
                "language",
                settings.getLanguage()
                        .getLanguageTag()
        );

        /*
         * =====================================================
         * Theme
         * =====================================================
         */

        properties.setProperty(
                "theme.mode",
                settings.getThemeMode()
                        .name()
        );

        properties.setProperty(
                "theme.custom.primaryColor",
                settings.getCustomPrimaryColor()
        );

        properties.setProperty(
                "theme.custom.backgroundColor",
                settings.getCustomBackgroundColor()
        );

        properties.setProperty(
                "theme.custom.textColor",
                settings.getCustomTextColor()
        );

        properties.setProperty(
                "theme.custom.accentColor",
                settings.getCustomAccentColor()
        );

        putPath(
                properties,
                "theme.custom.backgroundImage",
                settings.getCustomBackgroundImage()
        );

        /*
         * =====================================================
         * Python
         * =====================================================
         */

        putPath(
                properties,
                "python.defaultExecutable",
                settings.getDefaultPythonExecutable()
        );

        properties.setProperty(
                "python.autoDetect",
                String.valueOf(
                        settings.isAutoDetectPython()
                )
        );

        /*
         * =====================================================
         * Build
         * =====================================================
         */

        properties.setProperty(
                "build.default.mode",
                settings.getDefaultBuildMode()
                        .name()
        );

        properties.setProperty(
                "build.default.consoleMode",
                settings.getDefaultConsoleMode()
                        .name()
        );

        properties.setProperty(
                "build.default.clean",
                String.valueOf(
                        settings.isDefaultClean()
                )
        );

        properties.setProperty(
                "build.default.noconfirm",
                String.valueOf(
                        settings.isDefaultNoconfirm()
                )
        );

        properties.setProperty(
                "build.default.noUpx",
                String.valueOf(
                        settings.isDefaultNoUpx()
                )
        );

        putPath(
                properties,
                "build.default.outputDirectory",
                settings.getDefaultOutputDirectory()
        );

        return properties;
    }

    /**
     * 从 Properties 创建 AppSettings。
     */
    private AppSettings fromProperties(
            Properties properties) {

        AppSettings settings =
                createDefaultSettings();

        /*
         * =====================================================
         * Version
         * =====================================================
         */

        settings.setVersion(
                parseInt(
                        properties.getProperty(
                                "version"
                        ),
                        SETTINGS_VERSION
                )
        );

        /*
         * =====================================================
         * Language
         * =====================================================
         */

        settings.setLanguage(
                LanguageOption.fromLanguageTag(
                        properties.getProperty(
                                "language",
                                LanguageOption.ZH_CN
                                        .getLanguageTag()
                        )
                )
        );

        /*
         * =====================================================
         * Theme
         * =====================================================
         */

        settings.setThemeMode(
                parseThemeMode(
                        properties.getProperty(
                                "theme.mode"
                        ),
                        ThemeMode.AUTO
                )
        );

        settings.setCustomPrimaryColor(
                properties.getProperty(
                        "theme.custom.primaryColor",
                        "#0078D4"
                )
        );

        settings.setCustomBackgroundColor(
                properties.getProperty(
                        "theme.custom.backgroundColor",
                        "#FFFFFF"
                )
        );

        settings.setCustomTextColor(
                properties.getProperty(
                        "theme.custom.textColor",
                        "#1E1E1E"
                )
        );

        settings.setCustomAccentColor(
                properties.getProperty(
                        "theme.custom.accentColor",
                        "#0078D4"
                )
        );

        settings.setCustomBackgroundImage(
                parsePath(
                        properties.getProperty(
                                "theme.custom.backgroundImage"
                        )
                )
        );

        /*
         * =====================================================
         * Python
         * =====================================================
         */

        settings.setDefaultPythonExecutable(
                parsePath(
                        properties.getProperty(
                                "python.defaultExecutable"
                        )
                )
        );

        settings.setAutoDetectPython(
                parseBoolean(
                        properties.getProperty(
                                "python.autoDetect"
                        ),
                        true
                )
        );

        /*
         * =====================================================
         * Build
         * =====================================================
         */

        settings.setDefaultBuildMode(
                parseBuildMode(
                        properties.getProperty(
                                "build.default.mode"
                        ),
                        AppSettings
                                .PyInstallerBuildConfigMode
                                .ONEFILE
                )
        );

        settings.setDefaultConsoleMode(
                parseConsoleMode(
                        properties.getProperty(
                                "build.default.consoleMode"
                        ),
                        AppSettings
                                .PyInstallerConsoleMode
                                .WINDOWED
                )
        );

        settings.setDefaultClean(
                parseBoolean(
                        properties.getProperty(
                                "build.default.clean"
                        ),
                        false
                )
        );

        settings.setDefaultNoconfirm(
                parseBoolean(
                        properties.getProperty(
                                "build.default.noconfirm"
                        ),
                        false
                )
        );

        settings.setDefaultNoUpx(
                parseBoolean(
                        properties.getProperty(
                                "build.default.noUpx"
                        ),
                        false
                )
        );

        settings.setDefaultOutputDirectory(
                parsePath(
                        properties.getProperty(
                                "build.default.outputDirectory"
                        )
                )
        );

        return settings;
    }

    /**
     * 保存 Path。
     */
    private void putPath(
            Properties properties,
            String key,
            Path path) {

        if (path == null) {

            return;
        }

        properties.setProperty(
                key,
                path.toString()
        );
    }

    /**
     * 读取 Path。
     */
    private Path parsePath(
            String value) {

        if (value == null
                || value.isBlank()) {

            return null;
        }

        try {

            return Paths.get(
                    value.trim()
            );

        } catch (Exception ignored) {

            return null;
        }
    }

    /**
     * 解析整数。
     */
    private int parseInt(
            String value,
            int defaultValue) {

        if (value == null
                || value.isBlank()) {

            return defaultValue;
        }

        try {

            return Integer.parseInt(
                    value.trim()
            );

        } catch (NumberFormatException ignored) {

            return defaultValue;
        }
    }

    /**
     * 解析 Boolean。
     */
    private boolean parseBoolean(
            String value,
            boolean defaultValue) {

        if (value == null
                || value.isBlank()) {

            return defaultValue;
        }

        if ("true".equalsIgnoreCase(
                value.trim()
        )) {

            return true;
        }

        if ("false".equalsIgnoreCase(
                value.trim()
        )) {

            return false;
        }

        return defaultValue;
    }

    /**
     * 解析主题模式。
     */
    private ThemeMode parseThemeMode(
            String value,
            ThemeMode defaultValue) {

        if (value == null
                || value.isBlank()) {

            return defaultValue;
        }

        try {

            return ThemeMode.valueOf(
                    value.trim()
                            .toUpperCase()
            );

        } catch (IllegalArgumentException ignored) {

            return defaultValue;
        }
    }

    /**
     * 解析默认打包模式。
     */
    private AppSettings.PyInstallerBuildConfigMode
    parseBuildMode(
            String value,
            AppSettings.PyInstallerBuildConfigMode
                    defaultValue) {

        if (value == null
                || value.isBlank()) {

            return defaultValue;
        }

        try {

            return AppSettings
                    .PyInstallerBuildConfigMode
                    .valueOf(
                            value.trim()
                                    .toUpperCase()
                    );

        } catch (IllegalArgumentException ignored) {

            return defaultValue;
        }
    }

    /**
     * 解析默认控制台模式。
     */
    private AppSettings.PyInstallerConsoleMode
    parseConsoleMode(
            String value,
            AppSettings.PyInstallerConsoleMode
                    defaultValue) {

        if (value == null
                || value.isBlank()) {

            return defaultValue;
        }

        try {

            return AppSettings
                    .PyInstallerConsoleMode
                    .valueOf(
                            value.trim()
                                    .toUpperCase()
                    );

        } catch (IllegalArgumentException ignored) {

            return defaultValue;
        }
    }

    /**
     * 原子移动临时文件。
     */
    private void moveAtomically(
            Path source,
            Path target)
            throws IOException {

        try {

            Files.move(
                    source,
                    target,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );

        } catch (AtomicMoveNotSupportedException exception) {

            Files.move(
                    source,
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }
}