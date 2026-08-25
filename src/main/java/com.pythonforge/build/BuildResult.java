package com.pythonforge.build;

import com.pythonforge.model.BuildStatus;

import java.nio.file.Path;

/**
 * PyInstaller 构建结果。
 *
 * <p>
 * P3.9：
 * 支持返回实际生成的 EXE 文件。
 * </p>
 */
public final class BuildResult {

    /**
     * 构建状态。
     */
    private final BuildStatus status;


    /**
     * 实际生成文件。
     *
     * <p>
     * Windows 当前主要为 exe 文件。
     * </p>
     */
    private final Path outputFile;


    /**
     * 输出目录。
     */
    private final Path outputDirectory;


    /**
     * 描述信息。
     */
    private final String message;

    private final Path logFile;


    public BuildResult(
            BuildStatus status,
            Path outputFile,
            Path outputDirectory,
            Path logFile,
            String message
    ){

        this.logFile =
                logFile;

        this.status = status;

        this.outputFile = outputFile;

        this.outputDirectory = outputDirectory;

        this.message = message;
    }


    /**
     * 获取构建状态。
     */
    public BuildStatus getStatus() {

        return status;
    }


    /**
     * 获取生成文件。
     *
     * @return EXE 文件路径
     */
    public Path getOutputFile() {

        return outputFile;
    }


    /**
     * 获取输出目录。
     */
    public Path getOutputDirectory() {

        return outputDirectory;
    }


    /**
     * 获取消息。
     */
    public String getMessage() {

        return message;
    }


    /**
     * 判断是否成功。
     */
    public boolean isSuccess() {

        return status == BuildStatus.SUCCESS;
    }


    /**
     * 判断是否失败。
     */
    public boolean isFailed() {

        return status == BuildStatus.FAILED;
    }


    /**
     * 判断是否取消。
     */
    public boolean isCancelled() {

        return status == BuildStatus.CANCELLED;
    }


    @Override
    public String toString() {

        return "BuildResult{" +
                "status=" + status +
                ", outputFile=" + outputFile +
                ", outputDirectory=" + outputDirectory +
                ", message='" + message + '\'' +
                '}';
    }

    public Path getLogFile(){

        return logFile;
    }
}