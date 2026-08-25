package com.pythonforge.service;

import com.pythonforge.model.LanguageOption;

/**
 * PythonForge-win 语言变化监听器。
 *
 * <p>
 * 当当前应用语言发生变化时调用。
 * </p>
 */
@FunctionalInterface
public interface LanguageChangeListener {

    /**
     * 语言发生变化。
     *
     * @param oldLanguage 原语言
     * @param newLanguage 新语言
     */
    void onLanguageChanged(
            LanguageOption oldLanguage,
            LanguageOption newLanguage
    );
}