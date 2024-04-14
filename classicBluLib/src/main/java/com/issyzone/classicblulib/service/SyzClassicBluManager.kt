package com.issyzone.classicblulib.service

import android.bluetooth.BluetoothDevice
import android.graphics.Bitmap
import android.util.Log
import com.google.protobuf.ByteString
import com.issyzone.classicblulib.bean.FMPrinterOrder
import com.issyzone.classicblulib.bean.FmNotifyBeanUtils
import com.issyzone.classicblulib.bean.LogLiveData
import com.issyzone.classicblulib.bean.MPMessage
import com.issyzone.classicblulib.bean.NotifyResult
import com.issyzone.classicblulib.bean.NotifyResult2
import com.issyzone.classicblulib.bean.SyzFirmwareType
import com.issyzone.classicblulib.bean.SyzPrinter
import com.issyzone.classicblulib.callback.BluPrintingCallBack
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
import com.issyzone.classicblulib.utils.BitmapUtils
import com.issyzone.classicblulib.utils.FmPrintBimapUtils
import com.issyzone.classicblulib.utils.HeatShrinkUtils


import com.issyzone.classicblulib.utils.Upacker
import com.issyzone.classicblulib.utils.fileToByteArray
import com.issyzone.classicblulib.utils.isExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File


class SyzClassicBluManager {
    private val TAG = "SyzClassicBluManager>>"

    companion object {
        private var instance: SyzClassicBluManager? = null

        //var isCancelPrinting = false  //是否取消打印
        fun getInstance(): SyzClassicBluManager {
            if (instance == null) {
                instance = SyzClassicBluManager()
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

    fun setBluCallBack(bluCallBack: SyzBluCallBack) {
        this.bluCallBack = bluCallBack
    }

    //断开连接
    fun disConnectBlu() {
        BTManager.getInstance().disconnectAllConnections()
    }

    fun initClassicBlu() {
        BTManager.getInstance().initialize(AppGlobels.getApplication())
        BTManager.getInstance().setSyzBluCallBack(object : SyzBluCallBack {
            override fun onStartConnect() {

            }

            override fun onConnectFail(msg: String?) {

            }

            override fun onConnectSuccess(device: BluetoothDevice) {

            }

            override fun onDisConnected() {
                bluCallBack?.onDisConnected()

            }
        })
    }

    //主动回调
    private var activelyReportCallBack: ((msg: SyzPrinterState2) -> Unit)? = null

    /**
     * 主动上报的回调
     */
    fun setActivelyReportBack(activelyReport: ((msg: SyzPrinterState2) -> Unit)) {
        this.activelyReportCallBack = activelyReport
    }

    /**
     * 固件升级回调
     */
    private var dexUpdateCallBack: ((printState: SyzPrinterState) -> Unit)? = null

    //ble连接状态的回调
    private var bluNotifyCallBack: ((dataArray: ByteArray) -> Unit)? = null

    //
    private var cancelPrintCallBack: ((dataArray: ByteArray) -> Unit)? = null
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

        override fun onRead(device: BluetoothDevice, wrapper: UUIDWrapper, data: ByteArray) {
            super.onRead(device, wrapper, data)
            try {
                val mpRespondMsg = MPMessage.MPRespondMsg.parseFrom(data)
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
                LogLiveData.addLogs(
                    "蓝牙返回的信息 ${
                        mpRespondMsg.toString()
                    }"
                )
                LogLiveData.addLogs(
                    "蓝牙返回respondData ${
                        MPMessage.MPCodeMsg.parseFrom(
                            mpRespondMsg.respondData.toByteArray()
                        ).toString()
                    }"
                )
                if (mpRespondMsg.eventType == MPMessage.EventType.DEVICEREPORT) {
                    if (mpRespondMsg.code == 200) {
                        val mpCodeMsg = MPMessage.MPCodeMsg.parseFrom(
                            mpRespondMsg.respondData.toByteArray()
                        )
                        when (mpCodeMsg.code) {
//                            300 -> {
//                                //打印任务回调(包括打印自检页
//
//                            }

                            400 -> {
                                //固件升级任务回调
                                //固件升级任务回调
                                dexUpdateCallBack?.invoke(
                                    FmNotifyBeanUtils.getDexUpdateReport(
                                        MPMessage.MPCodeMsg.parseFrom(
                                            mpRespondMsg.respondData.toByteArray()
                                        )
                                    )
                                )
                            }

                            11 -> {
                                if (mpCodeMsg.info == "1") {
                                    //收到11.1代表打印机通知你下发下一张
                                    /*           PrintBimapUtils.getInstance()
                                                   .removePrintWhenSuccessAndPrintNext()*/
                                }
                                if (mpCodeMsg.info == "2") {
                                    //收到11.2代表2寸打印机,你需要发下一段的数据
                                    //Log.d(TAG,"收到11.2代表2寸打印机,你需要发下一段的数据")
                                    FmPrintBimapUtils.getInstance()
                                        .removePrintWhenSuccessAndPrintNext()
                                }
                            }

//                            11 -> {
//
//                            }

//                            13 -> {
//                                //电量回调
//                            }

                            10 -> {
                                //返回打印机的状态
                                val state = FmNotifyBeanUtils.getActivelyReport(
                                    MPMessage.MPCodeMsg.parseFrom(
                                        mpRespondMsg.respondData.toByteArray()
                                    )
                                )
                                if (state == SyzPrinterState2.PRINTER_OK) {
                                    //代表一张图的打印成功
                                    //Log.i(TAG,"一张图打印成功了")
                                    if (FmPrintBimapUtils.getInstance().getPrinterState()) {
                                        FmPrintBimapUtils.getInstance().updatePrintProcess()
                                    } else {
                                        activelyReportCallBack?.invoke(
                                            state
                                        )
                                    }

                                } else {
                                    if (FmPrintBimapUtils.getInstance().getPrinterState()) {
                                        FmPrintBimapUtils.getInstance()
                                            .handlePrintingMistakes(state)
                                    } else {
                                        activelyReportCallBack?.invoke(
                                            state
                                        )
                                    }
                                    //Log.e(TAG,"图片打印过程中出现异常${state}")
                                }


//                                currentPrintStatusFlow?.let {
//                                    CoroutineScope(Dispatchers.IO).launch {
//                                        it.emit(state)
//                                    }
//                                }
                            }
                        }
                    } else {
                        Log.e(TAG, "主动回调，responseCode!=200")
                    }

                } else {
                    //其他的回调
                    if (mpRespondMsg.eventType == MPMessage.EventType.CANCELPRINTING) {
                        //取消打印的命令的回调
                        cancelPrintCallBack?.invoke(data)
                        //
                        FmPrintBimapUtils.getInstance().setPrinterCancel()
                    } else {
                        bluNotifyCallBack?.invoke(data)
                    }

                }
            } catch (e: Exception) {
                Log.d("$TAG", "$TAG NOTIFY解析数据出错${data.contentToString()}")
                LogLiveData.addLogs("$TAG NOTIFY解析数据出错${data.contentToString()}")
            }
        }

    }
    private var bitListScope: CoroutineScope? = null

