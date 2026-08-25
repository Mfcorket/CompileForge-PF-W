package com.pythonforge.controller;

import com.pythonforge.build.BuildResult;
import com.pythonforge.context.ApplicationContext;
import com.pythonforge.history.BuildHistoryRecord;
import com.pythonforge.history.BuildHistoryService;
import com.pythonforge.model.PyInstallerBuildConfig;
import com.pythonforge.service.*;
import com.pythonforge.model.PythonEnvironment;
import com.pythonforge.util.LogUtils;
import com.pythonforge.build.PyInstallerBuildEngine;
import com.pythonforge.build.BuildTask;
//import com.pythonforge.model.BuildResult;
import com.pythonforge.build.BuildResult;

import javafx.concurrent.Worker;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.stage.*;
import javafx.collections.ObservableList;
import javafx.stage.Modality;
import javafx.stage.Stage;


import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * PythonForge 主界面控制器。
 *
 * <p>
 * P2.2：
 * Python 环境、入口文件、PyInstaller 基础配置、
 * 命令预览。
 * </p>
 */
public final class MainController {

    private static final Logger LOGGER =
            LogUtils.getLogger(MainController.class);

    // ============================================================
    // Python 环境
    // ============================================================

    @FXML
    private ComboBox<PythonEnvironment>
            pythonEnvironmentComboBox;

    @FXML
    private Label pythonEnvironmentStatusLabel;

    // ============================================================
    // Python 项目
    // ============================================================

    @FXML
    private TextField entryFileField;

    // ============================================================
    // 构建配置
    // ============================================================

    @FXML
    private TextField applicationNameField;

    @FXML
    private RadioButton oneFileRadioButton;

    @FXML
    private RadioButton oneDirRadioButton;

    @FXML
    private RadioButton consoleRadioButton;

    @FXML
    private RadioButton windowedRadioButton;

    @FXML
    private TextField outputDirectoryField;

    @FXML
    private TextField iconField;

    @FXML
    private CheckBox cleanCheckBox;

    @FXML
    private CheckBox noconfirmCheckBox;

    @FXML
    private CheckBox noUpxCheckBox;

    @FXML
    private Label advancedConfigStatusLabel;

    @FXML
    private TextArea buildLogArea;


    private final PythonDetector pythonDetector =
            new PythonDetector();

    private final FileOpenService fileOpenService =
            new FileOpenService();


    // ============================================================
    // 命令预览
    // ============================================================

    @FXML
    private TextArea commandPreviewArea;

    @FXML
    private Button buildButton;

    @FXML
    private Button openBuildLogButton;

    @FXML
    private Button cancelBuildButton;

    @FXML
    private Label buildStatusLabel;

    @FXML
    private Label buildResultLabel;


    @FXML
    private Button openExeButton;


    @FXML
    private Button openOutputButton;

    @FXML
    private ListView<String> dataFilesListView;

    @FXML
    private ListView<String> binariesListView;


    @FXML
    private Button copyExePathButton;

    @FXML
    private ListView<Path> additionalDirectoriesListView;



    private Path lastOutputFile;

    private Path lastBuildLogFile;


    private Path lastOutputDirectory;



    private BuildTask currentBuildTask;

    @FXML
    private BorderPane mainRoot;

    @FXML
    private BorderPane mainWorkspace;

    // ============================================================
    // Service
    // ============================================================

    private final PythonEnvironmentService
            environmentService =
            new PythonEnvironmentService();

    private final PyInstallerCommandBuilder
            commandBuilder =
            new PyInstallerCommandBuilder();

    private final PyInstallerConfigValidator
            configValidator =
            new PyInstallerConfigValidator();

    private final BuildResultDetector
            buildResultDetector =
            new BuildResultDetector();

    private PyInstallerBuildConfig
            currentConfig;

    //private BuildTask currentBuildTask;


    private final PyInstallerBuildEngine buildEngine =
            new PyInstallerBuildEngine();

    private final BuildHistoryService historyService =
            ApplicationContext
                    .getBuildHistoryService();

    private final ObservableList<Path>
            additionalDirectories =
            FXCollections.observableArrayList();



    /**
     * 当前构建开始时间。
     */
    private Instant buildStartTime;


    /**
    private final PyInstallerBuildEngine buildEngine =
            new PyInstallerBuildEngine();
     */

    //private BuildTask currentBuildTask;



    // ============================================================
    // 初始化
    // ============================================================

    @FXML
    private void initialize() {

        initializeBuildConfig();

        detectPython();

        setupListeners();

        updateCommandPreview();

        dataFilesListView.setItems(
                javafx.collections.FXCollections.observableArrayList()
        );

        binariesListView.setItems(
                javafx.collections.FXCollections.observableArrayList()
        );

        additionalDirectoriesListView.setItems(
                additionalDirectories
        );
    }

    /**
     * 初始化默认构建配置。
     */
    private void initializeBuildConfig() {

        currentConfig =
                PyInstallerBuildConfig.defaults();

        oneFileRadioButton.setSelected(
                true
        );

        consoleRadioButton.setSelected(
                true
        );

        cleanCheckBox.setSelected(
                true
        );

        noconfirmCheckBox.setSelected(
                true
        );

        noUpxCheckBox.setSelected(
                false
        );

        advancedConfigStatusLabel.setText(
                "未配置高级参数"
        );

        buildButton.setDisable(
                true
        );

        cancelBuildButton.setDisable(true);

        buildStatusLabel.setText(
                "状态：空闲"
        );

        openExeButton.setDisable(true);

        openOutputButton.setDisable(true);

        copyExePathButton.setDisable(true);


        buildResultLabel.setText(
                "暂无构建结果"
        );
    }

