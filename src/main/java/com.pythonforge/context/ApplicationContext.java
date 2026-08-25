package com.pythonforge.context;


import com.pythonforge.history.BuildHistoryService;


/**
 * PythonForge 应用上下文。
 *
 * <p>
 * P4.7：
 * 管理全局 Service 单例。
 * </p>
 */
public final class ApplicationContext {


    private static final BuildHistoryService
            BUILD_HISTORY_SERVICE =
            new BuildHistoryService();



    private ApplicationContext(){

    }



    /**
     * 获取构建历史服务。
     */
    public static BuildHistoryService
    getBuildHistoryService(){

        return BUILD_HISTORY_SERVICE;
    }

}