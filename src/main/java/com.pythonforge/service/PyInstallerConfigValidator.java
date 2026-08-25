package com.pythonforge.service;

import com.pythonforge.model.PyInstallerBuildConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * PyInstaller 构建配置校验器。
 *
 * <p>
 * 当前版本针对 Windows + PyInstaller。
 * </p>
 */
public final class PyInstallerConfigValidator {

    /**
     * 校验配置。
     *
     * @param config 构建配置
     * @return 错误列表；为空表示配置有效
     */
    public List<String> validate(
            PyInstallerBuildConfig config) {

        List<String> errors =
                new ArrayList<>();

        if (config == null) {

            errors.add(
                    "构建配置不能为空。"
            );

            for (
                    String data
                    : config.getDataFiles()
            ) {

                if (
                        data == null
                                ||
                                data.isBlank()
                ) {

                    errors.add(
                            "存在空的数据文件配置。"
                    );

                    continue;
                }

                int separator =
                        data.indexOf(';');

                if (separator <= 0) {

                    errors.add(
                            "数据文件格式错误："
                                    + data
                                    + "\n正确格式：source;destination"
                    );

                    continue;
                }

                String source =
                        data.substring(
                                0,
                                separator
                        );

                if (
                        !Files.exists(
                                Path.of(source)
                        )
                ) {

                    errors.add(
                            "数据文件不存在："
                                    + source
                    );
                }
            }

            for (
                    String binary
                    : config.getBinaries()
            ) {

                if (
                        binary == null
                                ||
                                binary.isBlank()
                ) {

                    errors.add(
                            "存在空的二进制文件配置。"
                    );

                    continue;
                }

                int separator =
                        binary.indexOf(';');

                if (separator <= 0) {

                    errors.add(
                            "二进制文件格式错误："
                                    + binary
                                    + "\n正确格式：source;destination"
                    );

                    continue;
                }

                String source =
                        binary.substring(
                                0,
                                separator
                        );

                if (
                        !Files.exists(
                                Path.of(source)
                        )
                ) {

                    errors.add(
                            "二进制文件不存在："
                                    + source
                    );
                }
            }

            return errors;
        }

        validateEntryFile(
                config,
                errors
        );

        validateName(
                config,
                errors
        );

        validateDirectories(
                config,
                errors
        );

        validateIcon(
                config,
                errors
        );

        validateLists(
                config,
                errors
        );

        validateMode(
                config,
                errors
        );

        /*
         * ============================================================
         * Version File
         * ============================================================
         */

        if (config.getVersionFile() != null) {

            if (!Files.isRegularFile(
                    config.getVersionFile()
            )) {

                errors.add(
                        "版本信息文件不存在："
                                + config.getVersionFile()
                );
            }
        }


        /*
         * ============================================================
         * Manifest
         * ============================================================
         */

        if (config.getManifestFile() != null) {

            if (!Files.isRegularFile(
                    config.getManifestFile()
            )) {

                errors.add(
                        "Manifest 文件不存在："
                                + config.getManifestFile()
                );
            }
        }

        return errors;
    }

    /**
     * 校验 Python 入口文件。
     */
    private void validateEntryFile(
            PyInstallerBuildConfig config,
            List<String> errors) {

        Path entryFile =
                config.getEntryFile();

        if (entryFile == null) {

            errors.add(
                    "未指定 Python 入口文件。"
            );

            return;
        }

        if (!Files.isRegularFile(entryFile)) {

            errors.add(
                    "Python 入口文件不存在："
                            + entryFile
            );

            return;
        }

        String fileName =
                entryFile
                        .getFileName()
                        .toString()
                        .toLowerCase();

        if (!fileName.endsWith(".py")
                && !fileName.endsWith(".pyc")) {

            errors.add(
                    "入口文件必须是 .py 或 .pyc 文件。"
            );
        }
    }

