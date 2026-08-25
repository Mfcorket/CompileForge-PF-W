package com.pythonforge.build;

import com.pythonforge.util.LogUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Logger;

/**
 * 外部进程执行器。
 *
 * <p>
 * 用于执行 PyInstaller 等外部程序。
 * </p>
 *
 * <p>
 * P3.8：
 * 支持保存当前 Process、实时输出日志以及取消外部进程。
 * </p>
 */
public final class BuildProcessExecutor {

    private static final Logger LOGGER =
            LogUtils.getLogger(
                    BuildProcessExecutor.class
            );

    /**
     * 当前正在执行的外部进程。
     */
    private volatile Process currentProcess;

    /**
     * 执行外部命令。
     *
     * @param command 需要执行的命令
     * @param listener 日志监听器
     * @return 进程退出码
     * @throws Exception 执行异常
     */
    public int execute(
            List<String> command,
            BuildLogListener listener
    ) throws Exception {

        if (command == null
                || command.isEmpty()) {

            throw new IllegalArgumentException(
                    "外部命令不能为空。"
            );
        }

        LogUtils.info(
                LOGGER,
                "Execute: "
                        + String.join(
                        " ",
                        command
                )
        );

        ProcessBuilder builder =
                new ProcessBuilder(command);

        /*
         * 将 stderr 合并到 stdout。
         *
         * PyInstaller 的错误信息也会进入
         * 同一个日志流。
         */
        builder.redirectErrorStream(true);

        Process process =
                builder.start();

        currentProcess = process;

        LogUtils.info(
                LOGGER,
                "External process started."
        );

        try {

            try (
                    BufferedReader reader =
                            new BufferedReader(
                                    new InputStreamReader(
                                            process.getInputStream(),
                                            StandardCharsets.UTF_8
                                    )
                            )
            ) {

                String line;

                while (
                        (line = reader.readLine()) != null
                ) {

                    if (listener != null) {

                        listener.onLog(line);
                    }

                    LogUtils.info(
                            LOGGER,
                            line
                    );

                    /*
                     * 如果外部调用已经取消进程，
                     * 尽快停止继续读取。
                     */
                    if (!process.isAlive()) {

                        break;
                    }
                }
            }

            /*
             * 等待进程最终退出。
             */
            return process.waitFor();

        } finally {

            currentProcess = null;

            LogUtils.info(
                    LOGGER,
                    "External process finished."
            );
        }
    }

    /**
     * 取消当前进程。
     */
    public void cancel() {

        Process process =
                currentProcess;

        if (process == null) {

            LogUtils.info(
                    LOGGER,
                    "No active build process."
            );

            return;
        }

        LogUtils.info(
                LOGGER,
                "Cancelling build process..."
        );

        try {

            /*
             * 第一步：
             * 尝试正常结束进程。
             */
            process.destroy();

            /*
             * 等待最多约 500ms。
             */
            long deadline =
                    System.currentTimeMillis()
                            + 500;

            while (
                    process.isAlive()
                            &&
                            System.currentTimeMillis()
                                    < deadline
            ) {

                try {

                    Thread.sleep(50);

                } catch (InterruptedException e) {

                    Thread.currentThread()
                            .interrupt();

                    break;
                }
            }

            /*
             * 第二步：
             * 如果仍然存在，则强制结束。
             */
            if (process.isAlive()) {

                LogUtils.warning(
                        LOGGER,
                        "Build process did not exit normally. "
                                + "Destroying forcibly."
                );

                process.destroyForcibly();
            }

        } catch (Exception e) {

            LogUtils.warning(
                    LOGGER,
                    "Failed to cancel build process: "
                            + e.getMessage()
            );
        }
    }

    /**
     * 判断当前是否正在执行进程。
     *
     * @return true 表示正在执行
     */
    public boolean isRunning() {

        Process process =
                currentProcess;

        return process != null
                && process.isAlive();
    }
}