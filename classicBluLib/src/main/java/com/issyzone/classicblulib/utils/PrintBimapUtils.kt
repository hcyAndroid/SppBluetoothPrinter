package com.issyzone.classicblulib.utils

import android.util.Log
import com.issyzone.classicblulib.bean.LogLiveData
import com.issyzone.classicblulib.bean.MPMessage
import com.issyzone.classicblulib.callback.BluPrinterInfoCall2
import com.issyzone.classicblulib.service.SyzClassicBluManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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

    private var bitmapCall: BluPrinterInfoCall2? = null
    fun setBimapCallBack(callBack: BluPrinterInfoCall2): PrintBimapUtils {
        this.bitmapCall = callBack
        return this
    }

    fun setBitmapTask(
        bitmapdataList: MutableList<MutableList<MPMessage.MPSendMsg>>,
    ): PrintBimapUtils {
        bitMapPrintTaskList.clear()
        bitmapdataList.forEach {
            bitMapPrintTaskList.add(it)
        }
        return this
    }

    fun doPrint() {
        if (bitMapPrintTaskList.isNullOrEmpty()) {
            Log.d("$TAG", "PrintBimapUtils>>>所有图片打印成功》》》")
            LogLiveData.addLogs("所有图片打印成功》》》")
            return
        }
        if (serviceScope != null && serviceScope!!.isActive) {
            serviceScope!!.cancel()
        }
        serviceScope = CoroutineScope(Dispatchers.IO)
        serviceScope!!.launch {
            val doFirst = bitMapPrintTaskList.firstOrNull()
            if (doFirst != null) {
                val getDeviceStateTask =
                    async { SyzClassicBluManager.getInstance().getPrintStatus(bitmapCall) }
                val stateFlag = getDeviceStateTask.await()

                if (stateFlag) {
                    SyzClassicBluManager.getInstance().fmWriteABF4(doFirst)
                } else {
                    Log.e(TAG, "打印机状态异常不能打印")
                   // release()//释放资源
                }
            }
        }
    }


    fun doPrint2() {
        if (bitMapPrintTaskList.isNullOrEmpty()) {
            Log.d("$TAG", "PrintBimapUtils>>>所有图片打印成功》》》")
            LogLiveData.addLogs("所有图片打印成功》》》")
            return
        }
        if (serviceScope != null && serviceScope!!.isActive) {
            serviceScope!!.cancel()
        }
        serviceScope = CoroutineScope(Dispatchers.IO)
        serviceScope!!.launch {
            val doFirst = bitMapPrintTaskList.firstOrNull()
            if (doFirst != null) {
                SyzClassicBluManager.getInstance().fmWriteABF4(doFirst)
            }
        }
    }

    fun removePrintWhenSuccess(allDown: () -> Unit) {
        Log.d("$TAG", "PrintBimapUtils>>>一张图片打印成功》》》")
        if (bitMapPrintTaskList != null && bitMapPrintTaskList.size != 0) {
            val doFistAlready = bitMapPrintTaskList.removeFirst()
            //判定是否取消打印，如果取消打印了，就不应该再打了
            if (SyzClassicBluManager.isCancelPrinting) {
                //收到了取消打印的指令
                bitMapPrintTaskList.clear()
                Log.e("$TAG", "收到取消打印的指令，bitmap全部成功")
                allDown.invoke()
            } else {
                Log.d("$TAG", "没有收到取消打印的指令，开始下一张打印")
                doPrint2()
            }
        } else {
            Log.d("$TAG", "bitmap全部成功")
            allDown.invoke()
        }

    }


   private  fun release() {
        if (serviceScope != null && serviceScope!!.isActive) {
            serviceScope!!.cancel()
            serviceScope=null
        }
        if (bitmapCall!=null){
            bitmapCall=null
        }
        if (instance != null) {
            instance = null
        }
    }

    fun isCompleteBitmapPrinter(): Boolean {
        return bitMapPrintTaskList.isNullOrEmpty()
    }

}