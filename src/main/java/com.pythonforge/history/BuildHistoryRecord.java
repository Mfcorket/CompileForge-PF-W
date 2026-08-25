package com.pythonforge.history;


import com.pythonforge.model.BuildStatus;

import java.nio.file.Path;
import java.time.LocalDateTime;


/**
 * PyInstaller 构建历史记录。
 */
public final class BuildHistoryRecord {


    private final LocalDateTime time;


    private final BuildStatus status;


    private final String applicationName;


    private final Path entryFile;


    private final Path outputFile;


    private final Path outputDirectory;


    private final String pythonVersion;


    /**
     * 构建日志文件。
     */
    private final Path logFile;


    private final String message;



    public BuildHistoryRecord(

            LocalDateTime time,

            BuildStatus status,

            String applicationName,

            Path entryFile,

            Path outputFile,

            Path outputDirectory,

            String pythonVersion,

            Path logFile,

            String message
    ){

        this.time = time;

        this.status = status;

        this.applicationName = applicationName;

        this.entryFile = entryFile;

        this.outputFile = outputFile;

        this.outputDirectory = outputDirectory;

        this.pythonVersion = pythonVersion;

        this.logFile = logFile;

        this.message = message;
    }



    public LocalDateTime getTime(){

        return time;
    }


    public BuildStatus getStatus(){

        return status;
    }


    public String getApplicationName(){

        return applicationName;
    }


    public Path getEntryFile(){

        return entryFile;
    }


    public Path getOutputFile(){

        return outputFile;
    }


    public Path getOutputDirectory(){

        return outputDirectory;
    }


    public String getPythonVersion(){

        return pythonVersion;
    }


    public Path getLogFile(){

        return logFile;
    }


    public String getMessage(){

        return message;
    }
}