package com.pythonforge.model;

/**
 * PythonForge-win 主题模式。
 *
 * <p>
 * 用于保存软件当前的主题设置。
 * </p>
 *
 * <ul>
 *     <li>AUTO - 自动跟随 Windows 系统主题</li>
 *     <li>LIGHT - 亮色主题</li>
 *     <li>DARK - 暗色主题</li>
 *     <li>CUSTOM - 用户自定义主题</li>
 * </ul>
 */
public enum ThemeMode {

    /**
     * 自动跟随系统。
     */
    AUTO,

    /**
     * 亮色主题。
     */
    LIGHT,

    /**
     * 暗色主题。
     */
    DARK,

    /**
     * 用户自定义主题。
     */
    CUSTOM
}