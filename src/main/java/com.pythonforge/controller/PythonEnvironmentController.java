package com.pythonforge.controller;

import com.pythonforge.model.PythonEnvironment;
import com.pythonforge.service.PythonEnvironmentService;
import com.pythonforge.util.LogUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Python 环境控制器。
 *
 * <p>
 * 负责 Python 环境列表的显示、
 * 检测、刷新以及当前环境选择。
 * </p>
 *
 * <p>
 * 当前版本：
 * PF-W Alpha.1.1.1-20260804
 * </p>
 */
public final class PythonEnvironmentController {

    private static final Logger LOGGER =
            LogUtils.getLogger(
                    PythonEnvironmentController.class
            );

    /**
     * Python 环境服务。
     */
    private final PythonEnvironmentService
            environmentService =
            new PythonEnvironmentService();

    /**
     * Python 环境列表。
     */
    @FXML
    private ListView<PythonEnvironment>
            environmentListView;

    /**
     * 当前 Python 路径。
     */
    @FXML
    private Label currentPythonLabel;

    /**
     * 当前 Python 版本。
     */
    @FXML
    private Label currentVersionLabel;

    /**
     * 当前 Python 架构。
     */
    @FXML
    private Label currentArchitectureLabel;

    /**
     * 当前 pip 状态。
     */
    @FXML
    private Label currentPipLabel;

    /**
     * 当前 PyInstaller 状态。
     */
    @FXML
    private Label currentPyInstallerLabel;

    /**
     * 刷新按钮。
     */
    @FXML
    private Button refreshButton;

    /**
     * 初始化。
     */
    @FXML
    private void initialize() {

        configureListView();

        detectEnvironments();
    }