    suspend fun getPrintStatus(): SyzPrinterState2 {
        val result = withTimeoutOrNull(3000) {
            val getDeviceStateTask = async { getDeviceState() }
            getDeviceStateTask.await()
        } ?: SyzPrinterState2.PRINTER_STATUS_UNKNOWN
        return result
    }

    private suspend fun getDeviceState(): SyzPrinterState2 {
        return suspendCancellableCoroutine<SyzPrinterState2> { cancellableContinuation ->
            getDeviceInfo(object : DeviceInfoCall {
                override fun getDeviceInfo(msg: MPMessage.MPDeviceInfoMsg) {
                    val printState = FmNotifyBeanUtils.getPrintStatus(msg)
                    Log.i(TAG, "打印前获取打印机状态===${printState}")
                    cancellableContinuation.resume(printState) {
                        Log.e(TAG, "打印前获取打印机状态异常===")
                    }
                }

                override fun getDeviceInfoError(errorMsg: MPMessage.MPCodeMsg) {
                    // callBack?.getBluNotifyInfo(false, SyzPrinterState2.PRINTER_STATUS_UNKNOWN)
                    //currentPrintStatusFlow = null
                    cancellableContinuation.resume(SyzPrinterState2.PRINTER_STATUS_UNKNOWN) {
                        Log.e(TAG, "打印前获取打印机状态异常===")
                    }
//                    cancellableContinuation.resume(false) {
//
//                    }
                }
            })
        }

    }


    //用quicklz压缩
    suspend fun compress(bitmapDataArray: ByteArray): ByteArray {
        return suspendCancellableCoroutine<ByteArray> { cancellableContinuation ->
            //val yaSuoArray = QuickLZ.compress(bitmapDataArray, 1)
            val yaSuoArray = HeatShrinkUtils.compress(bitmapDataArray)
            cancellableContinuation.resume(yaSuoArray) {
                Log.e(TAG, "压缩失败>>>>>")
            }
        }
    }

    //用quicklz解压缩
    private suspend fun decompress(bitmapDataArray: ByteArray): ByteArray {
        return suspendCancellableCoroutine<ByteArray> { cancellableContinuation ->
            //val yaSuoArray = QuickLZ.decompress(bitmapDataArray)
            val yaSuoArray = HeatShrinkUtils.decompress(bitmapDataArray)
            cancellableContinuation.resume(yaSuoArray) {
                Log.e(TAG, "解压缩失败>>>>>")
            }
        }
    }

