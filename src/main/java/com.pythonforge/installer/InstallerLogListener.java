package com.pythonforge.installer;


/**
 * 安装包生成日志。
 */
@FunctionalInterface
public interface InstallerLogListener {


    void onLog(String message);

}