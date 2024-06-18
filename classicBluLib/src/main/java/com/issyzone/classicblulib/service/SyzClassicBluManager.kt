package com.issyzone.classicblulib.service


import android.bluetooth.BluetoothDevice

import android.graphics.Bitmap

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.issyzone.classicblulib.bean.FMPrinterOrder
import com.issyzone.classicblulib.bean.FmNotifyBeanUtils
import com.issyzone.classicblulib.bean.LogLiveData
import com.issyzone.classicblulib.bean.MPMessage
import com.issyzone.classicblulib.bean.NotifyResult
import com.issyzone.classicblulib.bean.NotifyResult2
import com.issyzone.classicblulib.bean.SyzFirmwareType
import com.issyzone.classicblulib.bean.SyzPaperSize
import com.issyzone.classicblulib.bean.SyzPrinter
import com.issyzone.classicblulib.bean.SyzPrinterPaper
import com.issyzone.classicblulib.callback.BluPrintingCallBack
import com.issyzone.classicblulib.callback.BluSelfCheckCallBack
import com.issyzone.classicblulib.callback.CancelPrintCallBack
import com.issyzone.classicblulib.callback.DeviceBleInfoCall
import com.issyzone.classicblulib.callback.DeviceInfoCall
import com.issyzone.classicblulib.callback.SyzBluCallBack
import com.issyzone.classicblulib.callback.SyzPrinterState
import com.issyzone.classicblulib.callback.SyzPrinterState2
import com.issyzone.classicblulib.tools.BTManager
import com.issyzone.classicblulib.tools.ConnectCallback
import com.issyzone.classicblulib.tools.Connection
import com.issyzone.classicblulib.tools.EventObserver
import com.issyzone.classicblulib.tools.UUIDWrapper
import com.issyzone.classicblulib.utils.AppGlobels
import com.issyzone.classicblulib.utils.MsgCallback
import com.issyzone.classicblulib.utils.Upacker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull


class SyzClassicBluManager {
    private val TAG = "SyzClassicBluManager>>"

    companion object {
        private const val PAPER_SIZE_REPORT_CODE = 500  //纸张尺寸上报
        private const val DEX_UPDATE_CODE = 400  //固件升级任务回调
        private const val PIC_SEND_NEXT_CODE = 11  //消费下一段数据,图片打印
        private const val DEX_SEND_NEXT_CODE = 12  //消费下一个4k,固件升级
        private const val RETURN_PRINT_STATE_CODE = 10  //返回打印机的状态
        private const val ORDER_RECIVER_SUCCESS = 200  //命令接收成功
        private var instance: SyzClassicBluManager? = null
        private var bluScope: CoroutineScope? = null
        private var sppReadScope: CoroutineScope? = null
        private var sppBitmapScope: CoroutineScope? = null
        private var sppSelfPrintScope: CoroutineScope? = null
        private var blueNotifyDataProcessor: BlueNotifyDataProcessor? = null

        fun getInstance(): SyzClassicBluManager {
            if (instance == null) {
                instance = SyzClassicBluManager()
                sppSelfPrintScope = CoroutineScope(Dispatchers.IO)
                sppReadScope = CoroutineScope(Dispatchers.IO)
                sppBitmapScope = CoroutineScope(Dispatchers.IO)
                bluScope = CoroutineScope(Dispatchers.IO)
            }
            return instance!!
        }
    }

    /**
     * func:判断蓝牙是否连接
     */
    fun isBluConnected(): Boolean {
        return BTManager.getInstance().isConnect;
    }

    //蓝牙连接回调
    private var bluCallBack: SyzBluCallBack? = null;
    private var currentPrintType = SyzPrinter.SYZTWOINCH
    private var isOpenActiveReportWhenPrinting = false //打印过程中是否把错误也通过主动上报显示
    fun setBluCallBack(bluCallBack: SyzBluCallBack) {
        this.bluCallBack = bluCallBack
    }

    //设置打印过程中是否把错误也通过主动上报显示
    fun setIsOpenActiveReportWhenPrinting(isOpenActiveReportWhenPrinting: Boolean) {
        this.isOpenActiveReportWhenPrinting = isOpenActiveReportWhenPrinting
    }


    //断开连接
    fun disConnectBlu() {
        BTManager.getInstance().disconnectAllConnections()
    }

