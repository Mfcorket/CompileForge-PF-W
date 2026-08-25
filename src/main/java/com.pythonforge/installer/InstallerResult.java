package com.pythonforge.installer;


import java.nio.file.Path;


/**
 * 安装包生成结果。
 */
public final class InstallerResult {


    private final boolean success;


    private final Path installerFile;


    private final String message;



    public InstallerResult(
            boolean success,
            Path installerFile,
            String message){

        this.success =
                success;

        this.installerFile =
                installerFile;

        this.message =
                message;
    }



    public boolean isSuccess(){

        return success;
    }



    public Path getInstallerFile(){

        return installerFile;
    }



    public String getMessage(){

        return message;
    }
}