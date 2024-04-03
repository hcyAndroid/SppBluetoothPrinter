package com.issyzone.classicblulib.common;

import androidx.annotation.NonNull;

/**

 */
interface Poster {
    /**
     * 将要执行的任务加入队列
     * 
     * @param runnable 要执行的任务
     */
    void enqueue(@NonNull Runnable runnable);

    /**
     * 清除队列任务
     */
    void clear();
}