    /**
     * 建立 UI 监听。
     */
    private void setupListeners() {

        pythonEnvironmentComboBox
                .valueProperty()
                .addListener(
                        (observable,
                         oldValue,
                         newValue) -> {

                            updatePythonEnvironmentStatus();

                            updateCommandPreview();
                        }
                );

        applicationNameField
                .textProperty()
                .addListener(
                        (observable,
                         oldValue,
                         newValue) ->
                                updateCommandPreview()
                );

        entryFileField
                .textProperty()
                .addListener(
                        (observable,
                         oldValue,
                         newValue) ->
                                updateCommandPreview()
                );

        outputDirectoryField
                .textProperty()
                .addListener(
                        (observable,
                         oldValue,
                         newValue) ->
                                updateCommandPreview()
                );

        iconField
                .textProperty()
                .addListener(
                        (observable,
                         oldValue,
                         newValue) ->
                                updateCommandPreview()
                );

        cleanCheckBox
                .selectedProperty()
                .addListener(
                        (observable,
                         oldValue,
                         newValue) ->
                                updateCommandPreview()
                );

        noconfirmCheckBox
                .selectedProperty()
                .addListener(
                        (observable,
                         oldValue,
                         newValue) ->
                                updateCommandPreview()
                );

        noUpxCheckBox
                .selectedProperty()
                .addListener(
                        (observable,
                         oldValue,
                         newValue) ->
                                updateCommandPreview()
                );

        oneFileRadioButton
                .selectedProperty()
                .addListener(
                        (observable,
                         oldValue,
                         newValue) ->
                                updateCommandPreview()
                );

        oneDirRadioButton
                .selectedProperty()
                .addListener(
                        (observable,
                         oldValue,
                         newValue) ->
                                updateCommandPreview()
                );

        consoleRadioButton
                .selectedProperty()
                .addListener(
                        (observable,
                         oldValue,
                         newValue) ->
                                updateCommandPreview()
                );

        windowedRadioButton
                .selectedProperty()
                .addListener(
                        (observable,
                         oldValue,
                         newValue) ->
                                updateCommandPreview()
                );
    }

    // ============================================================
    // Python 检测
    // ============================================================

    private void detectPython() {

        try {

            List<PythonEnvironment> environments =
                    pythonDetector.detect();

            pythonEnvironmentComboBox.setItems(
                    FXCollections.observableArrayList(
                            environments
                    )
            );

            if (environments.isEmpty()) {

                pythonEnvironmentStatusLabel.setText(
                        "未检测到可用 Python 环境"
                );

                buildButton.setDisable(true);

                return;
            }

            /*
             * 优先选择已经安装 PyInstaller 的 Python。
             */
            PythonEnvironment selected =
                    environments.stream()
                            .filter(
                                    PythonEnvironment::isPyInstallerAvailable
                            )
                            .findFirst()
                            .orElse(
                                    environments.get(0)
                            );

            pythonEnvironmentComboBox
                    .getSelectionModel()
                    .select(selected);

            updatePythonEnvironmentStatus();

            LogUtils.info(
                    LOGGER,
                    "Total Python environments detected: "
                            + environments.size()
            );

        } catch (Exception e) {

            LogUtils.error(
                    LOGGER,
                    "Failed to detect Python environments: "
                            + e.getMessage()
            );

            pythonEnvironmentStatusLabel.setText(
                    "Python 环境检测失败："
                            + e.getMessage()
            );

            buildButton.setDisable(true);
        }
    }


    @FXML
    private void handleRefreshPython() {

        detectPython();
    }

    /**
     * 更新 Python 环境信息。
     */
    private void updatePythonEnvironmentStatus() {

        PythonEnvironment environment =
                pythonEnvironmentComboBox
                        .getValue();

        if (environment == null) {

            pythonEnvironmentStatusLabel
                    .setText(
                            "未选择 Python 环境"
                    );

            buildButton.setDisable(
                    true
            );

            return;
        }

        StringBuilder text =
                new StringBuilder();

        text.append(
                "Python "
        );

        text.append(
                environment.getVersion()
        );

        text.append(
                " | "
        );

        text.append(
                environment.getArchitecture()
        );

        text.append(
                " | "
        );

        text.append(
                environment.isPyInstallerAvailable()
                        ? "PyInstaller "
                          + environment
                        .getPyInstallerVersion()
                        : "PyInstaller 未安装"
        );

        pythonEnvironmentStatusLabel
                .setText(
                        text.toString()
                );

        buildButton.setDisable(
                !environment
                        .isPyInstallerAvailable()
        );
    }

    // ============================================================
    // 文件选择
    // ============================================================

    @FXML
    private void handleSelectEntryFile() {

        FileChooser chooser =
                new FileChooser();

        chooser.setTitle(
                "选择 Python 入口文件"
        );

        chooser.getExtensionFilters()
                .addAll(
                        new FileChooser.ExtensionFilter(
                                "Python 文件",
                                "*.py",
                                "*.pyc"
                        ),
                        new FileChooser.ExtensionFilter(
                                "所有文件",
                                "*.*"
                        )
                );

        Stage stage =
                getStage();

        File file =
                chooser.showOpenDialog(
                        stage
                );

        if (file == null) {
            return;
        }

        Path path =
                file.toPath();

        entryFileField.setText(
                path.toAbsolutePath()
                        .toString()
        );

        String fileName =
                file.getName();

        int index =
                fileName.lastIndexOf('.');

        if (index > 0) {

            fileName =
                    fileName.substring(
                            0,
                            index
                    );
        }

        applicationNameField.setText(
                fileName
        );

        if (
                outputDirectoryField
                        .getText()
                        .isBlank()
        ) {

            Path parent =
                    path.getParent();

            if (parent != null) {

                outputDirectoryField
                        .setText(
                                parent
                                        .resolve("dist")
                                        .toString()
                        );
            }
        }

        updateCommandPreview();
    }

    @FXML
    private void handleSelectOutputDirectory() {

        DirectoryChooser chooser =
                new DirectoryChooser();

        chooser.setTitle(
                "选择 EXE 输出目录"
        );

        Stage stage =
                getStage();

        File directory =
                chooser.showDialog(
                        stage
                );

        if (directory == null) {
            return;
        }

        outputDirectoryField.setText(
                directory
                        .toPath()
                        .toAbsolutePath()
                        .toString()
        );
    }

