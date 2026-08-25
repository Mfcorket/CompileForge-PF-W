package com.pythonforge.history;

import com.pythonforge.build.BuildResult;
import com.pythonforge.model.PyInstallerBuildConfig;
import com.pythonforge.model.PythonEnvironment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * PythonForge-win 构建历史服务。
 *
 * <p>
 * Beta.1.1.2 P3.2：
 * 统一构建历史记录生命周期以及实时刷新机制。
 * </p>
 *
 * <p>
 * 职责：
 * </p>
 *
 * <ul>
 *     <li>加载历史记录</li>
 *     <li>新增构建记录</li>
 *     <li>删除构建记录</li>
 *     <li>清空构建记录</li>
 *     <li>导入构建记录</li>
 *     <li>持久化构建记录</li>
 *     <li>通知 UI 历史记录发生变化</li>
 * </ul>
 */
public final class BuildHistoryService {

    /*
     * ============================================================
     * Manager
     * ============================================================
     */

    private final BuildHistoryManager manager =
            new BuildHistoryManager();


    /*
     * ============================================================
     * Records
     * ============================================================
     */

    private final List<BuildHistoryRecord> records =
            new ArrayList<>();


    /*
     * ============================================================
     * Listeners
     * ============================================================
     */

    private final List<BuildHistoryListener> listeners =
            new ArrayList<>();


    /*
     * ============================================================
     * Configuration
     * ============================================================
     */

    private static final int MAX_RECORDS =
            HistoryConfig.MAX_RECORDS;


    /*
     * ============================================================
     * Constructor
     * ============================================================
     */

    public BuildHistoryService() {

        try {

            List<BuildHistoryRecord> loaded =
                    manager.load();

            if (loaded != null) {

                records.addAll(
                        loaded
                );
            }

        } catch (Exception exception) {

            records.clear();
        }
    }


    /*
     * ============================================================
     * Add Build Result
     * ============================================================
     */

    /**
     * 添加一次构建产生的历史记录。
     *
     * @param environment Python 环境
     * @param config 构建配置
     * @param result 构建结果
     */
    /**
     * 添加构建记录。
     */
    public void add(
            PythonEnvironment environment,
            PyInstallerBuildConfig config,
            BuildResult result
    ) {

        if (
                environment == null
                        ||
                        config == null
                        ||
                        result == null
        ) {

            return;
        }


        BuildHistoryRecord record =
                new BuildHistoryRecord(

                        LocalDateTime.now(),

                        result.getStatus(),

                        config.getName(),

                        config.getEntryFile(),

                        result.getOutputFile(),

                        result.getOutputDirectory(),

                        environment.getVersion(),

                        result.getLogFile(),

                        result.getMessage()
                );


        /*
         * 新记录放在最前面。
         */
        records.add(
                0,
                record
        );


        /*
         * 限制历史记录数量。
         */
        while (
                records.size() > MAX_RECORDS
        ) {

            records.remove(
                    records.size() - 1
            );
        }


        /*
         * 持久化。
         */
        try {

            manager.save(
                    records
            );

        } catch (Exception exception) {

            /*
             * 历史保存失败不影响当前构建。
             */
            exception.printStackTrace();
        }


        /*
         * 最后通知 UI。
         *
         * 此时 records 已经是最终状态。
         */
        notifyChanged();
    }


    /*
     * ============================================================
     * Add Record
     * ============================================================
     */

    /**
     * 直接添加构建历史记录。
     *
     * <p>
     * 这是所有新增历史记录的统一入口。
     * </p>
     *
     * @param record 构建历史记录
     */
    /**
     * 直接添加构建历史记录。
     *
     * @param record 构建历史记录
     */
    public void add(
            BuildHistoryRecord record) {

        if (record == null) {

            return;
        }


        records.add(
                0,
                record
        );


        /*
         * 最大保存 MAX_RECORDS 条。
         */
        while (
                records.size() > MAX_RECORDS
        ) {

            records.remove(
                    records.size() - 1
            );
        }


        /*
         * 持久化。
         */
        try {

            manager.save(
                    records
            );

        } catch (Exception exception) {

            /*
             * 历史记录保存失败不应该影响
             * 当前构建结果。
             */
            exception.printStackTrace();
        }


        /*
         * 通知 UI。
         */
        notifyChanged();
    }


