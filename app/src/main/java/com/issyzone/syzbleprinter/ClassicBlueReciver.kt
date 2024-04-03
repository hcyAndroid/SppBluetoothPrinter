package com.issyzone.syzbleprinter

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

private class ClassicBlueReciver : BroadcastReceiver() {
    private val TAG = "ClassicBlueReciver"
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                val device =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice?
                val deviceName = device?.name.toString()
                val deviceHardwareAddress = device?.address // MAC address
                Log.i(TAG, "经典蓝牙设备连接成功${deviceName}====${deviceHardwareAddress}")
            }

            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                val device =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice?
                val deviceName = device?.name.toString()
                val deviceHardwareAddress = device?.address // MAC address
                Log.e(TAG, "经典蓝牙设备连接失败${deviceName}====${deviceHardwareAddress}")
            }

            BluetoothDevice.ACTION_FOUND -> {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                val device =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice?
                val deviceName = device?.name.toString()
                val deviceHardwareAddress = device?.address // MAC address
                Log.i(TAG, "经典蓝牙扫描设备>>>${deviceName}")
                Log.i(TAG, "经典蓝牙扫描设备mac>>>${deviceHardwareAddress}")
//                device?.let {
//                    if ((!it.name.isNullOrEmpty()) && (!it.address.isNullOrEmpty()) && ((it.name.lowercase()
//                            .startsWith("fm")) || (it.name.lowercase().startsWith("rw")))
//                    ) {
//                        mylist.add(it)
//                    }
//                }

            }
        }
    }

}