package com.pythonforge.service;

import com.pythonforge.model.ThemeConfig;
import com.pythonforge.model.ThemeType;
import javafx.application.Platform;
import javafx.scene.Scene;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * PythonForge-win 主题管理器。
 *
 * <p>
 * 负责：
 * </p>
 *
 * <ul>
 *     <li>加载主题配置</li>
 *     <li>应用亮色主题</li>
 *     <li>应用暗色主题</li>
 *     <li>应用自定义主题</li>
 *     <li>自动跟随 Windows 主题</li>
 *     <li>保存主题</li>
 * </ul>
 */
public class ThemeManager {

    private static final ThemeManager INSTANCE =
            new ThemeManager();


    private static final String COMMON_CSS =
            "/css/application.css";

    private static final String LIGHT_CSS =
            "/css/light.css";

    private static final String DARK_CSS =
            "/css/dark.css";

    private final List<Consumer<ThemeType>>
            themeListeners =
            new CopyOnWriteArrayList<>();


    private final ThemeStorage storage;

    private final WindowsThemeDetector detector;


    private ThemeConfig config;

    private Scene scene;

    private ScheduledExecutorService themeWatcher;


    private String currentThemeCss;


    private ThemeManager() {

        storage =
                new ThemeStorage();

        detector =
                new WindowsThemeDetector();

        config =
                storage.load();
    }


    public static ThemeManager getInstance() {

        return INSTANCE;
    }

    public void addThemeListener(
            Consumer<ThemeType> listener) {

        if (listener != null) {

            themeListeners.add(listener);
        }
    }


    public void removeThemeListener(
            Consumer<ThemeType> listener) {

        if (listener != null) {

            themeListeners.remove(listener);
        }
    }


    /**
     * 初始化主题系统。
     */
    public void initialize(
            Scene scene) {

        this.scene = scene;

        applyCurrentTheme();

        startThemeWatcher();
    }


    /**
     * 获取主题配置。
     */
    public ThemeConfig getConfig() {

        return config;
    }


    /**
     * 设置主题类型。
     */
    public void setThemeType(
            ThemeType themeType) {

        if (themeType == null) {

            return;
        }

        config.setThemeType(
                themeType
        );

        applyCurrentTheme();

        notifyThemeListeners();
    }

    private void notifyThemeListeners() {

        ThemeType resolvedTheme =
                getResolvedTheme();

        for (
                Consumer<ThemeType> listener
                : themeListeners
        ) {

            try {

                listener.accept(
                        resolvedTheme
                );

            } catch (Exception exception) {

                exception.printStackTrace();
            }
        }
    }


    /**
     * 应用当前配置。
     */
    public void applyCurrentTheme() {

        if (scene == null) {

            return;
        }

        Platform.runLater(
                this::applyThemeInternal
        );
    }


    private void applyThemeInternal() {

        String commonCss =
                resourceUrl(
                        COMMON_CSS
                );

        String themeCss;


        switch (config.getThemeType()) {

            case LIGHT -> {

                themeCss =
                        resourceUrl(
                                LIGHT_CSS
                        );
            }


            case DARK -> {

                themeCss =
                        resourceUrl(
                                DARK_CSS
                        );
            }


            case CUSTOM -> {

                themeCss =
                        generateCustomCss();
            }


            case AUTO -> {

                if (
                        detector.isLightTheme()
                ) {

                    themeCss =
                            resourceUrl(
                                    LIGHT_CSS
                            );

                } else {

                    themeCss =
                            resourceUrl(
                                    DARK_CSS
                            );
                }
            }


            default -> {

                themeCss =
                        resourceUrl(
                                LIGHT_CSS
                        );
            }
        }


        scene.getStylesheets().clear();

        scene.getStylesheets().add(
                commonCss
        );


        if (
                themeCss != null
                        &&
                        !themeCss.isBlank()
        ) {

            scene.getStylesheets().add(
                    themeCss
            );

            currentThemeCss =
                    themeCss;
        }

        replaceThemeStylesheets(
                commonCss,
                themeCss
        );

        currentThemeCss =
                themeCss;
    }


    /**
     * 保存当前主题。
     */
    public void save()
            throws IOException {

        storage.save(
                config
        );

        if (
                config.getThemeType()
                        ==
                        ThemeType.CUSTOM
        ) {

            generateCustomCss();
        }
    }


    /**
     * 保存并应用。
     */
    public void saveAndApply()
            throws IOException {

        save();

        applyCurrentTheme();
    }


    /**
     * 获取当前实际使用的主题。
     */
    public ThemeType getResolvedTheme() {

        if (
                config.getThemeType()
                        ==
                        ThemeType.AUTO
        ) {

            return detector.isLightTheme()
                    ? ThemeType.LIGHT
                    : ThemeType.DARK;
        }

        return config.getThemeType();
    }


    /**
     * 获取当前实际 CSS。
     */
    public String getCurrentThemeCss() {

        return currentThemeCss;
    }


    /**
     * 停止主题监视器。
     */
    public void shutdown() {

        if (themeWatcher != null) {

            themeWatcher.shutdownNow();

            themeWatcher = null;
        }
    }


    private void startThemeWatcher() {

        if (themeWatcher != null) {

            return;
        }


        themeWatcher =
                Executors.newSingleThreadScheduledExecutor(
                        runnable -> {

                            Thread thread =
                                    new Thread(
                                            runnable,
                                            "PythonForge-ThemeWatcher"
                                    );

                            thread.setDaemon(
                                    true
                            );

                            return thread;
                        }
                );


        themeWatcher.scheduleWithFixedDelay(
                this::checkSystemTheme,
                2,
                2,
                TimeUnit.SECONDS
        );
    }


