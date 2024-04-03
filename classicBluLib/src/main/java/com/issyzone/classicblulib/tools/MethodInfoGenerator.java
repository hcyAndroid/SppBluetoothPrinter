package com.issyzone.classicblulib.tools;


import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.issyzone.classicblulib.common.MethodInfo;


/**

 */
class MethodInfoGenerator {
    static MethodInfo onBluetoothAdapterStateChanged(int state) {
        return new MethodInfo("onBluetoothAdapterStateChanged", new MethodInfo.Parameter(int.class, state));
    }

    static MethodInfo onConnectionStateChanged(@NonNull BluetoothDevice device, @NonNull UUIDWrapper wrapper, int state) {
        return new MethodInfo("onConnectionStateChanged", new MethodInfo.Parameter(BluetoothDevice.class, device),
                new MethodInfo.Parameter(UUIDWrapper.class, wrapper), new MethodInfo.Parameter(int.class, state));
    }

    static MethodInfo onRead(@NonNull BluetoothDevice device, @NonNull UUIDWrapper wrapper, byte[] value) {
        return new MethodInfo("onRead", new MethodInfo.Parameter(BluetoothDevice.class, device),
                new MethodInfo.Parameter(UUIDWrapper.class, wrapper), new MethodInfo.Parameter(byte[].class, value));
    }

    static MethodInfo onWrite(@NonNull BluetoothDevice device, @NonNull UUIDWrapper wrapper, @NonNull String tag, @NonNull byte[] value, boolean result) {
        return new MethodInfo("onWrite", new MethodInfo.Parameter(BluetoothDevice.class, device),
                new MethodInfo.Parameter(UUIDWrapper.class, wrapper), new MethodInfo.Parameter(String.class, tag),
                new MethodInfo.Parameter(byte[].class, value), new MethodInfo.Parameter(boolean.class, result));
    }
}
