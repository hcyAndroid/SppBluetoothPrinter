package com.issyzone.classicblulib.utils

import android.util.Log
import com.issyzone.classicblulib.bean.LogLiveData
import com.issyzone.classicblulib.bean.MPMessage
import com.issyzone.classicblulib.callback.BluPrintingCallBack
import com.issyzone.classicblulib.callback.SyzPrinterState2
import com.issyzone.classicblulib.service.SyzClassicBluManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * 二寸打印逻辑
 * 取消打印的逻辑：是打完当前张，停止下发下一张的包
 * //判断是否是打印中的状态只有打印中才会取消
 */
class FmPrintBimapUtils {
    private val TAG = "FmPrintBimapUtils"
    private var totalPage = 0;//打印的总页数
    private var page=1;
    private var currentPrintPage = 0//当前打印的页数
    private var isPrintingState = false //判断是否是打印中的状态
    private var isCancelPrinting = false  //判断是否取消打印
    private var currentSendBitMapIndex = -1
    private var currentSendBitMapDuanIndex = -1
    private var currentSendPrintPage = 0//当前发送打印的页数

    companion object {
        private var currentBitMapList = mutableListOf<MutableList<MPMessage.MPSendMsg>>()
        private var allBitMapList = mutableListOf<MutableList<MutableList<MPMessage.MPSendMsg>>>()
        private var instance: FmPrintBimapUtils? = null
        private var serviceScope: CoroutineScope? = null
        fun getInstance(): FmPrintBimapUtils {
            if (instance == null) {
                serviceScope = CoroutineScope(Dispatchers.IO)
                instance = FmPrintBimapUtils()
            }
            return instance!!
        }
    }

    private var bitmapCall: BluPrintingCallBack? = null //打印回调
    fun setBimapCallBack(callBack: BluPrintingCallBack): FmPrintBimapUtils {
        this.bitmapCall = callBack
        return this
    }

    fun upackerFaiLed(){
        bitmapCall?.getPrintResult(false,SyzPrinterState2.PRINTER_UPACKER_FAILED)
    }


    //已经没有page的说法
    fun setBitmapTask(
        bitmapdataList: MutableList<MutableList<MutableList<MPMessage.MPSendMsg>>>,
        page:Int
    ): FmPrintBimapUtils {
        testAllBitmapSize(bitmapdataList)
        allBitMapList.clear()
        this.page=page
        bitmapdataList.forEach {
            allBitMapList.add(it)
        }
        currentSendBitMapIndex = -1
        currentSendBitMapDuanIndex = -1
        totalPage = allBitMapList.size*page

        return this
    }


