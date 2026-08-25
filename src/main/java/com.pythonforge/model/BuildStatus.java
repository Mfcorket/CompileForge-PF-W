package com.pythonforge.model;

/**
 * 构建状态。
 */
public enum BuildStatus {

    /**
     * 空闲。
     */
    IDLE,

    /**
     * 准备构建。
     */
    PREPARING,

    /**
     * 正在构建。
     */
    RUNNING,

    /**
     * 构建成功。
     */
    SUCCESS,

    /**
     * 构建失败。
     */
    FAILED,

    /**
     * 用户取消。
     */
    CANCELLED
}