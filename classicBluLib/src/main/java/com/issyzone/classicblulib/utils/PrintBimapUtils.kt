package com.issyzone.classicblulib.utils

import android.util.Log
import com.issyzone.classicblulib.bean.LogLiveData
import com.issyzone.classicblulib.bean.MPMessage
import com.issyzone.classicblulib.callback.BluPrinterInfoCall2
import com.issyzone.classicblulib.callback.BluPrintingCallBack
import com.issyzone.classicblulib.callback.SyzPrinterState2
import com.issyzone.classicblulib.service.SyzClassicBluManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


class PrintBimapUtils {
    private val TAG = "PrintBimapUtils"
    private var totalPage = 0;//打印的总页数
    private var currentPrintPage = 0
    private var isPrintingState = false //判断是否是打印中的状态
    private var isCancelPrinting=false

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

    private var bitmapCall: BluPrintingCallBack? = null //打印回调
    fun setBimapCallBack(callBack: BluPrintingCallBack): PrintBimapUtils {
        this.bitmapCall = callBack
        return this
    }

    fun setBitmapTask(
        bitmapdataList: MutableList<MutableList<MPMessage.MPSendMsg>>, page: Int
    ): PrintBimapUtils {
        bitMapPrintTaskList.clear()
        bitmapdataList.forEach {
            bitMapPrintTaskList.add(it)
        }
        totalPage = bitMapPrintTaskList.size * page
        return this
    }

    fun setBitmapTask2(
        bitmapdataList: MutableList<MutableList<MutableList<MPMessage.MPSendMsg>>>, page: Int
    ): PrintBimapUtils {
        bitMapPrintTaskList.clear()

        bitmapdataList.forEach {bitmap->
            //每张图
            var eachBitmap= mutableListOf<MPMessage.MPSendMsg>()
            bitmap.forEach {duanList->
                //每段
                duanList.forEach{
                    //每包
                    eachBitmap.add(it)
                }
            }
            bitMapPrintTaskList.add(eachBitmap)
        }
        totalPage = bitMapPrintTaskList.size * page
        return this
    }

    fun getPrinterState(): Boolean {
        return isPrintingState
    }

    //收到取消答应的通知
    fun setPrinterCancel(){
        isCancelPrinting=true;
        Log.i(TAG, "收到取消打印的指令，不会再发包")
        bitmapCall?.getPrintResult(false,SyzPrinterState2.PRINTER_CANCEL_PRINT)
    }

    //收到code10 info0 的状态代表一张图片打印成功
    fun updatePrintProcess() {
        currentPrintPage++
        Log.i(TAG, "  当前打印进度==第${currentPrintPage}打印完成==一共${totalPage}")
        bitmapCall?.printing(currentPrintPage = currentPrintPage, totalPage = totalPage)
        if (currentPrintPage == totalPage) {
            //代表打印完成
            Log.i(TAG, "全部打印完成")
            bitmapCall?.getPrintResult(true, SyzPrinterState2.PRINTER_OK)
            isPrintingState = false
        }
    }

    fun handlePrintingMistakes(state: SyzPrinterState2) {
        //处理打印过程中的错误
        Log.i(TAG, "当前的打印状态${isPrintingState}")
        if (isPrintingState) {
            bitmapCall?.getPrintResult(false, state)
        }
    }
    fun doPrintTest() {
        val doFirst = bitMapPrintTaskList.firstOrNull()
        if (serviceScope != null && serviceScope!!.isActive) {
            serviceScope!!.cancel()
        }
        serviceScope = CoroutineScope(Dispatchers.IO)
        serviceScope!!.launch {
            bitMapPrintTaskList.forEach {doFirst->
                if (doFirst!=null){
                    SyzClassicBluManager.getInstance().fmWriteABF4(doFirst)
                }
            }
           // val doFirst = bitMapPrintTaskList.firstOrNull()


        }
    }

