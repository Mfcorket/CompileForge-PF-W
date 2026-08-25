package com.pythonforge.history;


import java.util.List;


/**
 * 构建历史存储接口。
 */
public interface BuildHistoryStorage {


    /**
     * 保存历史。
     */
    void save(
            List<BuildHistoryRecord> records
    );


    /**
     * 加载历史。
     */
    List<BuildHistoryRecord> load();

}