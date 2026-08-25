package com.pythonforge.installer;


import java.nio.file.Path;


/**
 * 安装包配置。
 */
public final class InstallerConfig {


    /**
     * 安装包类型
     */
    private InstallerType type;


    /**
     * 应用名称
     */
    private String applicationName;


    /**
     * EXE 文件
     */
    private Path executableFile;


    /**
     * 输出目录
     */
    private Path outputDirectory;


    /**
     * 安装目录名称
     */
    private String installDirectory;


    /**
     * 创建桌面快捷方式
     */
    private boolean desktopShortcut;


    /**
     * 创建开始菜单
     */
    private boolean startMenuShortcut;



    public InstallerConfig(){

        type =
                InstallerType.NONE;

        desktopShortcut =
                true;

        startMenuShortcut =
                true;
    }



    public InstallerType getType(){

        return type;
    }


    public void setType(
            InstallerType type){

        this.type = type;
    }


    public String getApplicationName(){

        return applicationName;
    }


    public void setApplicationName(
            String applicationName){

        this.applicationName =
                applicationName;
    }


    public Path getExecutableFile(){

        return executableFile;
    }


    public void setExecutableFile(
            Path executableFile){

        this.executableFile =
                executableFile;
    }


    public Path getOutputDirectory(){

        return outputDirectory;
    }


    public void setOutputDirectory(
            Path outputDirectory){

        this.outputDirectory =
                outputDirectory;
    }


    public String getInstallDirectory(){

        return installDirectory;
    }


    public void setInstallDirectory(
            String installDirectory){

        this.installDirectory =
                installDirectory;
    }


    public boolean isDesktopShortcut(){

        return desktopShortcut;
    }


    public void setDesktopShortcut(
            boolean desktopShortcut){

        this.desktopShortcut =
                desktopShortcut;
    }


    public boolean isStartMenuShortcut(){

        return startMenuShortcut;
    }


    public void setStartMenuShortcut(
            boolean startMenuShortcut){

        this.startMenuShortcut =
                startMenuShortcut;
    }
}