package com.pythonforge.controller;

import com.pythonforge.context.ApplicationContext;
import com.pythonforge.history.BuildHistoryRecord;
import com.pythonforge.history.BuildHistoryService;
import com.pythonforge.model.BuildStatus;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 构建历史控制器。
 *
 * <p>
 * P4.10.6：
 * </p>
 *
 * <ul>
 *     <li>显示构建历史</li>
 *     <li>刷新历史</li>
 *     <li>清空历史</li>
 *     <li>打开 EXE</li>
 *     <li>打开构建日志</li>
 *     <li>双击历史记录执行默认操作</li>
 * </ul>
 */
public final class BuildHistoryController {

    /**
     * 历史表格。
     */
    @FXML
    private TableView<BuildHistoryRecord> historyTable;

    /**
     * 时间列。
     */
    @FXML
    private TableColumn<BuildHistoryRecord, String> timeColumn;

    /**
     * 状态列。
     */
    @FXML
    private TableColumn<BuildHistoryRecord, String> statusColumn;

    /**
     * 应用名称列。
     */
    @FXML
    private TableColumn<BuildHistoryRecord, String> nameColumn;

    /**
     * Python 版本列。
     */
    @FXML
    private TableColumn<BuildHistoryRecord, String> pythonColumn;

    /**
     * EXE 列。
     */
    @FXML
    private TableColumn<BuildHistoryRecord, String> exeColumn;

    /**
     * 日志列。
     */
    @FXML
    private TableColumn<BuildHistoryRecord, String> logColumn;

    /**
     * 刷新按钮。
     */
    @FXML
    private Button refreshButton;

    @FXML
    private Button exportButton;

    @FXML
    private Button importButton;

    /**
     * 历史服务。
     */
    private final BuildHistoryService historyService =
            ApplicationContext
                    .getBuildHistoryService();