    /*
     * ============================================================
     * Query
     * ============================================================
     */

    /**
     * 获取全部构建历史。
     *
     * @return 构建历史副本
     */
    public List<BuildHistoryRecord> getRecords() {

        return List.copyOf(
                records
        );
    }


    /**
     * 获取最近一次构建。
     *
     * @return 最近一次构建记录，没有记录时返回 null
     */
    public BuildHistoryRecord getLastBuild() {

        if (records.isEmpty()) {

            return null;
        }


        return records.get(0);
    }


    /*
     * ============================================================
     * Delete
     * ============================================================
     */

    /**
     * 删除指定构建历史记录。
     *
     * @param record 要删除的记录
     * @return 是否删除成功
     */
    public boolean remove(
            BuildHistoryRecord record) {

        if (record == null) {

            return false;
        }


        boolean removed =
                records.remove(
                        record
                );


        if (!removed) {

            return false;
        }


        try {

            manager.save(
                    records
            );

        } catch (Exception exception) {

            exception.printStackTrace();
        }


        notifyChanged();


        return true;
    }


    /**
     * 删除指定构建历史记录。
     *
     * <p>
     * 与 remove 保持兼容。
     * </p>
     *
     * @param record 要删除的记录
     */
    public void delete(
            BuildHistoryRecord record) {

        remove(record);
    }


    /**
     * 清空所有构建历史。
     */
    public void clear() {

        if (records.isEmpty()) {

            return;
        }


        records.clear();

        save();

        notifyChanged();
    }


    /*
     * ============================================================
     * Import
     * ============================================================
     */

    /**
     * 导入构建历史记录。
     *
     * <p>
     * 新导入的记录会放到当前历史记录最前面。
     * </p>
     *
     * @param imported 导入的历史记录
     */
    public void importRecords(
            List<BuildHistoryRecord> imported) {

        if (
                imported == null
                        ||
                        imported.isEmpty()
        ) {

            return;
        }


        records.addAll(
                0,
                imported
        );


        trimRecords();


        save();

        notifyChanged();
    }


    /*
     * ============================================================
     * Listener
     * ============================================================
     */

    /**
     * 注册历史记录监听器。
     *
     * @param listener 监听器
     */
    public void addListener(
            BuildHistoryListener listener) {

        if (listener == null) {

            return;
        }


        if (
                !listeners.contains(
                        listener
                )
        ) {

            listeners.add(
                    listener
            );
        }
    }


    /**
     * 移除历史记录监听器。
     *
     * @param listener 监听器
     */
    public void removeListener(
            BuildHistoryListener listener) {

        listeners.remove(
                listener
        );
    }


    /**
     * 通知历史记录发生变化。
     */
    private void notifyChanged() {

        /*
         * 创建副本。
         *
         * 防止监听器在回调过程中修改
         * listeners 集合导致 ConcurrentModificationException。
         */
        List<BuildHistoryListener> snapshot =
                List.copyOf(
                        listeners
                );


        for (
                BuildHistoryListener listener
                : snapshot
        ) {

            try {

                listener.onHistoryChanged();

            } catch (Exception ignored) {

                /*
                 * 单个监听器异常不能影响
                 * 其他监听器。
                 */
            }
        }
    }


    /*
     * ============================================================
     * Internal
     * ============================================================
     */

    /**
     * 限制历史记录数量。
     */
    private void trimRecords() {

        while (
                records.size()
                        >
                        MAX_RECORDS
        ) {

            records.remove(
                    records.size() - 1
            );
        }
    }


    /**
     * 保存历史记录。
     *
     * <p>
     * 持久化失败不影响当前内存中的历史记录。
     * </p>
     */
    private void save() {

        try {

            manager.save(
                    records
            );

        } catch (Exception exception) {

            /*
             * 历史记录保存失败不能影响
             * 当前构建流程。
             */
            exception.printStackTrace();
        }
    }
}