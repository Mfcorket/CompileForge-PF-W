package com.pythonforge.service;

import com.pythonforge.model.PythonEnvironment;
import com.pythonforge.util.LogUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;
import java.util.logging.Logger;

/**
 * Python 环境管理器。
 *
 * <p>
 * 负责管理 PythonDetector 检测到的 Python 环境，
 * 并维护 PythonForge-win 当前使用的 Python 环境。
 * </p>
 *
 * <p>
 * 当前版本：
 * PF-W Alpha.1.1.1-20260804
 * </p>
 */
public final class PythonEnvironmentManager {

    private static final Logger LOGGER =
            LogUtils.getLogger(
                    PythonEnvironmentManager.class
            );

    /**
     * Java Preferences。
     *
     * <p>
     * 用于保存用户上一次选择的 Python 路径。
     * </p>
     */
    private static final Preferences PREFERENCES =
            Preferences.userNodeForPackage(
                    PythonEnvironmentManager.class
            );

    /**
     * 保存 Python 路径的 Key。
     */
    private static final String KEY_PYTHON_PATH =
            "currentPythonPath";

    /**
     * Python 检测器。
     */
    private final PythonDetector detector;

    /**
     * 检测到的 Python 环境。
     */
    private final List<PythonEnvironment> environments =
            new ArrayList<>();

    /**
     * 当前 Python 环境。
     */
    private PythonEnvironment currentEnvironment;

    /**
     * 默认构造。
     */
    public PythonEnvironmentManager() {

        this.detector =
                new PythonDetector();
    }

    /**
     * 指定检测器。
     *
     * @param detector Python 检测器
     */
    public PythonEnvironmentManager(
            PythonDetector detector) {

        if (detector == null) {

            throw new IllegalArgumentException(
                    "detector must not be null"
            );
        }

        this.detector =
                detector;
    }

    // =========================================================
    // Python 环境检测
    // =========================================================

    /**
     * 刷新 Python 环境。
     *
     * @return 检测到的 Python 环境
     */
    public List<PythonEnvironment> refresh() {

        environments.clear();

        List<PythonEnvironment> detected =
                detector.detect();

        if (detected != null) {

            environments.addAll(
                    detected
            );
        }

        sortEnvironments();

        restoreSavedEnvironment();

        if (currentEnvironment == null) {

            selectDefaultEnvironment();
        }

        LogUtils.info(
                LOGGER,
                "Python environments refreshed: "
                        + environments.size()
        );

        return getEnvironments();
    }

    /**
     * 获取所有 Python 环境。
     *
     * @return Python 环境列表
     */
    public List<PythonEnvironment> getEnvironments() {

        return List.copyOf(
                environments
        );
    }

    /**
     * 是否存在 Python 环境。
     *
     * @return true 表示存在
     */
    public boolean hasEnvironment() {

        return !environments.isEmpty();
    }

    // =========================================================
    // 当前 Python
    // =========================================================

    /**
     * 获取当前 Python 环境。
     *
     * <p>
     * 使用 Optional 避免当前没有 Python
     * 时出现 NullPointerException。
     * </p>
     *
     * @return 当前 Python 环境
     */
    public Optional<PythonEnvironment>
    getCurrentEnvironment() {

        return Optional.ofNullable(
                currentEnvironment
        );
    }



    /**
     * 设置当前 Python 环境。
     *
     * <p>
     * PythonEnvironmentService 可能会根据检测结果
     * 创建新的 PythonEnvironment 对象，因此这里不能
     * 强制要求传入对象必须与 environments 列表中的
     * 对象是同一个实例。
     * </p>
     *
     * <p>
     * 当前版本真正用于判断 Python 环境身份的是
     * Python executable 路径。
     * </p>
     *
     * @param environment Python 环境
     */
    public void setCurrentEnvironment(
            PythonEnvironment environment) {

        if (environment == null) {

            clearCurrentEnvironment();

            return;
        }

        /*
         * 根据 executable 路径寻找当前管理器中的环境。
         */
        Optional<PythonEnvironment> managedEnvironment =
                find(
                        environment.getExecutable()
                );

        if (managedEnvironment.isPresent()) {

            /*
             * 使用管理器中已经存在的对象。
             */
            currentEnvironment =
                    managedEnvironment.get();

        } else {

            /*
             * Service 层可能传入的是一个新的
             * PythonEnvironment 对象。
             *
             * 如果 executable 是有效文件，
             * 则允许将它加入当前管理器。
             */
            Path executable =
                    environment.getExecutable();

            if (
                    executable == null
                            ||
                            !java.nio.file.Files.isRegularFile(
                                    executable
                            )
            ) {

                throw new IllegalArgumentException(
                        "Python executable does not exist: "
                                + executable
                );
            }

            environments.add(
                    environment
            );

            sortEnvironments();

            currentEnvironment =
                    environment;
        }

        /*
         * 保存当前 Python 路径。
         */
        savePythonPath(
                currentEnvironment.getExecutable()
        );

        LogUtils.info(
                LOGGER,
                "Current Python set to: "
                        + currentEnvironment.getExecutable()
        );
    }


    /**
     * 清除当前 Python 环境。
     */
    public void clearCurrentEnvironment() {

        currentEnvironment =
                null;

        PREFERENCES.remove(
                KEY_PYTHON_PATH
        );

        LogUtils.info(
                LOGGER,
                "Current Python environment cleared."
        );
    }

    // =========================================================
    // 兼容旧调用
    // =========================================================

    /**
     * 获取当前选中的 Python。
     *
     * @return 当前 Python
     */
    public Optional<PythonEnvironment>
    getSelectedEnvironment() {

        return getCurrentEnvironment();
    }

