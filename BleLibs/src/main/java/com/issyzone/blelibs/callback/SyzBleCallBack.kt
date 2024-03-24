package com.issyzone.blelibs.callback
import com.issyzone.blelibs.data.BleDevice
interface  SyzBleCallBack {
     fun onStartConnect()
     fun onConnectFail(bleDevice: BleDevice?, msg: String)
     fun onConnectFailNeedUserRestart(bleDevice: BleDevice?, msg: String)
     fun onConnectSuccess(bleDevice: BleDevice?)
     fun onDisConnected(device: BleDevice?)

}