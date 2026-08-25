package com.pythonforge.installer;


/**
 * 安装包类型。
 */
public enum InstallerType {


    /**
     * Inno Setup
     */
    INNO_SETUP,


    /**
     * NSIS
     */
    NSIS,


    /**
     * 暂不生成安装包
     */
    NONE
}