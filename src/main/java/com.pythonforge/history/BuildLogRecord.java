package com.pythonforge.history;

import java.nio.file.Path;
import java.time.LocalDateTime;


/**
 * 构建日志记录。
 */
public final class BuildLogRecord {


    private final LocalDateTime time;


    private final Path logFile;


    private final String applicationName;



    public BuildLogRecord(
            LocalDateTime time,
            Path logFile,
            String applicationName
    ){

        this.time = time;

        this.logFile = logFile;

        this.applicationName = applicationName;
    }



    public LocalDateTime getTime(){

        return time;
    }



    public Path getLogFile(){

        return logFile;
    }



    public String getApplicationName(){

        return applicationName;
    }

}