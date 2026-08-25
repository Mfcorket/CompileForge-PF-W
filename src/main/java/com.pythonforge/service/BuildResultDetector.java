package com.pythonforge.service;

import com.pythonforge.model.BuildResult;
import com.pythonforge.model.PyInstallerBuildConfig;
import com.pythonforge.util.LogUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * PyInstaller 构建结果检测器。
 *
 * <p>
 * P3.8：
 * 在 PyInstaller 进程结束后检查实际生成结果。
 * </p>
 */
public final class BuildResultDetector {

    private static final Logger LOGGER =
            LogUtils.getLogger(
                    BuildResultDetector.class
            );

    /**
     * 检测构建结果。
     *
     * @param config 构建配置
     * @param exitCode PyInstaller 退出码
     * @param startTime 构建开始时间
     * @return 构建结果
     */
    public BuildResult detect(
            PyInstallerBuildConfig config,
            int exitCode,
            Instant startTime) {

        Instant endTime =
                Instant.now();

        Duration duration =
                startTime == null
                        ? Duration.ZERO
                        : Duration.between(
                        startTime,
                        endTime
                );

        if (config == null) {

            return BuildResult.failure(
                    null,
                    exitCode,
                    duration,
                    "构建配置为空。"
            );
        }

        Path outputDirectory =
                config.getOutputDirectory();

        if (outputDirectory == null) {

            return BuildResult.failure(
                    null,
                    exitCode,
                    duration,
                    "未指定输出目录。"
            );
        }

        if (!Files.isDirectory(outputDirectory)) {

            LogUtils.warning(
                    LOGGER,
                    "Build output directory does not exist: "
                            + outputDirectory
            );

            return BuildResult.failure(
                    outputDirectory,
                    exitCode,
                    duration,
                    "输出目录不存在："
                            + outputDirectory
            );
        }

        /*
         * PyInstaller 进程失败时，
         * 即使目录中存在旧 EXE，也不能认为本次构建成功。
         */
        if (exitCode != 0) {

            return BuildResult.failure(
                    outputDirectory,
                    exitCode,
                    duration,
                    "PyInstaller 构建失败，退出码："
                            + exitCode
            );
        }

        Path outputFile =
                findOutputFile(config);

        if (outputFile == null) {

            return BuildResult.failure(
                    outputDirectory,
                    exitCode,
                    duration,
                    "PyInstaller 执行成功，但未找到生成的 EXE 文件。"
            );
        }

        try {

            long size =
                    Files.size(outputFile);

            LogUtils.info(
                    LOGGER,
                    "Build result detected: "
                            + outputFile
                            + " | size="
                            + size
            );

            return BuildResult.success(
                    outputFile,
                    outputDirectory,
                    size,
                    exitCode,
                    duration,
                    "构建成功。"
            );

        } catch (IOException e) {

            LogUtils.warning(
                    LOGGER,
                    "Unable to read output file size: "
                            + e.getMessage()
            );

            return BuildResult.success(
                    outputFile,
                    outputDirectory,
                    0L,
                    exitCode,
                    duration,
                    "构建成功，但无法读取 EXE 文件大小。"
            );
        }
    }

    /**
     * 查找最终 EXE。
     */
    private Path findOutputFile(
            PyInstallerBuildConfig config) {

        Path outputDirectory =
                config.getOutputDirectory();

        String applicationName =
                config.getName();

        /*
         * 优先使用：
         *
         * dist/name.exe
         */
        if (applicationName != null
                && !applicationName.isBlank()) {

            Path expected =
                    outputDirectory.resolve(
                            applicationName + ".exe"
                    );

            if (Files.isRegularFile(expected)) {
                return expected;
            }

            /*
             * ONEDIR：
             *
             * dist/name/name.exe
             */
            Path oneDir =
                    outputDirectory
                            .resolve(applicationName)
                            .resolve(
                                    applicationName
                                            + ".exe"
                            );

            if (Files.isRegularFile(oneDir)) {
                return oneDir;
            }
        }

        /*
         * 如果名称无法确定，
         * 扫描输出目录。
         */
        return findFirstExe(
                outputDirectory
        );
    }

    /**
     * 递归查找 EXE。
     */
    private Path findFirstExe(
            Path directory) {

        try (Stream<Path> stream =
                     Files.walk(directory)) {

            return stream
                    .filter(Files::isRegularFile)
                    .filter(path ->
                            path.getFileName()
                                    .toString()
                                    .toLowerCase()
                                    .endsWith(".exe")
                    )
                    .sorted(
                            Comparator.comparing(
                                    Path::toString
                            )
                    )
                    .findFirst()
                    .orElse(null);

        } catch (IOException e) {

            LogUtils.warning(
                    LOGGER,
                    "Failed to scan build output: "
                            + e.getMessage()
            );

            return null;
        }
    }
}