    //打印图片
    /*
        fun writeBitmaps(
            bipmaps: MutableList<Bitmap>,
            width: Int,
            height: Int,
            page: Int,
            printerType: SyzPrinter,
            callBack: BluPrinterInfoCall2
        ) {
            isCancelPrinting = false
            Log.i(TAG, "打印图片的张图::${bipmaps.size}===${page}")
            LogLiveData.addLogs("打印图片的张图::${bipmaps.size}==每张图片的page=${page}")
            currentPrintStatusFlow = MutableSharedFlow<SyzPrinterState2>()
            CoroutineScope(Dispatchers.IO).launch {
                currentPrintStatusFlow?.apply {
                    this.asSharedFlow().collectLatest {
                        if (it == SyzPrinterState2.PRINTER_OK) {
                            if (PrintBimapUtils.getInstance().isCompleteBitmapPrinter()) {
                                callBack.getBluNotifyInfo(true, it)
                                currentPrintStatusFlow = null
                            }
                        } else {
                            if (it != SyzPrinterState2.PRINTER_PRINTING) {
                                //打印中的状态没必要上报
                                callBack.getBluNotifyInfo(false, it)
                                currentPrintStatusFlow = null
                            }
                        }
                    }
                }

            }
            if (bitListScope != null && bitListScope!!.isActive) {
                bitListScope?.cancel()
            }
            bitListScope = CoroutineScope(Dispatchers.IO)
            bitListScope?.launch {
                //打印之前获取设备信息查询设备状态
                val isCompress =
                    printerType == SyzPrinter.SYZFOURINCH || printerType == SyzPrinter.SYZTWOINCH
                val chunkSize = if (isCompress) {
                    if (printerType == SyzPrinter.SYZTWOINCH) {
                        //2寸最大的机器MTU为240
                        200
                    } else if (printerType == SyzPrinter.SYZFOURINCH) {
                        width * 10   //四寸是按10行发的
                    } else {
                        width    //未0.5寸预留
                        // 500
                    }
                } else {
                    if (printerType == SyzPrinter.SYZTWOINCH) {
                        //2寸最大的机器MTU为240
                        width * 4   //2寸是按4行发的
                        //1024
                    } else if (printerType == SyzPrinter.SYZFOURINCH) {
                        width * 10   //四寸是按10行发的
                    } else {
                        width    //未0.5寸预留
                        // 500
                    }
                }
                val compressCode = if (isCompress) {
                    1
                } else {
                    0
                }
                var startTime = System.currentTimeMillis()
                val totalBipmapsDataList = mutableListOf<MutableList<MPMessage.MPSendMsg>>()
                bipmaps.forEachIndexed { index, bitmap ->
                    val bitmapPrintArray = BitmapUtils.print(bitmap, bitmap.width, bitmap.height)
                    var bitmapArray = if (compressCode == 1) {
                        Log.i(TAG, "压缩前bitmap大小==${bitmapPrintArray.size}")
                        val compressStartTime = System.currentTimeMillis()
                        val quicklzCompressTask = async { compress(bitmapPrintArray) }
                        val bitmapCompress = quicklzCompressTask.await()
                        Log.i(
                            TAG,
                            "压缩后bitmap大小==${bitmapCompress.size}==压缩耗时==${System.currentTimeMillis() - compressStartTime}"
                        )


                        val deCompressStartTime = System.currentTimeMillis()
                        val quicklzDecompressTask = async { decompress(bitmapCompress) }
                        val bitmapOrgin = quicklzDecompressTask.await()
                        Log.i(
                            TAG,
                            "解压缩后bitmap大小==${bitmapOrgin.size}==解压缩耗时==${System.currentTimeMillis() - deCompressStartTime}"
                        )
                        bitmapCompress
                    } else {
                        bitmapPrintArray
                    }
                    val aplitafter = FmBitmapOrDexPrinterUtils.splitByteArray(bitmapArray, chunkSize)
                    var total = aplitafter.size //总包数
                    val needSendDataList = mutableListOf<MPMessage.MPSendMsg>()
                    aplitafter.forEachIndexed { index, bytes ->
                        val mPPrintMsg = if (index == 0) {
                            //第一包设置宽高
                            MPMessage.MPPrintMsg.newBuilder().setPage(page)
                                .setDataLength(bitmapArray.size).setImgData(ByteString.copyFrom(bytes))
                                .setIndexPackage(index + 1).setTotalPackage(total).setWidth(width)
                                .setCompression(compressCode).setHeight(height).build()
                        } else {
                            MPMessage.MPPrintMsg.newBuilder().setPage(page)
                                .setDataLength(bitmapArray.size).setImgData(ByteString.copyFrom(bytes))
                                .setIndexPackage(index + 1).setTotalPackage(total)
                                .setCompression(compressCode).build()
                        }

                        needSendDataList.add(
                            MPMessage.MPSendMsg.newBuilder()
                                .setEventType(MPMessage.EventType.DEVICEPRINT)
                                .setSendData(mPPrintMsg.toByteString()).build()
                        )
                    }
                    totalBipmapsDataList.add(needSendDataList)
                }
                Log.d("$TAG", "生成数据需要的时间>>>>${System.currentTimeMillis() - startTime}")
                LogLiveData.addLogs("打印图片生成数据需要的时间>>>>${System.currentTimeMillis() - startTime}")
                //开始打印
                PrintBimapUtils.getInstance().setBimapCallBack(callBack)
                    .setBitmapTask(totalBipmapsDataList,page).doPrint()

            }
        }
    */

