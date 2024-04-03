package com.issyzone.blelibs.classicblu

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Parcelable

class SyzClassicBluReciver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent!!.action
        if (action != null) {
            val device =
                intent!!.getParcelableExtra<Parcelable>("android.bluetooth.device.extra.DEVICE") as BluetoothDevice?

            when (action) {
                "android.bluetooth.adapter.action.STATE_CHANGED", "android.bluetooth.device.action.ACL_DISCONNECT_REQUESTED" -> {}
                "android.bluetooth.adapter.action.DISCOVERY_STARTED" -> {}

                "android.bluetooth.adapter.action.DISCOVERY_FINISHED" -> {}

                "android.bluetooth.device.action.FOUND" -> {}

                "android.bluetooth.device.action.ACL_DISCONNECTED" -> {}

                "android.bluetooth.device.action.BOND_STATE_CHANGED" -> {}

                else -> {}
            }
        }
    }
}