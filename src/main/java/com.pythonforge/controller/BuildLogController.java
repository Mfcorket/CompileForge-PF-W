package com.pythonforge.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 构建日志查看器控制器。
 */
public final class BuildLogController {

    @FXML
    private Label titleLabel;

    @FXML
    private TextArea logArea;

    /**
     * 当前日志文件。
     */
    private Path logFile;

    /**
     * 设置日志文件。
     *
     * @param logFile 日志文件
     */
    public void setLogFile(Path logFile) {

        this.logFile = logFile;

        loadLog();
    }

    /**
     * 加载日志。
     */
    private void loadLog() {

        if (logArea == null) {

            return;
        }

        if (logFile == null) {

            logArea.setText(
                    "日志文件为空。"
            );

            return;
        }

        if (!Files.isRegularFile(logFile)) {

            logArea.setText(
                    "日志文件不存在：\n"
                            + logFile
            );

            return;
        }

        try {

            String content =
                    Files.readString(
                            logFile,
                            StandardCharsets.UTF_8
                    );

            logArea.setText(
                    content
            );

            logArea.positionCaret(
                    logArea.getText().length()
            );

            if (titleLabel != null) {

                titleLabel.setText(
                        "构建日志 - "
                                + logFile.getFileName()
                );
            }

        } catch (IOException e) {

            logArea.setText(
                    "读取日志失败：\n"
                            + e.getMessage()
            );
        }
    }

    /**
     * 刷新日志。
     */
    @FXML
    private void handleRefresh() {

        loadLog();
    }

    /**
     * 复制全部日志。
     */
    @FXML
    private void handleCopy() {

        if (logArea == null) {

            return;
        }

        javafx.scene.input.ClipboardContent content =
                new javafx.scene.input.ClipboardContent();

        content.putString(
                logArea.getText()
        );

        javafx.scene.input.Clipboard
                .getSystemClipboard()
                .setContent(content);
    }

    /**
     * 关闭窗口。
     */
    @FXML
    private void handleClose() {

        if (logArea == null) {

            return;
        }

        javafx.stage.Window window =
                logArea.getScene()
                        .getWindow();

        if (window != null) {

            window.hide();
        }
    }

    /**
     * 显示错误。
     */
    private void showError(
            String title,
            String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}