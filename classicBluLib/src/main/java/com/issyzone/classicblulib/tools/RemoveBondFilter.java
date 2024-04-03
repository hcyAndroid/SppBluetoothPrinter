package com.issyzone.classicblulib.tools;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

/**
 * 清空已配对设备时的过滤器
 */
public interface RemoveBondFilter {
    boolean accept(@NonNull BluetoothDevice device);
}
