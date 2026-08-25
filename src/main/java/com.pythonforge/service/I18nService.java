package com.pythonforge.service;

import com.pythonforge.model.LanguageOption;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * PythonForge-win 国际化服务。
 *
 * <p>
 * 负责加载和读取界面语言资源。
 * </p>
 *
 * <p>
 * 当前支持：
 * </p>
 *
 * <ul>
 *     <li>简体中文</li>
 *     <li>English</li>
 *     <li>Français</li>
 *     <li>Deutsch</li>
 *     <li>Español</li>
 * </ul>
 *
 * <p>
 * 本类只负责国际化资源的读取，
 * 不直接修改 JavaFX 控件。
 * </p>
 */
public class I18nService {

    /**
     * ResourceBundle 基础名称。
     *
     * <p>
     * 对应：
     *
     * <pre>
     * src/main/resources/i18n/messages_zh_CN.properties
     * src/main/resources/i18n/messages_en_US.properties
     * src/main/resources/i18n/messages_fr_FR.properties
     * src/main/resources/i18n/messages_de_DE.properties
     * src/main/resources/i18n/messages_es_ES.properties
     * </pre>
     * </p>
     */
    private static final String BUNDLE_BASE_NAME =
            "i18n.messages";

    /**
     * 默认语言。
     */
    private static final LanguageOption
            DEFAULT_LANGUAGE =
            LanguageOption.ZH_CN;

    /**
     * 当前语言。
     */
    private LanguageOption currentLanguage;

    /**
     * 当前 ResourceBundle。
     */
    private ResourceBundle currentBundle;

    /**
     * 设置服务。
     *
     * <p>
     * 用于读取用户保存的语言设置。
     * </p>
     */
    private final SettingsService settingsService;

    /**
     * 创建国际化服务。
     *
     * <p>
     * 创建后自动从 SettingsService
     * 读取当前语言。
     * </p>
     *
     * @param settingsService 设置服务
     */
    public I18nService(
            SettingsService settingsService) {

        this.settingsService =
                Objects.requireNonNull(
                        settingsService,
                        "settingsService"
                );

        LanguageOption language =
                settingsService
                        .getSettings()
                        .getLanguage();

        if (language == null) {

            language =
                    DEFAULT_LANGUAGE;
        }

        this.currentLanguage =
                language;

        this.currentBundle =
                loadBundle(
                        language
                );
    }

    /**
     * 获取当前语言。
     *
     * @return 当前语言
     */
    public synchronized LanguageOption
    getCurrentLanguage() {

        return currentLanguage;
    }

    /**
     * 获取当前 Locale。
     *
     * @return 当前 Locale
     */
    public synchronized Locale getCurrentLocale() {

        return currentLanguage.getLocale();
    }

    /**
     * 获取当前 ResourceBundle。
     *
     * @return 当前资源包
     */
    public synchronized ResourceBundle
    getCurrentBundle() {

        return currentBundle;
    }

    /**
     * 切换语言。
     *
     * <p>
     * 此方法只修改内存中的当前语言，
     * 不自动保存到磁盘。
     * </p>
     *
     * <p>
     * P3.4 会在此基础上实现：
     * </p>
     *
     * <ul>
     *     <li>设置界面切换语言</li>
     *     <li>刷新 JavaFX UI</li>
     *     <li>保存语言设置</li>
     * </ul>
     *
     * @param language 新语言
     */
    public synchronized void setLanguage(
            LanguageOption language) {

        Objects.requireNonNull(
                language,
                "language"
        );

        if (language == currentLanguage
                && currentBundle != null) {

            return;
        }

        ResourceBundle bundle =
                loadBundle(
                        language
                );

        this.currentLanguage =
                language;

        this.currentBundle =
                bundle;
    }

    /**
     * 切换语言并保存设置。
     *
     * <p>
     * 此方法属于服务层的便捷方法。
     * 后续设置界面可以直接调用。
     * </p>
     *
     * @param language 新语言
     *
     * @throws IOException 保存设置失败
     */
    public synchronized void setLanguageAndSave(
            LanguageOption language)
            throws IOException {

        setLanguage(
                language
        );

        settingsService
                .getSettings()
                .setLanguage(
                        language
                );

        settingsService.save();
    }

    /**
     * 重新读取当前语言资源。
     *
     * <p>
     * 用于资源文件发生变化后重新加载。
     * </p>
     */
    public synchronized void reload() {

        this.currentBundle =
                loadBundle(
                        currentLanguage
                );
    }

    /**
     * 获取文本。
     *
     * @param key properties 中的 key
     *
     * @return 对应文本
     */
    public synchronized String get(
            String key) {

        if (key == null
                || key.isBlank()) {

            return "";
        }

        /*
         * 当前语言。
         */
        String value =
                getFromBundle(
                        currentBundle,
                        key
                );

        if (value != null) {

            return value;
        }

        /*
         * 当前语言不存在时，
         * 尝试 English。
         */
        if (currentLanguage
                != LanguageOption.EN_US) {

            ResourceBundle englishBundle =
                    loadBundleSafely(
                            LanguageOption.EN_US
                    );

            value =
                    getFromBundle(
                            englishBundle,
                            key
                    );

            if (value != null) {

                return value;
            }
        }

        /*
         * English 也不存在时，
         * 尝试中文。
         */
        if (currentLanguage
                != LanguageOption.ZH_CN) {

            ResourceBundle chineseBundle =
                    loadBundleSafely(
                            LanguageOption.ZH_CN
                    );

            value =
                    getFromBundle(
                            chineseBundle,
                            key
                    );

            if (value != null) {

                return value;
            }
        }

        /*
         * 最终仍不存在，
         * 返回明显的缺失标记。
         */
        return "!" + key + "!";
    }

