package com.pythonforge.history;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;


/**
 * 构建历史文件管理器。
 *
 * <p>
 * P4.3:
 * JSON 持久化。
 * </p>
 */
public final class BuildHistoryManager {


    private final ObjectMapper mapper;


    private final Path historyFile;



    public BuildHistoryManager(){


        mapper =
                new ObjectMapper();


        mapper.findAndRegisterModules();


        mapper.enable(
                SerializationFeature.INDENT_OUTPUT
        );


        historyFile =
                getHistoryPath();

    }



    /**
     * 获取历史文件路径。
     */
    private Path getHistoryPath(){


        String userHome =
                System.getProperty(
                        "user.home"
                );


        Path directory =
                Paths.get(
                        userHome,
                        ".pythonforge-win"
                );


        try {

            Files.createDirectories(
                    directory
            );

        } catch(IOException ignored){

        }


        return directory.resolve(
                "build-history.json"
        );
    }



    /**
     * 保存历史。
     */
    public void save(
            List<BuildHistoryRecord> records
    )
            throws IOException {


        mapper.writeValue(
                historyFile.toFile(),
                records
        );

    }



    /**
     * 加载历史。
     */
    public List<BuildHistoryRecord> load()
            throws IOException {


        if(
                !Files.exists(historyFile)
        ){

            return new ArrayList<>();
        }


        return mapper.readValue(
                historyFile.toFile(),
                new TypeReference<>(){
                }
        );

    }



    /**
     * 删除历史文件。
     */
    public void delete()
            throws IOException {


        Files.deleteIfExists(
                historyFile
        );
    }



    /**
     * 获取文件位置。
     */
    public Path getHistoryFile(){

        return historyFile;
    }

}