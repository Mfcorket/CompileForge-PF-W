package com.pythonforge.model;

import java.util.Locale;

/**
 * PythonForge-win 支持的界面语言。
 *
 * <p>
 * Alpha.1.1.2 首期最多支持五种语言。
 * </p>
 */
public enum LanguageOption {

    /**
     * 简体中文。
     */
    ZH_CN(
            "zh-CN",
            Locale.SIMPLIFIED_CHINESE,
            "简体中文"
    ),

    /**
     * English。
     */
    EN_US(
            "en-US",
            Locale.US,
            "English"
    ),

    /**
     * Français。
     */
    FR_FR(
            "fr-FR",
            Locale.FRANCE,
            "Français"
    ),

    /**
     * Deutsch。
     */
    DE_DE(
            "de-DE",
            Locale.GERMANY,
            "Deutsch"
    ),

    /**
     * Español。
     */
    ES_ES(
            "es-ES",
            Locale.forLanguageTag("es-ES"),
            "Español"
    );

    /**
     * BCP 47 Language Tag。
     */
    private final String languageTag;

    /**
     * Java Locale。
     */
    private final Locale locale;

    /**
     * 用户可见名称。
     */
    private final String displayName;

    LanguageOption(
            String languageTag,
            Locale locale,
            String displayName) {

        this.languageTag = languageTag;
        this.locale = locale;
        this.displayName = displayName;
    }

    /**
     * 获取语言标签。
     *
     * @return BCP 47 language tag
     */
    public String getLanguageTag() {

        return languageTag;
    }

    /**
     * 获取 Locale。
     *
     * @return Locale
     */
    public Locale getLocale() {

        return locale;
    }

    /**
     * 获取显示名称。
     *
     * @return 显示名称
     */
    public String getDisplayName() {

        return displayName;
    }

    /**
     * 根据 language tag 查找语言。
     *
     * @param languageTag language tag
     *
     * @return 对应语言；找不到时返回中文
     */
    public static LanguageOption fromLanguageTag(
            String languageTag) {

        if (languageTag == null
                || languageTag.isBlank()) {

            return ZH_CN;
        }

        for (LanguageOption option : values()) {

            if (option.languageTag.equalsIgnoreCase(
                    languageTag.trim()
            )) {

                return option;
            }
        }

        return ZH_CN;
    }

    /**
     * 根据 Locale 查找语言。
     *
     * @param locale Locale
     *
     * @return 对应语言；找不到时返回中文
     */
    public static LanguageOption fromLocale(
            Locale locale) {

        if (locale == null) {
            return ZH_CN;
        }

        for (LanguageOption option : values()) {

            if (option.locale.equals(locale)) {
                return option;
            }

            if (option.languageTag.equalsIgnoreCase(
                    locale.toLanguageTag()
            )) {

                return option;
            }
        }

        /*
         * 只比较语言代码。
         *
         * 例如：
         *
         * en-GB -> English
         * fr-CA -> Français
         */
        String language =
                locale.getLanguage();

        for (LanguageOption option : values()) {

            if (option.locale
                    .getLanguage()
                    .equalsIgnoreCase(language)) {

                return option;
            }
        }

        return ZH_CN;
    }

    @Override
    public String toString() {

        return displayName;
    }
}