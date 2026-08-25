package com.pythonforge.controller;

import com.pythonforge.model.ThemeConfig;
import com.pythonforge.model.ThemeType;
import com.pythonforge.service.ThemeManager;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

/**
 * 主题设置控制器。
 */
public class ThemeSettingsController {

    @FXML
    private ComboBox<ThemeType> themeTypeComboBox;


    @FXML
    private ColorPicker backgroundColorPicker;


    @FXML
    private ColorPicker panelColorPicker;


    @FXML
    private ColorPicker textColorPicker;


    @FXML
    private ColorPicker secondaryTextColorPicker;


    @FXML
    private ColorPicker borderColorPicker;


    @FXML
    private ColorPicker accentColorPicker;


    @FXML
    private TextField backgroundImageField;


    @FXML
    private Label statusLabel;


    private final ThemeManager themeManager =
            ThemeManager.getInstance();


    @FXML
    private void initialize() {

        themeTypeComboBox
                .getItems()
                .setAll(
                        ThemeType.values()
                );


        loadCurrentConfig();


        themeTypeComboBox
                .valueProperty()
                .addListener(
                        (observable,
                         oldValue,
                         newValue) -> {

                            updateStatus();
                        }
                );
    }


    /**
     * 加载当前配置。
     */
    private void loadCurrentConfig() {

        ThemeConfig config =
                themeManager.getConfig();


        themeTypeComboBox.setValue(
                config.getThemeType()
        );


        backgroundColorPicker.setValue(
                javafx.scene.paint.Color.web(
                        config.getBackgroundColor()
                )
        );


        panelColorPicker.setValue(
                javafx.scene.paint.Color.web(
                        config.getPanelColor()
                )
        );


        textColorPicker.setValue(
                javafx.scene.paint.Color.web(
                        config.getTextColor()
                )
        );


        secondaryTextColorPicker.setValue(
                javafx.scene.paint.Color.web(
                        config.getSecondaryTextColor()
                )
        );


        borderColorPicker.setValue(
                javafx.scene.paint.Color.web(
                        config.getBorderColor()
                )
        );


        accentColorPicker.setValue(
                javafx.scene.paint.Color.web(
                        config.getAccentColor()
                )
        );


        backgroundImageField.setText(
                config.getBackgroundImage()
        );


        updateStatus();
    }


    /**
     * 选择背景图片。
     */
    @FXML
    private void handleSelectBackgroundImage() {

        FileChooser chooser =
                new FileChooser();


        chooser.setTitle(
                "选择主题背景图片"
        );


        chooser.getExtensionFilters()
                .addAll(
                        new FileChooser.ExtensionFilter(
                                "图片文件",
                                "*.png",
                                "*.jpg",
                                "*.jpeg",
                                "*.gif",
                                "*.bmp"
                        ),
                        new FileChooser.ExtensionFilter(
                                "所有文件",
                                "*.*"
                        )
                );


        File file =
                chooser.showOpenDialog(
                        getStage()
                );


        if (file != null) {

            backgroundImageField.setText(
                    file.getAbsolutePath()
            );
        }
    }


    /**
     * 清除背景图片。
     */
    @FXML
    private void handleClearBackgroundImage() {

        backgroundImageField.clear();
    }


    /**
     * 恢复自定义主题默认值。
     */
    @FXML
    private void handleReset() {

        ThemeConfig config =
                new ThemeConfig();


        backgroundColorPicker.setValue(
                javafx.scene.paint.Color.web(
                        config.getBackgroundColor()
                )
        );


        panelColorPicker.setValue(
                javafx.scene.paint.Color.web(
                        config.getPanelColor()
                )
        );


        textColorPicker.setValue(
                javafx.scene.paint.Color.web(
                        config.getTextColor()
                )
        );


        secondaryTextColorPicker.setValue(
                javafx.scene.paint.Color.web(
                        config.getSecondaryTextColor()
                )
        );


        borderColorPicker.setValue(
                javafx.scene.paint.Color.web(
                        config.getBorderColor()
                )
        );


        accentColorPicker.setValue(
                javafx.scene.paint.Color.web(
                        config.getAccentColor()
                )
        );


        backgroundImageField.clear();


        statusLabel.setText(
                "已恢复自定义主题默认值"
        );
    }


    /**
     * 保存主题。
     */
    @FXML
    private void handleSave() {

        ThemeConfig config =
                themeManager.getConfig();


        ThemeType type =
                themeTypeComboBox.getValue();


        if (type == null) {

            type = ThemeType.AUTO;
        }


        config.setThemeType(
                type
        );


        config.setBackgroundColor(
                toHex(
                        backgroundColorPicker.getValue()
                )
        );


        config.setPanelColor(
                toHex(
                        panelColorPicker.getValue()
                )
        );


        config.setTextColor(
                toHex(
                        textColorPicker.getValue()
                )
        );


        config.setSecondaryTextColor(
                toHex(
                        secondaryTextColorPicker.getValue()
                )
        );


        config.setBorderColor(
                toHex(
                        borderColorPicker.getValue()
                )
        );


        config.setAccentColor(
                toHex(
                        accentColorPicker.getValue()
                )
        );


        config.setBackgroundImage(
                backgroundImageField.getText()
        );


        try {

            themeManager.saveAndApply();


            statusLabel.setText(
                    "主题已保存"
            );


            close();

        } catch (IOException exception) {

            statusLabel.setText(
                    "保存失败：" +
                            exception.getMessage()
            );
        }
    }


    /**
     * 取消。
     */
    @FXML
    private void handleCancel() {

        close();
    }


    private void updateStatus() {

        ThemeType type =
                themeTypeComboBox.getValue();


        if (type == null) {

            type = ThemeType.AUTO;
        }


        statusLabel.setText(
                "当前选择：" +
                        type.getDisplayName()
        );
    }


    private String toHex(
            javafx.scene.paint.Color color) {

        if (color == null) {

            return "#FFFFFF";
        }


        int red =
                (int) Math.round(
                        color.getRed() * 255
                );


        int green =
                (int) Math.round(
                        color.getGreen() * 255
                );


        int blue =
                (int) Math.round(
                        color.getBlue() * 255
                );


        return String.format(
                "#%02X%02X%02X",
                red,
                green,
                blue
        );
    }


    private Stage getStage() {

        return (Stage)
                backgroundImageField
                        .getScene()
                        .getWindow();
    }


    private void close() {

        Stage stage =
                getStage();

        stage.close();
    }
}