    @FXML
    private void handleSelectIcon() {

        FileChooser chooser =
                new FileChooser();

        chooser.setTitle(
                "选择 Windows 图标"
        );

        chooser.getExtensionFilters()
                .add(
                        new FileChooser.ExtensionFilter(
                                "ICO 图标",
                                "*.ico"
                        )
                );

        File file =
                chooser.showOpenDialog(
                        getStage()
                );

        if (file == null) {
            return;
        }

        iconField.setText(
                file.toPath()
                        .toAbsolutePath()
                        .toString()
        );

        updateCommandPreview();
    }

    // ============================================================
    // 配置生成
    // ============================================================

    @FXML
    private void handleGenerateConfig() {

        try {

            currentConfig =
                    buildConfigFromUI();

            List<String> errors =
                    configValidator.validate(
                            currentConfig
                    );

            if (!errors.isEmpty()) {

                showValidationErrors(
                        errors
                );

                buildButton.setDisable(
                        true
                );

                return;
            }

            updateCommandPreview();

            buildButton.setDisable(
                    pythonEnvironmentComboBox
                            .getValue() == null
                            ||
                            !pythonEnvironmentComboBox
                                    .getValue()
                                    .isPyInstallerAvailable()
            );

            showInfo(
                    "构建配置",
                    "PyInstaller 构建配置生成成功。"
            );

        } catch (Exception e) {

            LogUtils.error(
                    LOGGER,
                    "Failed to generate build config: "
                            + e.getMessage()
            );

            showError(
                    "构建配置生成失败",
                    e.getMessage()
            );
        }
    }

    /**
     * 从 UI 创建 BuildConfig。
     */
    private PyInstallerBuildConfig
    buildConfigFromUI() {

        PyInstallerBuildConfig config;

        if (currentConfig != null) {

            config = currentConfig;

        } else {

            config =
                    PyInstallerBuildConfig.defaults();

            currentConfig = config;
        }

        String entry =
                entryFileField.getText();

        if (
                entry != null
                        &&
                        !entry.isBlank()
        ) {

            config.setEntryFile(
                    Path.of(entry)
            );
        }

        String name =
                applicationNameField.getText();

        if (
                name != null
                        &&
                        !name.isBlank()
        ) {

            config.setName(
                    name.trim()
            );
        }

        if (
                oneFileRadioButton.isSelected()
        ) {

            config.setMode(
                    PyInstallerBuildConfig.Mode.ONEFILE
            );

        } else {

            config.setMode(
                    PyInstallerBuildConfig.Mode.ONEDIR
            );
        }

        if (
                consoleRadioButton.isSelected()
        ) {

            config.setConsoleMode(
                    PyInstallerBuildConfig.ConsoleMode.CONSOLE
            );

        } else {

            config.setConsoleMode(
                    PyInstallerBuildConfig.ConsoleMode.WINDOWED
            );
        }

        String output =
                outputDirectoryField.getText();

        if (
                output != null
                        &&
                        !output.isBlank()
        ) {

            config.setOutputDirectory(
                    Path.of(output)
            );
        }

        if (
                entry != null
                        &&
                        !entry.isBlank()
        ) {

            Path entryPath =
                    Path.of(entry);

            Path parent =
                    entryPath.getParent();

            if (parent != null) {

                config.setWorkDirectory(
                        parent.resolve("build")
                );

                config.setSpecDirectory(
                        parent.resolve("spec")
                );
            }
        }

        String icon =
                iconField.getText();

        if (
                icon != null
                        &&
                        !icon.isBlank()
        ) {

            config.setIcon(
                    Path.of(icon)
            );
        }

        config.setClean(
                cleanCheckBox.isSelected()
        );

        config.setNoconfirm(
                noconfirmCheckBox.isSelected()
        );

        config.setNoUpx(
                noUpxCheckBox.isSelected()
        );

        config.setDataFiles(
                dataFilesListView
                        .getItems()
        );

        config.setBinaries(
                binariesListView
                        .getItems()
        );

        return config;
    }

    // ============================================================
    // 命令预览
    // ============================================================

    private void updateCommandPreview() {

        PythonEnvironment environment =
                pythonEnvironmentComboBox
                        .getValue();

        if (environment == null) {

            commandPreviewArea.setText(
                    "请选择 Python 环境。"
            );

            return;
        }

        try {

            PyInstallerBuildConfig config =
                    buildConfigFromUI();

            List<String> command =
                    commandBuilder.build(
                            environment.getExecutable(),
                            config
                    );

            commandPreviewArea.setText(
                    formatCommand(command)
            );

        } catch (Exception e) {

            commandPreviewArea.setText(
                    "无法生成命令预览："
                            + e.getMessage()
            );
        }
    }

    /**
     * 将命令参数转换成 Windows 命令行预览。
     */
    private String formatCommand(
            List<String> command) {

        StringBuilder result =
                new StringBuilder();

        for (String argument : command) {

            if (result.length() > 0) {
                result.append(' ');
            }

            if (
                    argument.contains(" ")
                            ||
                            argument.contains("\t")
            ) {

                result.append('"');
                result.append(
                        argument
                );
                result.append('"');

            } else {

                result.append(
                        argument
                );
            }
        }

        return result.toString();
    }

    // ============================================================
    // 高级配置
    // ============================================================

    @FXML
    private void handleAdvancedConfig() {

        try {

            /*
             * 确保当前配置存在。
             */
            if (currentConfig == null) {

                currentConfig =
                        buildConfigFromUI();
            }

            javafx.fxml.FXMLLoader loader =
                    new javafx.fxml.FXMLLoader(
                            getClass()
                                    .getResource(
                                            "/fxml/AdvancedConfigView.fxml"
                                    )
                    );

            javafx.scene.Parent root =
                    loader.load();

            AdvancedConfigController controller =
                    loader.getController();

            /*
             * 将当前 BuildConfig 交给高级配置窗口。
             */
            controller.setConfig(
                    currentConfig
            );

            javafx.stage.Stage stage =
                    new javafx.stage.Stage();

            stage.setTitle(
                    "PythonForge-win - 高级配置"
            );

            stage.initOwner(
                    getStage()
            );

            stage.initModality(
                    javafx.stage.Modality.APPLICATION_MODAL
            );

            stage.setScene(
                    new javafx.scene.Scene(
                            root,
                            760,
                            620
                    )
            );

            stage.showAndWait();

            /*
             * 只有点击确定后才更新状态。
             */
            if (controller.isConfirmed()) {

                currentConfig =
                        controller.getConfig();

                advancedConfigStatusLabel.setText(
                        "高级参数已配置"
                );

                updateCommandPreview();

            } else {

                advancedConfigStatusLabel.setText(
                        "高级参数未修改"
                );
            }

        } catch (Exception e) {

            LogUtils.error(
                    LOGGER,
                    "Failed to open advanced configuration: "
                            + e.getMessage()
            );

            showError(
                    "高级配置",
                    "无法打开高级配置窗口："
                            + e.getMessage()
            );
        }
    }

