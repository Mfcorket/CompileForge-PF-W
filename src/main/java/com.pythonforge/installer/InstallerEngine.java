package com.pythonforge.installer;


/**
 * 安装包生成引擎。
 */
public interface InstallerEngine {


    /**
     * 生成安装包。
     */
    InstallerResult build(
            InstallerConfig config,
            InstallerLogListener listener
    );


    /**
     * 取消生成。
     */
    void cancel();

}