package com.issyzone.classicblulib.callback

import android.bluetooth.BluetoothDevice
import com.issyzone.classicblulib.bean.MPMessage


interface DeviceInfoCall {
    fun getDeviceInfo(msg: MPMessage.MPDeviceInfoMsg)
    fun getDeviceInfoError(errorMsg: MPMessage.MPCodeMsg)
}

interface DeviceBleInfoCall {
    fun getBleNotifyInfo(isSuccess: Boolean, msg: MPMessage.MPCodeMsg?)

}

interface CancelPrintCallBack {
    fun cancelSuccess();

    fun cancelFail();
}

interface BluPrinterInfoCall2 {
    fun getBluNotifyInfo(isSuccess: Boolean, msg: SyzPrinterState2)

}


interface BluPrintingCallBack {
    fun printing(currentPrintPage:Int,totalPage:Int)
    fun getPrintResult(isSuccess: Boolean, msg: SyzPrinterState2)

}

interface SyzBluCallBack {
    fun onStartConnect()
    fun onConnectFail(msg: String?)

    //fun onConnectFailNeedUserRestart(bleDevice: BluetoothDevice?, msg: String)
    fun onConnectSuccess(device:BluetoothDevice)
    fun onDisConnected()
}

