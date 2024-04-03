package com.issyzone.classicblulib.common;

/**
 * 线程策略
 * <p>
 */
public enum ThreadMode {
    /**
     * 和调用者同一线程
     */
    POSTING,
    /**
     * 主线程，UI线程
     */
    MAIN,
    /**
     * 后台线程，同步的
     */
    BACKGROUND,
    /**
     * 异步线程
     */
    ASYNC,
    /**
     * 未指定
     */
    UNSPECIFIED
}
