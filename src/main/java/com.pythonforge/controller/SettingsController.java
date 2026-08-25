package com.pythonforge.controller;

import com.pythonforge.model.LanguageOption;
import com.pythonforge.model.PythonEnvironment;
import com.pythonforge.service.LanguageManager;
import com.pythonforge.service.SettingsService;
import com.pythonforge.service.PythonDetector;
import com.pythonforge.service.ThemeManager;
import com.pythonforge.model.ThemeType;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.application.Platform;
import javafx.scene.Scene;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * PythonForge-win 设置中心控制器。
 *
 * <p>
 * P3.5：
 * </p>
 *
 * <ul>
 *     <li>建立设置中心 UI</li>
 *     <li>建立设置分类导航</li>
 *     <li>接入语言设置</li>
 *     <li>接入 Python 环境显示</li>
 *     <li>预留主题设置</li>
 *     <li>预留默认构建设置</li>
 * </ul>
 */
public class SettingsController
        implements Initializable {

    /*
     * ============================================================
     * 页面导航
     * ============================================================
     */

    @FXML
    private VBox generalPane;

    @FXML
    private VBox appearancePane;

    @FXML
    private VBox pythonPane;

    @FXML
    private VBox buildPane;

    @FXML
    private Button generalButton;

    @FXML
    private Button appearanceButton;

    @FXML
    private Button pythonButton;

    @FXML
    private Button buildButton;


    /*
     * ============================================================
     * 页面标题
     * ============================================================
     */

    @FXML
    private Label settingsTitleLabel;

    @FXML
    private Label pageTitleLabel;

    @FXML
    private Label pageDescriptionLabel;


    /*
     * ============================================================
     * 常规 / 语言
     * ============================================================
     */

    @FXML
    private Label languageTitleLabel;

    @FXML
    private Label languageDescriptionLabel;

    @FXML
    private ComboBox<LanguageOption>
            languageComboBox;

    @FXML
    private Label languageRestartLabel;


    /*
     * ============================================================
     * 外观
     * ============================================================
     */

    @FXML
    private Label appearanceTitleLabel;

    @FXML
    private Label appearanceDescriptionLabel;

    @FXML
    private Label themeTitleLabel;

    @FXML
    private Label themeDescriptionLabel;

    @FXML
    private ComboBox<String>
            themeComboBox;

    @FXML
    private Label customThemeHintLabel;

    @FXML
    private Label customThemeDescriptionLabel;


    /*
     * ============================================================
     * Python
     * ============================================================
     */

    @FXML
    private Label pythonTitleLabel;

    @FXML
    private Label pythonDescriptionLabel;

    @FXML
    private Label pythonEnvironmentTitleLabel;

    @FXML
    private Label pythonEnvironmentDescriptionLabel;

    @FXML
    private ComboBox<PythonEnvironment>
            pythonEnvironmentComboBox;

    @FXML
    private Button refreshPythonButton;

    @FXML
    private Label pythonEnvironmentStatusLabel;


    /*
     * ============================================================
     * 构建
     * ============================================================
     */

    @FXML
    private Label buildSettingsTitleLabel;

    @FXML
    private Label buildSettingsDescriptionLabel;

    @FXML
    private Label buildDefaultsTitleLabel;

    @FXML
    private Label buildDefaultsDescriptionLabel;

    @FXML
    private TextArea buildSettingsInfoArea;


    /*
     * ============================================================
     * 底部操作
     * ============================================================
     */

    @FXML
    private Label saveStatusLabel;

    @FXML
    private Button cancelButton;

    @FXML
    private Button saveButton;


    /*
     * ============================================================
     * 服务
     * ============================================================
     */

    private final SettingsService settingsService =
            new SettingsService();

    private final LanguageManager languageManager =
            new LanguageManager(
                    settingsService
            );

    private final PythonDetector pythonDetector =
            new PythonDetector();

    private final ThemeManager themeManager =
            ThemeManager.getInstance();

    /*
     * ============================================================
     * Theme
     * ============================================================
     */

    private static final String LIGHT_THEME =
            "/css/light.css";

    private static final String DARK_THEME =
            "/css/dark.css";


    /*
     * ============================================================
     * 临时设置
     * ============================================================
     */

    /**
     * 当前页面选择的语言。
     *
     * <p>
     * 在用户点击保存之前，
     * 不直接修改持久化设置。
     * </p>
     */
    private LanguageOption pendingLanguage;

    /**
     * 进入设置页面时的原始主题。
     *
     * <p>
     * 用于点击“取消”时恢复。
     * </p>
     */
    private ThemeType originalThemeType;


    /**
     * 初始化。
     */
    @Override
    public void initialize(
            URL location,
            ResourceBundle resources) {

        initializeControls();

        loadSettings();

        registerLanguageListener();

        updateTexts();

        showGeneralPage();

        detectPythonInBackground();
    }

    /**
     * 初始化设置页面控件。
     */
    private void initializeControls() {

        /*
         * 初始化语言选择框。
         */
        initializeLanguageComboBox();

        /*
         * 初始化主题选择框。
         */
        initializeThemeComboBox();

        /*
         * 初始化 Python 环境选择框。
         */
        initializePythonComboBox();

        /*
         * 默认保存状态为空。
         */
        if (saveStatusLabel != null) {

            saveStatusLabel.setText("");
        }

        /*
         * 默认 Python 状态。
         */
        if (pythonEnvironmentStatusLabel != null) {

            pythonEnvironmentStatusLabel.setText(
                    "正在准备 Python 环境检测..."
            );
        }
    }

    /**
     * 加载当前持久化设置。
     *
     * <p>
     * 作为 SettingsController 对外的统一设置加载入口。
     * </p>
     */
    public void loadSettings() {

        loadCurrentSettings();
    }


    /*
     * ============================================================
     * Language
     * ============================================================
     */

    /**
     * 初始化语言下拉框。
     */
    private void initializeLanguageComboBox() {

        languageComboBox.setItems(
                FXCollections.observableArrayList(
                        LanguageOption.values()
                )
        );

        languageComboBox.setCellFactory(
                listView ->
                        new ListCell<>() {

                            @Override
                            protected void updateItem(
                                    LanguageOption item,
                                    boolean empty) {

                                super.updateItem(
                                        item,
                                        empty
                                );

                                if (empty
                                        || item == null) {

                                    setText(null);

                                } else {

                                    setText(
                                            getLanguageDisplayName(
                                                    item
                                            )
                                    );
                                }
                            }
                        }
        );

        languageComboBox.setButtonCell(
                new ListCell<>() {

                    @Override
                    protected void updateItem(
                            LanguageOption item,
                            boolean empty) {

                        super.updateItem(
                                item,
                                empty
                        );

                        if (empty
                                || item == null) {

                            setText(null);

                        } else {

                            setText(
                                    getLanguageDisplayName(
                                            item
                                    )
                            );
                        }
                    }
                }
        );

        languageComboBox
                .valueProperty()
                .addListener(
                        (observable,
                         oldValue,
                         newValue) -> {

                            if (newValue != null) {

                                pendingLanguage =
                                        newValue;
                            }
                        }
                );
    }


    /**
     * 获取语言显示名称。
     *
     * <p>
     * 这里不使用 emoji。
     * </p>
     */
    private String getLanguageDisplayName(
            LanguageOption language) {

        if (language == null) {

            return "";
        }

        return switch (language) {

            case ZH_CN ->
                    "简体中文";

            case EN_US ->
                    "English";

            case FR_FR ->
                    "Français";

            case DE_DE ->
                    "Deutsch";

            case ES_ES ->
                    "Español";
        };
    }


    /*
     * ============================================================
     * Theme
     * ============================================================
     */

    /**
     * 初始化主题选择框。
     *
     * <p>
     * P3.5 先建立 UI。
     * 实际主题应用将在后续主题阶段完成。
     * </p>
     */
    /**
     * 初始化主题选择框。
     */
    private void initializeThemeComboBox() {

        themeComboBox.setItems(
                FXCollections.observableArrayList(
                        "自动",
                        "亮色",
                        "暗色",
                        "自定义"
                )
        );


        themeComboBox
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable,
                         oldValue,
                         newValue) -> {

                            if (newValue == null) {

                                return;
                            }

                            applySelectedTheme(
                                    newValue
                            );
                        }
                );


        updateThemeComboBoxSelection();
    }

    /**
     * 应用主题。
     *
     * @param theme 主题名称
     */
    private void applyTheme(
            String theme) {

        /*
         * SettingsView 在 FXMLLoader.initialize()
         * 执行期间可能还没有 Scene。
         *
         * 因此这里使用 Platform.runLater，
         * 等节点进入 Scene 后再应用主题。
         */
        Platform.runLater(() -> {

            if (themeComboBox == null) {
                return;
            }

            Scene scene =
                    themeComboBox.getScene();

            if (scene == null) {

                /*
                 * 当前还没有 Scene。
                 *
                 * 注册 scene 监听器，
                 * 等进入 Scene 后再应用。
                 */
                themeComboBox
                        .sceneProperty()
                        .addListener(
                                (observable,
                                 oldScene,
                                 newScene) -> {

                                    if (newScene != null) {

                                        applyThemeToScene(
                                                newScene,
                                                theme
                                        );
                                    }
                                }
                        );

                return;
            }

            applyThemeToScene(
                    scene,
                    theme
            );
        });
    }

    /**
     * 将指定主题应用到 Scene。
     */
    private void applyThemeToScene(
            Scene scene,
            String theme) {

        if (scene == null) {
            return;
        }

        String stylesheet = null;

        switch (theme) {

            case "亮色" ->

                    stylesheet =
                            getClass()
                                    .getResource(
                                            LIGHT_THEME
                                    )
                                    .toExternalForm();

            case "暗色" ->

                    stylesheet =
                            getClass()
                                    .getResource(
                                            DARK_THEME
                                    )
                                    .toExternalForm();

            case "自动" -> {

                /*
                 * 当前阶段“自动”默认使用亮色。
                 *
                 * 后续可以接入 Windows 系统主题检测。
                 */
                stylesheet =
                        getClass()
                                .getResource(
                                        LIGHT_THEME
                                )
                                .toExternalForm();
            }

            case "自定义" -> {

                /*
                 * 自定义主题暂时没有真正的主题文件。
                 *
                 * 当前先保持亮色。
                 */
                stylesheet =
                        getClass()
                                .getResource(
                                        LIGHT_THEME
                                )
                                .toExternalForm();
            }

            default -> {

                stylesheet =
                        getClass()
                                .getResource(
                                        LIGHT_THEME
                                )
                                .toExternalForm();
            }
        }

        /*
         * 防止资源不存在时出现 NullPointerException。
         */
        if (stylesheet == null) {

            System.err.println(
                    "Theme stylesheet not found: "
                            + theme
            );

            return;
        }

        /*
         * 删除 PythonForge-win 自己的主题 CSS。
         *
         * 不删除其他第三方 CSS。
         */
        scene.getStylesheets()
                .removeIf(
                        css ->
                                css.endsWith(
                                        "/light.css"
                                )
                                        || css.endsWith(
                                        "/dark.css"
                                )
                );

        /*
         * 添加新的主题。
         */
        if (!scene.getStylesheets()
                .contains(stylesheet)) {

            scene.getStylesheets()
                    .add(stylesheet);
        }

        System.out.println(
                "Theme applied: "
                        + theme
                        + " -> "
                        + stylesheet
        );
    }


    /*
     * ============================================================
     * Python
     * ============================================================
     */

    /**
     * 初始化 Python 环境选择框。
     */
    private void initializePythonComboBox() {

        pythonEnvironmentComboBox.setCellFactory(
                listView ->
                        new ListCell<>() {

                            @Override
                            protected void updateItem(
                                    PythonEnvironment item,
                                    boolean empty) {

                                super.updateItem(
                                        item,
                                        empty
                                );

                                if (empty
                                        || item == null) {

                                    setText(null);

                                    return;
                                }

                                setText(
                                        formatPythonEnvironment(
                                                item
                                        )
                                );
                            }
                        }
        );

        pythonEnvironmentComboBox.setButtonCell(
                new ListCell<>() {

                    @Override
                    protected void updateItem(
                            PythonEnvironment item,
                            boolean empty) {

                        super.updateItem(
                                item,
                                empty
                        );

                        if (empty
                                || item == null) {

                            setText(null);

                            return;
                        }

                        setText(
                                formatPythonEnvironment(
                                        item
                                )
                        );
                    }
                }
        );
    }


    /**
     * 格式化 Python 环境。
     */
    private String formatPythonEnvironment(
            PythonEnvironment environment) {

        if (environment == null) {

            return "";
        }

        String version =
                environment.getVersion();

        if (version == null
                || version.isBlank()) {

            version = "Unknown";
        }

        if (environment.getExecutable()
                == null) {

            return version;
        }

        return "Python "
                + version
                + "  —  "
                + environment
                .getExecutable()
                .toString();
    }


    /**
     * 刷新 Python 环境。
     */
    private void refreshPythonEnvironments() {

        try {

            List<PythonEnvironment>
                    environments =
                    pythonDetector.detect();

            pythonEnvironmentComboBox.setItems(
                    FXCollections.observableArrayList(
                            environments
                    )
            );

            if (environments.isEmpty()) {

                pythonEnvironmentStatusLabel
                        .setText(
                                "未检测到 Python 环境。"
                        );

                return;
            }

            pythonEnvironmentStatusLabel
                    .setText(
                            "检测到 "
                                    + environments.size()
                                    + " 个 Python 环境。"
                    );

        } catch (Exception exception) {

            pythonEnvironmentStatusLabel
                    .setText(
                            "Python 环境检测失败："
                                    + exception
                                    .getMessage()
                    );
        }
    }


    /*
     * ============================================================
     * Settings
     * ============================================================
     */

    /**
     * 加载当前持久化设置。
     */
    private void loadCurrentSettings() {

        LanguageOption currentLanguage =
                languageManager
                        .getCurrentLanguage();

        if (currentLanguage == null) {

            currentLanguage =
                    LanguageOption.ZH_CN;
        }

        pendingLanguage =
                currentLanguage;

        languageComboBox
                .getSelectionModel()
                .select(
                        currentLanguage
                );


        /*
         * 保存进入设置页面时的主题。
         */
        originalThemeType =
                themeManager
                        .getConfig()
                        .getThemeType();


        updateThemeComboBoxSelection();
    }


    /*
     * ============================================================
     * Navigation
     * ============================================================
     */

    @FXML
    private void handleOpenGeneral() {

        showGeneralPage();
    }

    @FXML
    private void handleOpenAppearance() {

        showAppearancePage();
    }

    @FXML
    private void handleOpenPython() {

        showPythonPage();
    }

    @FXML
    private void handleOpenBuild() {

        showBuildPage();
    }


    /**
     * 显示常规页面。
     */
    private void showGeneralPage() {

        setPageVisible(
                generalPane,
                true
        );

        setPageVisible(
                appearancePane,
                false
        );

        setPageVisible(
                pythonPane,
                false
        );

        setPageVisible(
                buildPane,
                false
        );

        setSelectedNavigation(
                generalButton
        );
    }


    /**
     * 显示外观页面。
     */
    private void showAppearancePage() {

        setPageVisible(
                generalPane,
                false
        );

        setPageVisible(
                appearancePane,
                true
        );

        setPageVisible(
                pythonPane,
                false
        );

        setPageVisible(
                buildPane,
                false
        );

        setSelectedNavigation(
                appearanceButton
        );
    }


    /**
     * 显示 Python 页面。
     */
    private void showPythonPage() {

        setPageVisible(
                generalPane,
                false
        );

        setPageVisible(
                appearancePane,
                false
        );

        setPageVisible(
                pythonPane,
                true
        );

        setPageVisible(
                buildPane,
                false
        );

        setSelectedNavigation(
                pythonButton
        );
    }


    /**
     * 显示构建页面。
     */
    private void showBuildPage() {

        setPageVisible(
                generalPane,
                false
        );

        setPageVisible(
                appearancePane,
                false
        );

        setPageVisible(
                pythonPane,
                false
        );

        setPageVisible(
                buildPane,
                true
        );

        setSelectedNavigation(
                buildButton
        );
    }


    /**
     * 设置页面可见性。
     */
    private void setPageVisible(
            VBox page,
            boolean visible) {

        page.setVisible(
                visible
        );

        page.setManaged(
                visible
        );
    }


    /**
     * 设置左侧导航选中状态。
     */
    private void setSelectedNavigation(
            Button selected) {

        Button[] buttons = {

                generalButton,

                appearanceButton,

                pythonButton,

                buildButton
        };

        for (Button button : buttons) {

            button.getStyleClass()
                    .remove(
                            "settings-navigation-selected"
                    );
        }

        if (selected != null) {

            if (!selected.getStyleClass()
                    .contains(
                            "settings-navigation-selected"
                    )) {

                selected.getStyleClass()
                        .add(
                                "settings-navigation-selected"
                        );
            }
        }
    }


    /*
     * ============================================================
     * Python refresh
     * ============================================================
     */

    @FXML
    private void handleRefreshPython() {

        detectPythonInBackground();
    }


    /*
     * ============================================================
     * Save
     * ============================================================
     */

    @FXML
    private void handleSave() {

        if (pendingLanguage == null) {

            pendingLanguage =
                    LanguageOption.ZH_CN;
        }

        try {

            /*
             * 保存语言
             */
            languageManager
                    .setLanguageAndSave(
                            pendingLanguage
                    );


            /*
             * 保存主题
             */
            themeManager.save();


            saveStatusLabel.setText(
                    "设置已保存。"
            );

        } catch (IOException exception) {

            saveStatusLabel.setText(
                    "保存设置失败："
                            + exception.getMessage()
            );
        }
    }


    /*
     * ============================================================
     * Cancel
     * ============================================================
     */

    @FXML
    private void handleCancel() {

        /*
         * ========================================================
         * 恢复语言
         * ========================================================
         */

        LanguageOption currentLanguage =
                languageManager
                        .getCurrentLanguage();

        if (currentLanguage == null) {

            currentLanguage =
                    LanguageOption.ZH_CN;
        }

        pendingLanguage =
                currentLanguage;

        languageComboBox
                .getSelectionModel()
                .select(
                        currentLanguage
                );


        /*
         * ========================================================
         * 恢复主题
         * ========================================================
         */

        if (originalThemeType != null) {

            themeManager.setThemeType(
                    originalThemeType
            );
        }

        updateThemeComboBoxSelection();


        saveStatusLabel.setText(
                "已恢复当前设置。"
        );
    }


    /*
     * ============================================================
     * Language listener
     * ============================================================
     */

    /**
     * 注册语言变化监听器。
     */
    private void registerLanguageListener() {

        languageManager.addListener(
                (oldLanguage, newLanguage) -> {

                    if (newLanguage == null) {
                        return;
                    }

                    pendingLanguage =
                            newLanguage;

                    LanguageOption selectedLanguage =
                            languageComboBox
                                    .getSelectionModel()
                                    .getSelectedItem();

                    if (selectedLanguage != newLanguage) {

                        languageComboBox
                                .getSelectionModel()
                                .select(newLanguage);
                    }

                    updateTexts();
                }
        );
    }


    /*
     * ============================================================
     * UI 文本
     * ============================================================
     */

    /**
     * 更新设置页面文本。
     *
     * <p>
     * 当前先完成设置页面自身的国际化。
     * 后续会把更多 properties key 补充进去。
     * </p>
     */
    private void updateTexts() {

        settingsTitleLabel.setText(
                languageManager.get(
                        "settings.title"
                )
        );

        generalButton.setText(
                languageManager.get(
                        "settings.general"
                )
        );

        appearanceButton.setText(
                languageManager.get(
                        "settings.appearance"
                )
        );

        pythonButton.setText(
                languageManager.get(
                        "settings.python"
                )
        );

        buildButton.setText(
                languageManager.get(
                        "settings.build"
                )
        );

        languageTitleLabel.setText(
                languageManager.get(
                        "settings.language"
                )
        );

        themeTitleLabel.setText(
                languageManager.get(
                        "settings.theme"
                )
        );

        pythonEnvironmentTitleLabel.setText(
                languageManager.get(
                        "settings.python.environment"
                )
        );

        buildSettingsTitleLabel.setText(
                languageManager.get(
                        "settings.build"
                )
        );

        refreshPythonButton.setText(
                languageManager.get(
                        "settings.python.refresh"
                )
        );

        saveButton.setText(
                languageManager.get(
                        "common.save"
                )
        );

        cancelButton.setText(
                languageManager.get(
                        "common.cancel"
                )
        );
    }

    private void detectPythonInBackground() {

        if (pythonEnvironmentStatusLabel != null) {

            pythonEnvironmentStatusLabel.setText(
                    "正在检测 Python 环境..."
            );
        }

        if (refreshPythonButton != null) {

            refreshPythonButton.setDisable(true);
        }

        Task<List<PythonEnvironment>> task =
                new Task<>() {

                    @Override
                    protected List<PythonEnvironment> call() {

                        return pythonDetector.detect();
                    }
                };

        task.setOnSucceeded(event -> {

            if (refreshPythonButton != null) {

                refreshPythonButton.setDisable(false);
            }

            List<PythonEnvironment> environments =
                    task.getValue();

            updatePythonEnvironments(
                    environments
            );
        });

        task.setOnFailed(event -> {

            if (refreshPythonButton != null) {

                refreshPythonButton.setDisable(false);
            }

            if (pythonEnvironmentStatusLabel != null) {

                pythonEnvironmentStatusLabel.setText(
                        "Python 环境检测失败"
                );
            }

            Throwable exception =
                    task.getException();

            if (exception != null) {

                exception.printStackTrace();
            }
        });

        Thread thread =
                new Thread(
                        task,
                        "Python-Environment-Detection"
                );

        thread.setDaemon(true);

        thread.start();
    }

    private void updatePythonEnvironments(
            List<PythonEnvironment> environments) {

        pythonEnvironmentComboBox
                .getItems()
                .setAll(environments);

        if (environments.isEmpty()) {

            pythonEnvironmentStatusLabel.setText(
                    "未检测到 Python 环境"
            );

            return;
        }

        pythonEnvironmentStatusLabel.setText(
                "检测到 "
                        + environments.size()
                        + " 个 Python 环境"
        );
    }

    private ThemeType getThemeType(
            String themeName) {

        if (themeName == null) {

            return ThemeType.AUTO;
        }

        return switch (themeName) {

            case "亮色" ->
                    ThemeType.LIGHT;

            case "暗色" ->
                    ThemeType.DARK;

            case "自定义" ->
                    ThemeType.CUSTOM;

            case "自动" ->
                    ThemeType.AUTO;

            default ->
                    ThemeType.AUTO;
        };
    }

    private void applySelectedTheme(
            String themeName) {

        ThemeType themeType =
                getThemeType(
                        themeName
                );

        themeManager.setThemeType(
                themeType
        );
    }

    private void updateThemeComboBoxSelection() {

        ThemeType themeType =
                themeManager
                        .getConfig()
                        .getThemeType();

        if (themeType == null) {

            themeType =
                    ThemeType.AUTO;
        }

        String themeName =
                switch (themeType) {

                    case AUTO ->
                            "自动";

                    case LIGHT ->
                            "亮色";

                    case DARK ->
                            "暗色";

                    case CUSTOM ->
                            "自定义";
                };

        themeComboBox
                .getSelectionModel()
                .select(
                        themeName
                );
    }


}