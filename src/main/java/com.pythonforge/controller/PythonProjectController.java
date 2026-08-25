package com.pythonforge.controller;

import com.pythonforge.model.PythonProject;
import com.pythonforge.service.PythonProjectService;
import com.pythonforge.util.LogUtils;
import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Python 项目控制器。
 *
 * <p>
 * 当前负责：
 * </p>
 *
 * <ul>
 *     <li>选择 Python 文件</li>
 *     <li>识别 .py</li>
 *     <li>识别 .pyc</li>
 *     <li>建立 PythonProject</li>
 * </ul>
 */
public final class PythonProjectController {

    private static final Logger LOGGER =
            LogUtils.getLogger(
                    PythonProjectController.class
            );

    private final PythonProjectService
            projectService =
            new PythonProjectService();

    /**
     * 选择 Python 文件。
     */
    @FXML
    private void choosePythonFile() {

        FileChooser chooser =
                new FileChooser();

        chooser.setTitle(
                "选择 Python 项目"
        );

        FileChooser.ExtensionFilter
                pythonFilter =
                new FileChooser.ExtensionFilter(
                        "Python 文件 (*.py, *.pyc)",
                        "*.py",
                        "*.pyc"
                );

        chooser
                .getExtensionFilters()
                .add(
                        pythonFilter
                );

        Window window =
                getWindow();

        File file =
                chooser.showOpenDialog(
                        window
                );

        if (file == null) {

            return;
        }

        analyze(
                file.toPath()
        );
    }

    /**
     * 分析项目。
     *
     * @param path Python 文件
     */
    private void analyze(
            Path path) {

        try {

            Optional<PythonProject>
                    project =
                    projectService.analyze(
                            path
                    );

            if (project.isEmpty()) {

                LogUtils.warning(
                        LOGGER,
                        "Unable to analyze Python project: "
                                + path
                );

                return;
            }

            PythonProject value =
                    project.get();

            LogUtils.info(
                    LOGGER,
                    "Python project ready: "
                            + value
            );

        } catch (Exception e) {

            LogUtils.error(
                    LOGGER,
                    "Python project analysis failed: "
                            + e.getMessage()
            );
        }
    }

    /**
     * 获取当前窗口。
     *
     * @return JavaFX Window
     */
    private Window getWindow() {

        return null;
    }
}