    private fun splitByteArray(input: ByteArray, chunkSize: Int = 100): List<ByteArray> {
        return input.toList().chunked(chunkSize).map { it.toByteArray() }
    }

    fun testNextDuan() {
        FmPrintBimapUtils.getInstance().doTestNextPrint()
    }

    private var printerType: SyzPrinter = SyzPrinter.SYZTWOINCH


    fun printBitmaps(
        bipmaps: MutableList<Bitmap>,
        width: Int,
        height: Int,
        page: Int,
        printerType: SyzPrinter,
        isShake: Boolean,
        callBack: BluPrintingCallBack
    ) {
        //按4k,取段数
        //每一段再分包
        // isCancelPrinting = false
        this.printerType = printerType
        Log.i(TAG, "打印图片的张图::${bipmaps.size}===${page}")
        LogLiveData.addLogs("打印图片的张图::${bipmaps.size}==每张图片的page=${page}")
        if (bitListScope != null && bitListScope!!.isActive) {
            bitListScope?.cancel()
        }
        bitListScope = CoroutineScope(Dispatchers.IO)
        bitListScope?.launch {
            val startTime = System.currentTimeMillis()
            var isCompress = printerType == SyzPrinter.SYZFOURINCH || printerType == SyzPrinter.SYZTWOINCH
            val compressCode = if (isCompress) {
                1
            } else {
                0
            }
            val dataNeedSendList = mutableListOf<MutableList<MutableList<MPMessage.MPSendMsg>>>()
            bipmaps.forEachIndexed { indexBitMap, bitmap ->
                val bitmapPrintArray = BitmapUtils.print(bitmap, bitmap.width, bitmap.height)
                //按4k取段数
                val bitMapFenDuanList = splitByteArray(bitmapPrintArray, 4 * 1024)
                Log.i(
                    TAG, "第${indexBitMap}张图片===分了${bitMapFenDuanList.size}段"
                )
                val dataDuanEachBitmapList = mutableListOf<MutableList<MPMessage.MPSendMsg>>()
                var eachBitmapTotalBaoNums = 0
                bitMapFenDuanList.forEachIndexed { indexDuan, duanBytes ->
//                    Log.i(
//                        TAG,
//                        "第${indexBitMap}张图片===第${indexDuan}段压缩前的大小${duanBytes.size}"
//                    )
                    val duanByteArray = if (isCompress) {
                        //
                        val duanCompressTask = async { compress(duanBytes) }
                        val duanCompress = duanCompressTask.await()
                        Log.i(
                            TAG,
                            "第${indexBitMap}张图片===第${indexDuan}段压缩后的大小${duanCompress.size}"
                        )
                        /*                        val duanDecompressTask = async { decompress(duanCompress) }
                                                val duanOrgin = duanDecompressTask.await()
                                                Log.i(
                                                    TAG,
                                                    "第${indexBitMap}张图片===第${indexDuan}段解压缩后的大小${duanOrgin.size}"
                                                )
                                                */
                        duanCompress
                    } else {
                        Log.i(
                            TAG,
                            "第${indexBitMap}张图片===第${indexDuan}段不压缩的大小${duanBytes.size}"
                        )
                        duanBytes
                    }


                    //每一段再按200分包
                    val chunkSizePack = 180
                    val bitMapFenBaoList = splitByteArray(duanByteArray, chunkSizePack)
                    val totalBaoEachDuan = bitMapFenBaoList.size
                    eachBitmapTotalBaoNums += totalBaoEachDuan
                    Log.i(
                        TAG,
                        "第${indexBitMap}张图片===第${indexDuan}段按${chunkSizePack}分了${bitMapFenBaoList.size}包"
                    )
                    val baoDataEachDuanList = mutableListOf<MPMessage.MPSendMsg>()
                    bitMapFenBaoList.forEachIndexed { baoIndex, baoBytes ->
                        Log.i(
                            TAG,
                            "第${indexBitMap}张图片===第${indexDuan}段==第${baoIndex}包==数据大小==${baoBytes.size}"
                        )
                        if (baoIndex == 0) {
                            //第一包传宽高
                            val mPPrintMsg = MPMessage.MPPrintMsg.newBuilder().setPage(page)
                                .setDataLength(bitmapPrintArray.size)
                                .setImgData(ByteString.copyFrom(baoBytes))
                                .setIndexPackage(baoIndex + 1).setTotalPackage(totalBaoEachDuan)
                                .setWidth(width).setCompression(compressCode).setHeight(height)
                                .setSectionLength(duanByteArray.size).build()
                            val baoData = MPMessage.MPSendMsg.newBuilder()
                                .setEventType(MPMessage.EventType.DEVICEPRINT)
                                .setSendData(mPPrintMsg.toByteString()).build()
                            baoDataEachDuanList.add(baoData)
                        } else {
                            //Log.i(TAG,">>>>sssss>>${baoIndex}")
                            val mPPrintMsg = MPMessage.MPPrintMsg.newBuilder().setPage(page)
                                .setDataLength(bitmapPrintArray.size)
                                .setImgData(ByteString.copyFrom(baoBytes))
                                .setIndexPackage(baoIndex + 1).setTotalPackage(totalBaoEachDuan)
                                .setCompression(compressCode).setSectionLength(duanByteArray.size)
                                .build()
                            val baoData = MPMessage.MPSendMsg.newBuilder()
                                .setEventType(MPMessage.EventType.DEVICEPRINT)
                                .setSendData(mPPrintMsg.toByteString()).build()
                            baoDataEachDuanList.add(baoData)
                        }
                    }
                    dataDuanEachBitmapList.add(baoDataEachDuanList)
                }
                Log.i(TAG, "第${indexBitMap}张图片===总包数${eachBitmapTotalBaoNums}")
                dataNeedSendList.add(dataDuanEachBitmapList)
            }
            Log.i(
                TAG,
                "当前需要处理几张图${dataNeedSendList.size}==生成数据需要的时间>>>>${System.currentTimeMillis() - startTime}"
            )
            FmPrintBimapUtils.getInstance().setBimapCallBack(callBack)
                .setBitmapTask(dataNeedSendList).doPrint()


        }
    }



