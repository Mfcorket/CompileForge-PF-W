package com.pythonforge.service;

import com.pythonforge.model.LanguageOption;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * PythonForge-win 语言管理器。
 *
 * <p>
 * 负责管理应用当前语言，并连接：
 * </p>
 *
 * <ul>
 *     <li>LanguageOption</li>
 *     <li>I18nService</li>
 *     <li>SettingsService</li>
 * </ul>
 *
 * <p>
 * 本类不负责 JavaFX 控件。
 * JavaFX Controller 可以监听语言变化，
 * 然后刷新自身 UI。
 * </p>
 */
public final class LanguageManager {

    /**
     * 设置服务。
     */
    private final SettingsService settingsService;

    /**
     * 国际化服务。
     */
    private final I18nService i18nService;

    /**
     * 语言变化监听器。
     */
    private final List<LanguageChangeListener>
            listeners =
            new CopyOnWriteArrayList<>();

    /**
     * 创建语言管理器。
     *
     * @param settingsService 设置服务
     */
    public LanguageManager(
            SettingsService settingsService) {

        this.settingsService =
                Objects.requireNonNull(
                        settingsService,
                        "settingsService"
                );

        this.i18nService =
                new I18nService(
                        settingsService
                );
    }

    /**
     * 获取设置服务。
     *
     * @return 设置服务
     */
    public SettingsService getSettingsService() {

        return settingsService;
    }

    /**
     * 获取国际化服务。
     *
     * @return 国际化服务
     */
    public I18nService getI18nService() {

        return i18nService;
    }

    /**
     * 获取当前语言。
     *
     * @return 当前语言
     */
    public LanguageOption getCurrentLanguage() {

        return i18nService
                .getCurrentLanguage();
    }

    /**
     * 获取当前语言的 Locale。
     *
     * @return Locale
     */
    public java.util.Locale getCurrentLocale() {

        return i18nService
                .getCurrentLocale();
    }

    /**
     * 获取当前语言文本。
     *
     * @param key properties key
     *
     * @return 国际化文本
     */
    public String get(
            String key) {

        return i18nService.get(
                key
        );
    }

    /**
     * 获取带参数的国际化文本。
     *
     * @param key properties key
     * @param arguments 参数
     *
     * @return 格式化文本
     */
    public String get(
            String key,
            Object... arguments) {

        return i18nService.get(
                key,
                arguments
        );
    }

    /**
     * 设置语言。
     *
     * <p>
     * 此方法只修改当前运行时语言，
     * 不保存到磁盘。
     * </p>
     *
     * <p>
     * 适合设置页面中的“预览语言”。
     * </p>
     *
     * @param language 新语言
     */
    public void setLanguage(
            LanguageOption language) {

        Objects.requireNonNull(
                language,
                "language"
        );

        LanguageOption oldLanguage =
                getCurrentLanguage();

        if (oldLanguage == language) {

            return;
        }

        i18nService.setLanguage(
                language
        );

        notifyLanguageChanged(
                oldLanguage,
                language
        );
    }

    /**
     * 设置语言并保存。
     *
     * <p>
     * 设置页面最终点击“保存”时使用。
     * </p>
     *
     * @param language 新语言
     *
     * @throws IOException 保存失败
     */
    public void setLanguageAndSave(
            LanguageOption language)
            throws IOException {

        Objects.requireNonNull(
                language,
                "language"
        );

        LanguageOption oldLanguage =
                getCurrentLanguage();

        if (oldLanguage == language) {

            /*
             * 即使语言没有变化，
             * 也确保设置文件已经保存。
             */
            settingsService
                    .getSettings()
                    .setLanguage(
                            language
                    );

            settingsService.save();

            return;
        }

        i18nService.setLanguage(
                language
        );

        settingsService
                .getSettings()
                .setLanguage(
                        language
                );

        settingsService.save();

        notifyLanguageChanged(
                oldLanguage,
                language
        );
    }

    /**
     * 保存当前语言。
     *
     * <p>
     * 当前语言已经切换完成，
     * 但还没有保存时使用。
     * </p>
     *
     * @throws IOException 保存失败
     */
    public void saveCurrentLanguage()
            throws IOException {

        settingsService
                .getSettings()
                .setLanguage(
                        getCurrentLanguage()
                );

        settingsService.save();
    }

    /**
     * 重新从设置文件读取语言。
     *
     * <p>
     * 如果其他模块修改了设置文件，
     * 可以调用此方法重新同步。
     * </p>
     */
    public void reload() {

        LanguageOption oldLanguage =
                getCurrentLanguage();

        LanguageOption newLanguage =
                settingsService
                        .reload()
                        .getLanguage();

        if (newLanguage == null) {

            newLanguage =
                    LanguageOption.ZH_CN;
        }

        i18nService.setLanguage(
                newLanguage
        );

        if (oldLanguage != newLanguage) {

            notifyLanguageChanged(
                    oldLanguage,
                    newLanguage
            );
        }
    }

    /**
     * 添加语言变化监听器。
     *
     * @param listener 监听器
     */
    public void addListener(
            LanguageChangeListener listener) {

        if (listener == null) {

            return;
        }

        if (!listeners.contains(listener)) {

            listeners.add(
                    listener
            );
        }
    }

    /**
     * 删除语言变化监听器。
     *
     * @param listener 监听器
     */
    public void removeListener(
            LanguageChangeListener listener) {

        if (listener == null) {

            return;
        }

        listeners.remove(
                listener
        );
    }

    /**
     * 清空全部监听器。
     */
    public void clearListeners() {

        listeners.clear();
    }

    /**
     * 通知语言变化。
     */
    private void notifyLanguageChanged(
            LanguageOption oldLanguage,
            LanguageOption newLanguage) {

        for (
                LanguageChangeListener listener
                : listeners
        ) {

            try {

                listener.onLanguageChanged(
                        oldLanguage,
                        newLanguage
                );

            } catch (Exception ignored) {

                /*
                 * 单个 UI 监听器出现异常，
                 * 不应该影响其他监听器。
                 */
            }
        }
    }
}