    fun initClassicBlu() {
        BTManager.getInstance().initialize(AppGlobels.getApplication())
        blueNotifyDataProcessor = BlueNotifyDataProcessor(sppReadScope!!)
        BTManager.getInstance().setSyzBluCallBack(object : SyzBluCallBack {
            override fun onStartConnect() {
                bluCallBack?.onStartConnect()
            }

            override fun onConnectFail(msg: String?) {
                //这里不会走
            }

            override fun onConnectSuccess(device: BluetoothDevice) {
                Log.i(TAG, "连接设备>>>${device.name}")
                currentPrintType = SyzPrinter.values()
                    .find { it.device.lowercase().startsWith(device.name.lowercase()) } ?: return
                Log.i(TAG, "连接到SYZ设备>>>${device.name}==${currentPrintType}")
                bluScope = CoroutineScope(Dispatchers.IO)
                bluScope?.launch {
                    delay(500)
                    //这个命令就是为了触发设备的信息的主动上报，少了他根本不上报
                    writeABF1(FMPrinterOrder.orderForGetFmDevicesInfo(), "${TAG}=getDeviceInfo>>>>")
                }
                bluCallBack?.onConnectSuccess(device)/*    if (ContextCompat.checkSelfPermission(AppGlobels.getApplication(), Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {

                    }else{
                        Log.e(TAG, "没有蓝牙权限")
                    }*/
            }

            override fun onDisConnected() {
                Log.i(TAG, "onDisConnected>>>>")
                bluCallBack?.onDisConnected()

            }
        })
    }

    //主动回调
    private var activelyReportCallBack: ((msg: SyzPrinterState2) -> Unit)? = null


    //打印机纸张尺寸回调
    private var paperReportCallBack: ((paper: SyzPrinterPaper) -> Unit)? = null

    /**
     * 主动上报的回调
     */
    fun setActivelyReportBack(activelyReport: ((msg: SyzPrinterState2) -> Unit)) {
        this.activelyReportCallBack = activelyReport
    }

    fun setPaperReportCallBack(paperReportCallBack: ((msg: SyzPrinterPaper) -> Unit)) {
        this.paperReportCallBack = paperReportCallBack
    }

    /**
     * 固件升级回调
     */
    private var dexUpdateCallBack: ((printState: SyzPrinterState) -> Unit)? = null

    //blu连接状态的回调
    private var bluNotifyCallBack: ((dataArray: ByteArray) -> Unit)? = null


    //添加打印自检页的回调
    private var bluSelfCheckCallBack: ((isWork: Boolean) -> Unit)? = null

    //blu取消打印的回调
    private var cancelPrintCallBack: ((dataArray: ByteArray) -> Unit)? = null