    fun testBitmaps(
        bipmaps: MutableList<Bitmap>,
        width: Int,
        height: Int,
        page: Int,
        printerType: SyzPrinter,
        isShake: Boolean,
        callBack: BluPrintingCallBack
    ) {
        //按4k,取段数
        //每一段再分包
        // isCancelPrinting = false
        this.printerType = printerType
        Log.i(TAG, "打印图片的张图::${bipmaps.size}===${page}")
        LogLiveData.addLogs("打印图片的张图::${bipmaps.size}==每张图片的page=${page}")
        if (bitListScope != null && bitListScope!!.isActive) {
            bitListScope?.cancel()
        }
        bitListScope = CoroutineScope(Dispatchers.IO)
        bitListScope?.launch {
            val startTime = System.currentTimeMillis()
            //var isCompress = printerType == SyzPrinter.SYZFOURINCH || printerType == SyzPrinter.SYZTWOINCH
            var isCompress = false
            val compressCode = if (isCompress) {
                1
            } else {
                0
            }
            val dataNeedSendList = mutableListOf<MutableList<MutableList<MPMessage.MPSendMsg>>>()
            bipmaps.forEachIndexed { indexBitMap, bitmap ->
                val bitmapPrintArray = BitmapUtils.print(bitmap, bitmap.width, bitmap.height)
                //按4k取段数
                val bitMapFenDuanList = splitByteArray(bitmapPrintArray, 4 * 1024)
                Log.i(
                    TAG, "第${indexBitMap}张图片===分了${bitMapFenDuanList.size}段"
                )
                val dataDuanEachBitmapList = mutableListOf<MutableList<MPMessage.MPSendMsg>>()
                var eachBitmapTotalBaoNums = 0
                bitMapFenDuanList.forEachIndexed { indexDuan, duanBytes ->
//                    Log.i(
//                        TAG,
//                        "第${indexBitMap}张图片===第${indexDuan}段压缩前的大小${duanBytes.size}"
//                    )
                    val duanByteArray = if (isCompress) {
                        //
                        val duanCompressTask = async { compress(duanBytes) }
                        val duanCompress = duanCompressTask.await()
                        Log.i(
                            TAG,
                            "第${indexBitMap}张图片===第${indexDuan}段压缩后的大小${duanCompress.size}"
                        )
                        /*                        val duanDecompressTask = async { decompress(duanCompress) }
                                                val duanOrgin = duanDecompressTask.await()
                                                Log.i(
                                                    TAG,
                                                    "第${indexBitMap}张图片===第${indexDuan}段解压缩后的大小${duanOrgin.size}"
                                                )
                                                */
                        duanCompress
                    } else {
                        Log.i(
                            TAG,
                            "第${indexBitMap}张图片===第${indexDuan}段不压缩的大小${duanBytes.size}"
                        )
                        duanBytes
                    }


                    //每一段再按200分包
                    val chunkSizePack = 180
                    val bitMapFenBaoList = splitByteArray(duanByteArray, chunkSizePack)
                    val totalBaoEachDuan = bitMapFenBaoList.size
                    eachBitmapTotalBaoNums += totalBaoEachDuan
                    Log.i(
                        TAG,
                        "第${indexBitMap}张图片===第${indexDuan}段按${chunkSizePack}分了${bitMapFenBaoList.size}包"
                    )
                    val baoDataEachDuanList = mutableListOf<MPMessage.MPSendMsg>()
                    bitMapFenBaoList.forEachIndexed { baoIndex, baoBytes ->
                        Log.i(
                            TAG,
                            "第${indexBitMap}张图片===第${indexDuan}段==第${baoIndex}包==数据大小==${baoBytes.size}"
                        )
                        if (baoIndex == 0) {
                            //第一包传宽高
                            val mPPrintMsg = MPMessage.MPPrintMsg.newBuilder().setPage(page)
                                .setDataLength(bitmapPrintArray.size)
                                .setImgData(ByteString.copyFrom(baoBytes))
                                .setIndexPackage(baoIndex + 1).setTotalPackage(totalBaoEachDuan)
                                .setWidth(width).setCompression(compressCode).setHeight(height)
                                .setSectionLength(duanByteArray.size).build()
                            val baoData = MPMessage.MPSendMsg.newBuilder()
                                .setEventType(MPMessage.EventType.DEVICEPRINT)
                                .setSendData(mPPrintMsg.toByteString()).build()
                            baoDataEachDuanList.add(baoData)
                        } else {
                            //Log.i(TAG,">>>>sssss>>${baoIndex}")
                            val mPPrintMsg = MPMessage.MPPrintMsg.newBuilder().setPage(page)
                                .setDataLength(bitmapPrintArray.size)
                                .setImgData(ByteString.copyFrom(baoBytes))
                                .setIndexPackage(baoIndex + 1).setTotalPackage(totalBaoEachDuan)
                                .setCompression(compressCode).setSectionLength(duanByteArray.size)
                                .build()
                            val baoData = MPMessage.MPSendMsg.newBuilder()
                                .setEventType(MPMessage.EventType.DEVICEPRINT)
                                .setSendData(mPPrintMsg.toByteString()).build()
                            baoDataEachDuanList.add(baoData)
                        }
                    }
                    dataDuanEachBitmapList.add(baoDataEachDuanList)
                }
                Log.i(TAG, "第${indexBitMap}张图片===总包数${eachBitmapTotalBaoNums}")
                dataNeedSendList.add(dataDuanEachBitmapList)
            }
            Log.i(
                TAG,
                "当前需要处理几张图${dataNeedSendList.size}==生成数据需要的时间>>>>${System.currentTimeMillis() - startTime}"
            )
            FmPrintBimapUtils.getInstance().setBimapCallBack(callBack)
                .setBitmapTask(dataNeedSendList).doTest()
        }
    }