    /**
     * 校验应用名称。
     */
    private void validateName(
            PyInstallerBuildConfig config,
            List<String> errors) {

        String name =
                config.getName();

        if (name == null
                || name.isBlank()) {

            errors.add(
                    "应用名称不能为空。"
            );

            return;
        }

        if (!name.matches(
                "[a-zA-Z0-9._-]+"
        )) {

            errors.add(
                    "应用名称只能包含字母、数字、点、下划线和短横线。"
            );
        }
    }

    /**
     * 校验目录。
     */
    private void validateDirectories(
            PyInstallerBuildConfig config,
            List<String> errors) {

        if (config.getOutputDirectory() == null) {

            errors.add(
                    "未指定输出目录。"
            );
        }

        if (config.getWorkDirectory() == null) {

            errors.add(
                    "未指定工作目录。"
            );
        }

        if (config.getSpecDirectory() == null) {

            errors.add(
                    "未指定 Spec 目录。"
            );
        }

        validateDirectoryIfExists(
                config.getOutputDirectory(),
                "输出目录",
                errors
        );

        validateDirectoryIfExists(
                config.getWorkDirectory(),
                "工作目录",
                errors
        );

        validateDirectoryIfExists(
                config.getSpecDirectory(),
                "Spec 目录",
                errors
        );
    }

    private void validateDirectoryIfExists(
            Path path,
            String name,
            List<String> errors) {

        if (path == null) {
            return;
        }

        if (Files.exists(path)
                && !Files.isDirectory(path)) {

            errors.add(
                    name
                            + "不是目录："
                            + path
            );
        }
    }

    /**
     * 校验图标。
     */
    private void validateIcon(
            PyInstallerBuildConfig config,
            List<String> errors) {

        Path icon =
                config.getIcon();

        if (icon == null) {
            return;
        }

        if (!Files.isRegularFile(icon)) {

            errors.add(
                    "图标文件不存在："
                            + icon
            );

            return;
        }

        String fileName =
                icon
                        .getFileName()
                        .toString()
                        .toLowerCase();

        if (!fileName.endsWith(".ico")) {

            errors.add(
                    "Windows 图标必须使用 .ico 文件。"
            );
        }
    }

    /**
     * 校验所有高级列表。
     */
    private void validateLists(
            PyInstallerBuildConfig config,
            List<String> errors) {

        validateList(
                config.getHiddenImports(),
                "隐藏导入",
                errors
        );

        validateList(
                config.getDataFiles(),
                "数据文件",
                errors
        );

        validateList(
                config.getBinaries(),
                "二进制文件",
                errors
        );

        validateList(
                config.getPaths(),
                "模块搜索路径",
                errors
        );

        validateList(
                config.getExcludes(),
                "排除模块",
                errors
        );

        validateList(
                config.getCollectSubmodules(),
                "收集子模块",
                errors
        );

        validateList(
                config.getCollectData(),
                "收集数据",
                errors
        );

        validateList(
                config.getCollectBinaries(),
                "收集二进制",
                errors
        );

        validateList(
                config.getCollectAll(),
                "收集全部",
                errors
        );
    }

    /**
     * 校验列表内容。
     */
    private void validateList(
            List<String> values,
            String name,
            List<String> errors) {

        if (values == null) {
            return;
        }

        for (String value : values) {

            if (value == null
                    || value.isBlank()) {

                errors.add(
                        name
                                + "中存在空项目。"
                );
            }
        }
    }

    /**
     * 校验打包模式。
     */
    private void validateMode(
            PyInstallerBuildConfig config,
            List<String> errors) {

        if (config.getMode() == null) {

            errors.add(
                    "未指定打包模式。"
            );
        }

        if (config.getConsoleMode() == null) {

            errors.add(
                    "未指定控制台模式。"
            );
        }
    }

    /**
     * 判断配置是否有效。
     */
    public boolean isValid(
            PyInstallerBuildConfig config) {

        return validate(config)
                .isEmpty();


    }
}
