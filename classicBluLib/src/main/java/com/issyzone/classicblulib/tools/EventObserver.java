package com.issyzone.classicblulib.tools;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.issyzone.classicblulib.common.Observer;


/**
 * 各种事件。蓝牙状态，连接状态，接收到数据等等
 */
public interface EventObserver extends Observer {
    /**
     * 蓝牙开关状态变化
     *
     * @param state {@link BluetoothAdapter#STATE_OFF}等
     */
    default void onBluetoothAdapterStateChanged(int state) {
    }

    /**
     * 收到数据
     *
     * @param device  设备
     * @param wrapper 包含通信的UUID
     * @param value   收到的数据
     */
    default void onRead(@NonNull BluetoothDevice device, @NonNull UUIDWrapper wrapper, @NonNull byte[] value) {
    }

    /**
     * 写入结果
     *
     * @param device  设备
     * @param wrapper 包含通信的UUID
     * @param tag     写入时设置的tag
     * @param value   要写入的数据
     * @param result  写入结果
     */
    default void onWrite(@NonNull BluetoothDevice device, @NonNull UUIDWrapper wrapper, @NonNull String tag, @NonNull byte[] value, boolean result) {
    }

    /**
     * 连接状态变化
     *
     * @param device  设备
     * @param wrapper 包含通信的UUID
     * @param state   设备。状态{@link Connection#STATE_CONNECTED}，可能的值{@link Connection#STATE_RELEASED}等
     */
    default void onConnectionStateChanged(@NonNull BluetoothDevice device, @NonNull UUIDWrapper wrapper, int state) {
    }
}
