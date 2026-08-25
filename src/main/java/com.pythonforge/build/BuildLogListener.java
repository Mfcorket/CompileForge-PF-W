package com.pythonforge.build;


/**
 * 构建日志监听器。
 */
@FunctionalInterface
public interface BuildLogListener {


    /**
     * 接收日志。
     *
     * @param message 日志内容
     */
    void onLog(String message);

}