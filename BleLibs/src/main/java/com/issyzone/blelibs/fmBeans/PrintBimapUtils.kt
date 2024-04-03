package com.issyzone.blelibs.fmBeans

import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
import com.issyzone.blelibs.bluetooth.BleBluetooth
import com.issyzone.blelibs.service.SyzBleManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


class PrintBimapUtils {
    private val TAG = "PrintBimapUtils"

    companion object {
        private var bitMapPrintTaskList = mutableListOf<MutableList<MPMessage.MPSendMsg>>()
        private var instance: PrintBimapUtils? = null
        private var serviceScope: CoroutineScope? = null
        fun getInstance(): PrintBimapUtils {
            if (instance == null) {
                serviceScope = CoroutineScope(Dispatchers.IO)
                instance = PrintBimapUtils()
            }
            return instance!!
        }
    }

    private var bleBlue: BleBluetooth? = null;
    private var bleCharac: BluetoothGattCharacteristic? = null

    fun setBitmapTask(
        bitmapdataList: MutableList<MutableList<MPMessage.MPSendMsg>>,
        bleBlue: BleBluetooth,
        bleCharac: BluetoothGattCharacteristic
    ): PrintBimapUtils {
        bitMapPrintTaskList.clear()
        this.bleBlue = bleBlue
        this.bleCharac = bleCharac
        bitmapdataList.forEach {
            bitMapPrintTaskList.add(it)
        }
        return this
    }

    fun doPrint() {
        if (bitMapPrintTaskList.isNullOrEmpty()) {
            Log.d("$TAG", "PrintBimapUtils>>>所有图片打印成功》》》")
            return
        }
        if (serviceScope != null && serviceScope!!.isActive) {
            serviceScope!!.cancel()
        }
        serviceScope = CoroutineScope(Dispatchers.IO)
        serviceScope!!.launch {
            val doFirst = bitMapPrintTaskList.firstOrNull()
            if (doFirst != null) {
                SyzBleManager.getInstance().fmWriteABF4(doFirst, bleBlue!!, bleCharac!!)
            }
        }
    }

    fun removePrintWhenSuccess() {
        Log.d("$TAG", "PrintBimapUtils>>>一张图片打印成功》》》")
        if (bitMapPrintTaskList != null && bitMapPrintTaskList.size != 0) {
            val doFistAlready = bitMapPrintTaskList.removeFirst()
            Log.d("$TAG", "开始下一张打印")
            doPrint()
        } else {
            Log.d("$TAG", "bitmap全部成功")
        }

    }


    fun isCompleteBitmapPrinter(): Boolean {
        return bitMapPrintTaskList.isNullOrEmpty()
    }


}