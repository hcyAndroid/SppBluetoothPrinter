package com.issyzone.classicblulib.tools;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public interface ConnectCallback {
    /**
     * 连接成功
     */
    void onSuccess( BluetoothDevice device);

    /**
     * 连接失败
     *
     * @param errMsg 错误信息
     * @param e      异常
     */
    void onFail(@NonNull String errMsg, @Nullable Throwable e);
}