    /**
     * @param isShake   是否抖动   要么图片全部抖动，要么不抖动
     */
    fun writeBitmaps(
        bipmaps: MutableList<Bitmap>,
        width: Int,
        height: Int,
        page: Int,
        printerType: SyzPrinter,
        isShake: Boolean,
        callBack: BluPrintingCallBack
    ) {/* // isCancelPrinting = false
          this.printerType=printerType
          Log.i(TAG, "打印图片的张图::${bipmaps.size}===${page}")
          LogLiveData.addLogs("打印图片的张图::${bipmaps.size}==每张图片的page=${page}")
          if (bitListScope != null && bitListScope!!.isActive) {
              bitListScope?.cancel()
          }
          bitListScope = CoroutineScope(Dispatchers.IO)
          bitListScope?.launch {
              //打印之前获取设备信息查询设备状态
              var isCompress =
                  printerType == SyzPrinter.SYZFOURINCH || printerType == SyzPrinter.SYZTWOINCH
              if (isShake) {
                  isCompress = false //抖动不压缩
              }
              val chunkSize = if (isCompress) {
                  if (printerType == SyzPrinter.SYZTWOINCH) {
                      //2寸最大的机器MTU为240
                      200
                  } else if (printerType == SyzPrinter.SYZFOURINCH) {
                      width * 10   //四寸是按10行发的
                  } else {
                      width    //未0.5寸预留
                      // 500
                  }
              } else {
                  if (printerType == SyzPrinter.SYZTWOINCH) {
                      //2寸最大的机器MTU为240
                      width * 4   //2寸是按4行发的
                      //1024
                  } else if (printerType == SyzPrinter.SYZFOURINCH) {
                      width * 10   //四寸是按10行发的
                  } else {
                      width    //未0.5寸预留
                      // 500
                  }
              }
              val compressCode = if (isCompress) {
                  1
              } else {
                  0
              }
              var startTime = System.currentTimeMillis()
              val totalBipmapsDataList = mutableListOf<MutableList<MPMessage.MPSendMsg>>()
              bipmaps.forEachIndexed { index, bitmap ->
                  val bitmapPrintArray = BitmapUtils.print(bitmap, bitmap.width, bitmap.height)
                  var bitmapArray = if (compressCode == 1) {
                      // Log.i(TAG, "压缩前bitmap大小==${bitmapPrintArray.size}")
                      val compressStartTime = System.currentTimeMillis()
                      val quicklzCompressTask = async { compress(bitmapPrintArray) }
                      val bitmapCompress = quicklzCompressTask.await()
                      Log.i(
                          TAG,
                          "第${index}张压缩后bitmap大小==${bitmapCompress.size}==压缩耗时==${System.currentTimeMillis() - compressStartTime}"
                      )
                      val deCompressStartTime = System.currentTimeMillis()
                      val quicklzDecompressTask = async { decompress(bitmapCompress) }
                      val bitmapOrgin = quicklzDecompressTask.await()
  //                    Log.i(
  //                        TAG,
  //                        "解压缩后bitmap大小==${bitmapOrgin.size}==解压缩耗时==${System.currentTimeMillis() - deCompressStartTime}"
  //                    )
                      bitmapCompress
                  } else {
                      bitmapPrintArray
                  }
                  val aplitafter = splitByteArray(bitmapArray, chunkSize)
                  var total = aplitafter.size //总包数
                  val needSendDataList = mutableListOf<MPMessage.MPSendMsg>()
                  aplitafter.forEachIndexed { index, bytes ->
                      val mPPrintMsg = if (index == 0) {
                          //第一包设置宽高
                          MPMessage.MPPrintMsg.newBuilder().setPage(page)
                              .setDataLength(bitmapArray.size).setImgData(ByteString.copyFrom(bytes))
                              .setIndexPackage(index + 1).setTotalPackage(total).setWidth(width)
                              .setCompression(compressCode).setHeight(height).build()
                      } else {
                          MPMessage.MPPrintMsg.newBuilder().setPage(page)
                              .setDataLength(bitmapArray.size).setImgData(ByteString.copyFrom(bytes))
                              .setIndexPackage(index + 1).setTotalPackage(total)
                              .setCompression(compressCode).build()
                      }

                      needSendDataList.add(
                          MPMessage.MPSendMsg.newBuilder()
                              .setEventType(MPMessage.EventType.DEVICEPRINT)
                              .setSendData(mPPrintMsg.toByteString()).build()
                      )
                  }
                  totalBipmapsDataList.add(needSendDataList)
              }
              Log.d("$TAG", "生成数据需要的时间>>>>${System.currentTimeMillis() - startTime}")
              LogLiveData.addLogs("打印图片生成数据需要的时间>>>>${System.currentTimeMillis() - startTime}")
              //开始打印
              PrintBimapUtils.getInstance().setBimapCallBack(callBack)
                  .setBitmapTask(totalBipmapsDataList, page).doPrint()

          }*/
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


    //检查自检页
    fun writeSelfCheck() {
        writeABF1(FMPrinterOrder.orderForGetFmSelfcheckingPage(), "${TAG}=writeSelfCheck>>>>")
    }

    private fun writeABF1(data: ByteArray, tag: String = "") {
        connection?.apply {
            write(
                "${TAG}=${tag}>>>", Upacker.frameEncode(data), null
            )
        }
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

    //private var isCancelPrinting = false;//是否收到取消打印的指令
    // private var cpb: CancelPrintCallBack? = null

    /**
     * 取消打印
     */
    fun writeCancelPrinter(callBack: CancelPrintCallBack) {
//        isCancelPrinting = false
//        this.cpb = callBack
        // isCancelPrinting = false
        cancelPrintCallBack = { dataArray ->
            val result = FmNotifyBeanUtils.getCancelPrintingInfo(dataArray)
            when (result) {
                is NotifyResult2.Success -> {
                    //这里需要等待打印过程中的回调，
                    // isCancelPrinting = true//打印机收到了取消指令，同时需要判定打印机是否结束打印
                    Log.i(TAG, "打印机收到了取消指令")
                    //isCancelPrinting = true
                    callBack.cancelSuccess()
                }

                is NotifyResult2.Error -> {
                    Log.i(TAG, "打印机收到了取消指令，但code！=200")
                    // isCancelPrinting = false
                    callBack.cancelFail()
                }

                else -> {}
            }
        }
        writeABF1(FMPrinterOrder.orderForGetFmCancelPrinter(), "${TAG}=writeCancelPrinter>>>>")
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
        writeABF1(FMPrinterOrder.orderForGetFmSetPrintSpeed(speed), "${TAG}=writePrintSpeed>>>>")
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
            }
        }
        writeABF1(
            FMPrinterOrder.orderForGetFmSetPrintConcentration(Concentration),
            "${TAG}=设置打印浓度>>>>"
        )
    }

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


