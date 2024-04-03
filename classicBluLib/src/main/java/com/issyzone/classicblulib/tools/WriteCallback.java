package com.issyzone.classicblulib.tools;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

/**
 */
public interface WriteCallback {
    /**
     * 写入结果
     *
     * @param device 设备
     * @param tag    写入时设置的tag
     * @param value  要写入的数据
     * @param result 写入结果
     */
    void onWrite(@NonNull BluetoothDevice device, @NonNull String tag, @NonNull byte[] value, boolean result);
}