    @FXML
    private void handleAddDataFile() {

        javafx.stage.FileChooser chooser =
                new javafx.stage.FileChooser();

        chooser.setTitle(
                "选择数据文件"
        );

        java.io.File file =
                chooser.showOpenDialog(
                        dataFilesListView
                                .getScene()
                                .getWindow()
                );

        if (file == null) {

            return;
        }

        javafx.scene.control.TextInputDialog dialog =
                new javafx.scene.control.TextInputDialog(
                        "data"
                );

        dialog.setTitle(
                "数据文件目标目录"
        );

        dialog.setHeaderText(
                "设置文件在程序中的目标目录"
        );

        dialog.setContentText(
                "目标目录："
        );

        java.util.Optional<String> result =
                dialog.showAndWait();

        if (
                result.isEmpty()
                        ||
                        result.get().isBlank()
        ) {

            return;
        }

        String value =
                file.getAbsolutePath()
                        + ";"
                        + result.get().trim();

        dataFilesListView
                .getItems()
                .add(value);
    }

    @FXML
    private void handleAddBinary() {

        javafx.stage.FileChooser chooser =
                new javafx.stage.FileChooser();

        chooser.setTitle(
                "选择二进制文件"
        );

        java.io.File file =
                chooser.showOpenDialog(
                        binariesListView
                                .getScene()
                                .getWindow()
                );

        if (file == null) {

            return;
        }

        javafx.scene.control.TextInputDialog dialog =
                new javafx.scene.control.TextInputDialog(
                        "bin"
                );

        dialog.setTitle(
                "二进制文件目标目录"
        );

        dialog.setHeaderText(
                "设置文件在程序中的目标目录"
        );

        dialog.setContentText(
                "目标目录："
        );

        java.util.Optional<String> result =
                dialog.showAndWait();

        if (
                result.isEmpty()
                        ||
                        result.get().isBlank()
        ) {

            return;
        }

        String value =
                file.getAbsolutePath()
                        + ";"
                        + result.get().trim();

        binariesListView
                .getItems()
                .add(value);
    }

    private String buildAdvancedConfigStatus() {

        if (currentConfig == null) {

            return "未配置高级参数";
        }

        int hiddenImports =
                currentConfig
                        .getHiddenImports()
                        .size();

        int dataFiles =
                currentConfig
                        .getDataFiles()
                        .size();

        int binaries =
                currentConfig
                        .getBinaries()
                        .size();

        int paths =
                currentConfig
                        .getPaths()
                        .size();

        int excludes =
                currentConfig
                        .getExcludes()
                        .size();

        int collectSubmodules =
                currentConfig
                        .getCollectSubmodules()
                        .size();

        int collectData =
                currentConfig
                        .getCollectData()
                        .size();

        int collectBinaries =
                currentConfig
                        .getCollectBinaries()
                        .size();

        int collectAll =
                currentConfig
                        .getCollectAll()
                        .size();

        int total =
                hiddenImports
                        + dataFiles
                        + binaries
                        + paths
                        + excludes
                        + collectSubmodules
                        + collectData
                        + collectBinaries
                        + collectAll;

        if (total == 0) {

            return "未配置高级参数";
        }

        return "高级参数已配置："
                + total
                + " 项";
    }



    // ============================================================
    // 构建
    // ============================================================
    @FXML
    private void handleBuild() {

        PythonEnvironment environment =
                pythonEnvironmentComboBox.getValue();

        if (environment == null) {

            showError(
                    "构建失败",
                    "没有选择 Python 环境。"
            );

            return;
        }

        if (!environment.isPyInstallerAvailable()) {

            showError(
                    "构建失败",
                    "当前 Python 环境没有安装 PyInstaller。"
            );

            return;
        }

        /*
         * 生成构建配置。
         */
        try {

            /*
             * 如果高级配置还没有生成，
             * 则从 UI 创建基础配置。
             */
            if (currentConfig == null) {

                currentConfig =
                        buildConfigFromUI();

            } else {

                /*
                 * 更新基础 UI 参数，
                 * 但保留高级配置。
                 */
                updateBasicConfigFromUI(
                        currentConfig
                );
            }

            List<String> errors =
                    configValidator.validate(
                            currentConfig
                    );

            if (!errors.isEmpty()) {

                showValidationErrors(
                        errors
                );

                return;
            }

        } catch (Exception e) {

            LogUtils.error(
                    LOGGER,
                    "Failed to prepare build: "
                            + e.getMessage()
            );

            showError(
                    "构建失败",
                    e.getMessage()
            );

            return;
        }

        /*
         * 清空旧日志。
         */
        clearLastBuildResult();

        buildLogArea.clear();

        buildLogArea.appendText(
                "===== PYTHONFORGE BUILD =====\n"
        );

        buildLogArea.appendText(
                "Python: "
                        + environment.getExecutable()
                        + "\n"
        );

        buildLogArea.appendText(
                "Version: "
                        + environment.getVersion()
                        + "\n"
        );

        buildLogArea.appendText(
                "PyInstaller: "
                        + environment.getPyInstallerVersion()
                        + "\n\n"
        );

        /*
         * 更新 UI。
         */
        buildButton.setDisable(true);

        cancelBuildButton.setDisable(false);

        buildStatusLabel.setText(
                "状态：正在构建..."
        );

        /*
         * 创建后台任务。
         */
        BuildTask task =
                new BuildTask(
                        environment,
                        buildEngine,
                        currentConfig
                );

        currentBuildTask = task;

        /*
         * 实时日志。
         */
        task.messageProperty()
                .addListener(
                        (observable,
                         oldValue,
                         newValue) -> {

                            if (newValue == null
                                    || newValue.isBlank()) {

                                return;
                            }

                            javafx.application.Platform
                                    .runLater(
                                            () -> {

                                                buildLogArea
                                                        .appendText(
                                                                newValue
                                                                        + "\n"
                                                        );

                                                buildLogArea
                                                        .positionCaret(
                                                                buildLogArea
                                                                        .getText()
                                                                        .length()
                                                        );
                                            }
                                    );
                        }
                );

        /*
         * 构建成功。
         */
        task.setOnSucceeded(
                event -> {

                    BuildResult result =
                            task.getValue();

                    finishBuild(
                            result
                    );
                }
        );

        /*
         * Task 本身发生异常。
         */
        task.setOnFailed(
                event -> {

                    Throwable exception =
                            task.getException();

                    String message =
                            exception == null
                                    ? "未知异常"
                                    : exception.getMessage();

                    buildStatusLabel.setText(
                            "状态：构建失败"
                    );

                    buildLogArea.appendText(
                            "\n===== BUILD FAILED =====\n"
                    );

                    buildLogArea.appendText(
                            message
                                    + "\n"
                    );

                    buildButton.setDisable(false);

                    cancelBuildButton.setDisable(true);

                    currentBuildTask = null;
                }
        );

        /*
         * 用户取消。
         */
        task.setOnCancelled(
                event -> {

                    buildStatusLabel.setText(
                            "状态：已取消"
                    );

                    buildLogArea.appendText(
                            "\n===== BUILD CANCELLED =====\n"
                    );

                    buildButton.setDisable(false);

                    cancelBuildButton.setDisable(true);

                    currentBuildTask = null;
                }
        );

        /*
         * 创建后台线程。
         */
        Thread thread =
                new Thread(
                        task,
                        "PythonForge-PyInstaller-Build"
                );

        thread.setDaemon(true);

        thread.start();
    }

