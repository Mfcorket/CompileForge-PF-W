package com.pythonforge.model;

/**
 * PythonForge-win 主题配置。
 *
 * <p>
 * 用于保存用户选择的主题类型以及自定义主题参数。
 * </p>
 */
public class ThemeConfig {

    /**
     * 当前主题类型。
     *
     * <p>
     * 默认使用 AUTO。
     * </p>
     */
    private ThemeType themeType = ThemeType.AUTO;

    /**
     * 主背景颜色。
     */
    private String backgroundColor = "#F3F3F3";

    /**
     * 面板颜色。
     */
    private String panelColor = "#FFFFFF";

    /**
     * 主文字颜色。
     */
    private String textColor = "#1F1F1F";

    /**
     * 次要文字颜色。
     */
    private String secondaryTextColor = "#666666";

    /**
     * 边框颜色。
     */
    private String borderColor = "#D6D6D6";

    /**
     * 主强调颜色。
     */
    private String accentColor = "#0078D4";

    /**
     * 背景图片。
     *
     * <p>
     * 为空表示不使用背景图片。
     * </p>
     */
    private String backgroundImage = "";


    public ThemeConfig() {
    }


    public ThemeType getThemeType() {
        return themeType;
    }


    public void setThemeType(ThemeType themeType) {

        if (themeType == null) {

            this.themeType = ThemeType.AUTO;

            return;
        }

        this.themeType = themeType;
    }


    public String getBackgroundColor() {
        return backgroundColor;
    }


    public void setBackgroundColor(String backgroundColor) {

        this.backgroundColor =
                normalizeColor(
                        backgroundColor,
                        "#F3F3F3"
                );
    }


    public String getPanelColor() {
        return panelColor;
    }


    public void setPanelColor(String panelColor) {

        this.panelColor =
                normalizeColor(
                        panelColor,
                        "#FFFFFF"
                );
    }


    public String getTextColor() {
        return textColor;
    }


    public void setTextColor(String textColor) {

        this.textColor =
                normalizeColor(
                        textColor,
                        "#1F1F1F"
                );
    }


    public String getSecondaryTextColor() {
        return secondaryTextColor;
    }


    public void setSecondaryTextColor(
            String secondaryTextColor) {

        this.secondaryTextColor =
                normalizeColor(
                        secondaryTextColor,
                        "#666666"
                );
    }


    public String getBorderColor() {
        return borderColor;
    }


    public void setBorderColor(String borderColor) {

        this.borderColor =
                normalizeColor(
                        borderColor,
                        "#D6D6D6"
                );
    }


    public String getAccentColor() {
        return accentColor;
    }


    public void setAccentColor(String accentColor) {

        this.accentColor =
                normalizeColor(
                        accentColor,
                        "#0078D4"
                );
    }


    public String getBackgroundImage() {
        return backgroundImage;
    }


    public void setBackgroundImage(
            String backgroundImage) {

        this.backgroundImage =
                backgroundImage == null
                        ? ""
                        : backgroundImage.trim();
    }


    /**
     * 恢复自定义主题默认值。
     */
    public void resetCustomValues() {

        backgroundColor = "#F3F3F3";

        panelColor = "#FFFFFF";

        textColor = "#1F1F1F";

        secondaryTextColor = "#666666";

        borderColor = "#D6D6D6";

        accentColor = "#0078D4";

        backgroundImage = "";
    }


    private String normalizeColor(
            String color,
            String defaultColor) {

        if (color == null) {

            return defaultColor;
        }

        String value = color.trim();

        if (value.isEmpty()) {

            return defaultColor;
        }

        if (!value.startsWith("#")) {

            value = "#" + value;
        }

        return value.toUpperCase();
    }
}