    private fun testAllBitmapSize(bitmapdataList: MutableList<MutableList<MutableList<MPMessage.MPSendMsg>>>) {
        var totalSize = 0L
        bitmapdataList.forEachIndexed { index, mutableLists ->
            val eachPicSize = getEachBitmapByteCount(mutableLists)
            totalSize += eachPicSize
            Log.e(
                TAG, "第${index}张图片===发送图片的数据大小==${eachPicSize}"
            )
            LogLiveData.addLogs(
                "第${index}张图片===发送图片的数据大小==${
                    eachPicSize
                }"
            )
        }
        LogLiveData.addLogs(
            "打印${bitmapdataList.size}张图片===发送图片的数据的总大小==${
                totalSize + 6
            }"
        )
    }

    private fun getEachBitmapByteCount(bitmap: MutableList<MutableList<MPMessage.MPSendMsg>>): Long {
        var bytecount = 0L
        bitmap.forEach { duanList ->
            duanList.forEach {
                bytecount += Upacker.frameEncode(it.toByteArray()).size
            }
        }
        return bytecount
    }


    suspend fun doTest() {
        allBitMapList.forEach {
            it.forEach { duan ->
                SyzClassicBluManager.getInstance().fmWriteABF4(duan)
            }
        }
    }

    fun getPrinterState(): Boolean {
        return isPrintingState
    }

    //收到取消答应的通知
    fun setPrinterCancel() {
        isCancelPrinting = true;
        Log.i(TAG, "UUYYY收到取消打印的指令，不会再发包${isCancelPrinting}")
        bitmapCall?.getPrintResult(false, SyzPrinterState2.PRINTER_CANCEL_PRINT)
    }

    //收到code10 info0 的状态代表一张图片打印成功
    fun updatePrintProcess() {
        currentPrintPage++
   /*     if (isCancelPrinting){
            //这里可以做取消打印发送和收到一致

        }*/
        Log.i(TAG, "当前打印进度==第${currentPrintPage}打印完成==一共${totalPage}===已经发送的图片数==${currentSendPrintPage}")
        LogLiveData.addLogs("当前打印进度==第${currentPrintPage}打印完成==一共${totalPage}")
        bitmapCall?.printing(currentPrintPage = currentPrintPage, totalPage = totalPage)
        if (currentPrintPage == totalPage) {
            //代表打印完成
            Log.i(TAG, "全部打印完成")
            LogLiveData.addLogs("全部打印完成")
            bitmapCall?.getPrintResult(true, SyzPrinterState2.PRINTER_OK)
            isPrintingState = false
        }
    }

    fun handlePrintingMistakes(state: SyzPrinterState2) {
        //处理打印过程中的错误
        Log.i(TAG, "当前的打印状态${isPrintingState}")
        if (isPrintingState) {
            bitmapCall?.getPrintResult(false, state)
            isPrintingState=false
        }
    }

    //每次取图片任务中的第一个
    private fun getCurrentBitMapTask() {
        val bitmapData = allBitMapList.firstOrNull()
        if (!bitmapData.isNullOrEmpty()) {
            currentBitMapList.clear()
            currentSendBitMapIndex++
            bitmapData.forEach {
                currentBitMapList.add(it)  //每段加入
            }
            currentSendBitMapDuanIndex = -1
        } else {
            Log.i(TAG, "图片的包已经全部发完")
        }
    }

    suspend fun writeEachDuanOneBitmap() {
        val duan = currentBitMapList.firstOrNull()
        if (!duan.isNullOrEmpty()) {
            currentSendBitMapDuanIndex++
            Log.e(
                TAG,
                "向蓝牙设备写入第${currentSendBitMapIndex}图=====第${currentSendBitMapDuanIndex}段的数据"
            )
            SyzClassicBluManager.getInstance().fmWriteABF4(duan)
        }
    }

    fun doPrint() {
        if (allBitMapList.isEmpty()) {
            Log.d(TAG, "当前打印任务为0")
            return
        }
        if (serviceScope != null && serviceScope!!.isActive) {
            serviceScope!!.cancel()
        }
        serviceScope = CoroutineScope(Dispatchers.IO)
        serviceScope!!.launch {
            getCurrentBitMapTask()
            if (currentBitMapList.isNotEmpty()) {
                val getDeviceStateTask =
                    async { SyzClassicBluManager.getInstance().getPrintStatus() }
                val stateFlag = getDeviceStateTask.await()
                if (stateFlag == SyzPrinterState2.PRINTER_OK) {
                    currentPrintPage = 0
                    bitmapCall?.printing(currentPrintPage = currentPrintPage, totalPage = totalPage)
                    isPrintingState = true
                    isCancelPrinting = false
                    //获取当前图片的所有段
                    writeEachDuanOneBitmap()//写入一段
                    //SyzClassicBluManager.getInstance().fmWriteABF4(doFirst)
                } else {
                    Log.e(TAG, "打印机状态异常不能打印==${stateFlag}")
                    isPrintingState = false
                    isCancelPrinting = false
                    bitmapCall?.getPrintResult(false, stateFlag)
                    release()
                }
            }
        }
    }

    fun doTestPrint() {
        if (allBitMapList.isEmpty()) {
            Log.d(TAG, "当前打印任务为0")
            return
        }
        if (serviceScope != null && serviceScope!!.isActive) {
            serviceScope!!.cancel()
        }
        serviceScope = CoroutineScope(Dispatchers.IO)
        serviceScope!!.launch {
            getCurrentBitMapTask()
            if (currentBitMapList.isNotEmpty()) {
                currentPrintPage = 0
                bitmapCall?.printing(currentPrintPage = currentPrintPage, totalPage = totalPage)
                isPrintingState = true
                isCancelPrinting = false
                //获取当前图片的所有段
                writeEachDuanOneBitmap()//写入一段
            }
        }
    }

    fun doTestNextPrint() {
        //doPrint2()
    }

    private val mutex = Mutex()
    suspend fun doPrintFirst() = withContext(Dispatchers.IO) {
        mutex.withLock {
            getCurrentBitMapTask()
            if (currentBitMapList.isNotEmpty()) {
                val getDeviceStateTask =
                    async { SyzClassicBluManager.getInstance().getPrintStatus() }
                val stateFlag = getDeviceStateTask.await()
                if (stateFlag == SyzPrinterState2.PRINTER_OK) {
                    currentPrintPage = 0
                    currentSendPrintPage=0
                    bitmapCall?.printing(currentPrintPage = currentPrintPage, totalPage = totalPage)
                    isPrintingState = true
                    isCancelPrinting = false
                    //获取当前图片的所有段
                    writeEachDuanOneBitmap()//写入一段
                    //SyzClassicBluManager.getInstance().fmWriteABF4(doFirst)
                } else {
                    Log.e(TAG, "打印机状态异常不能打印==${stateFlag}")
                    isPrintingState = false
                    isCancelPrinting = false
                    bitmapCall?.getPrintResult(false, stateFlag)
                    release()
                }
            }
        }
    }


    private suspend fun doPrintSecond() = withContext(Dispatchers.IO) {
        // 注意：现在不再需要创建新的 CoroutineScope，因为我们用的是 withContext
        // 互斥锁，保证同时间内只有一个协程运行以下代码
        //因为设备哪里会突然连续返回code11 info2
        mutex.withLock {
            if (currentBitMapList.isNotEmpty()) {
                currentBitMapList.removeFirst() // 移除上一段
            }
            if (currentBitMapList.isEmpty()) {
                if (allBitMapList.isNotEmpty()) {
                    allBitMapList.removeFirst()//移除一张图片的任务，上一张图片任务打印完
                    currentSendPrintPage++
                    Log.i(TAG, "移除一张图片的任务，上一张图片发送任务完成")
                }
                if (allBitMapList.isEmpty()) {
                    Log.i(TAG, "所有图片已经发完")
                } else {
                    Log.i(TAG, "UUYYY当前图片已经发完,开始发下一张${isCancelPrinting}=已经发送的图片数==${currentSendPrintPage}")
                    if (isCancelPrinting) {
                        //取消打印了
                        //取消打印了
                        currentBitMapList.clear()
                        allBitMapList.clear()   //图片任务就清空
                        isPrintingState = false
                        Log.i(TAG, "收到取消打印的标记，清空所有的任务=已经发送的图片数==${currentSendPrintPage}")
                    } else {
                        getCurrentBitMapTask()
                        writeEachDuanOneBitmap()//写入
                    }
                }
            } else {
                writeEachDuanOneBitmap()//写入下一段
            }
        }
    }

    /*    private suspend fun doPrint2() = mutex.withLock {
            if (serviceScope != null && serviceScope!!.isActive) {
                serviceScope!!.cancel()
            }
            //serviceScope = CoroutineScope(Dispatchers.IO)
            CoroutineScope(Dispatchers.IO)!!.launch {
                if (currentBitMapList.isNotEmpty()) {
                    currentBitMapList.removeFirst() //移除上一段
                }
                if (currentBitMapList.isEmpty()) {
                    //  Log.i(TAG, "TTTTYYYYYYYYY")
                    if (allBitMapList.isNotEmpty()) {
                        allBitMapList.removeFirst()//移除一张图片的任务，上一张图片任务打印完
                    }
                    if (allBitMapList.isEmpty()) {
                        Log.i(TAG, "所有图片已经发完")
                    } else {
                        Log.i(TAG, "当前图片已经发完,开始发下一张")
                        if (isCancelPrinting) {
                            //取消打印了
                            //取消打印了
                            currentBitMapList.clear()
                            allBitMapList.clear()   //图片任务就清空
                            isPrintingState = false
                            Log.i(TAG, "收到取消打印的标记，清空所有的任务")
                        } else {
                            getCurrentBitMapTask()
                            writeEachDuanOneBitmap()//写入
                        }
                    }
                } else {
                    writeEachDuanOneBitmap()//写入下一段
                }
            }
        }*/


    suspend fun removePrintWhenSuccessAndPrintNext() {
        doPrintSecond()
    }

    private fun release() {
        Log.d(TAG, "打印工具类释放资源")
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