    private void checkSystemTheme() {

        if (
                config.getThemeType()
                        !=
                        ThemeType.AUTO
        ) {

            return;
        }


        ThemeType resolved =
                getResolvedTheme();


        if (
                resolved
                        !=
                        getCurrentResolvedTheme()
        ) {

            Platform.runLater(
                    this::applyThemeInternal
            );
        }
    }


    private ThemeType getCurrentResolvedTheme() {

        if (
                currentThemeCss == null
        ) {

            return null;
        }


        if (
                currentThemeCss.endsWith(
                        "light.css"
                )
        ) {

            return ThemeType.LIGHT;
        }


        if (
                currentThemeCss.endsWith(
                        "dark.css"
                )
        ) {

            return ThemeType.DARK;
        }


        return ThemeType.CUSTOM;
    }


    private String resourceUrl(
            String resource) {

        var url =
                ThemeManager.class.getResource(
                        resource
                );

        if (url == null) {

            return null;
        }

        return url.toExternalForm();
    }


    /**
     * 生成用户自定义 CSS。
     */
    private String generateCustomCss() {

        Path cssFile =
                storage.getCustomCssFile();

        try {

            Files.createDirectories(
                    cssFile.getParent()
            );


            StringBuilder css =
                    new StringBuilder();


            css.append(
                    "/* PythonForge-win Custom Theme */\n"
            );


            css.append(
                    ".root {\n"
            );

            css.append(
                    "    -fx-background-color: "
            );

            css.append(
                    config.getBackgroundColor()
            );

            css.append(
                    ";\n"
            );

            css.append(
                    "    -fx-text-fill: "
            );

            css.append(
                    config.getTextColor()
            );

            css.append(
                    ";\n"
            );

            css.append(
                    "}\n\n"
            );


            css.append(
                    ".workspace {\n"
            );

            css.append(
                    "    -fx-background-color: "
            );

            css.append(
                    config.getBackgroundColor()
            );

            css.append(
                    ";\n"
            );

            css.append(
                    "}\n\n"
            );


            css.append(
                    ".workspace-header,\n"
            );

            css.append(
                    ".project-card,\n"
            );

            css.append(
                    ".build-toolbar {\n"
            );

            css.append(
                    "    -fx-background-color: "
            );

            css.append(
                    config.getPanelColor()
            );

            css.append(
                    ";\n"
            );

            css.append(
                    "}\n\n"
            );


            css.append(
                    ".workspace-title,\n"
            );

            css.append(
                    ".card-value,\n"
            );

            css.append(
                    ".panel-title {\n"
            );

            css.append(
                    "    -fx-text-fill: "
            );

            css.append(
                    config.getTextColor()
            );

            css.append(
                    ";\n"
            );

            css.append(
                    "}\n\n"
            );


            css.append(
                    ".secondary-label,\n"
            );

            css.append(
                    ".workspace-subtitle {\n"
            );

            css.append(
                    "    -fx-text-fill: "
            );

            css.append(
                    config.getSecondaryTextColor()
            );

            css.append(
                    ";\n"
            );

            css.append(
                    "}\n\n"
            );


            css.append(
                    ".project-card,\n"
            );

            css.append(
                    ".build-toolbar,\n"
            );

            css.append(
                    ".workspace-tabs,\n"
            );

            css.append(
                    ".result-label {\n"
            );

            css.append(
                    "    -fx-border-color: "
            );

            css.append(
                    config.getBorderColor()
            );

            css.append(
                    ";\n"
            );

            css.append(
                    "}\n\n"
            );


            css.append(
                    ".button,\n"
            );

            css.append(
                    ".primary-button {\n"
            );

            css.append(
                    "    -fx-border-color: "
            );

            css.append(
                    config.getAccentColor()
            );

            css.append(
                    ";\n"
            );

            css.append(
                    "}\n\n"
            );


            css.append(
                    ".primary-button,\n"
            );

            css.append(
                    ".status-bar {\n"
            );

            css.append(
                    "    -fx-background-color: "
            );

            css.append(
                    config.getAccentColor()
            );

            css.append(
                    ";\n"
            );

            css.append(
                    "}\n\n"
            );


            /*
             * 自定义背景图片。
             */
            if (
                    config.getBackgroundImage()
                            != null
                            &&
                            !config
                                    .getBackgroundImage()
                                    .isBlank()
            ) {

                Path imagePath =
                        Path.of(
                                config
                                        .getBackgroundImage()
                        );

                if (
                        Files.exists(
                                imagePath
                        )
                ) {

                    String imageUrl =
                            imagePath
                                    .toUri()
                                    .toASCIIString();


                    css.append(
                            ".workspace {\n"
                    );

                    css.append(
                            "    -fx-background-image: url(\""
                    );

                    css.append(
                            imageUrl
                    );

                    css.append(
                            "\");\n"
                    );

                    css.append(
                            "    -fx-background-repeat: no-repeat;\n"
                    );

                    css.append(
                            "    -fx-background-position: center center;\n"
                    );

                    css.append(
                            "    -fx-background-size: cover;\n"
                    );

                    css.append(
                            "}\n"
                    );
                }
            }


            Files.writeString(
                    cssFile,
                    css.toString(),
                    StandardCharsets.UTF_8
            );


            return cssFile
                    .toUri()
                    .toASCIIString();

        } catch (IOException exception) {

            return null;
        }
    }

    private void replaceThemeStylesheets(
            String commonCss,
            String themeCss) {

        scene.getStylesheets().clear();

        if (
                commonCss != null
                        &&
                        !commonCss.isBlank()
        ) {

            scene.getStylesheets().add(
                    commonCss
            );
        }

        if (
                themeCss != null
                        &&
                        !themeCss.isBlank()
        ) {

            scene.getStylesheets().add(
                    themeCss
            );
        }
    }


}