package com.pythonforge.service;

import com.pythonforge.model.PythonEnvironment;
import com.pythonforge.util.LogUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Python 环境服务。
 *
 * <p>
 * 为 JavaFX UI 提供 Python 环境检测、
 * 查询、选择以及刷新功能。
 * </p>
 *
 * <p>
 * 当前版本：
 * PF-W Alpha.1.1.1-20260804
 * </p>
 */
public final class PythonEnvironmentService {

    private static final Logger LOGGER =
            LogUtils.getLogger(
                    PythonEnvironmentService.class
            );

    private final PythonEnvironmentManager manager;

    /**
     * 默认构造。
     */
    public PythonEnvironmentService() {

        this.manager =
                new PythonEnvironmentManager();
    }

    /**
     * 指定 Manager。
     *
     * @param manager Python 环境管理器
     */
    public PythonEnvironmentService(
            PythonEnvironmentManager manager) {

        if (manager == null) {

            throw new IllegalArgumentException(
                    "manager must not be null"
            );
        }

        this.manager =
                manager;
    }

    /**
     * 检测 Python 环境。
     *
     * @return Python 环境列表
     */
    public List<PythonEnvironment> detect() {

        LogUtils.info(
                LOGGER,
                "Detecting Python environments..."
        );

        return manager.refresh();
    }

    /**
     * 获取所有 Python 环境。
     *
     * @return Python 环境列表
     */
    public List<PythonEnvironment>
    getEnvironments() {

        return manager.getEnvironments();
    }

    /**
     * 获取当前 Python 环境。
     *
     * @return 当前 Python 环境
     */
    public Optional<PythonEnvironment>
    getCurrentEnvironment() {

        return manager.getCurrentEnvironment();
    }

    /**
     * 获取保存的 Python 路径。
     *
     * @return Python 路径
     */
    public Optional<String>
    getSavedPythonPath() {

        return manager.getSavedPythonPath();
    }

    /**
     * 设置当前 Python 环境。
     *
     * @param environment Python 环境
     */
    public void setCurrentEnvironment(
            PythonEnvironment environment) {

        manager.setCurrentEnvironment(
                environment
        );
    }

    /**
     * 根据路径设置当前 Python。
     *
     * @param executable Python executable
     * @return 是否成功
     */
    public boolean setCurrentEnvironment(
            Path executable) {

        if (executable == null) {

            return false;
        }

        Optional<PythonEnvironment> environment =
                manager.find(executable);

        if (environment.isEmpty()) {

            return false;
        }

        manager.setCurrentEnvironment(
                environment.get()
        );

        return true;
    }

    /**
     * 清除当前 Python。
     */
    public void clearCurrentEnvironment() {

        manager.clearCurrentEnvironment();
    }

    /**
     * 获取 Python 管理器。
     *
     * <p>
     * 主要供后续构建流程使用。
     * </p>
     *
     * @return Python 环境管理器
     */
    public PythonEnvironmentManager
    getManager() {

        return manager;
    }
}
