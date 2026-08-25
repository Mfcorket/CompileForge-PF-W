package com.pythonforge.service;

import com.pythonforge.model.ThemeConfig;
import com.pythonforge.model.ThemeType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * PythonForge-win 主题配置存储。
 */
public class ThemeStorage {

    private static final String APP_DATA_NAME =
            "PythonForge-win";

    private static final String SETTINGS_DIRECTORY =
            "settings";

    private static final String THEMES_DIRECTORY =
            "themes";

    private static final String THEME_FILE =
            "theme.properties";


    private final Path applicationDirectory;

    private final Path settingsDirectory;

    private final Path themesDirectory;

    private final Path themeFile;


    public ThemeStorage() {

        Path appData =
                resolveApplicationData();

        applicationDirectory =
                appData.resolve(
                        APP_DATA_NAME
                );

        settingsDirectory =
                applicationDirectory.resolve(
                        SETTINGS_DIRECTORY
                );

        themesDirectory =
                applicationDirectory.resolve(
                        THEMES_DIRECTORY
                );

        themeFile =
                settingsDirectory.resolve(
                        THEME_FILE
                );
    }


    /**
     * 加载主题配置。
     *
     * <p>
     * 如果不存在配置文件，则返回默认配置。
     * </p>
     */
    public ThemeConfig load() {

        ThemeConfig config =
                new ThemeConfig();

        if (!Files.exists(themeFile)) {

            return config;
        }

        Properties properties =
                new Properties();

        try (InputStream inputStream =
                     Files.newInputStream(themeFile)) {

            properties.load(inputStream);

            String themeType =
                    properties.getProperty(
                            "theme.type"
                    );

            if (themeType != null) {

                try {

                    config.setThemeType(
                            ThemeType.valueOf(
                                    themeType
                            )
                    );

                } catch (IllegalArgumentException ignored) {

                    config.setThemeType(
                            ThemeType.AUTO
                    );
                }
            }


            config.setBackgroundColor(
                    properties.getProperty(
                            "custom.backgroundColor",
                            config.getBackgroundColor()
                    )
            );


            config.setPanelColor(
                    properties.getProperty(
                            "custom.panelColor",
                            config.getPanelColor()
                    )
            );


            config.setTextColor(
                    properties.getProperty(
                            "custom.textColor",
                            config.getTextColor()
                    )
            );


            config.setSecondaryTextColor(
                    properties.getProperty(
                            "custom.secondaryTextColor",
                            config.getSecondaryTextColor()
                    )
            );


            config.setBorderColor(
                    properties.getProperty(
                            "custom.borderColor",
                            config.getBorderColor()
                    )
            );


            config.setAccentColor(
                    properties.getProperty(
                            "custom.accentColor",
                            config.getAccentColor()
                    )
            );


            config.setBackgroundImage(
                    properties.getProperty(
                            "custom.backgroundImage",
                            ""
                    )
            );

        } catch (IOException ignored) {

            /*
             * 配置读取失败时使用默认配置。
             */
        }

        return config;
    }


    /**
     * 保存主题配置。
     */
    public void save(
            ThemeConfig config)
            throws IOException {

        Files.createDirectories(
                settingsDirectory
        );

        Files.createDirectories(
                themesDirectory
        );

        Properties properties =
                new Properties();

        properties.setProperty(
                "theme.type",
                config.getThemeType().name()
        );

        properties.setProperty(
                "custom.backgroundColor",
                config.getBackgroundColor()
        );

        properties.setProperty(
                "custom.panelColor",
                config.getPanelColor()
        );

        properties.setProperty(
                "custom.textColor",
                config.getTextColor()
        );

        properties.setProperty(
                "custom.secondaryTextColor",
                config.getSecondaryTextColor()
        );

        properties.setProperty(
                "custom.borderColor",
                config.getBorderColor()
        );

        properties.setProperty(
                "custom.accentColor",
                config.getAccentColor()
        );

        properties.setProperty(
                "custom.backgroundImage",
                config.getBackgroundImage()
        );


        try (OutputStream outputStream =
                     Files.newOutputStream(themeFile)) {

            properties.store(
                    outputStream,
                    "PythonForge-win Theme Settings"
            );
        }
    }


    /**
     * 获取自定义 CSS 文件。
     */
    public Path getCustomCssFile() {

        return themesDirectory.resolve(
                "custom.css"
        );
    }


    public Path getApplicationDirectory() {

        return applicationDirectory;
    }


    private Path resolveApplicationData() {

        String appData =
                System.getenv("APPDATA");

        if (
                appData != null
                        &&
                        !appData.isBlank()
        ) {

            return Path.of(appData);
        }

        return Path.of(
                System.getProperty(
                        "user.home"
                )
        );
    }
}