    private var dexScope: CoroutineScope? = null

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
            val file = File(filePath)
            if (!file.exists()) {
                Log.d("$TAG", " 找不到${filePath}目录下的文件")
                return@launch
            }
            if (!file.isExtension("bin")) {
                Log.d("$TAG", " 该${filePath}文件不是Bin文件")
                return@launch
            }
            //转byte数组
            val fileArray = file.fileToByteArray()
            Log.d("$TAG", "Dex文件总字节数${fileArray.size}")
            //分包
            val aplitafter = splitByteArray(fileArray, 100)
            //crc算法
            val crccode = CRC16_XMODEM(fileArray)
            var total = aplitafter.size //总包数
            Log.d("$TAG", "Dex文件总包数${total}")
            LogLiveData.addLogs("Dex文件总包数${total}")
            val needSendDataList = mutableListOf<MPMessage.MPSendMsg>()
            aplitafter.forEachIndexed { index, bytes ->
                Log.d("$TAG", "第${index}的字节数据${bytes.toString()}")
                Log.d("$TAG", "第${index}的字节数据${ByteString.copyFrom(bytes)}")
                val mpFirmwareMsg = MPMessage.MPFirmwareMsg.newBuilder()
                    .setBinData(ByteString.copyFrom(bytes))//分包数据
                    .setDataLength(fileArray.size)//
                    .setFirmwareType(type.funValue)//0打印机固件，1字库，2文件系统
                    .setIndexPackage(index + 1)//分包序列号 第一包是 1 以
                    .setCrcCode(crccode).setTotalPackage(total).build()
                needSendDataList.add(
                    MPMessage.MPSendMsg.newBuilder()
                        .setEventType(MPMessage.EventType.FIRMWAREUPGRADE)
                        .setSendData(mpFirmwareMsg.toByteString()).build()
                )
            }
            fmWriteDexABF4(needSendDataList)
        }
    }

    private var totalSize = 0
    suspend fun fmWriteABF4(dataList: MutableList<MPMessage.MPSendMsg>) {
        var start = System.currentTimeMillis()
        Log.d("$TAG", "========总共要发${dataList.size}个包")
        LogLiveData.addLogs("========单张图总共要发${dataList.size}个包")
        for (index in 0 until dataList.size) {
            try {
                val data = dataList[index]
                val dataArray = data.toByteArray()
                val upackerData = Upacker.frameEncode(dataArray)
                connection?.apply {
                    write(
                        "${TAG}=writeBitmaps>>>>", upackerData, null
                    )
                }
                Log.d(
                    "$TAG", "=======第${index}包===字节数${upackerData.size}="
                )
                totalSize += upackerData.size
                Log.d(
                    "$TAG", "=======第${index}包===累计大小=${totalSize}"
                )
                LogLiveData.addLogs("=======第${index}包===字节数${upackerData.size}=")
                LogLiveData.addLogs("====累计大小=${totalSize}")
                delay(1)
            } catch (e: Exception) {
                break
            }
        }
        Log.d("$TAG", "=======发包耗时${System.currentTimeMillis() - start}")
        LogLiveData.addLogs("=======发包耗时${System.currentTimeMillis() - start}")
    }

    private suspend fun fmWriteDexABF4(dataList: MutableList<MPMessage.MPSendMsg>) {
        // var success = true
        var start = System.currentTimeMillis()
        Log.d("$TAG", "========总共要发${dataList.size}个包")
        LogLiveData.addLogs("========总共要发${dataList.size}个包")
        var writeCount = 0
        for (index in 0 until dataList.size) {
            try {
                val data = dataList[index]
                val dataArray = data.toByteArray()
                val upackerData = Upacker.frameEncode(dataArray)
                connection?.apply {
                    write(
                        "${TAG}=writeDex>>>>", upackerData, null
                    )
                }
                Log.d("$TAG", "========第${index}包===字节数${dataArray.size}")
                LogLiveData.addLogs("========第${index}包===字节数${dataArray.size}")
                writeCount += 100  //这里指原始数据累加，而不是包装的数据
                if (writeCount >= 4 * 1024) {
                    Log.d(
                        "$TAG", "abf4写入delay========第${index}包===delay400==${writeCount}"
                    )
                    delay(400)
                    writeCount = writeCount - (4 * 1024)//重新计算,有4k减去继续累加
                } else {
                    Log.d(
                        "$TAG", "abf4写入delay========第${index}包===delay1==${writeCount}"
                    )
                    delay(6)
                }
            } catch (e: Exception) {
                Log.d("$TAG", "fmWriteDexABF4异常>>>>${e.message}")
                break
            }
        }
        Log.d("$TAG", "=======发包耗时${System.currentTimeMillis() - start}")
        LogLiveData.addLogs("=======发包耗时${System.currentTimeMillis() - start}")
    }


    private var connection: Connection? = null
    fun connect(address: String) {
//
//        val sFscSppCentralApi = FscSppCentralApiImp.getInstance();
//        sFscSppCentralApi.setCallbacks(FscSppCentralCallbacksImp());
//        sFscSppCentralApi.connect(address);
        bluCallBack?.onStartConnect()
        connection =
            BTManager.getInstance().createConnection(address, UUIDWrapper.useDefault(), reciver)
                .apply {
                    this?.connect(object : ConnectCallback {
                        override fun onSuccess(device: BluetoothDevice) {
                            Log.i(TAG, "经典蓝牙连接成功")
                            bluCallBack?.onConnectSuccess(device)
                        }

                        override fun onFail(errMsg: String, e: Throwable?) {
                            Log.e(TAG, "经典蓝牙连接失败${e?.message}")
                            bluCallBack?.onConnectFail(msg = e?.message)
                        }
                    })
                }
    }


    fun onDestory() {
        if (instance != null) {
            instance = null;
        }
//        if (currentPrintStatusFlow != null) {
//            currentPrintStatusFlow = null
//        }
        if (bitListScope != null && bitListScope!!.isActive) {
            bitListScope?.cancel()
            bitListScope = null
        }
        if (dexScope != null && dexScope!!.isActive) {
            dexScope!!.cancel()
            dexScope = null
        }
        BTManager.getInstance().destroy()
    }


}