    // ============================================================
    // UI 工具
    // ============================================================

    private Stage getStage() {

        return (Stage)
                entryFileField
                        .getScene()
                        .getWindow();
    }

    private void showError(
            String title,
            String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle(
                title
        );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                message == null
                        ? "未知错误"
                        : message
        );

        alert.showAndWait();
    }

    private void showInfo(
            String title,
            String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(
                title
        );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                message
        );

        alert.showAndWait();
    }

    private void showValidationErrors(
            List<String> errors) {

        StringBuilder message =
                new StringBuilder();

        for (String error : errors) {

            if (message.length() > 0) {
                message.append('\n');
            }

            message.append(
                    "• "
            );

            message.append(
                    error
            );
        }

        showError(
                "构建配置无效",
                message.toString()
        );
    }


    /**
     * 完成构建后的统一处理。
     *
     * @param result PyInstaller 构建结果
     */
    private void finishBuild(
            BuildResult result) {

        /*
         * ============================================================
         * 1. 防止构建结果为空
         * ============================================================
         */
        if (result == null) {

            buildStatusLabel.setText(
                    "状态：构建失败"
            );

            buildLogArea.appendText(
                    "\n===== BUILD FAILED =====\n"
            );

            buildLogArea.appendText(
                    "构建结果为空。\n"
            );

            buildResultLabel.setText(
                    "构建失败"
            );

            lastOutputFile = null;

            lastOutputDirectory = null;

            openExeButton.setDisable(true);

            openOutputButton.setDisable(true);

            copyExePathButton.setDisable(true);

            buildButton.setDisable(false);

            cancelBuildButton.setDisable(true);

            currentBuildTask = null;

            return;
        }


        /*
         * ============================================================
         * 2. 根据构建状态处理结果
         * ============================================================
         */
        switch (result.getStatus()) {

            /*
             * ========================================================
             * 构建成功
             * ========================================================
             */
            case SUCCESS -> {

                buildStatusLabel.setText(
                        "状态：构建成功"
                );

                buildLogArea.appendText(
                        "\n===== BUILD RESULT =====\n"
                );

                /*
                 * EXE 文件。
                 */
                if (result.getOutputFile() != null) {

                    buildLogArea.appendText(
                            "EXE 文件："
                                    + result.getOutputFile()
                                    + "\n"
                    );
                }

                /*
                 * 输出目录。
                 */
                if (result.getOutputDirectory() != null) {

                    buildLogArea.appendText(
                            "输出目录："
                                    + result.getOutputDirectory()
                                    + "\n"
                    );
                }

                /*
                 * 日志文件。
                 */
                if (result.getLogFile() != null) {

                    buildLogArea.appendText(
                            "构建日志："
                                    + result.getLogFile()
                                    + "\n"
                    );
                }

                /*
                 * 构建消息。
                 */
                buildLogArea.appendText(
                        "消息："
                                + (
                                result.getMessage() == null
                                        ? ""
                                        : result.getMessage()
                        )
                                + "\n"
                );


                /*
                 * 保存最后一次构建结果。
                 */
                lastOutputFile =
                        result.getOutputFile();

                lastOutputDirectory =
                        result.getOutputDirectory();


                /*
                 * 更新构建结果标签。
                 */
                if (lastOutputFile != null) {

                    buildResultLabel.setText(
                            "构建成功："
                                    + lastOutputFile
                    );

                } else {

                    buildResultLabel.setText(
                            "构建成功"
                    );
                }


                /*
                 * 更新操作按钮状态。
                 */
                openExeButton.setDisable(
                        lastOutputFile == null
                );

                openOutputButton.setDisable(
                        lastOutputDirectory == null
                );

                copyExePathButton.setDisable(
                        lastOutputFile == null
                );


                /*
                 * 给用户提示。
                 */
                buildLogArea.appendText(
                        "可执行文件已生成。\n"
                );

                if (lastOutputFile != null) {

                    buildLogArea.appendText(
                            "可以点击【打开 EXE】运行。\n"
                    );
                }

                lastBuildLogFile =
                        result.getLogFile();

                openBuildLogButton.setDisable(
                        lastBuildLogFile == null
                                ||
                                !java.nio.file.Files.isRegularFile(
                                        lastBuildLogFile
                                )
                );
            }


            /*
             * ========================================================
             * 构建失败
             * ========================================================
             */
            case FAILED -> {

                buildStatusLabel.setText(
                        "状态：构建失败"
                );

                buildLogArea.appendText(
                        "\n===== BUILD FAILED =====\n"
                );

                String message =
                        result.getMessage();

                if (message == null
                        || message.isBlank()) {

                    message =
                            "未知错误";
                }

                buildLogArea.appendText(
                        message
                                + "\n"
                );


                /*
                 * 清除上一次构建成功留下的结果。
                 */
                lastOutputFile = null;

                lastOutputDirectory = null;


                buildResultLabel.setText(
                        "构建失败"
                );

                clearLastBuildResult();


                /*
                 * 禁用结果操作按钮。
                 */
                openExeButton.setDisable(true);

                openOutputButton.setDisable(true);

                copyExePathButton.setDisable(true);


                /*
                 * 如果有日志文件，则显示日志位置。
                 */
                if (result.getLogFile() != null) {

                    buildLogArea.appendText(
                            "构建日志："
                                    + result.getLogFile()
                                    + "\n"
                    );
                }

                lastBuildLogFile = null;

                openBuildLogButton.setDisable(true);
            }


            /*
             * ========================================================
             * 构建取消
             * ========================================================
             */
            case CANCELLED -> {

                buildStatusLabel.setText(
                        "状态：已取消"
                );

                buildLogArea.appendText(
                        "\n===== BUILD CANCELLED =====\n"
                );

                buildLogArea.appendText(
                        "构建已被用户取消。\n"
                );


                buildResultLabel.setText(
                        "构建已取消"
                );

                clearLastBuildResult();


                /*
                 * 取消后不保留旧的 EXE 操作状态。
                 */
                lastOutputFile = null;

                lastOutputDirectory = null;

                openExeButton.setDisable(true);

                openOutputButton.setDisable(true);

                copyExePathButton.setDisable(true);


                /*
                 * 如果存在日志文件，显示日志位置。
                 */
                if (result.getLogFile() != null) {

                    buildLogArea.appendText(
                            "构建日志："
                                    + result.getLogFile()
                                    + "\n"
                    );
                }

                lastBuildLogFile = null;

                openBuildLogButton.setDisable(true);
            }


            /*
             * ========================================================
             * 未知状态
             * ========================================================
             */
            default -> {

                buildStatusLabel.setText(
                        "状态：未知"
                );

                buildLogArea.appendText(
                        "\n===== UNKNOWN BUILD STATUS =====\n"
                );

                buildLogArea.appendText(
                        "未知构建状态。\n"
                );

                buildResultLabel.setText(
                        "未知构建状态"
                );

                lastOutputFile = null;

                lastOutputDirectory = null;

                openExeButton.setDisable(true);

                openOutputButton.setDisable(true);

                copyExePathButton.setDisable(true);
            }


        }


        /*
         * ============================================================
         * 3. 保存构建历史
         *
         * 只保存一次。
         *
         * saveBuildHistory() 内部负责：
         *
         * BuildResult
         *      ↓
         * BuildHistoryRecord
         *      ↓
         * BuildHistoryService
         * ============================================================
         */
        saveBuildHistory(
                result
        );


        /*
         * ============================================================
         * 4. 恢复构建按钮状态
         * ============================================================
         */
        buildButton.setDisable(false);

        cancelBuildButton.setDisable(true);


        /*
         * ============================================================
         * 5. 清理当前后台任务
         * ============================================================
         */
        currentBuildTask = null;
    }



    /**
     * 保存构建历史。
     *
     * @param result 构建结果
     */
    private void saveBuildHistory(
            BuildResult result) {

        if (result == null) {

            return;
        }

        PythonEnvironment environment =
                pythonEnvironmentComboBox.getValue();

        if (environment == null) {

            return;
        }

        if (currentConfig == null) {

            return;
        }

        BuildHistoryRecord record =
                new BuildHistoryRecord(

                        java.time.LocalDateTime.now(),

                        result.getStatus(),

                        applicationNameField.getText(),

                        currentConfig.getEntryFile(),

                        result.getOutputFile(),

                        result.getOutputDirectory(),

                        environment.getVersion(),

                        result.getLogFile(),

                        result.getMessage()
                );

        historyService.add(
                record
        );
    }



    @FXML
    private void handleCancelBuild() {

        if (currentBuildTask == null) {

            return;
        }

        buildStatusLabel.setText(
                "状态：正在取消..."
        );

        cancelBuildButton.setDisable(true);

        buildLogArea.appendText(
                "\n正在取消 PyInstaller...\n"
        );

        currentBuildTask.cancelBuildProcess();
    }

    /**
     * 根据主界面 UI 更新基础构建参数。
     *
     * <p>
     * 不修改高级 PyInstaller 参数。
     * </p>
     */
    private void updateBasicConfigFromUI(
            PyInstallerBuildConfig config) {

        if (config == null) {
            return;
        }

        String entry =
                entryFileField.getText();

        if (entry != null
                && !entry.isBlank()) {

            config.setEntryFile(
                    Path.of(entry)
            );
        }

        String name =
                applicationNameField.getText();

        if (name != null
                && !name.isBlank()) {

            config.setName(
                    name.trim()
            );
        }

        /*
         * 打包模式。
         */
        if (oneFileRadioButton.isSelected()) {

            config.setMode(
                    PyInstallerBuildConfig.Mode.ONEFILE
            );

        } else {

            config.setMode(
                    PyInstallerBuildConfig.Mode.ONEDIR
            );
        }

        /*
         * 控制台模式。
         */
        if (consoleRadioButton.isSelected()) {

            config.setConsoleMode(
                    PyInstallerBuildConfig.ConsoleMode.CONSOLE
            );

        } else {

            config.setConsoleMode(
                    PyInstallerBuildConfig.ConsoleMode.WINDOWED
            );
        }

        /*
         * 输出目录。
         */
        String output =
                outputDirectoryField.getText();

        if (output != null
                && !output.isBlank()) {

            config.setOutputDirectory(
                    Path.of(output)
            );
        }

        /*
         * 工作目录和 Spec 目录。
         */
        if (entry != null
                && !entry.isBlank()) {

            Path entryPath =
                    Path.of(entry);

            Path parent =
                    entryPath.getParent();

            if (parent != null) {

                config.setWorkDirectory(
                        parent.resolve("build")
                );

                config.setSpecDirectory(
                        parent.resolve("spec")
                );
            }
        }

        /*
         * 图标。
         */
        String icon =
                iconField.getText();

        if (icon != null
                && !icon.isBlank()) {

            config.setIcon(
                    Path.of(icon)
            );

        } else {

            config.setIcon(null);
        }

        /*
         * 基础开关。
         */
        config.setClean(
                cleanCheckBox.isSelected()
        );

        config.setNoconfirm(
                noconfirmCheckBox.isSelected()
        );

        config.setNoUpx(
                noUpxCheckBox.isSelected()
        );
    }

    @FXML
    private void handleOpenOutputDirectory() {


        String text =
                outputDirectoryField.getText();


        if (
                text == null
                        ||
                        text.isBlank()
        ) {

            return;
        }


        try {


            java.awt.Desktop
                    .getDesktop()
                    .open(
                            Path.of(text)
                                    .toFile()
                    );


        } catch (Exception e) {


            showError(
                    "打开目录失败",
                    e.getMessage()
            );

        }

    }

    @FXML
    private void handleBuildHistory(){


        try{

            javafx.fxml.FXMLLoader loader =
                    new javafx.fxml.FXMLLoader(
                            getClass()
                                    .getResource(
                                            "/fxml/BuildHistoryView.fxml"
                                    )
                    );


            javafx.scene.Parent root =
                    loader.load();


            Stage stage =
                    new Stage();


            stage.setTitle(
                    "PythonForge-win 构建历史"
            );


            stage.initOwner(
                    getStage()
            );


            stage.setScene(
                    new javafx.scene.Scene(
                            root,
                            900,
                            500
                    )
            );


            stage.show();


        }catch(Exception e){

            showError(
                    "构建历史",
                    e.getMessage()
            );
        }
    }

    @FXML
    private void handleOpenExe() {

        if (lastOutputFile == null) {

            showError(
                    "打开 EXE",
                    "当前没有可打开的 EXE 文件。"
            );

            return;
        }

        try {

            fileOpenService.openFile(
                    lastOutputFile
            );

        } catch (IOException exception) {

            showError(
                    "打开 EXE 失败",
                    exception.getMessage()
            );
        }
    }

    @FXML
    private void handleOpenOutput() {

        if (lastOutputDirectory == null) {

            showError(
                    "打开输出目录",
                    "当前没有可打开的输出目录。"
            );

            return;
        }

        try {

            fileOpenService.openDirectory(
                    lastOutputDirectory
            );

        } catch (IOException exception) {

            showError(
                    "打开输出目录失败",
                    exception.getMessage()
            );
        }
    }

    @FXML
    private void handleCopyExePath(){


        if(lastOutputFile == null){

            return;
        }


        ClipboardContent content =
                new ClipboardContent();


        content.putString(
                lastOutputFile
                        .toAbsolutePath()
                        .toString()
        );


        Clipboard.getSystemClipboard()
                .setContent(
                        content
                );


        buildLogArea.appendText(
                "\n已复制 EXE 路径。\n"
        );
    }

    @FXML
    private void handleHistory(){

        try {

            javafx.fxml.FXMLLoader loader =
                    new javafx.fxml.FXMLLoader(
                            getClass()
                                    .getResource(
                                            "/fxml/HistoryView.fxml"
                                    )
                    );


            javafx.scene.Parent root =
                    loader.load();


            Stage stage =
                    new Stage();


            stage.setTitle(
                    "PythonForge-win 构建历史"
            );


            stage.initOwner(
                    getStage()
            );


            stage.initModality(
                    javafx.stage.Modality.APPLICATION_MODAL
            );


            stage.setScene(
                    new javafx.scene.Scene(
                            root,
                            850,
                            500
                    )
            );


            stage.showAndWait();


        } catch(Exception e){

            showError(
                    "构建历史",
                    e.getMessage()
            );
        }
    }

    @FXML
    private void handleOpenHistory(){

        try{

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass()
                                    .getResource(
                                            "/fxml/BuildHistoryView.fxml"
                                    )
                    );


            Parent root =
                    loader.load();


            Stage stage =
                    new Stage();


            stage.setTitle(
                    "PythonForge-win - 构建历史"
            );


            stage.initOwner(
                    getStage()
            );


            stage.initModality(
                    Modality.APPLICATION_MODAL
            );


            stage.setScene(
                    new Scene(
                            root,
                            1000,
                            600
                    )
            );


            stage.show();


        }catch(Exception e){

            showError(
                    "构建历史",
                    e.getMessage()
            );
        }
    }

    @FXML
    private void handleOpenBuildLog() {

        if (currentBuildTask != null) {

            showError(
                    "无法打开日志",
                    "当前构建仍在进行中。"
            );

            return;
        }

        if (lastBuildLogFile == null) {

            showError(
                    "无法打开日志",
                    "当前没有可用的构建日志。"
            );

            return;
        }

        try {

            if (
                    !java.nio.file.Files.isRegularFile(
                            lastBuildLogFile
                    )
            ) {

                showError(
                        "无法打开日志",
                        "日志文件不存在：\n"
                                + lastBuildLogFile
                );

                return;
            }

            java.awt.Desktop.getDesktop()
                    .open(
                            lastBuildLogFile.toFile()
                    );

        } catch (Exception e) {

            showError(
                    "打开日志失败",
                    e.getMessage()
            );
        }
    }

    /**
     * 清除上一次构建结果。
     */
    private void clearLastBuildResult() {

        lastOutputFile = null;

        lastOutputDirectory = null;

        lastBuildLogFile = null;

        buildResultLabel.setText(
                "暂无构建结果"
        );

        openExeButton.setDisable(true);

        openOutputButton.setDisable(true);

        copyExePathButton.setDisable(true);

        if (openBuildLogButton != null) {

            openBuildLogButton.setDisable(true);
        }
    }

    /**
     * 删除当前选中的数据文件。
     */
    @FXML
    private void handleRemoveDataFile() {

        if (dataFilesListView == null) {

            return;
        }

        int selectedIndex =
                dataFilesListView
                        .getSelectionModel()
                        .getSelectedIndex();

        if (selectedIndex < 0) {

            showInfo(
                    "删除数据文件",
                    "请先选择要删除的数据文件。"
            );

            return;
        }

        dataFilesListView
                .getItems()
                .remove(selectedIndex);
    }

    @FXML
    private void handleRemoveBinary() {

        if (binariesListView == null) {
            return;
        }

        int selectedIndex =
                binariesListView
                        .getSelectionModel()
                        .getSelectedIndex();

        if (selectedIndex < 0) {

            showInfo(
                    "删除二进制文件",
                    "请先选择要删除的二进制文件。"
            );

            return;
        }

        binariesListView
                .getItems()
                .remove(selectedIndex);
    }

    @FXML
    private void handleAddAdditionalDirectory() {

        DirectoryChooser chooser =
                new DirectoryChooser();

        chooser.setTitle(
                "选择附加目录"
        );

        Window window = null;

        if (
                additionalDirectoriesListView != null
                        &&
                        additionalDirectoriesListView.getScene() != null
        ) {

            window =
                    additionalDirectoriesListView
                            .getScene()
                            .getWindow();
        }

        File selected =
                chooser.showDialog(
                        window
                );

        if (selected == null) {

            return;
        }

        Path directory =
                selected.toPath()
                        .toAbsolutePath()
                        .normalize();

        if (!Files.isDirectory(directory)) {

            showError(
                    "添加失败",
                    "选择的路径不是有效目录。"
            );

            return;
        }

        if (
                additionalDirectories.contains(
                        directory
                )
        ) {

            showInfo(
                    "附加目录",
                    "该目录已经添加。"
            );

            return;
        }

        additionalDirectories.add(
                directory
        );

        syncAdditionalDirectories();
    }

    @FXML
    private void handleRemoveAdditionalDirectory() {

        Path selected =
                additionalDirectoriesListView
                        .getSelectionModel()
                        .getSelectedItem();

        if (selected == null) {

            showInfo(
                    "删除附加目录",
                    "请先选择一个附加目录。"
            );

            return;
        }

        additionalDirectories.remove(
                selected
        );

        syncAdditionalDirectories();
    }

    private void syncAdditionalDirectories() {

        if (currentConfig == null) {

            return;
        }

        currentConfig.setAdditionalDirectories(
                new ArrayList<>(
                        additionalDirectories
                )
        );
    }

    private void loadAdditionalDirectories() {

        additionalDirectories.clear();

        if (currentConfig == null) {

            return;
        }

        additionalDirectories.addAll(
                currentConfig
                        .getAdditionalDirectories()
        );
    }

    /**
     * 打开主题设置。
     */
    @FXML
    private void handleOpenThemeSettings() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            MainController.class
                                    .getResource(
                                            "/fxml/ThemeSettingsView.fxml"
                                    )
                    );


            Parent root =
                    loader.load();


            Stage stage =
                    new Stage();


            stage.setTitle(
                    "PythonForge-win - 主题设置"
            );


            stage.initModality(
                    Modality.APPLICATION_MODAL
            );


            if (
                    buildButton != null
                            &&
                            buildButton
                                    .getScene()
                                    != null
            ) {

                stage.initOwner(
                        buildButton
                                .getScene()
                                .getWindow()
                );
            }


            Scene scene =
                    new Scene(
                            root,
                            620,
                            620
                    );


            /*
             * 主题设置窗口也使用当前主题。
             */
            var themeManager =
                    com.pythonforge.service.ThemeManager
                            .getInstance();


            String currentCss =
                    themeManager
                            .getCurrentThemeCss();


            String commonCss =
                    MainController.class
                            .getResource(
                                    "/css/application.css"
                            )
                            .toExternalForm();


            scene.getStylesheets().add(
                    commonCss
            );


            if (
                    currentCss != null
                            &&
                            !currentCss.isBlank()
            ) {

                scene.getStylesheets().add(
                        currentCss
                );
            }


            stage.setScene(
                    scene
            );


            stage.setResizable(
                    false
            );


            stage.showAndWait();

        } catch (Exception exception) {

            exception.printStackTrace();
        }
    }

    /**
     * 打开设置页面。
     *
     * <p>
     * 设置页面属于 PythonForge-win 的一级页面。
     * 点击左侧导航栏的“设置”后，
     * 将 SettingsView.fxml 加载到主界面的中心区域。
     * </p>
     *
     * //@param event 设置按钮点击事件
     */
    @FXML
    private void handleOpenSettings() {

        try {

            LOGGER.info(
                    "Opening SettingsView.fxml..."
            );

            FXMLLoader loader =
                    new FXMLLoader(
                            MainController.class.getResource(
                                    "/fxml/SettingsView.fxml"
                            )
                    );

            Parent settingsView =
                    loader.load();

            LOGGER.info(
                    "SettingsView.fxml loaded successfully."
            );

            mainRoot.setCenter(
                    settingsView
            );

            LOGGER.info(
                    "SettingsView displayed successfully."
            );

        } catch (IOException exception) {

            LOGGER.severe(
                    "Failed to open SettingsView.fxml: "
                            + exception.getMessage()
            );

            showError(
                    "设置",
                    "无法打开设置页面：\n"
                            + exception.getMessage()
            );
        }
    }

    @FXML
    private void handleOpenMain() {

        if (mainWorkspace == null) {

            showError(
                    "主界面",
                    "主工作区没有正确初始化。"
            );

            return;
        }

        mainRoot.setCenter(
                mainWorkspace
        );
    }

    private void showPage(Node page) {

        if (mainRoot == null) {
            return;
        }

        mainRoot.setCenter(page);
    }


}