    /**
     * 配置 Python 环境列表。
     */
    private void configureListView() {

        environmentListView
                .getSelectionModel()
                .setSelectionMode(
                        SelectionMode.SINGLE
                );

        /*
         * Python 环境显示格式。
         */
        environmentListView.setCellFactory(
                listView ->
                        new javafx.scene.control.ListCell<>() {

                            @Override
                            protected void updateItem(
                                    PythonEnvironment item,
                                    boolean empty) {

                                super.updateItem(
                                        item,
                                        empty
                                );

                                if (
                                        empty
                                                ||
                                                item == null
                                ) {

                                    setText(null);

                                    return;
                                }

                                setText(
                                        formatEnvironment(
                                                item
                                        )
                                );
                            }
                        }
        );

        /*
         * 监听选择变化。
         */
        environmentListView
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable,
                         oldValue,
                         newValue) -> {

                            if (newValue == null) {
                                return;
                            }

                            selectEnvironment(
                                    newValue
                            );
                        }
                );
    }

    /**
     * 检测 Python 环境。
     */
    @FXML
    private void detectEnvironments() {

        try {

            LogUtils.info(
                    LOGGER,
                    "Detecting Python environments..."
            );

            /*
             * 使用 Service 的统一 detect() API。
             */
            List<PythonEnvironment> environments =
                    environmentService.detect();

            /*
             * 使用 setAll()，
             * 避免 ObservableList.addAll()
             * 与 java.util.List.addAll()
             * 的重载歧义。
             */
            environmentListView
                    .getItems()
                    .setAll(
                            environments
                    );

            /*
             * 恢复当前 Python。
             */
            Optional<PythonEnvironment>
                    current =
                    environmentService
                            .getCurrentEnvironment();

            if (current.isPresent()) {

                selectListEnvironment(
                        current.get()
                );

                updateCurrentEnvironment(
                        current.get()
                );

            } else if (!environments.isEmpty()) {

                /*
                 * 第一次运行时默认选择
                 * 第一个检测到的 Python。
                 */
                PythonEnvironment first =
                        environments.get(0);

                environmentListView
                        .getSelectionModel()
                        .select(
                                first
                        );

            } else {

                clearCurrentEnvironment();
            }

            LogUtils.info(
                    LOGGER,
                    "Python environments loaded: "
                            + environments.size()
            );

        } catch (Exception e) {

            LogUtils.error(
                    LOGGER,
                    "Failed to detect Python environments: "
                            + e.getMessage()
            );

            environmentListView
                    .getItems()
                    .clear();

            clearCurrentEnvironment();
        }
    }

    /**
     * 选择 Python 环境。
     *
     * @param environment Python 环境
     */
    private void selectEnvironment(
            PythonEnvironment environment) {

        if (environment == null) {
            return;
        }

        try {

            environmentService
                    .setCurrentEnvironment(
                            environment
                    );

            updateCurrentEnvironment(
                    environment
            );

            LogUtils.info(
                    LOGGER,
                    "Current Python changed to: "
                            + environment
                            .getExecutable()
            );

        } catch (Exception e) {

            LogUtils.error(
                    LOGGER,
                    "Failed to select Python environment: "
                            + e.getMessage()
            );
        }
    }

    /**
     * 在 ListView 中选择指定环境。
     *
     * @param environment Python 环境
     */
    private void selectListEnvironment(
            PythonEnvironment environment) {

        if (environment == null) {
            return;
        }

        environmentListView
                .getSelectionModel()
                .select(
                        environment
                );
    }

    /**
     * 更新当前 Python 信息。
     *
     * @param environment Python 环境
     */
    private void updateCurrentEnvironment(
            PythonEnvironment environment) {

        if (environment == null) {

            clearCurrentEnvironment();

            return;
        }

        /*
         * Python 路径。
         */
        if (environment.getExecutable() != null) {

            currentPythonLabel.setText(
                    environment
                            .getExecutable()
                            .toString()
            );

        } else {

            currentPythonLabel.setText(
                    "-"
            );
        }

        /*
         * Python 版本。
         */
        currentVersionLabel.setText(
                safe(
                        environment.getVersion()
                )
        );

        /*
         * Python 架构。
         */
        if (
                environment.getArchitecture()
                        != null
        ) {

            currentArchitectureLabel.setText(
                    environment
                            .getArchitecture()
                            .toString()
            );

        } else {

            currentArchitectureLabel.setText(
                    "-"
            );
        }

        /*
         * pip。
         */
        if (
                environment.getPipExecutable()
                        != null
        ) {

            currentPipLabel.setText(
                    "已安装"
            );

        } else {

            currentPipLabel.setText(
                    "未安装"
            );
        }

        /*
         * PyInstaller。
         */
        if (
                environment.isPyInstallerAvailable()
        ) {

            String version =
                    environment
                            .getPyInstallerVersion();

            if (
                    version != null
                            &&
                            !version.isBlank()
            ) {

                currentPyInstallerLabel
                        .setText(
                                version
                        );

            } else {

                currentPyInstallerLabel
                        .setText(
                                "已安装"
                        );
            }

        } else {

            currentPyInstallerLabel
                    .setText(
                            "未安装"
                    );
        }
    }

    /**
     * 清空当前 Python 信息。
     */
    private void clearCurrentEnvironment() {

        currentPythonLabel.setText(
                "未选择 Python"
        );

        currentVersionLabel.setText(
                "-"
        );

        currentArchitectureLabel.setText(
                "-"
        );

        currentPipLabel.setText(
                "-"
        );

        currentPyInstallerLabel.setText(
                "-"
        );
    }

    /**
     * 格式化环境名称。
     *
     * @param environment Python 环境
     * @return 显示文本
     */
    private String formatEnvironment(
            PythonEnvironment environment) {

        if (environment == null) {

            return "未知 Python";
        }

        String version =
                safe(
                        environment.getVersion()
                );

        String architecture =
                environment.getArchitecture() != null
                        ? environment
                        .getArchitecture()
                        .toString()
                        : "UNKNOWN";

        String pyInstaller;

        if (
                environment
                        .isPyInstallerAvailable()
        ) {

            String versionValue =
                    environment
                            .getPyInstallerVersion();

            if (
                    versionValue != null
                            &&
                            !versionValue.isBlank()
            ) {

                pyInstaller =
                        "PyInstaller "
                                + versionValue;

            } else {

                pyInstaller =
                        "PyInstaller 已安装";
            }

        } else {

            pyInstaller =
                    "PyInstaller 未安装";
        }

        return "Python "
                + version
                + " ("
                + architecture
                + ") - "
                + pyInstaller;
    }

    /**
     * 防止 null。
     *
     * @param value 字符串
     * @return 安全字符串
     */
    private String safe(
            String value) {

        if (
                value == null
                        ||
                        value.isBlank()
        ) {

            return "-";
        }

        return value;
    }
}

