package com.pythonforge.controller;

import com.pythonforge.model.PyInstallerInfo;
import com.pythonforge.service.PyInstallerService;
import com.pythonforge.util.LogUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * PyInstaller 控制器。
 *
 * <p>
 * 当前只负责显示当前 Python 环境的
 * PyInstaller 状态。
 * </p>
 */
public final class PyInstallerController {

    private static final Logger LOGGER =
            LogUtils.getLogger(
                    PyInstallerController.class
            );

    private final PyInstallerService
            pyInstallerService =
            new PyInstallerService();

    @FXML
    private Label statusLabel;

    @FXML
    private Label versionLabel;

    /**
     * 初始化。
     */
    @FXML
    private void initialize() {

        detectPyInstaller();
    }

    /**
     * 检测 PyInstaller。
     */
    @FXML
    private void detectPyInstaller() {

        Optional<PyInstallerInfo>
                result =
                pyInstallerService.detect();

        if (result.isEmpty()) {

            setUnavailable(
                    "未选择 Python"
            );

            return;
        }

        PyInstallerInfo info =
                result.get();

        if (!info.isInstalled()) {

            setUnavailable(
                    "PyInstaller 未安装"
            );

            return;
        }

        if (!info.isExecutable()) {

            setUnavailable(
                    "PyInstaller 无法执行"
            );

            return;
        }

        statusLabel.setText(
                "可用"
        );

        String version =
                info.getVersion();

        versionLabel.setText(
                version == null
                        || version.isBlank()
                        ? "-"
                        : version
        );

        LogUtils.info(
                LOGGER,
                "PyInstaller is ready: "
                        + version
        );
    }

    /**
     * 设置不可用状态。
     *
     * @param message 信息
     */
    private void setUnavailable(
            String message) {

        statusLabel.setText(
                message
        );

        versionLabel.setText(
                "-"
        );
    }
}