    //处理蓝牙发来的数据,保证线程的安全
    private suspend fun spp_read(readData: ByteArray?) {
        readData?.apply {
            try {
                val mpRespondMsg = MPMessage.MPRespondMsg.parseFrom(this)
                Log.d(
                    "$TAG", " NOTIFY返回的信息 ${
                        mpRespondMsg.toString()
                    }"
                )
                Log.d(
                    "$TAG", " NOTIFY返回respondData ${
                        MPMessage.MPCodeMsg.parseFrom(
                            mpRespondMsg.respondData.toByteArray()
                        ).toString()
                    }"
                )
//                LogLiveData.addLogs(
//                    "蓝牙返回的信息 ${
//                        mpRespondMsg.toString()
//                    }"
//                )
//                LogLiveData.addLogs(
//                    "蓝牙返回respondData ${
//                        MPMessage.MPCodeMsg.parseFrom(
//                            mpRespondMsg.respondData.toByteArray()
//                        ).toString()
//                    }"
//                )
                if (mpRespondMsg.eventType == MPMessage.EventType.SELFTEST) {
                    if (mpRespondMsg.code == ORDER_RECIVER_SUCCESS) {
                        Log.i(TAG, "自检页打印成功")
                        bluSelfCheckCallBack?.invoke(true)
                    } else {
                        Log.e(TAG, "自检页打印失败")
                        bluSelfCheckCallBack?.invoke(false)
                    }
                }

                if (mpRespondMsg.eventType == MPMessage.EventType.DEVICEPRINT) {
                    if (mpRespondMsg.code == 4) {
                        Log.e(TAG, "打印机打印图片出错")
                    }
                }
                if (mpRespondMsg.eventType == MPMessage.EventType.DEVICEREPORT) {
                    if (mpRespondMsg.code == ORDER_RECIVER_SUCCESS) {
                        val mpCodeMsg = MPMessage.MPCodeMsg.parseFrom(
                            mpRespondMsg.respondData.toByteArray()
                        )

                        when (mpCodeMsg.code) {
                            //纸张尺寸上报
                            PAPER_SIZE_REPORT_CODE -> {
                                //纸张尺寸上报,code 500 info:宽*高（mm）
                                val paperSizeStr = mpCodeMsg.info
                                Log.i(TAG, "纸张尺寸==${paperSizeStr}")
                                try {
                                    val paperList = paperSizeStr.split("*").toMutableList()
                                    if (paperList.isNotEmpty() && paperList.size == 2) {
                                        val width: Float = paperList[0].toFloatOrNull() ?: 0.0f
                                        val height: Float = paperList[1].toFloatOrNull() ?: 0.0f
                                        if (width != 0.0f && height != 0.0f) {
                                            //只有宽高都没0的时候才上报
                                            val paper = SyzPrinterPaper(
                                                printerState2 = SyzPrinterState2.PRINTER_HAS_STUDY_PAPER,
                                                paper_width = width,
                                                pager_height = height
                                            )
                                            paperReportCallBack?.invoke(paper)
                                            Log.i(
                                                TAG,
                                                "打印机上报尺寸::width=${paper.paper_width}===height=${paper.pager_height}"
                                            )
                                        } else {
                                            val paper = SyzPrinterPaper(
                                                printerState2 = SyzPrinterState2.PRINTER_NO_STUDY_PAPER,
                                                paper_width = width,
                                                pager_height = height
                                            )
                                            paperReportCallBack?.invoke(paper)
                                            Log.e(
                                                TAG,
                                                "打印机上报尺寸：没学纸::${paperSizeStr}==width=${paper.paper_width}===height=${paper.pager_height}"
                                            )
                                        }
                                    } else {
                                        Log.e(TAG, "打印机上报尺寸出错::${paperSizeStr}")
                                    }
                                } catch (e: Exception) {
                                    Log.e(
                                        TAG,
                                        "打印机上报尺寸出错::${paperSizeStr}====${e.message.toString()}"
                                    )
                                }
                            }

                            DEX_UPDATE_CODE -> {
                                //固件升级任务回调
                                dexUpdateCallBack?.invoke(
                                    FmNotifyBeanUtils.getDexUpdateReport(
                                        MPMessage.MPCodeMsg.parseFrom(
                                            mpRespondMsg.respondData.toByteArray()
                                        )
                                    )
                                )
                            }

                            PIC_SEND_NEXT_CODE -> {
                                if (mpCodeMsg.info == "2") {
                                    //消费下一段数据,图片打印
                                    bitmapPrintHandler?.consumeDuansOneBitmap()
                                }
                            }

                            DEX_SEND_NEXT_CODE -> {
                                if (mpCodeMsg.info == "2") {
                                    //消费下一个4k,固件升级
                                    dexUploadHandler?.consumeOne4K()
                                }
                            }

                            RETURN_PRINT_STATE_CODE -> {
                                //返回打印机的状态
                                val state = FmNotifyBeanUtils.getActivelyReport(
                                    MPMessage.MPCodeMsg.parseFrom(
                                        mpRespondMsg.respondData.toByteArray()
                                    )
                                )
                                if (state == SyzPrinterState2.PRINTER_OK) {
                                    //代表一张图的打印成功

                                    if (bitmapPrintHandler?.getPrinterState() == true) {
                                        Log.i(TAG, "一张图打印成功了")
                                        bitmapPrintHandler?.updatePrintProcess()

                                    } else {
//                                        activelyReportCallBack?.invoke(
//                                            state
//                                        )
                                        handleActiveUp(state)
                                    }

                                } else {
                                    if (bitmapPrintHandler?.getPrinterState() == true) {
                                        bitmapPrintHandler?.handlePrintingMistakes(state)
                                    } else {
                                        handleActiveUp(state)
//                                        activelyReportCallBack?.invoke(
//                                            state
//                                        )
                                    }

                                }
                            }
                        }
                    } else {
                        Log.e(TAG, "主动回调，responseCode!=200")
                    }

                } else {
                    //其他的回调
                    if (mpRespondMsg.eventType == MPMessage.EventType.CANCELPRINTING) {
                        if (currentPrintType != SyzPrinter.SYZTWOINCH) {
                            //二寸不处理
                            //取消打印的命令的回调
                            cancelPrintCallBack?.invoke(this)
                            //
                            bitmapPrintHandler?.setPrinterCancel()
                        }

                    } else {
                        bluNotifyCallBack?.invoke(this)
                    }

                }
            } catch (e: Exception) {
                Log.d(
                    TAG, "$TAG NOTIFY解析数据出错${this.contentToString()}==${e.message.toString()}"
                )
                // LogLiveData.addLogs("$TAG NOTIFY解析数据出错${data.contentToString()}")
            }
        }
    }


    private var lastState: SyzPrinterState2? = null
    private var lastUpdateTime: Long = 0
    private fun handleActiveUp(state: SyzPrinterState2) {
        Log.d(TAG, "主动上报==硬件主动上报的状态==${state}")
        //如果十秒内上报的内容是相同的，就不回调
        val currentTime = System.currentTimeMillis()
        if (state != lastState || currentTime - lastUpdateTime >= 10 * 60 * 1000) {
            // 如果状态发生变化，或者距离上次更新已经超过10秒，那么调用回调函数
            Log.i(TAG, "主动上报==SDK主动上报的状态==${state}")
            activelyReportCallBack?.invoke(state)
            // 更新上一次的状态和更新时间
            lastState = state
            lastUpdateTime = currentTime
        } else {
            Log.e(TAG, "主动上报==十秒内上报的内容是相同的==${state}==不回调")
        }
    }

    private val reciver = object : EventObserver {
        override fun onWrite(
            device: BluetoothDevice,
            wrapper: UUIDWrapper,
            tag: String,
            value: ByteArray,
            result: Boolean
        ) {
            super.onWrite(device, wrapper, tag, value, result)
            //这里监听写入
            // Log.e(TAG, "${tag}========${value}======${result}")
        }

        override fun onRead(device: BluetoothDevice, wrapper: UUIDWrapper, updatasdata: ByteArray) {
            super.onRead(device, wrapper, updatasdata)
            Log.i(TAG, ">>>>>>>>>onRead>>>>>>")
            blueNotifyDataProcessor?.onRead(updatasdata)
        }
    }

    /**
     * 专门用来梳理接收的消息
     */

    private inner class BlueNotifyDataProcessor(private val mySppReadScope: CoroutineScope) {
        private val dataChannel = Channel<ByteArray>(Channel.UNLIMITED)

        init {
            // 启动一个单独的协程，专门用来按顺序处理数据
            mySppReadScope.launch {
                for (data in dataChannel) {
                    try {
                        spp_read(data)
                    } catch (e: Exception) {
                        Log.e(TAG, "解包异常>>>>${e.message.toString()}")
                    }
                }
            }
        }

        private val upacker = Upacker(object : MsgCallback {
            override fun onMsgPrased(prasedData: ByteArray?, len: Int) {
                Log.i(TAG, "Upacker>>>>解包成功")
                prasedData?.apply {
                    dataChannel.trySend(this)
                }
            }

            override fun onMsgFailed() {
                Log.e(TAG, "Upacker>>>>解包失败==")
                if (bitmapPrintHandler?.getPrinterState() == true) {
                    bitmapPrintHandler?.upackerFaiLed()
                } else {
                    activelyReportCallBack?.invoke(SyzPrinterState2.PRINTER_UPACKER_FAILED)
                }
            }
        })

        fun onRead(updatasdata: ByteArray) {
            upacker.unpack(updatasdata)
        }

        // 记得在不需要时关闭Channel，释放资源
        fun close() {
            dataChannel.close()
        }

    }


    private var bitListScope: CoroutineScope? = null

    suspend fun getPrintStatus(): Pair<SyzPaperSize?, SyzPrinterState2> {
        val result = withTimeoutOrNull(ORDER_TIME_OUT) {
            val getDeviceStateTask = async { getDeviceState() }
            getDeviceStateTask.await()
        } ?: Pair(null, SyzPrinterState2.PRINTER_STATUS_UNKNOWN)
        return result
    }

    private suspend fun getDeviceState(): Pair<SyzPaperSize?, SyzPrinterState2> {
        return suspendCancellableCoroutine<Pair<SyzPaperSize?, SyzPrinterState2>> { cancellableContinuation ->
            getDeviceInfo(object : DeviceInfoCall {
                override fun getDeviceInfo(msg: MPMessage.MPDeviceInfoMsg) {
                    val printState = FmNotifyBeanUtils.getPrintStatus(msg)
                    val syzPaperSize = SyzPaperSize.values().find {
                        it.paperSet == msg.paperType
                    }
                    Log.i(TAG, "打印前获取打印机状态===${printState}===纸张类型==${syzPaperSize}")
                    cancellableContinuation.resume(Pair(syzPaperSize, printState)) {
                        Log.e(TAG, "打印前获取打印机状态异常===")
                    }
                }

                override fun getDeviceInfoError(errorMsg: MPMessage.MPCodeMsg) {
                    cancellableContinuation.resume(
                        Pair(
                            null, SyzPrinterState2.PRINTER_STATUS_UNKNOWN
                        )
                    ) {
                        Log.e(TAG, "打印前获取打印机状态异常===")
                    }
                }
            })
        }

    }


    private var bitmapPrintHandler: SyzBitmapProcessor? = null
    private var dexUploadHandler: SyzDexProcessor? = null

    /**
     * 此方法支持多张图片的多份
     * 【A,B,C】 page=2  AA BB CC
     */
    fun printBitmaps(
        bipmaps: MutableList<Bitmap>,
        width: Int,
        height: Int,
        page: Int,
        callBack: BluPrintingCallBack,
        currentPaperSize: SyzPaperSize? = null
    ) {
        bitListScope = CoroutineScope(Dispatchers.IO)
        bitListScope?.launch {
            bitmapPrintHandler = SyzBitmapProcessor.build {
                this.printerType = currentPrintType
                this.bitmapWidth = width
                this.bitmapHeight = height
                this.printPage = page
                this.currentPaperSize = currentPaperSize
            }
            bitmapPrintHandler?.setBimapCallBack(object : BluPrintingCallBack {
                override fun printing(currentPrintPage: Int, totalPage: Int) {
                    callBack.printing(currentPrintPage, totalPage)
                }

                override fun getPrintResult(isSuccess: Boolean, msg: SyzPrinterState2) {
                    callBack.getPrintResult(isSuccess, msg)
                    if (isOpenActiveReportWhenPrinting) {
                        if (!isSuccess) {
                            activelyReportCallBack?.invoke(msg)
                        }
                    }
                }

                override fun checkPaperSizeBeforePrint(
                    isSame: Boolean,
                    printerSize: SyzPaperSize?,
                    doPrintSize: SyzPaperSize?
                ) {
                   callBack.checkPaperSizeBeforePrint(isSame, printerSize, doPrintSize)
                }

                override fun checkPrinterBeforePrint(isOK: Boolean, msg: SyzPrinterState2) {
                    callBack.checkPrinterBeforePrint(isOK, msg)
                }
            })
            if ((bitmapPrintHandler?.checkPrinterStatus() ?: false)) {
                bitmapPrintHandler?.produceBitmaps2(bipmaps)
                bitmapPrintHandler?.doPrint()
            }
        }
    }


    /**
     * 获取设备信息
     */
    fun getDeviceInfo(
        callBack: DeviceInfoCall
    ) {
        bluNotifyCallBack = { dataArray ->
            val result = FmNotifyBeanUtils.getDeviceInfo(dataArray)
            when (result) {
                is NotifyResult.Success -> {
                    //这里可能要去判定打印机状态
                    result.data.printStatus
                    callBack.getDeviceInfo(result.data)
                }

                is NotifyResult.Error -> {
                    callBack.getDeviceInfoError(result.errorMsg)
                }
            }
        }
        writeABF1(FMPrinterOrder.orderForGetFmDevicesInfo(), "${TAG}=getDeviceInfo>>>>")
    }

    private val ORDER_TIME_OUT = 3000L//命令超时时间


    //切换到主线程
    fun onMainThread(callBack: () -> Unit) {
        Handler(Looper.getMainLooper()).post {
            callBack.invoke()
        }
    }

    //检查自检页
    //三秒没收到回调就是失败
    fun writeSelfCheck(callBack: BluSelfCheckCallBack) {
        bluNotifyCallBack = { dataArray ->

        }
        //检查设备信息
        sppSelfPrintScope = CoroutineScope(Dispatchers.IO)
        sppSelfPrintScope?.launch {
            //检查设备信息
            val printStatus = getPrintStatus().second
            if (printStatus == SyzPrinterState2.PRINTER_OK) {
                //状态正常可以打印自检页
                // 创建一个可取消的协程
                val job = GlobalScope.launch {
                    delay(ORDER_TIME_OUT) // 等待3秒
                    // 如果3秒后回调还没有被调用，打印失败信息
                    callBack.getPrintResult(false, SyzPrinterState2.PRINTER_STATUS_UNKNOWN)
                    Log.e(TAG, "自检页打印失败")
                }
                bluSelfCheckCallBack = {
                    if (it) {
                        callBack.getPrintResult(true, SyzPrinterState2.PRINTER_OK)
                    } else {
                        callBack.getPrintResult(false, SyzPrinterState2.PRINTER_SELF_PRINT_FAIL)
                        Log.e(TAG, "自检页打印失败")
                    }
                    job.cancel()
                }
                writeABF1(
                    FMPrinterOrder.orderForGetFmSelfcheckingPage(), "${TAG}=writeSelfCheck>>>>"
                )
            } else {
                //把状态上报
                callBack.getPrintResult(false, printStatus)
            }
        }


    }

    /**
     * SPP  写入数据
     */
    fun writeABF1(data: ByteArray, tag: String = "") {
        connection?.apply {
            write(
                "${TAG}=${tag}>>>", Upacker.frameEncode(data), null
            )
        }
    }
    fun sendPaperSet(paperType: SyzPaperSize,callBack: DeviceBleInfoCall) {
        bluNotifyCallBack = { dataArray ->
            val result = FmNotifyBeanUtils.getGetFmPaperTypeResult(dataArray)
            when (result) {
                is NotifyResult2.Success -> {
                    callBack.getBleNotifyInfo(true, result.msg)
                }

                is NotifyResult2.Error -> {
                    callBack.getBleNotifyInfo(false, result.errorMsg)
                }

                else -> {

                }
            }
        }
        writeABF1(
            FMPrinterOrder.orderForGetFmPaperType(paperType),
            "${TAG}=设置纸张类型>>>>${paperType.toString()}"
        )
    }

    /**
     * 设置关机时间
     */
    fun writeShutdown(min: Int, callBack: DeviceBleInfoCall) {
        bluNotifyCallBack = { dataArray ->
            val result = FmNotifyBeanUtils.getShutDownPrinterResult(dataArray)
            when (result) {
                is NotifyResult2.Success -> {
                    callBack.getBleNotifyInfo(true, result.msg)
                }

                is NotifyResult2.Error -> {
                    callBack.getBleNotifyInfo(false, result.errorMsg)
                }

                else -> {

                }
            }
        }
        writeABF1(FMPrinterOrder.orderForGetFmSetShutdownTime(min), "${TAG}=writeShutdown>>>>")
    }


    /**
     * 取消打印
     */
    fun writeCancelPrinter(callBack: CancelPrintCallBack) {
        if (bitmapPrintHandler?.getPrinterState() == true) {
            Log.d(TAG, "当前是打印中的状态，可以去取消打印")
            cancelPrintCallBack = { dataArray ->
                val result = FmNotifyBeanUtils.getCancelPrintingInfo(dataArray)
                when (result) {
                    is NotifyResult2.Success -> {
                        //这里需要等待打印过程中的回调，
                        Log.i(TAG, "打印机收到了取消指令")
                        callBack.cancelSuccess()
                    }

                    is NotifyResult2.Error -> {
                        Log.i(TAG, "打印机收到了取消指令，但code！=200")
                        callBack.cancelFail()
                    }

                    else -> {}
                }
            }
            writeABF1(
                FMPrinterOrder.orderForGetFmCancelPrinter(), "${TAG}=writeCancelPrinter>>>>"
            )
            if (currentPrintType == SyzPrinter.SYZTWOINCH) {
                //二寸不发指令直接回复
                bitmapPrintHandler?.setPrinterCancel()
                callBack.cancelSuccess()
            } else {


            }

        } else {
            Log.e(TAG, "当前不是打印中的状态，不可以去取消打印")
        }
    }



    /**
     * 设置打印速度
     */
    fun writePrintSpeed(
        speed: Int, callBack: DeviceBleInfoCall
    ) {
        bluNotifyCallBack = { dataArray ->
            val result = FmNotifyBeanUtils.getSetSpeedPrinterResult(dataArray)
            when (result) {
                is NotifyResult2.Success -> {
                    callBack.getBleNotifyInfo(true, result.msg)
                }

                is NotifyResult2.Error -> {
                    callBack.getBleNotifyInfo(false, result.errorMsg)
                }

            }
        }
        writeABF1(
            FMPrinterOrder.orderForGetFmSetPrintSpeed(speed, currentPrintType),
            "${TAG}=writePrintSpeed>>>>"
        )
    }


    /**
     * 设置打印浓度
     */
    fun writePrintConcentration(
        Concentration: Int, callBack: DeviceBleInfoCall
    ) {

        bluNotifyCallBack = { dataArray ->
            val result = FmNotifyBeanUtils.getSetConcentrationPrinterResult(dataArray)
            when (result) {
                is NotifyResult2.Success -> {
                    callBack.getBleNotifyInfo(true, result.msg)
                }

                is NotifyResult2.Error -> {
                    callBack.getBleNotifyInfo(false, result.errorMsg)
                }

                else -> {}
            }
        }
        writeABF1(
            FMPrinterOrder.orderForGetFmSetPrintConcentration(Concentration, currentPrintType),
            "${TAG}=设置打印浓度>>>>"
        )
    }





    /*
        private fun CRC16_XMODEM(buffer: ByteArray): Int {
            var wCRCin = 0x0000
            val wCPoly = 0x1021
            for (b in buffer) {
                for (i in 0..7) {
                    val bit = b.toInt() shr 7 - i and 1 == 1
                    val c15 = wCRCin shr 15 and 1 == 1
                    wCRCin = wCRCin shl 1
                    if (c15 xor bit) wCRCin = wCRCin xor wCPoly
                }
            }
            wCRCin = wCRCin and 0xffff
            return 0x0000.let { wCRCin = wCRCin xor it; wCRCin }
        }
    */


    private var dexScope: CoroutineScope? = null/*
        private fun splitByteArray(input: ByteArray, chunkSize: Int = 100): List<ByteArray> {
            return input.toList().chunked(chunkSize).map { it.toByteArray() }
        }
    */


    /**
     * 写入固件
     * @param type OTA升级类型
     */
    fun writeDex(
        filePath: String,
        type: SyzFirmwareType = SyzFirmwareType.SYZFIRMWARETYPE01,
        callBack: ((printState: SyzPrinterState) -> Unit)
    ) {
        dexUpdateCallBack = {
            if (it == SyzPrinterState.PRINTER_DEXUPDATE_SUCCESS) {
                Log.d(TAG, "固件更新成功")
            } else {
                Log.d(TAG, "固件更新失败")
                //stopWriteDex() //停止写入dex文件
            }
            callBack.invoke(it)
        }
        if (dexScope != null && dexScope!!.isActive) {
            dexScope!!.cancel()
        }
        dexScope = CoroutineScope(Dispatchers.IO)
        dexScope?.launch {
            dexUploadHandler = SyzDexProcessor.build {
                this.printerType = currentPrintType
                this.syzFirmwareType = type
            }
            dexUploadHandler?.produceDex(filePath)
            dexUploadHandler?.consumeOne4K()
        }
    }

    private val sppWriteMutex = Mutex()
    private suspend fun spp_write(upackerData: ByteArray, connection: Connection) =
        sppWriteMutex.withLock {
            //不让它并发执行，加了互斥锁
            connection.write("${TAG}=spp_write>>>>", upackerData, null)
        }

    private val bitmapPrintMutex = Mutex()

    //加锁的原因是code11 info1 频繁的调用，如果不加锁，for循环就会顺序所乱
    suspend fun fmWriteABF4(dataList: MutableList<MPMessage.MPSendMsg>) =
        withContext(Dispatchers.IO) {
            bitmapPrintMutex.withLock {
                Log.d("$TAG", "========图片打印总共要发${dataList.size}个包")
                for (index in 0 until dataList.size) {
                    try {
                        val data = dataList[index]
                        val dataArray = data.toByteArray()
                        val upackerData = Upacker.frameEncode(dataArray)
                        connection?.apply {
                            spp_write(upackerData, this)
                        }
                        Log.d(
                            "$TAG", "=======第${index}包===字节数${upackerData.size}="
                        )
                        delay(1)
                    } catch (e: Exception) {
                        Log.d("$TAG", "fmWriteABF4异常>>>>${e.message}")
                        break
                    }
                }
            }
        }

    //private val dexMutex= Mutex()
    /*
        private suspend fun fmWriteDexABF4(dataList: MutableList<MPMessage.MPSendMsg>, chunkSize: Int) {
            // var success = true
            // var start = System.currentTimeMillis()
            val delaySize = 4 * 1024
            Log.d("$TAG", "========总共要发${dataList.size}个包")
            // LogLiveData.addLogs("========总共要发${dataList.size}个包")
            var writeCount = 0
            for (index in 0 until dataList.size) {
                //  dexMutex.withLock {
                try {
                    val data = dataList[index]
                    val dataArray = data.toByteArray()
                    val upackerData = Upacker.frameEncode(dataArray)
                    connection?.apply {
                        spp_write(upackerData, this)
                    }
                    Log.d("$TAG", "========第${index}包===字节数${dataArray.size}")
                    LogLiveData.addLogs("========第${index}包===字节数${dataArray.size}")
                    writeCount += chunkSize  //这里指原始数据累加，而不是包装的数据
                    if (writeCount >= delaySize) {
                        Log.d(
                            "$TAG", "abf4写入delay========第${index}包===delay400==${writeCount}"
                        )
                        delay(400)
                        writeCount = writeCount - (delaySize)//重新计算,有4k减去继续累加
                    } else {
                        Log.d(
                            "$TAG", "abf4写入delay========第${index}包===delay6==${writeCount}"
                        )
                        delay(6)
                    }
                } catch (e: Exception) {
                    Log.d("$TAG", "fmWriteDexABF4异常>>>>${e.message}")
                    //break
                }
                //  }

            }
        }
    */


    suspend fun writeDexABF4(msg: MPMessage.MPSendMsg) {
        try {
            val data = MPMessage.MPFirmwareMsg.parseFrom(msg.sendData.toByteArray())
            Log.i(
                TAG,
                "SPP_写入>>>${data.indexPackage}=====${data.totalPackage}====${data.binData.toByteArray().size}"
            )

            val dataArray = msg.toByteArray()
            val upackerData = Upacker.frameEncode(dataArray)
            connection?.apply {
                spp_write(upackerData, this)
                delay(1)
            }
        } catch (e: Exception) {
            Log.d("$TAG", "fmWriteDexABF4异常>>>>${e.message}")
        }
    }


    private var connection: Connection? = null
    fun connect(address: String) {

        connection =
            BTManager.getInstance().createConnection(address, UUIDWrapper.useDefault(), reciver)
                .apply {
                    this?.connect(object : ConnectCallback {
                        override fun onSuccess(device: BluetoothDevice) {
                            Log.i(TAG, "经典蓝牙连接成功")
                            //bluCallBack?.onConnectSuccess(device)
                        }

                        override fun onFail(errMsg: String, e: Throwable?) {
                            Log.e(TAG, "经典蓝牙连接失败${e?.message}")
                            bluCallBack?.onConnectFail(msg = e?.message)
                        }
                    })
                }
    }


    fun onDestory() {
        if (bitListScope != null && bitListScope!!.isActive) {
            bitListScope?.cancel()
            bitListScope = null
        }
        if (dexScope != null && dexScope!!.isActive) {
            dexScope!!.cancel()
            dexScope = null
        }
        if (sppReadScope != null && sppReadScope!!.isActive) {
            sppReadScope!!.cancel()
            sppReadScope = null
        }
        if (sppBitmapScope != null && sppBitmapScope!!.isActive) {
            sppBitmapScope!!.cancel()
            sppBitmapScope = null
        }

        if (bluScope != null && bluScope!!.isActive) {
            bluScope!!.cancel()
            bluScope = null
        }
        if (blueNotifyDataProcessor != null) {
            blueNotifyDataProcessor!!.close()
            blueNotifyDataProcessor = null
        }
        if (bitmapPrintHandler != null) {
            bitmapPrintHandler!!.releaseResources()
            bitmapPrintHandler = null
        }
        BTManager.getInstance().destroy()
        if (instance != null) {
            instance = null;
        }

    }
}


