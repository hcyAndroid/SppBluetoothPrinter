package com.issyzone.classicblulib.common;

/**
 * 观察者
 */
public interface Observer {
    /**
     * 数据变化
     */
    @Observe
    default void onChanged(Object o) {}
}
