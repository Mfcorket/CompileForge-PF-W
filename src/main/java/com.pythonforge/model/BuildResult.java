package com.pythonforge.model;

import java.nio.file.Path;
import java.time.Duration;

/**
 * PyInstaller 构建结果。
 *
 * <p>
 * 用于描述一次 PyInstaller 构建完成后的最终结果。
 * </p>
 */
public final class BuildResult {

    /**
     * 构建是否成功。
     */
    private final boolean success;

    /**
     * 最终输出文件。
     *
     * <p>
     * ONEFILE 模式通常为 exe。
     * ONEDIR 模式下为最终 exe。
     * </p>
     */
    private final Path outputFile;

    /**
     * 输出目录。
     */
    private final Path outputDirectory;

    /**
     * 输出文件大小。
     */
    private final long outputSize;

    /**
     * 进程退出码。
     */
    private final int exitCode;

    /**
     * 构建耗时。
     */
    private final Duration duration;

    /**
     * 结果消息。
     */
    private final String message;

    private BuildResult(
            boolean success,
            Path outputFile,
            Path outputDirectory,
            long outputSize,
            int exitCode,
            Duration duration,
            String message) {

        this.success = success;
        this.outputFile = outputFile;
        this.outputDirectory = outputDirectory;
        this.outputSize = outputSize;
        this.exitCode = exitCode;
        this.duration = duration;
        this.message = message;
    }

    /**
     * 创建成功结果。
     */
    public static BuildResult success(
            Path outputFile,
            Path outputDirectory,
            long outputSize,
            int exitCode,
            Duration duration,
            String message) {

        return new BuildResult(
                true,
                outputFile,
                outputDirectory,
                outputSize,
                exitCode,
                duration,
                message
        );
    }

    /**
     * 创建失败结果。
     */
    public static BuildResult failure(
            Path outputDirectory,
            int exitCode,
            Duration duration,
            String message) {

        return new BuildResult(
                false,
                null,
                outputDirectory,
                0L,
                exitCode,
                duration,
                message
        );
    }

    public boolean isSuccess() {
        return success;
    }

    public Path getOutputFile() {
        return outputFile;
    }

    public Path getOutputDirectory() {
        return outputDirectory;
    }

    public long getOutputSize() {
        return outputSize;
    }

    public int getExitCode() {
        return exitCode;
    }

    public Duration getDuration() {
        return duration;
    }

    public String getMessage() {
        return message;
    }

    /**
     * 返回可读的文件大小。
     */
    public String getFormattedOutputSize() {

        if (outputSize < 1024) {
            return outputSize + " B";
        }

        double size =
                outputSize / 1024.0;

        if (size < 1024) {
            return String.format(
                    "%.2f KB",
                    size
            );
        }

        size /= 1024.0;

        if (size < 1024) {
            return String.format(
                    "%.2f MB",
                    size
            );
        }

        size /= 1024.0;

        return String.format(
                "%.2f GB",
                size
        );
    }

    @Override
    public String toString() {

        return "BuildResult{" +
                "success=" + success +
                ", outputFile=" + outputFile +
                ", outputDirectory=" + outputDirectory +
                ", outputSize=" + outputSize +
                ", exitCode=" + exitCode +
                ", duration=" + duration +
                ", message='" + message + '\'' +
                '}';
    }
}