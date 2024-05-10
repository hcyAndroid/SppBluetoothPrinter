package com.issyzone.blelibs.dataimp

import android.bluetooth.BluetoothDevice
import com.issyzone.blelibs.bean.MPMessage
import com.issyzone.blelibs.data.SyzPrinterState2


interface DeviceInfoCall {
    fun getDeviceInfo(msg: MPMessage.MPDeviceInfoMsg)
    fun getDeviceInfoError(errorMsg: MPMessage.MPCodeMsg)
}

interface DeviceBluInfoCall {
    fun getBleNotifyInfo(isSuccess: Boolean, msg: MPMessage.MPCodeMsg?)

}

interface CancelPrintCallBack {
    fun cancelSuccess();

    fun cancelFail();
}

interface BlePrinterInfoCall2 {
    fun getBluNotifyInfo(isSuccess: Boolean, msg: SyzPrinterState2)

}


interface BlePrintingCallBack {
    fun printing(currentPrintPage:Int,totalPage:Int)
    fun getPrintResult(isSuccess: Boolean, msg: SyzPrinterState2)

}

interface SyzBleCallBack {
    fun onStartConnect()
    fun onConnectFail(msg: String?)

    //fun onConnectFailNeedUserRestart(bleDevice: BluetoothDevice?, msg: String)
    fun onConnectSuccess(device:BluetoothDevice)
    fun onDisConnected()
}

