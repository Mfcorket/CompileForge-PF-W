package com.pythonforge.service;

import com.pythonforge.model.PythonEnvironment;
import com.pythonforge.model.PythonProject;
import com.pythonforge.util.LogUtils;
import com.pythonforge.util.PythonFileUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Python 项目服务。
 *
 * <p>
 * 负责验证用户选择的 Python 项目，
 * 并创建 PythonProject。
 * </p>
 */
public final class PythonProjectService {

    private static final Logger LOGGER =
            LogUtils.getLogger(
                    PythonProjectService.class
            );

    private final PythonEnvironmentService
            environmentService;

    /**
     * 默认构造。
     */
    public PythonProjectService() {

        this.environmentService =
                new PythonEnvironmentService();
    }

    /**
     * 使用指定 Python 环境服务。
     *
     * @param environmentService Python 环境服务
     */
    public PythonProjectService(
            PythonEnvironmentService
                    environmentService) {

        if (environmentService == null) {

            throw new IllegalArgumentException(
                    "environmentService must not be null"
            );
        }

        this.environmentService =
                environmentService;
    }

    /**
     * 分析 Python 文件。
     *
     * @param file Python 文件
     * @return 项目
     */
    public Optional<PythonProject> analyze(
            Path file) {

        if (file == null) {

            LogUtils.warning(
                    LOGGER,
                    "Python project file is null."
            );

            return Optional.empty();
        }

        Path normalized;

        try {

            normalized =
                    file
                            .toAbsolutePath()
                            .normalize();

        } catch (Exception e) {

            LogUtils.warning(
                    LOGGER,
                    "Invalid Python project path: "
                            + file
            );

            return Optional.empty();
        }

        if (!Files.isRegularFile(normalized)) {

            LogUtils.warning(
                    LOGGER,
                    "Python project file does not exist: "
                            + normalized
            );

            return Optional.empty();
        }

        if (
                !PythonFileUtils
                        .isPythonFile(
                                normalized
                        )
        ) {

            LogUtils.warning(
                    LOGGER,
                    "Unsupported Python file: "
                            + normalized
            );

            return Optional.empty();
        }

        Optional<PythonEnvironment>
                current =
                environmentService
                        .getCurrentEnvironment();

        if (current.isEmpty()) {

            LogUtils.warning(
                    LOGGER,
                    "No Python environment selected."
            );

            return Optional.empty();
        }

        PythonEnvironment environment =
                current.get();

        Path parent =
                normalized.getParent();

        if (parent == null) {

            return Optional.empty();
        }

        boolean source =
                PythonFileUtils
                        .isPythonSource(
                                normalized
                        );

        boolean bytecode =
                PythonFileUtils
                        .isPythonBytecode(
                                normalized
                        );

        PythonProject project =
                new PythonProject(
                        normalized,
                        parent,
                        environment,
                        source,
                        bytecode
                );

        LogUtils.info(
                LOGGER,
                "Python project analyzed: "
                        + project
        );

        return Optional.of(project);
    }

    /**
     * 获取当前 Python 环境。
     *
     * @return 当前环境
     */
    public Optional<PythonEnvironment>
    getCurrentEnvironment() {

        return environmentService
                .getCurrentEnvironment();
    }
}