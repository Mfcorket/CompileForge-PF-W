package com.pythonforge.build;

import com.pythonforge.model.PyInstallerBuildConfig;
import com.pythonforge.model.PythonEnvironment;
import javafx.concurrent.Task;

/**
 * PyInstaller 后台构建任务。
 */
public final class BuildTask
        extends Task<BuildResult> {

    private final PythonEnvironment environment;

    private final PyInstallerBuildEngine engine;

    private final PyInstallerBuildConfig config;

    public BuildTask(
            PythonEnvironment environment,
            PyInstallerBuildEngine engine,
            PyInstallerBuildConfig config
    ) {

        this.environment = environment;

        this.engine = engine;

        this.config = config;
    }

    @Override
    protected BuildResult call()
            throws Exception {

        updateMessage(
                "正在启动 PyInstaller..."
        );

        return engine.build(
                environment,
                config,
                message -> {

                    updateMessage(
                            message
                    );
                }
        );
    }

    /**
     * 取消真正的 PyInstaller 进程。
     */
    public void cancelBuildProcess() {

        engine.cancel();

        cancel();
    }
}