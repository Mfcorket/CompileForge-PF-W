package com.pythonforge.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Windows 系统主题检测器。
 *
 * <p>
 * PythonForge-win 为 Windows 专用程序，
 * 因此这里直接读取 Windows Personalize 注册表配置。
 * </p>
 */
public class WindowsThemeDetector {

    private static final String REGISTRY_PATH =
            "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize";


    /**
     * 判断当前 Windows 是否为亮色模式。
     *
     * @return true 亮色，false 暗色
     */
    public boolean isLightTheme() {

        Integer value =
                readRegistryValue(
                        "AppsUseLightTheme"
                );

        if (value != null) {

            return value != 0;
        }


        /*
         * 如果 AppsUseLightTheme 不存在，
         * 尝试读取系统主题设置。
         */
        value =
                readRegistryValue(
                        "SystemUsesLightTheme"
                );

        if (value != null) {

            return value != 0;
        }


        /*
         * 无法检测时默认使用亮色。
         */
        return true;
    }


    /**
     * 获取当前 Windows 主题。
     *
     * @return LIGHT / DARK
     */
    public String getCurrentTheme() {

        return isLightTheme()
                ? "LIGHT"
                : "DARK";
    }


    private Integer readRegistryValue(
            String valueName) {

        try {

            Process process =
                    new ProcessBuilder(
                            "reg",
                            "query",
                            REGISTRY_PATH,
                            "/v",
                            valueName
                    )
                            .redirectErrorStream(true)
                            .start();


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
                        (line = reader.readLine())
                                != null
                ) {

                    line = line.trim();

                    if (
                            line.startsWith(
                                    valueName
                            )
                    ) {

                        String[] parts =
                                line.split(
                                        "\\s+"
                                );

                        if (parts.length >= 3) {

                            String value =
                                    parts[
                                            parts.length - 1
                                            ];

                            return Integer.parseInt(
                                    value.startsWith("0x")
                                            ? value.substring(2)
                                            : value
                            );
                        }
                    }
                }
            }

            process.waitFor();

        } catch (
                Exception ignored) {

        }

        return null;
    }
}