    /**
     * 设置当前 Python。
     *
     * @param environment Python 环境
     */
    public void selectEnvironment(
            PythonEnvironment environment) {

        setCurrentEnvironment(
                environment
        );
    }

    /**
     * 根据路径选择 Python。
     *
     * @param executable Python executable
     * @return 是否成功
     */
    public boolean selectEnvironment(
            Path executable) {

        if (executable == null) {

            return false;
        }

        Optional<PythonEnvironment> environment =
                find(executable);

        if (environment.isEmpty()) {

            return false;
        }

        setCurrentEnvironment(
                environment.get()
        );

        return true;
    }

    // =========================================================
    // Python 路径
    // =========================================================

    /**
     * 获取保存的 Python 路径。
     *
     * <p>
     * 使用 String 是为了与当前 UI / 配置层保持一致。
     * </p>
     *
     * @return 保存的 Python 路径
     */
    public Optional<String>
    getSavedPythonPath() {

        String value =
                PREFERENCES.get(
                        KEY_PYTHON_PATH,
                        null
                );

        if (
                value == null
                        ||
                        value.isBlank()
        ) {

            return Optional.empty();
        }

        return Optional.of(
                value
        );
    }

    /**
     * 保存 Python 路径。
     *
     * @param executable Python executable
     */
    private void savePythonPath(
            Path executable) {

        if (executable == null) {

            PREFERENCES.remove(
                    KEY_PYTHON_PATH
            );

            return;
        }

        Path normalized =
                executable
                        .toAbsolutePath()
                        .normalize();

        PREFERENCES.put(
                KEY_PYTHON_PATH,
                normalized.toString()
        );
    }

    /**
     * 恢复之前保存的 Python 环境。
     */
    private void restoreSavedEnvironment() {

        Optional<String> savedPath =
                getSavedPythonPath();

        if (savedPath.isEmpty()) {

            return;
        }

        try {

            Path path =
                    Path.of(
                            savedPath.get()
                    );

            Optional<PythonEnvironment> environment =
                    find(path);

            if (environment.isPresent()) {

                currentEnvironment =
                        environment.get();

                LogUtils.info(
                        LOGGER,
                        "Restored saved Python: "
                                + savedPath.get()
                );

            } else {

                LogUtils.info(
                        LOGGER,
                        "Saved Python is no longer available: "
                                + savedPath.get()
                );
            }

        } catch (Exception e) {

            LogUtils.warning(
                    LOGGER,
                    "Failed to restore saved Python: "
                            + e.getMessage()
            );
        }
    }

    // =========================================================
    // 查找
    // =========================================================

    /**
     * 根据 Python executable 查找环境。
     *
     * @param executable Python executable
     * @return Python 环境
     */
    public Optional<PythonEnvironment>
    find(Path executable) {

        if (executable == null) {

            return Optional.empty();
        }

        Path normalized =
                executable
                        .toAbsolutePath()
                        .normalize();

        for (
                PythonEnvironment environment
                : environments
        ) {

            if (environment == null) {
                continue;
            }

            Path environmentExecutable =
                    environment
                            .getExecutable()
                            .toAbsolutePath()
                            .normalize();

            if (
                    environmentExecutable.equals(
                            normalized
                    )
            ) {

                return Optional.of(
                        environment
                );
            }
        }

        return Optional.empty();
    }

    /**
     * 判断环境是否属于当前管理器。
     */
    private boolean containsEnvironment(
            PythonEnvironment environment) {

        if (environment == null) {

            return false;
        }

        return find(
                environment.getExecutable()
        ).isPresent();
    }

    // =========================================================
    // 默认 Python
    // =========================================================

    /**
     * 获取默认 Python。
     *
     * @return 默认 Python
     */
    public Optional<PythonEnvironment>
    getDefaultEnvironment() {

        if (environments.isEmpty()) {

            return Optional.empty();
        }

        return Optional.of(
                environments.get(0)
        );
    }

    /**
     * 选择默认 Python。
     */
    private void selectDefaultEnvironment() {

        if (environments.isEmpty()) {

            currentEnvironment =
                    null;

            return;
        }

        currentEnvironment =
                environments.get(0);

        savePythonPath(
                currentEnvironment
                        .getExecutable()
        );

        LogUtils.info(
                LOGGER,
                "Default Python selected: "
                        + currentEnvironment
                        .getExecutable()
        );
    }

    // =========================================================
    // 排序
    // =========================================================

    /**
     * Python 版本降序排列。
     */
    private void sortEnvironments() {

        environments.sort(
                Comparator.comparing(
                        PythonEnvironmentManager::
                                versionKey,
                        Comparator.reverseOrder()
                )
        );
    }

    /**
     * 创建版本排序 Key。
     */
    private static String versionKey(
            PythonEnvironment environment) {

        if (
                environment == null
                        ||
                        environment.getVersion() == null
        ) {

            return "000000000";
        }

        String version =
                environment
                        .getVersion()
                        .trim();

        if (version.isEmpty()) {

            return "000000000";
        }

        String[] parts =
                version.split("\\.");

        StringBuilder key =
                new StringBuilder();

        for (String part : parts) {

            String number =
                    part.replaceAll(
                            "[^0-9]",
                            ""
                    );

            if (number.isEmpty()) {

                number = "0";
            }

            try {

                int value =
                        Integer.parseInt(
                                number
                        );

                key.append(
                        String.format(
                                "%03d",
                                value
                        )
                );

            } catch (
                    NumberFormatException e
            ) {

                key.append(
                        "000"
                );
            }
        }

        while (key.length() < 9) {

            key.append("000");
        }

        return key.toString();
    }

    // =========================================================
    // 清理
    // =========================================================

    /**
     * 清空当前检测结果。
     */
    public void clear() {

        environments.clear();

        currentEnvironment =
                null;
    }
}
