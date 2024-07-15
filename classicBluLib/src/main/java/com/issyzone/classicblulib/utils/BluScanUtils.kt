package com.issyzone.classicblulib.utils
import android.bluetooth.BluetoothDevice
import com.issyzone.classicblulib.tools.BTManager

object BluScanUtils {
    //处理扫描的回调数据
    private suspend  fun handleDiscoveryCallback(
        start: Boolean,
        device: BluetoothDevice?,
        rssi: Int,
        errorCode: Int,
        errorMsg: String
    ) {
      //  BTManager.getInstance().
    }
}