package com.pythonforge.model;

/**
 * PythonForge-win 主题类型。
 */
public enum ThemeType {

    /**
     * 自动跟随 Windows 系统主题。
     */
    AUTO("自动"),

    /**
     * 亮色主题。
     */
    LIGHT("亮色"),

    /**
     * 暗色主题。
     */
    DARK("暗色"),

    /**
     * 用户自定义主题。
     */
    CUSTOM("自定义");

    private final String displayName;

    ThemeType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}