    fun doPrint() {
        if (bitMapPrintTaskList.isNullOrEmpty()) {
            Log.d("$TAG", "当前打印任务为0")
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
                    async { SyzClassicBluManager.getInstance().getPrintStatus() .second}
                val stateFlag = getDeviceStateTask.await()
                //SyzClassicBluManager.getInstance().fmWriteABF4(doFirst)
                if (stateFlag == SyzPrinterState2.PRINTER_OK) {
                    currentPrintPage = 0
                    bitmapCall?.printing(currentPrintPage = currentPrintPage, totalPage = totalPage)
                    isPrintingState = true
                    isCancelPrinting=false
                    SyzClassicBluManager.getInstance().fmWriteABF4(doFirst)
                } else {
                    Log.e(TAG, "打印机状态异常不能打印==${stateFlag}")
                    isPrintingState = false
                    isCancelPrinting=false
                    bitmapCall?.getPrintResult(false, stateFlag)
                }
            }
        }
    }


   private  fun doPrint2() {
        if (bitMapPrintTaskList.isNullOrEmpty()) {
            Log.d(TAG, "图片所有的包发完>>>")
            // LogLiveData.addLogs("所有图片打印成功》》》")
            return
        }
        if (serviceScope != null && serviceScope!!.isActive) {
            serviceScope!!.cancel()
        }
        serviceScope = CoroutineScope(Dispatchers.IO)
        serviceScope!!.launch {
            val doFirst = bitMapPrintTaskList.firstOrNull()
            if (doFirst != null) {
                if (isCancelPrinting){
                    //取消打印了
                    bitMapPrintTaskList.clear()
                    isPrintingState = false
                }else{
                    SyzClassicBluManager.getInstance().fmWriteABF4(doFirst)
                }
            }
        }
    }

    fun removePrintWhenSuccessAndPrintNext() {
        // Log.d("$TAG", "PrintBimapUtils>>>一张图片打印成功》》》")
        if (bitMapPrintTaskList != null && bitMapPrintTaskList.size != 0) {
             val doFistAlready = bitMapPrintTaskList.removeFirst()
              doPrint2()
//            val doFistAlready = bitMapPrintTaskList.removeFirst()
//            //判定是否取消打印，如果取消打印了，就不应该再打了
//            if (SyzClassicBluManager.isCancelPrinting) {
//                //收到了取消打印的指令
//                bitMapPrintTaskList.clear()
//
//                if (isPrintingState) {
//                    isPrintingState = false
//                }
//                Log.e("$TAG", "收到取消打印的指令，不会再发包,当前打印状态${isPrintingState}")
//                allDown.invoke()
//            } else {
//                Log.d("$TAG", "没有收到取消打印的指令，开始下一张打印")
//
//            }
        } else {
            Log.d("$TAG", "所有图片全部发包成功")
           // allDown.invoke()
        }

    }


    fun removePrintWhenSuccessAndPrintNextDuan() {
        // Log.d("$TAG", "PrintBimapUtils>>>一张图片打印成功》》》")
        if (bitMapPrintTaskList != null && bitMapPrintTaskList.size != 0) {
            val doFistAlready = bitMapPrintTaskList.removeFirst()
            doPrint2()
//            val doFistAlready = bitMapPrintTaskList.removeFirst()
//            //判定是否取消打印，如果取消打印了，就不应该再打了
//            if (SyzClassicBluManager.isCancelPrinting) {
//                //收到了取消打印的指令
//                bitMapPrintTaskList.clear()
//
//                if (isPrintingState) {
//                    isPrintingState = false
//                }
//                Log.e("$TAG", "收到取消打印的指令，不会再发包,当前打印状态${isPrintingState}")
//                allDown.invoke()
//            } else {
//                Log.d("$TAG", "没有收到取消打印的指令，开始下一张打印")
//
//            }
        } else {
            Log.d("$TAG", "所有图片全部发包成功")
            // allDown.invoke()
        }

    }


    private fun release() {
        if (serviceScope != null && serviceScope!!.isActive) {
            serviceScope!!.cancel()
            serviceScope = null
        }
        if (bitmapCall != null) {
            bitmapCall = null
        }
        if (instance != null) {
            instance = null
        }
    }

//    fun isCompleteBitmapPrinter(): Boolean {
//        return bitMapPrintTaskList.isNullOrEmpty()
//    }

}