    /**
     * 日期格式。
     */
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd HH:mm:ss"
            );

    @FXML
    private Button openExeButton;

    @FXML
    private Button openOutputButton;

    @FXML
    private Button openLogButton;

    @FXML
    private Label selectionLabel;

    @FXML
    private Button deleteButton;

    @FXML
    private Button copyEntryButton;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private Label detailTimeLabel;

    @FXML
    private Label detailStatusLabel;

    @FXML
    private Label detailNameLabel;

    @FXML
    private Label detailPythonLabel;

    @FXML
    private Label detailEntryLabel;

    @FXML
    private Label detailExeLabel;

    @FXML
    private Label detailOutputDirectoryLabel;

    @FXML
    private Label detailLogLabel;

    @FXML
    private TextArea detailMessageArea;

    @FXML
    private Button copyExeButton;

    @FXML
    private Button copyLogButton;


    private final ObservableList<BuildHistoryRecord> historyItems =
            FXCollections.observableArrayList();

    private List<BuildHistoryRecord> allRecords =
            new ArrayList<>();

    /**
     * 初始化。
     */
    @FXML
    private void initialize() {

        configureColumns();

        configureTable();

        refreshHistory();

        configureTableInteraction();

        loadHistory();

        updateSelectionState();

        initializeStatusFilter();

        historyTable.setOnMouseClicked(
                event -> {

                    if (event.getClickCount() != 2) {

                        return;
                    }

                    BuildHistoryRecord record =
                            historyTable
                                    .getSelectionModel()
                                    .getSelectedItem();

                    if (record == null) {

                        return;
                    }

                    if (record.getOutputFile() != null) {

                        openExeFile(
                                record.getOutputFile()
                        );

                    } else if (record.getLogFile() != null) {

                        openFile(
                                record.getLogFile()
                        );
                    }
                }
        );

        historyTable
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                                updateSelectionState()
                );

        if (searchField != null) {

            searchField.textProperty()
                    .addListener(
                            (observable, oldValue, newValue) ->
                                    applyFilters()
                    );
        }

        historyService.addListener(
                this::handleHistoryChanged
        );
    }

    private void handleHistoryChanged() {

        javafx.application.Platform.runLater(
                this::refreshHistory
        );
    }

    private void initializeContextMenu() {

        MenuItem openExeItem =
                new MenuItem("打开 EXE");

        MenuItem openLogItem =
                new MenuItem("打开日志");

        MenuItem openDirectoryItem =
                new MenuItem("打开输出目录");

        MenuItem deleteItem =
                new MenuItem("删除历史");

        deleteItem.setOnAction(event -> {

            handleDelete();
        });


        openLogItem.setOnAction(event -> {

            BuildHistoryRecord record =
                    getSelectedRecord();

            if (record != null) {

                openLog(
                        record.getLogFile()
                );
            }
        });


        openLogItem.setOnAction(event -> {

            BuildHistoryRecord record =
                    getSelectedRecord();

            if (record != null) {

                openFile(
                        record.getLogFile()
                );
            }
        });


        openDirectoryItem.setOnAction(event -> {

            BuildHistoryRecord record =
                    getSelectedRecord();

            if (record != null) {

                openFile(
                        record.getOutputDirectory()
                );
            }
        });


        ContextMenu menu =
                new ContextMenu(
                        openExeItem,
                        openLogItem,
                        openDirectoryItem,
                        new SeparatorMenuItem(),
                        deleteItem
                );


        historyTable.setContextMenu(
                menu
        );
    }



    /**
     * 配置表格列。
     */
    private void configureColumns() {

        /*
         * 时间。
         */
        timeColumn.setCellValueFactory(
                cellData -> {

                    BuildHistoryRecord record =
                            cellData.getValue();

                    if (record == null
                            || record.getTime() == null) {

                        return new javafx.beans.property.SimpleStringProperty(
                                ""
                        );
                    }

                    return new javafx.beans.property.SimpleStringProperty(
                            record.getTime()
                                    .format(TIME_FORMATTER)
                    );
                }
        );


        /*
         * 状态。
         */
        statusColumn.setCellValueFactory(
                cellData -> {

                    BuildHistoryRecord record =
                            cellData.getValue();

                    return new javafx.beans.property.SimpleStringProperty(
                            getStatusText(
                                    record == null
                                            ? null
                                            : record.getStatus()
                            )
                    );
                }
        );


        /*
         * 应用名称。
         */
        nameColumn.setCellValueFactory(
                cellData -> {

                    BuildHistoryRecord record =
                            cellData.getValue();

                    return new javafx.beans.property.SimpleStringProperty(
                            record == null
                                    || record.getApplicationName() == null
                                    ? ""
                                    : record.getApplicationName()
                    );
                }
        );


        /*
         * Python 版本。
         */
        pythonColumn.setCellValueFactory(
                cellData -> {

                    BuildHistoryRecord record =
                            cellData.getValue();

                    return new javafx.beans.property.SimpleStringProperty(
                            record == null
                                    || record.getPythonVersion() == null
                                    ? ""
                                    : record.getPythonVersion()
                    );
                }
        );


        /*
         * EXE。
         */
        exeColumn.setCellValueFactory(
                cellData -> {

                    BuildHistoryRecord record =
                            cellData.getValue();

                    if (record == null
                            || record.getOutputFile() == null) {

                        return new javafx.beans.property.SimpleStringProperty(
                                ""
                        );
                    }

                    return new javafx.beans.property.SimpleStringProperty(
                            record.getOutputFile()
                                    .toString()
                    );
                }
        );


        /*
         * 日志。
         */
        logColumn.setCellValueFactory(
                cellData -> {

                    BuildHistoryRecord record =
                            cellData.getValue();

                    if (record == null
                            || record.getLogFile() == null) {

                        return new javafx.beans.property.SimpleStringProperty(
                                ""
                        );
                    }

                    return new javafx.beans.property.SimpleStringProperty(
                            record.getLogFile()
                                    .toString()
                    );
                }
        );
    }

    /**
     * 配置历史表格。
     */
    private void configureTable() {

        historyTable.setPlaceholder(
                new javafx.scene.control.Label(
                        "暂无构建历史"
                )
        );


        /*
         * 双击历史记录。
         *
         * 默认优先打开 EXE。
         * 如果没有 EXE，则打开日志。
         */
        historyTable.setRowFactory(
                tableView -> {

                    TableRow<BuildHistoryRecord> row =
                            new TableRow<>();

                    row.setOnMouseClicked(
                            event -> {

                                if (event.getClickCount() != 2) {

                                    return;
                                }

                                if (row.isEmpty()) {

                                    return;
                                }

                                BuildHistoryRecord record =
                                        row.getItem();

                                if (record == null) {

                                    return;
                                }

                                handleDoubleClick(
                                        record
                                );
                            }
                    );

                    historyTable.setItems(
                            historyItems
                    );


                    return row;
                }
        );
    }

    /**
     * 刷新历史。
     */
    @FXML
    private void handleRefresh() {

        refreshHistory();
    }

    /**
     * 实际执行历史刷新。
     */
    private void refreshHistory() {

        try {

            allRecords =
                    new ArrayList<>(
                            historyService.getRecords()
                    );

            applyFilters();

            updateSelectionState();

        } catch (Exception e) {

            showError(
                    "刷新失败",
                    e.getMessage() == null
                            ? "无法刷新构建历史。"
                            : e.getMessage()
            );
        }
    }



    /**
     * 清空历史。
     */
    @FXML
    private void handleClear() {

        if (
                historyTable == null
                        ||
                        historyTable.getItems().isEmpty()
        ) {

            showInfo(
                    "构建历史",
                    "当前没有构建历史。"
            );

            return;
        }

        Alert alert =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        alert.setTitle(
                "清空构建历史"
        );

        alert.setHeaderText(
                "确定要清空所有构建历史吗？"
        );

        alert.setContentText(
                "此操作只删除历史记录，"
                        + "不会删除 EXE、输出目录或日志文件。"
        );

        alert.showAndWait()
                .ifPresent(
                        buttonType -> {

                            if (
                                    buttonType.getButtonData()
                                            .isDefaultButton()
                            ) {

                                historyService.clear();

                                loadHistory();

                                updateSelectionState();
                            }
                        }
                );
    }

    /**
     * 双击记录。
     */
    private void handleDoubleClick(
            BuildHistoryRecord record) {

        /*
         * 成功构建且存在 EXE：
         * 优先打开 EXE。
         */
        if (
                record.getStatus()
                        == BuildStatus.SUCCESS
                        &&
                        record.getOutputFile() != null
        ) {

            try {

                openFile(
                        record.getOutputFile()
                );

                return;

            } catch (Exception e) {

                showError(
                        "打开 EXE 失败",
                        e.getMessage()
                );

                return;
            }
        }


        /*
         * 如果没有 EXE，
         * 尝试打开日志。
         */
        if (record.getLogFile() != null) {

            try {

                openFile(
                        record.getLogFile()
                );

            } catch (Exception e) {

                showError(
                        "打开构建日志失败",
                        e.getMessage()
                );
            }

            return;
        }


        showError(
                "无法打开",
                "该构建记录没有可打开的 EXE 或日志文件。"
        );
    }

    /**
     * 打开 EXE。
     */
    private void openExe(
            BuildHistoryRecord record) {

        if (record == null) {

            return;
        }

        Path outputFile =
                record.getOutputFile();

        if (outputFile == null) {

            showError(
                    "打开 EXE",
                    "该构建记录没有 EXE 文件。"
            );

            return;
        }

        try {

            openFile(
                    outputFile
            );

        } catch (Exception e) {

            showError(
                    "打开 EXE 失败",
                    e.getMessage()
            );
        }
    }

    /**
     * 打开日志。
     */
    private void openLog(
            BuildHistoryRecord record) {

        if (record == null) {

            return;
        }

        Path logFile =
                record.getLogFile();

        if (logFile == null) {

            showError(
                    "打开日志",
                    "该构建记录没有日志文件。"
            );

            return;
        }

        try {

            openFile(
                    logFile
            );

        } catch (Exception e) {

            showError(
                    "打开日志失败",
                    e.getMessage()
            );
        }
    }

    /**
     * 使用 Windows 默认程序打开文件。
     */
    private void openFile(Path path) {

        if (path == null) {

            showError(
                    "打开失败",
                    "文件路径为空。"
            );

            return;
        }

        if (!java.nio.file.Files.exists(path)) {

            showError(
                    "打开失败",
                    "文件不存在：\n"
                            + path
            );

            return;
        }

        try {

            java.awt.Desktop desktop =
                    java.awt.Desktop.getDesktop();

            if (!desktop.isSupported(
                    java.awt.Desktop.Action.OPEN
            )) {

                showError(
                        "打开失败",
                        "当前系统不支持打开文件。"
                );

                return;
            }

            desktop.open(
                    path.toFile()
            );

        } catch (Exception e) {

            showError(
                    "打开失败",
                    e.getMessage() == null
                            ? "无法打开文件。"
                            : e.getMessage()
            );
        }
    }

    /**
     * 获取状态显示文字。
     */
    private String getStatusText(
            BuildStatus status) {

        if (status == null) {

            return "未知";
        }

        return switch (status) {

            case SUCCESS ->
                    "成功";

            case FAILED ->
                    "失败";

            case CANCELLED ->
                    "已取消";

            default ->
                    status.name();
        };
    }

    /**
     * 显示错误提示框。
     *
     * @param title   标题
     * @param message 错误信息
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

        alert.setContentText(
                message == null
                        ? "未知错误。"
                        : message
        );

        alert.showAndWait();
    }

    private BuildHistoryRecord getSelectedRecord() {

        if (historyTable == null) {

            return null;
        }

        BuildHistoryRecord record =
                historyTable
                        .getSelectionModel()
                        .getSelectedItem();

        if (record == null) {

            showInfo(
                    "构建历史",
                    "请先选择一条构建历史。"
            );
        }

        return record;
    }
    private void loadHistory() {

        try {

            List<BuildHistoryRecord> records =
                    historyService.getRecords();

            historyItems.setAll(
                    records
            );

            applyFilters();

            updateSelectionState();

        } catch (Exception e) {

            showError(
                    "加载构建历史失败",
                    e.getMessage() == null
                            ? "无法加载构建历史。"
                            : e.getMessage()
            );
        }
    }

    private void openLog(
            Path logFile) {

        if (logFile == null) {

            showError(
                    "打开日志失败",
                    "日志文件路径为空。"
            );

            return;
        }

        if (!java.nio.file.Files.isRegularFile(logFile)) {

            showError(
                    "打开日志失败",
                    "日志文件不存在：\n"
                            + logFile
            );

            return;
        }

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass()
                                    .getResource(
                                            "/fxml/BuildLogView.fxml"
                                    )
                    );

            Parent root =
                    loader.load();

            BuildLogController controller =
                    loader.getController();

            controller.setLogFile(
                    logFile
            );

            Stage stage =
                    new Stage();

            stage.setTitle(
                    "构建日志"
            );

            stage.setScene(
                    new Scene(
                            root,
                            900,
                            600
                    )
            );

            stage.show();

        } catch (Exception e) {

            showError(
                    "打开日志失败",
                    e.getMessage() == null
                            ? "无法打开构建日志。"
                            : e.getMessage()
            );
        }
    }

    @FXML
    private void handleDelete() {

        BuildHistoryRecord record =
                getSelectedRecord();

        if (record == null) {
            return;
        }

        String applicationName =
                record.getApplicationName();

        if (
                applicationName == null
                        ||
                        applicationName.isBlank()
        ) {

            applicationName = "未命名应用";
        }

        Alert alert =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        alert.setTitle(
                "删除构建历史"
        );

        alert.setHeaderText(
                "确定要删除这条构建历史吗？"
        );

        alert.setContentText(
                "应用："
                        + applicationName
                        + "\n\n"
                        + "注意：只删除历史记录，"
                        + "不会删除 EXE、输出目录或日志文件。"
        );

        alert.showAndWait()
                .ifPresent(
                        buttonType -> {

                            if (
                                    buttonType.getButtonData()
                                            .isDefaultButton()
                            ) {

                                boolean removed =
                                        historyService.remove(
                                                record
                                        );

                                if (removed) {

                                    loadHistory();

                                    showInfo(
                                            "删除成功",
                                            "构建历史已删除。"
                                    );

                                } else {

                                    showError(
                                            "删除失败",
                                            "未找到指定的构建历史记录。"
                                    );
                                }
                            }
                        }
                );
    }

    /**
     * 配置历史表格交互。
     */
    private void configureTableInteraction() {

        historyTable
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable,
                         oldValue,
                         newValue) -> {

                            updateSelectionState();
                        }
                );

        historyTable.setOnMouseClicked(
                event -> {

                    if (
                            event.getButton()
                                    == MouseButton.PRIMARY
                                    &&
                                    event.getClickCount() == 2
                    ) {

                        handleOpenExe();
                    }
                }
        );

        configureContextMenu();
    }

    /**
     * 配置历史记录右键菜单。
     */
    private void configureContextMenu() {

        MenuItem openExeItem =
                new MenuItem(
                        "打开 EXE"
                );

        openExeItem.setOnAction(
                event ->
                        handleOpenExe()
        );

        MenuItem openOutputItem =
                new MenuItem(
                        "打开输出目录"
                );

        openOutputItem.setOnAction(
                event ->
                        handleOpenOutput()
        );

        MenuItem openLogItem =
                new MenuItem(
                        "打开日志"
                );

        openLogItem.setOnAction(
                event ->
                        handleOpenLog()
        );

        ContextMenu contextMenu =
                new ContextMenu();


        /**
        contextMenu.getItems().addAll(
                openExeItem,
                openOutputItem,
                openLogItem
        );
         **/

        historyTable.setContextMenu(
                contextMenu
        );

        MenuItem deleteItem =
                new MenuItem(
                        "删除记录"
                );

        deleteItem.setOnAction(
                event ->
                        handleDelete()
        );

        contextMenu.getItems().addAll(
                openExeItem,
                openOutputItem,
                openLogItem,
                new SeparatorMenuItem(),
                deleteItem
        );
    }

    /**
     * 根据当前选择更新按钮状态。
     */
    private void updateSelectionState() {

        BuildHistoryRecord record =
                historyTable
                        .getSelectionModel()
                        .getSelectedItem();

        boolean hasRecord =
                record != null;

        /*
         * 更新详情区域。
         */
        showRecordDetails(
                record
        );

        if (deleteButton != null) {

            deleteButton.setDisable(
                    !hasRecord
            );
        }

        if (openExeButton != null) {

            openExeButton.setDisable(
                    !hasRecord
                            ||
                            record.getOutputFile() == null
            );
        }

        if (copyEntryButton != null) {

            copyEntryButton.setDisable(
                    !hasRecord
                            ||
                            record.getEntryFile() == null
            );
        }

        if (openOutputButton != null) {

            openOutputButton.setDisable(
                    !hasRecord
                            ||
                            record.getOutputDirectory() == null
            );
        }

        if (openLogButton != null) {

            openLogButton.setDisable(
                    !hasRecord
                            ||
                            record.getLogFile() == null
            );
        }

        if (selectionLabel != null) {

            if (!hasRecord) {

                selectionLabel.setText(
                        "未选择构建记录"
                );

            } else {

                String name =
                        record.getApplicationName();

                if (
                        name == null
                                ||
                                name.isBlank()
                ) {

                    name = "未命名应用";
                }

                selectionLabel.setText(
                        "当前选择：" + name
                );
            }
        }

        if (copyExeButton != null) {

            copyExeButton.setDisable(
                    !hasRecord
                            ||
                            record.getOutputFile() == null
            );
        }

        if (copyLogButton != null) {

            copyLogButton.setDisable(
                    !hasRecord
                            ||
                            record.getLogFile() == null
            );
        }


    }

    @FXML
    private void handleCopyExePath() {

        BuildHistoryRecord record =
                getSelectedRecord();

        if (record == null) {
            return;
        }

        Path path =
                record.getOutputFile();

        if (path == null) {

            showError(
                    "复制路径",
                    "该构建记录没有 EXE 文件。"
            );

            return;
        }

        copyPathToClipboard(
                path,
                "EXE 路径"
        );
    }

    @FXML
    private void handleCopyLogPath() {

        BuildHistoryRecord record =
                getSelectedRecord();

        if (record == null) {
            return;
        }

        Path path =
                record.getLogFile();

        if (path == null) {

            showError(
                    "复制路径",
                    "该构建记录没有日志文件。"
            );

            return;
        }

        copyPathToClipboard(
                path,
                "日志路径"
        );
    }

    private void copyPathToClipboard(
            Path path,
            String type) {

        if (path == null) {
            return;
        }

        try {

            ClipboardContent content =
                    new ClipboardContent();

            content.putString(
                    path.toAbsolutePath()
                            .normalize()
                            .toString()
            );

            Clipboard.getSystemClipboard()
                    .setContent(
                            content
                    );

            showInfo(
                    "复制成功",
                    type
                            + "已经复制到剪贴板。"
            );

        } catch (Exception e) {

            showError(
                    "复制失败",
                    e.getMessage() == null
                            ? "无法复制路径。"
                            : e.getMessage()
            );
        }
    }



    @FXML
    private void handleOpenExe() {

        BuildHistoryRecord record =
                getSelectedRecord();

        if (record == null) {

            showError(
                    "打开 EXE",
                    "请先选择一条构建记录。"
            );

            return;
        }

        Path exePath =
                record.getOutputFile();

        if (exePath == null) {

            showError(
                    "打开 EXE",
                    "该构建记录没有 EXE 文件。"
            );

            return;
        }

        openExeFile(
                exePath
        );
    }


    /**
     * 使用 Windows Shell 打开并执行 EXE。
     *
     * <p>
     * EXE 不使用 Desktop.open()。
     * Windows 下直接交给 cmd /c start 处理，
     * 避免 JavaFX/桌面 URI 处理产生
     * Unsupported URI Content。
     * </p>
     *
     * @param exePath EXE 文件路径
     */
    private void openExeFile(
            Path exePath) {

        if (exePath == null) {

            showError(
                    "打开 EXE",
                    "EXE 文件路径为空。"
            );

            return;
        }

        Path normalizedPath =
                exePath
                        .toAbsolutePath()
                        .normalize();

        if (!Files.exists(normalizedPath)) {

            showError(
                    "打开 EXE",
                    "EXE 文件不存在：\n"
                            + normalizedPath
            );

            return;
        }

        if (!Files.isRegularFile(normalizedPath)) {

            showError(
                    "打开 EXE",
                    "指定路径不是一个文件：\n"
                            + normalizedPath
            );

            return;
        }

        /*
         * 确认文件扩展名。
         */
        String fileName =
                normalizedPath
                        .getFileName()
                        .toString();

        if (!fileName
                .toLowerCase(
                        java.util.Locale.ROOT
                )
                .endsWith(".exe")) {

            showError(
                    "打开 EXE",
                    "指定文件不是 EXE 文件：\n"
                            + normalizedPath
            );

            return;
        }

        /*
         * PythonForge-win 当前为 Windows 主程序。
         *
         * 使用 Windows Shell 启动 EXE。
         *
         * start 的第一个参数为空字符串，
         * 是为了正确处理带空格的路径。
         */
        try {

            new ProcessBuilder(
                    "cmd",
                    "/c",
                    "start",
                    "",
                    normalizedPath.toString()
            ).start();

        } catch (IOException exception) {

            showError(
                    "打开 EXE 失败",
                    "无法启动 EXE：\n"
                            + normalizedPath
                            + "\n\n"
                            + "错误信息："
                            + (
                            exception.getMessage()
                                    == null
                                    ? "未知错误"
                                    : exception.getMessage()
                    )
            );
        }
    }

    @FXML
    private void handleOpenOutput() {

        BuildHistoryRecord record =
                getSelectedRecord();

        if (record == null) {
            return;
        }

        if (record.getOutputDirectory() == null) {

            showError(
                    "打开输出目录",
                    "该记录没有输出目录。"
            );

            return;
        }

        openDirectory(
                record.getOutputDirectory()
        );
    }

    @FXML
    private void handleOpenLog() {

        BuildHistoryRecord record =
                getSelectedRecord();

        if (record == null) {
            return;
        }

        if (record.getLogFile() == null) {

            showError(
                    "打开日志",
                    "该记录没有日志文件。"
            );

            return;
        }

        openFile(
                record.getLogFile()
        );
    }

    /**
     * 显示信息提示框。
     *
     * @param title   标题
     * @param message 消息
     */
    private void showInfo(
            String title,
            String message) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(title);

        alert.setHeaderText(null);

        alert.setContentText(
                message == null
                        ? ""
                        : message
        );

        alert.showAndWait();
    }

    /**
     * 使用系统默认文件管理器打开目录。
     *
     * @param path 目录路径
     */
    private void openDirectory(
            Path path) {

        if (path == null) {

            showError(
                    "打开输出目录",
                    "输出目录为空。"
            );

            return;
        }

        try {

            if (!Files.isDirectory(path)) {

                showError(
                        "打开输出目录",
                        "输出目录不存在：\n"
                                + path
                );

                return;
            }

            if (!Desktop.isDesktopSupported()) {

                showError(
                        "打开输出目录",
                        "当前系统不支持桌面文件操作。"
                );

                return;
            }

            Desktop desktop =
                    Desktop.getDesktop();

            if (!desktop.isSupported(
                    Desktop.Action.OPEN
            )) {

                showError(
                        "打开输出目录",
                        "当前系统不支持打开目录。"
                );

                return;
            }

            desktop.open(
                    path.toFile()
            );

        } catch (Exception e) {

            showError(
                    "打开输出目录失败",
                    e.getMessage() == null
                            ? "无法打开输出目录。"
                            : e.getMessage()
            );
        }
    }

    private void initializeStatusFilter() {

        statusComboBox.getItems().clear();

        statusComboBox.getItems().addAll(
                "全部",
                "成功",
                "失败",
                "已取消"
        );

        statusComboBox.setValue("全部");

        statusComboBox.valueProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                                applyFilters()
                );

        searchField.textProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                                applyFilters()
                );
    }

    /**
     * 应用搜索和状态筛选。
     */
    private void applyFilters() {

        String text = "";

        if (searchField != null) {

            String value =
                    searchField.getText();

            if (value != null) {

                text =
                        value.trim();
            }
        }

        final String keyword =
                text.toLowerCase();

        List<BuildHistoryRecord> filtered =
                allRecords
                        .stream()
                        .filter(
                                record ->
                                        matchesKeyword(
                                                record,
                                                keyword
                                        )
                        )
                        .toList();

        historyTable
                .getItems()
                .setAll(
                        filtered
                );

        updateSelectionState();
    }

    private boolean matchesKeyword(
            BuildHistoryRecord record,
            String keyword) {

        if (
                keyword == null
                        ||
                        keyword.isBlank()
        ) {

            return true;
        }

        if (record == null) {

            return false;
        }

        return contains(
                record.getApplicationName(),
                keyword
        )
                ||
                contains(
                        record.getPythonVersion(),
                        keyword
                )
                ||
                contains(
                        pathToString(
                                record.getEntryFile()
                        ),
                        keyword
                )
                ||
                contains(
                        pathToString(
                                record.getOutputFile()
                        ),
                        keyword
                )
                ||
                contains(
                        pathToString(
                                record.getOutputDirectory()
                        ),
                        keyword
                )
                ||
                contains(
                        pathToString(
                                record.getLogFile()
                        ),
                        keyword
                )
                ||
                contains(
                        record.getMessage(),
                        keyword
                );
    }

    private boolean contains(
            String value,
            String keyword) {

        if (value == null) {

            return false;
        }

        return value
                .toLowerCase()
                .contains(keyword);
    }

    private String pathToString(
            Path path) {

        if (path == null) {

            return "";
        }

        return path.toString();
    }

    private boolean matchesStatus(
            BuildHistoryRecord record,
            String status) {

        if (record == null) {

            return false;
        }

        if (
                status == null
                        ||
                        status.equals("全部")
        ) {

            return true;
        }

        return switch (record.getStatus()) {

            case SUCCESS ->
                    status.equals("成功");

            case FAILED ->
                    status.equals("失败");

            case CANCELLED ->
                    status.equals("已取消");

            default ->
                    false;
        };
    }

    @FXML
    private void handleResetFilter() {

        searchField.clear();

        statusComboBox.setValue(
                "全部"
        );

        applyFilters();
    }

    /**
     * 显示当前选中的构建历史详情。
     *
     * @param record 构建历史记录
     */
    private void showRecordDetails(
            BuildHistoryRecord record) {

        if (record == null) {

            clearRecordDetails();

            return;
        }

        detailTimeLabel.setText(
                record.getTime() == null
                        ? "-"
                        : record.getTime().toString()
        );

        detailStatusLabel.setText(
                getStatusText(
                        record.getStatus()
                )
        );

        detailNameLabel.setText(
                emptyIfNull(
                        record.getApplicationName()
                )
        );

        detailPythonLabel.setText(
                emptyIfNull(
                        record.getPythonVersion()
                )
        );

        detailEntryLabel.setText(
                pathToString(
                        record.getEntryFile()
                )
        );

        detailExeLabel.setText(
                pathToString(
                        record.getOutputFile()
                )
        );

        detailOutputDirectoryLabel.setText(
                pathToString(
                        record.getOutputDirectory()
                )
        );

        detailLogLabel.setText(
                pathToString(
                        record.getLogFile()
                )
        );

        detailMessageArea.setText(
                emptyIfNull(
                        record.getMessage()
                )
        );
    }

    /**
     * 清空构建详情。
     */
    private void clearRecordDetails() {

        detailTimeLabel.setText("-");

        detailStatusLabel.setText("-");

        detailNameLabel.setText("-");

        detailPythonLabel.setText("-");

        detailEntryLabel.setText("-");

        detailExeLabel.setText("-");

        detailOutputDirectoryLabel.setText("-");

        detailLogLabel.setText("-");

        detailMessageArea.clear();
    }

    private String emptyIfNull(
            String value) {

        if (value == null) {

            return "-";
        }

        if (value.isBlank()) {

            return "-";
        }

        return value;
    }

    @FXML
    private void handleCopyEntryPath() {

        BuildHistoryRecord record =
                getSelectedRecord();

        if (record == null) {
            return;
        }

        Path path =
                record.getEntryFile();

        if (path == null) {

            showError(
                    "复制路径",
                    "该构建记录没有入口文件。"
            );

            return;
        }

        copyPathToClipboard(
                path,
                "入口文件路径"
        );
    }

    @FXML
    private void handleExport() {

        List<BuildHistoryRecord> records =
                historyService.getRecords();

        if (records.isEmpty()) {

            showInfo(
                    "导出历史",
                    "当前没有可导出的构建历史记录。"
            );

            return;
        }

        FileChooser fileChooser =
                new FileChooser();

        fileChooser.setTitle(
                "导出构建历史"
        );

        fileChooser.setInitialFileName(
                "PythonForge-BuildHistory.csv"
        );

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "CSV 文件 (*.csv)",
                        "*.csv"
                )
        );

        Window window =
                historyTable.getScene() == null
                        ? null
                        : historyTable.getScene()
                        .getWindow();

        File file =
                fileChooser.showSaveDialog(
                        window
                );

        if (file == null) {

            return;
        }

        try {

            exportHistory(
                    records,
                    file.toPath()
            );

            showInfo(
                    "导出成功",
                    "构建历史已经导出到：\n"
                            + file.getAbsolutePath()
            );

        } catch (Exception e) {

            showError(
                    "导出失败",
                    e.getMessage() == null
                            ? "无法导出构建历史。"
                            : e.getMessage()
            );
        }
    }

    private void exportHistory(
            List<BuildHistoryRecord> records,
            Path target)
            throws IOException {

        if (records == null) {

            throw new IllegalArgumentException(
                    "构建历史记录为空。"
            );
        }

        if (target == null) {

            throw new IllegalArgumentException(
                    "导出文件不能为空。"
            );
        }

        Path parent =
                target.getParent();

        if (parent != null) {

            Files.createDirectories(
                    parent
            );
        }

        try (
                BufferedWriter writer =
                        Files.newBufferedWriter(
                                target,
                                StandardCharsets.UTF_8
                        )
        ) {

            /*
             * CSV 表头。
             */
            writer.write(
                    "构建时间,"
                            + "状态,"
                            + "应用名称,"
                            + "Python版本,"
                            + "入口文件,"
                            + "EXE文件,"
                            + "输出目录,"
                            + "日志文件,"
                            + "构建消息"
            );

            writer.newLine();

            /*
             * 构建记录。
             */
            for (
                    BuildHistoryRecord record
                    : records
            ) {

                if (record == null) {

                    continue;
                }

                writer.write(
                        csv(record.getTime())
                                + ","
                                + csv(
                                getStatusText(
                                        record.getStatus()
                                )
                        )
                                + ","
                                + csv(
                                record.getApplicationName()
                        )
                                + ","
                                + csv(
                                record.getPythonVersion()
                        )
                                + ","
                                + csv(
                                pathToString(
                                        record.getEntryFile()
                                )
                        )
                                + ","
                                + csv(
                                pathToString(
                                        record.getOutputFile()
                                )
                        )
                                + ","
                                + csv(
                                pathToString(
                                        record.getOutputDirectory()
                                )
                        )
                                + ","
                                + csv(
                                pathToString(
                                        record.getLogFile()
                                )
                        )
                                + ","
                                + csv(
                                record.getMessage()
                        )
                );

                writer.newLine();
            }
        }
    }

    private String csv(
            Object value) {

        if (value == null) {

            return "\"\"";
        }

        String text =
                String.valueOf(value);

        text =
                text.replace(
                        "\"",
                        "\"\""
                );

        return "\"" + text + "\"";
    }

    @FXML
    private void handleImport() {

        FileChooser fileChooser =
                new FileChooser();

        fileChooser.setTitle(
                "导入构建历史"
        );

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "CSV 文件 (*.csv)",
                        "*.csv"
                )
        );

        Window window =
                historyTable.getScene() == null
                        ? null
                        : historyTable
                        .getScene()
                        .getWindow();

        File file =
                fileChooser.showOpenDialog(
                        window
                );

        if (file == null) {

            return;
        }

        try {

            List<BuildHistoryRecord> imported =
                    importHistory(
                            file.toPath()
                    );

            if (imported.isEmpty()) {

                showInfo(
                        "导入历史",
                        "CSV 文件中没有有效的构建历史记录。"
                );

                return;
            }

            historyService.importRecords(
                    imported
            );

            refreshHistory();

            showInfo(
                    "导入成功",
                    "成功导入 "
                            + imported.size()
                            + " 条构建历史记录。"
            );

        } catch (Exception e) {

            showError(
                    "导入失败",
                    e.getMessage() == null
                            ? "无法导入构建历史。"
                            : e.getMessage()
            );
        }
    }

    private List<BuildHistoryRecord> importHistory(
            Path source)
            throws IOException {

        List<BuildHistoryRecord> result =
                new ArrayList<>();

        if (source == null) {

            throw new IllegalArgumentException(
                    "导入文件不能为空。"
            );
        }

        if (!Files.isRegularFile(source)) {

            throw new IOException(
                    "导入文件不存在："
                            + source
            );
        }

        List<String> lines =
                Files.readAllLines(
                        source,
                        StandardCharsets.UTF_8
                );

        if (lines.size() <= 1) {

            return result;
        }

        String header =
                lines.get(0);

        if (
                header == null
                        ||
                        !header.contains("构建时间")
                        ||
                        !header.contains("状态")
                        ||
                        !header.contains("应用名称")
        ) {

            throw new IOException(
                    "不是有效的 PythonForge 构建历史 CSV 文件。"
            );
        }

        for (
                int i = 1;
                i < lines.size();
                i++
        ) {

            String line =
                    lines.get(i);

            if (
                    line == null
                            ||
                            line.isBlank()
            ) {

                continue;
            }

            try {

                List<String> fields =
                        parseCsvLine(
                                line
                        );

                if (fields.size() < 9) {

                    continue;
                }

                BuildHistoryRecord record =
                        createHistoryRecord(
                                fields
                        );

                if (record != null) {

                    result.add(
                            record
                    );
                }

            } catch (Exception ignored) {

                /*
                 * 当前记录格式错误时跳过。
                 */
            }
        }

        return result;
    }

    private List<String> parseCsvLine(
            String line) {

        List<String> fields =
                new ArrayList<>();

        StringBuilder current =
                new StringBuilder();

        boolean quoted = false;

        for (int i = 0; i < line.length(); i++) {

            char c =
                    line.charAt(i);

            if (c == '"') {

                /*
                 * CSV 中两个连续双引号
                 * 表示一个双引号。
                 */
                if (
                        quoted
                                &&
                                i + 1 < line.length()
                                &&
                                line.charAt(i + 1) == '"'
                ) {

                    current.append('"');

                    i++;

                } else {

                    quoted = !quoted;
                }

            } else if (
                    c == ','
                            &&
                            !quoted
            ) {

                fields.add(
                        current.toString()
                );

                current.setLength(0);

            } else {

                current.append(c);
            }
        }

        fields.add(
                current.toString()
        );

        return fields;
    }

    private BuildHistoryRecord createHistoryRecord(
            List<String> fields) {

        if (
                fields == null
                        ||
                        fields.size() < 9
        ) {

            return null;
        }

        LocalDateTime time =
                parseDateTime(
                        fields.get(0)
                );

        BuildStatus status =
                parseBuildStatus(
                        fields.get(1)
                );

        if (time == null || status == null) {

            return null;
        }

        return new BuildHistoryRecord(

                time,

                status,

                emptyToNull(
                        fields.get(2)
                ),

                pathFromString(
                        fields.get(4)
                ),

                pathFromString(
                        fields.get(5)
                ),

                pathFromString(
                        fields.get(6)
                ),

                emptyToNull(
                        fields.get(3)
                ),

                pathFromString(
                        fields.get(7)
                ),

                emptyToNull(
                        fields.get(8)
                )
        );
    }

    private LocalDateTime parseDateTime(
            String value) {

        if (
                value == null
                        ||
                        value.isBlank()
        ) {

            return null;
        }

        try {

            return LocalDateTime.parse(
                    value.trim()
            );

        } catch (Exception e) {

            return null;
        }
    }

    private Path pathFromString(
            String value) {

        if (
                value == null
                        ||
                        value.isBlank()
                        ||
                        "-".equals(value)
        ) {

            return null;
        }

        try {

            return Path.of(
                    value
            );

        } catch (Exception e) {

            return null;
        }
    }

    private String emptyToNull(
            String value) {

        if (
                value == null
                        ||
                        value.isBlank()
                        ||
                        "-".equals(value)
        ) {

            return null;
        }

        return value;
    }

    private BuildStatus parseBuildStatus(
            String value) {

        if (
                value == null
                        ||
                        value.isBlank()
        ) {

            return null;
        }

        String status =
                value.trim();

        switch (status) {

            case "成功":
                return BuildStatus.SUCCESS;

            case "失败":
                return BuildStatus.FAILED;

            case "已取消":
                return BuildStatus.CANCELLED;

            default:

                try {

                    return BuildStatus.valueOf(
                            status
                    );

                } catch (Exception e) {

                    return null;
                }
        }
    }



}