    /**
     * 获取带参数的文本。
     *
     * <p>
     * 例如 properties：
     *
     * <pre>
     * build.success=构建成功：{0}
     * </pre>
     *
     * Java：
     *
     * <pre>
     * i18n.get(
     *     "build.success",
     *     "Demo.exe"
     * );
     * </pre>
     * </p>
     *
     * @param key properties key
     * @param arguments 参数
     *
     * @return 格式化后的文本
     */
    public synchronized String get(
            String key,
            Object... arguments) {

        String pattern =
                get(key);

        if (arguments == null
                || arguments.length == 0) {

            return pattern;
        }

        try {

            MessageFormat format =
                    new MessageFormat(
                            pattern,
                            currentLanguage.getLocale()
                    );

            return format.format(
                    arguments
            );

        } catch (IllegalArgumentException ignored) {

            /*
             * 如果 properties 中的文本不是合法
             * MessageFormat 格式，则直接返回原文本。
             */
            return pattern;
        }
    }

    /**
     * 判断当前语言是否存在指定 key。
     *
     * @param key properties key
     *
     * @return 是否存在
     */
    public synchronized boolean contains(
            String key) {

        if (key == null
                || key.isBlank()) {

            return false;
        }

        return currentBundle != null
                && currentBundle.containsKey(
                key
        );
    }

    /**
     * 加载 ResourceBundle。
     *
     * @param language 语言
     *
     * @return ResourceBundle
     */
    private ResourceBundle loadBundle(
            LanguageOption language) {

        try {

            return ResourceBundle.getBundle(
                    BUNDLE_BASE_NAME,
                    language.getLocale(),
                    UTF8_CONTROL
            );

        } catch (MissingResourceException exception) {

            /*
             * 当前语言加载失败时，
             * 尝试中文。
             */
            if (language
                    != DEFAULT_LANGUAGE) {

                try {

                    return ResourceBundle.getBundle(
                            BUNDLE_BASE_NAME,
                            DEFAULT_LANGUAGE
                                    .getLocale(),
                            UTF8_CONTROL
                    );

                } catch (MissingResourceException ignored) {

                    throw new IllegalStateException(
                            "无法加载 PythonForge-win "
                                    + "国际化资源文件："
                                    + BUNDLE_BASE_NAME,
                            exception
                    );
                }
            }

            throw new IllegalStateException(
                    "无法加载 PythonForge-win "
                            + "国际化资源文件："
                            + BUNDLE_BASE_NAME,
                    exception
            );
        }
    }

    /**
     * 安全加载 ResourceBundle。
     *
     * <p>
     * 加载失败时返回 null，
     * 不影响主程序。
     * </p>
     */
    private ResourceBundle loadBundleSafely(
            LanguageOption language) {

        try {

            return loadBundle(
                    language
            );

        } catch (RuntimeException ignored) {

            return null;
        }
    }

    /**
     * 从 ResourceBundle 中读取文本。
     */
    private String getFromBundle(
            ResourceBundle bundle,
            String key) {

        if (bundle == null) {

            return null;
        }

        try {

            if (!bundle.containsKey(key)) {

                return null;
            }

            return bundle.getString(
                    key
            );

        } catch (
                MissingResourceException
                | ClassCastException exception) {

            return null;
        }
    }

    /**
     * JavaFX / Java 21 环境下使用 UTF-8
     * 读取 properties。
     *
     * <p>
     * 这样法语、德语、西班牙语以及中文
     * 都不会因为编码问题出现乱码。
     * </p>
     */
    private static final ResourceBundle.Control
            UTF8_CONTROL =
            new ResourceBundle.Control() {

                @Override
                public ResourceBundle newBundle(
                        String baseName,
                        Locale locale,
                        String format,
                        ClassLoader loader,
                        boolean reload)
                        throws IllegalAccessException,
                        InstantiationException,
                        IOException {

                    String bundleName =
                            toBundleName(
                                    baseName,
                                    locale
                            );

                    String resourceName =
                            toResourceName(
                                    bundleName,
                                    "properties"
                            );

                    InputStream inputStream;

                    if (reload) {

                        java.net.URL url =
                                loader.getResource(
                                        resourceName
                                );

                        if (url == null) {

                            return null;
                        }

                        java.net.URLConnection connection =
                                url.openConnection();

                        if (connection != null) {

                            connection.setUseCaches(
                                    false
                            );
                        }

                        inputStream =
                                connection.getInputStream();

                    } else {

                        inputStream =
                                loader.getResourceAsStream(
                                        resourceName
                                );
                    }

                    if (inputStream == null) {

                        return null;
                    }

                    try (
                            InputStream stream =
                                    inputStream;

                            Reader reader =
                                    new InputStreamReader(
                                            stream,
                                            StandardCharsets.UTF_8
                                    )
                    ) {

                        return new PropertyResourceBundle(
                                reader
                        );
                    }
                }
            };
}