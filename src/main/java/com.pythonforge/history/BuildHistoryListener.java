package com.pythonforge.history;


/**
 * 构建历史变化监听器。
 */
@FunctionalInterface
public interface BuildHistoryListener {


    void onHistoryChanged();

}