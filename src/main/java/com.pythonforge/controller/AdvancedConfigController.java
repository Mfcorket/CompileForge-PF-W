package com.pythonforge.controller;

import com.pythonforge.model.PyInstallerBuildConfig;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * PyInstaller 高级配置控制器。
 *
 * <p>
 * P3.9：
 * 负责编辑 PyInstallerBuildConfig 中的高级参数。
 * </p>
 */
public final class AdvancedConfigController {

    @FXML
    private ListView<String> hiddenImportsListView;

    @FXML
    private TextField hiddenImportField;

    @FXML
    private ListView<String> addDataListView;

    @FXML
    private TextField addDataField;

    @FXML
    private ListView<String> addBinaryListView;

    @FXML
    private TextField addBinaryField;

    @FXML
    private ListView<String> pathsListView;

    @FXML
    private TextField pathField;

    @FXML
    private ListView<String> excludeModulesListView;

    @FXML
    private TextField excludeModuleField;

    @FXML
    private ListView<String> collectSubmodulesListView;

    @FXML
    private TextField collectSubmodulesField;

    @FXML
    private ListView<String> collectDataListView;

    @FXML
    private TextField collectDataField;

    @FXML
    private ListView<String> collectBinariesListView;

    @FXML
    private TextField collectBinariesField;

    @FXML
    private ListView<String> collectAllListView;

    @FXML
    private TextField collectAllField;

    /**
     * 当前编辑的构建配置。
     */
    private PyInstallerBuildConfig config;

    /**
     * 是否点击了确定。
     */
    private boolean confirmed;

    @FXML
    private void initialize() {

        hiddenImportsListView.setItems(
                FXCollections.observableArrayList()
        );

        addDataListView.setItems(
                FXCollections.observableArrayList()
        );

        addBinaryListView.setItems(
                FXCollections.observableArrayList()
        );

        pathsListView.setItems(
                FXCollections.observableArrayList()
        );

        excludeModulesListView.setItems(
                FXCollections.observableArrayList()
        );

        collectSubmodulesListView.setItems(
                FXCollections.observableArrayList()
        );

        collectDataListView.setItems(
                FXCollections.observableArrayList()
        );

        collectBinariesListView.setItems(
                FXCollections.observableArrayList()
        );

        collectAllListView.setItems(
                FXCollections.observableArrayList()
        );
    }

    /**
     * 设置当前构建配置。
     *
     * @param config 构建配置
     */
    public void setConfig(
            PyInstallerBuildConfig config) {

        this.config = config;

        loadConfig();
    }

    /**
     * 获取构建配置。
     *
     * @return 构建配置
     */
    public PyInstallerBuildConfig getConfig() {

        return config;
    }

    /**
     * 判断用户是否点击确定。
     *
     * @return true 表示确定
     */
    public boolean isConfirmed() {

        return confirmed;
    }

    /**
     * 将 BuildConfig 中的数据加载到 UI。
     */
    private void loadConfig() {

        if (config == null) {
            return;
        }

        hiddenImportsListView.getItems().setAll(
                config.getHiddenImports()
        );

        addDataListView.getItems().setAll(
                config.getDataFiles()
        );

        addBinaryListView.getItems().setAll(
                config.getBinaries()
        );

        pathsListView.getItems().setAll(
                config.getPaths()
        );

        excludeModulesListView.getItems().setAll(
                config.getExcludes()
        );

        collectSubmodulesListView.getItems().setAll(
                config.getCollectSubmodules()
        );

        collectDataListView.getItems().setAll(
                config.getCollectData()
        );

        collectBinariesListView.getItems().setAll(
                config.getCollectBinaries()
        );

        collectAllListView.getItems().setAll(
                config.getCollectAll()
        );
    }

