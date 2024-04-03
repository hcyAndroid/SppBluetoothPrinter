package com.issyzone.classicblulib.tools;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public abstract class Connection {

    /**
     * 未连接
     */
    public static final int STATE_DISCONNECTED = 0;
    /**
     * 连接中
     */
    public static final int STATE_CONNECTING = 1;
    /**
     * 配对中
     */
    public static final int STATE_PAIRING = 2;
    /**
     * 已配对
     */
    public static final int STATE_PAIRED = 3;
    /**
     * 已连接
     */
    public static final int STATE_CONNECTED = 4;
    /**
     * 连接已释放
     */
    public static final int STATE_RELEASED = 5;

    /**
     * 设置连接状态
     */
    abstract void setState(int state);

    /**
     * 连接是否已释放
     */
    public abstract boolean isReleased();

    /**
     * 是否已连接
     */
    public abstract boolean isConnected();

    @NonNull
    public abstract BluetoothDevice getDevice();

    /**
     * 指定连接的UUID
     *
     * @param callback 连接回调
     */
    public abstract void connect(ConnectCallback callback);

    /**
     * 断开连接
     */
    public abstract void disconnect();

    /**
     * 销毁连接
     */
    public abstract void release();

    /**
     * 销毁连接，不通知观察者
     */
    public abstract void releaseNoEvent();

    /**
     * 获取连接状态
     */
    public abstract int getState();

    /**
     * 清除请求队列，不触发事件
     */
    public abstract void clearQueue();

    /**
     * 写数据，加入队列尾部
     *
     * @param tag      数据标识
     * @param value    要写入的数据
     * @param callback 写入回调。不为null时，写入结果以回调返回；传null时，写入结果以通知观察者方式返回
     */
    public abstract void write(@Nullable String tag, @NonNull byte[] value, @Nullable WriteCallback callback);

    /**
     * 写数据，加入队列最前
     *
     * @param tag      数据标识
     * @param value    要写入的数据
     * @param callback 写入回调。不为null时，写入结果以回调返回；传null时，写入结果以通知观察者方式返回
     */
    public abstract void writeImmediately(@Nullable String tag, @NonNull byte[] value, @Nullable WriteCallback callback);
}
