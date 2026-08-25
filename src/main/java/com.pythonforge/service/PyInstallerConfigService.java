package com.pythonforge.service;

import com.pythonforge.model.PyInstallerBuildConfig;
import com.pythonforge.model.PythonProject;

import java.nio.file.Path;
import java.util.List;

/**
 * PyInstaller 配置服务。
 */
public final class PyInstallerConfigService {

    private final PyInstallerConfigValidator
            validator =
            new PyInstallerConfigValidator();

    /**
     * 创建默认配置。
     *
     * @param project Python 项目
     * @return 默认配置
     */
    public PyInstallerBuildConfig createDefault(
            PythonProject project) {

        if (project == null) {

            throw new IllegalArgumentException(
                    "Python project cannot be null."
            );
        }

        PyInstallerBuildConfig config =
                PyInstallerBuildConfig.defaults();

        Path entry =
                project.getEntryFile();

        config.setEntryFile(
                entry
        );

        String fileName =
                project.getFileName();

        if (
                fileName != null
                        &&
                        !fileName.isBlank()
        ) {

            int dot =
                    fileName.lastIndexOf('.');

            if (dot > 0) {

                fileName =
                        fileName.substring(
                                0,
                                dot
                        );
            }

            config.setName(
                    fileName
            );
        } else {

            config.setName(
                    "PythonApplication"
            );
        }

        Path projectDirectory =
                project.getProjectDirectory();

        Path buildDirectory =
                projectDirectory
                        .resolve("build");

        Path distDirectory =
                projectDirectory
                        .resolve("dist");

        Path specDirectory =
                projectDirectory
                        .resolve("spec");

        config.setWorkDirectory(
                buildDirectory
        );

        config.setOutputDirectory(
                distDirectory
        );

        config.setSpecDirectory(
                specDirectory
        );

        return config;
    }

    /**
     * 校验配置。
     */
    public List<String> validate(
            PyInstallerBuildConfig config) {

        return validator.validate(
                config
        );
    }

    /**
     * 判断配置是否有效。
     */
    public boolean isValid(
            PyInstallerBuildConfig config) {

        return validator.isValid(
                config
        );
    }
}