    /**
     * 将 UI 数据保存回 BuildConfig。
     */
    private void saveConfig() {

        if (config == null) {
            return;
        }

        /*
         * Hidden imports
         */
        config.clearHiddenImports();

        for (String value :
                hiddenImportsListView.getItems()) {

            config.addHiddenImport(value);
        }

        /*
         * Add data
         */
        config.clearDataFiles();

        for (String value :
                addDataListView.getItems()) {

            config.addDataFile(value);
        }

        /*
         * Add binary
         */
        config.clearBinaries();

        for (String value :
                addBinaryListView.getItems()) {

            config.addBinary(value);
        }

        /*
         * Paths
         */
        config.clearPaths();

        for (String value :
                pathsListView.getItems()) {

            config.addPath(value);
        }

        /*
         * Excludes
         */
        config.clearExcludes();

        for (String value :
                excludeModulesListView.getItems()) {

            config.addExclude(value);
        }

        /*
         * Collect submodules
         */
        config.clearCollectSubmodules();

        for (String value :
                collectSubmodulesListView.getItems()) {

            config.addCollectSubmodule(value);
        }

        /*
         * Collect data
         */
        config.clearCollectData();

        for (String value :
                collectDataListView.getItems()) {

            config.addCollectData(value);
        }

        /*
         * Collect binaries
         */
        config.clearCollectBinaries();

        for (String value :
                collectBinariesListView.getItems()) {

            config.addCollectBinary(value);
        }

        /*
         * Collect all
         */
        config.clearCollectAll();

        for (String value :
                collectAllListView.getItems()) {

            config.addCollectAll(value);
        }
    }

    @FXML
    private void handleAddHiddenImport() {

        add(
                hiddenImportField,
                hiddenImportsListView
        );
    }

    @FXML
    private void handleRemoveHiddenImport() {

        remove(
                hiddenImportsListView
        );
    }

    @FXML
    private void handleAddData() {

        add(
                addDataField,
                addDataListView
        );
    }

    @FXML
    private void handleRemoveData() {

        remove(
                addDataListView
        );
    }

    @FXML
    private void handleAddBinary() {

        add(
                addBinaryField,
                addBinaryListView
        );
    }

    @FXML
    private void handleRemoveBinary() {

        remove(
                addBinaryListView
        );
    }

    @FXML
    private void handleAddPath() {

        add(
                pathField,
                pathsListView
        );
    }

    @FXML
    private void handleRemovePath() {

        remove(
                pathsListView
        );
    }

    @FXML
    private void handleAddExcludeModule() {

        add(
                excludeModuleField,
                excludeModulesListView
        );
    }

    @FXML
    private void handleRemoveExcludeModule() {

        remove(
                excludeModulesListView
        );
    }

    @FXML
    private void handleAddCollectSubmodules() {

        add(
                collectSubmodulesField,
                collectSubmodulesListView
        );
    }

    @FXML
    private void handleRemoveCollectSubmodules() {

        remove(
                collectSubmodulesListView
        );
    }

    @FXML
    private void handleAddCollectData() {

        add(
                collectDataField,
                collectDataListView
        );
    }

    @FXML
    private void handleRemoveCollectData() {

        remove(
                collectDataListView
        );
    }

    @FXML
    private void handleAddCollectBinaries() {

        add(
                collectBinariesField,
                collectBinariesListView
        );
    }

    @FXML
    private void handleRemoveCollectBinaries() {

        remove(
                collectBinariesListView
        );
    }

    @FXML
    private void handleAddCollectAll() {

        add(
                collectAllField,
                collectAllListView
        );
    }

    @FXML
    private void handleRemoveCollectAll() {

        remove(
                collectAllListView
        );
    }

    /**
     * 添加项目。
     */
    private void add(
            TextField field,
            ListView<String> listView) {

        String value =
                field.getText();

        if (value == null) {
            return;
        }

        value = value.trim();

        if (value.isEmpty()) {
            return;
        }

        if (!listView.getItems().contains(value)) {

            listView.getItems().add(value);
        }

        field.clear();
    }

    /**
     * 删除选中项目。
     */
    private void remove(
            ListView<String> listView) {

        int index =
                listView
                        .getSelectionModel()
                        .getSelectedIndex();

        if (index >= 0) {

            listView.getItems()
                    .remove(index);
        }
    }

    /**
     * 确定。
     */
    @FXML
    private void handleConfirm() {

        saveConfig();

        confirmed = true;

        close();
    }

    /**
     * 取消。
     */
    @FXML
    private void handleCancel() {

        confirmed = false;

        close();
    }

    /**
     * 关闭窗口。
     */
    private void close() {

        Stage stage =
                (Stage)
                        hiddenImportsListView
                                .getScene()
                                .getWindow();

        stage.close();
    }
}