package com.issyzone.blelibs.service

import android.bluetooth.BluetoothGatt

import android.graphics.Bitmap
import com.google.protobuf.ByteString
import com.issyzone.blelibs.BleManager
import com.issyzone.blelibs.FMPrinter
import com.issyzone.blelibs.SYZBleUtils

import com.issyzone.blelibs.bluetooth.FMBle
import com.issyzone.blelibs.callback.BleGattCallback
import com.issyzone.blelibs.callback.BleMtuChangedCallback
import com.issyzone.blelibs.callback.BleNotifyCallback
import com.issyzone.blelibs.callback.BleWriteCallback
import com.issyzone.blelibs.callback.SyzBleCallBack
import com.issyzone.blelibs.data.BLE_CONNECT_ERROR_MSG
import com.issyzone.blelibs.data.BLE_CONNECT_ERROR_MSG2
import com.issyzone.blelibs.data.BleDevice
import com.issyzone.blelibs.dataimp.DeviceBleInfoCall
import com.issyzone.blelibs.dataimp.DeviceInfoCall

import com.issyzone.blelibs.exception.BleException
import com.issyzone.blelibs.fmBeans.FMPrinterOrder
import com.issyzone.blelibs.fmBeans.FmBitmapOrDexPrinterUtils
import com.issyzone.blelibs.fmBeans.FmNotifyBeanUtils
import com.issyzone.blelibs.fmBeans.MPMessage
import com.issyzone.blelibs.fmBeans.MPMessage.EventType
import com.issyzone.blelibs.fmBeans.MPMessage.MPSendMsg.Builder
import com.issyzone.blelibs.fmBeans.NotifyResult
import com.issyzone.blelibs.fmBeans.NotifyResult2


import com.issyzone.blelibs.fmBeans.PrintBimapUtils
import com.issyzone.blelibs.upacker.Upacker
import com.issyzone.blelibs.utils.BitmapExt
import com.issyzone.blelibs.utils.BitmapUtils
import com.issyzone.blelibs.utils.fileToByteArray
import com.issyzone.blelibs.utils.isExtension
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class SyzBleManager {

    private val TAG = "SyzBleManager>>>>>"
    private var fmBle: FMBle? = null

    //只有一个service实例
    companion object {
        private var instance: SyzBleManager? = null
        private var serviceScope: CoroutineScope? = null
        private var bitScope: CoroutineScope? = null
        private var dexScope: CoroutineScope? = null
        private var bleisConencted: Boolean = false
        fun getInstance(): SyzBleManager {
            if (instance == null) {
                instance = SyzBleManager()
                dexScope = CoroutineScope(Dispatchers.IO)
                bitScope = CoroutineScope(Dispatchers.IO)
                serviceScope = CoroutineScope(Dispatchers.IO)
            }
            return instance!!
        }
    }

    /**
     * initBle只调用一次
     */
    fun initBle() {
        serviceScope?.launch(Dispatchers.IO) {
            // 在后台线程中执行耗时操作
            //判断是否支持BLE, 判断蓝牙是否打开
            SYZBleUtils.initBle()
            if (SYZBleUtils.isSupportBle() && SYZBleUtils.isBleOpen()) {
                delay(500)
            }
        }
    }


    /**
     * func:判断蓝牙是否连接
     */
    fun isBleConnected(): Boolean {
        if (fmBle != null && fmBle?.bleDevice != null) {
            return BleManager.getInstance().isConnected(fmBle?.bleDevice?.mac) && bleisConencted
        } else {
            return false
        }
    }

    private var bleCallBack: SyzBleCallBack? = null;
    fun setBleCallBack(bleCallBack: SyzBleCallBack) {
        this.bleCallBack = bleCallBack
    }

    private var bleNotifyCallBack: ((dataArray: ByteArray) -> Unit)? = null

    private var activelyReportCallBack: ((msg: MPMessage.MPCodeMsg) -> Unit)? = null
    private var currentOrderEventType: MPMessage.EventType? = null

    fun setActivelyReportBack(activelyReport: ((msg: MPMessage.MPCodeMsg) -> Unit)) {
        this.activelyReportCallBack = activelyReport
    }

    fun disconnectBle() {
        BleManager.getInstance().disconnect(fmBle?.bleDevice)
        if (serviceScope != null && serviceScope!!.isActive) {
            serviceScope!!.cancel()
        }
        if (bitScope != null && bitScope!!.isActive) {
            bitScope!!.cancel()
        }

        if (dexScope != null && dexScope!!.isActive) {
            dexScope!!.cancel()
        }

        instance = null
    }

    fun connectBle(name: String, mac: String) {
        if (serviceScope!=null&& serviceScope!!.isActive){
            serviceScope!!.cancel()
        }
        serviceScope = CoroutineScope(Dispatchers.IO)
        serviceScope?.launch {
            bleisConencted = false
            withContext(Dispatchers.IO) {
                val connectBleTask = async { connectBLe(name, mac) }
                fmBle = connectBleTask.await()
                if (fmBle != null && fmBle?.bleDevice != null) {
                    val setMTUTask = async { initBleMTU(fmBle!!.bleDevice!!) }
                    val mtuflag = setMTUTask.await()
                    if (mtuflag) {
                        //注测读取通道
                        val setNotyfyTask = async { initFmNotify() }
                        val notifyFlag = setNotyfyTask.await()
                        if (notifyFlag) {
                            bleisConencted = true
                            bleCallBack?.onConnectSuccess(fmBle!!.bleDevice)

                        } else {
                            bleisConencted = false
                            bleCallBack?.onConnectFail(fmBle?.bleDevice, "notify create fail")
                            // bleCallBack?.onDisConnected(fmBle!!.bleDevice)
                        }
                    }
                }
            }
        }
    }


    private suspend fun initFmNotify(): Boolean {
        return withContext(Dispatchers.IO) {
            suspendCoroutine<Boolean> { continuation ->
                // val notify = fmBle?.getCharatersType(FMPrinter.Charac_ABF3)?.get(0)
                val notifyGatte = SYZBleUtils.getNotifyCharac(fmBle?.gatt)
                Logger.d("$TAG=======正在打开通知serviceUUID=${notifyGatte?.first.toString()}==CharaUUID=${notifyGatte?.second?.uuid}")
                BleManager.getInstance().notify2(fmBle?.bleDevice,
                    notifyGatte?.first.toString(),
                    notifyGatte?.second?.uuid.toString(),
                    notifyGatte?.second,
                    object : BleNotifyCallback() {
                        override fun onNotifySuccess() {
                            // 打开通知操作成功
                            Logger.d("$TAG=======打开通知操作成功==")

                            if (isActive) {
                                continuation.resume(true)
                            }
                        }

                        override fun onNotifyFailure(exception: BleException) {
                            // 打开通知操作失败
                            Logger.d("$TAG=======打开通知操作失败==${exception.code}====${exception.description}")


                            if (isActive) {
                                continuation.resume(false)
                            }
                            //通道连接失败我就算它连接失败
                        }

                        override fun onCharacteristicChanged(data: ByteArray) {
                            // 打开通知后，设备发过来的数据将在这里出现
                            Logger.d(
                                "$TAG===ABF3====收到蓝牙数据${
                                    String(data, Charsets.UTF_8)
                                }==>字节数据长度>>>>>${data.size}"
                            )
                            try {
                                val mpRespondMsg = MPMessage.MPRespondMsg.parseFrom(data)
                                if (mpRespondMsg.eventType == MPMessage.EventType.DEVICEREPORT) {
                                    //主动上报的信息
                                    if (currentOrderEventType == EventType.SELFTEST || currentOrderEventType == EventType.FIRMWAREUPGRADE || currentOrderEventType == EventType.DEVICEPRINT) {
                                        bleNotifyCallBack?.invoke(data)
                                    } else {
                                        activelyReportCallBack?.invoke(
                                            if (mpRespondMsg.code == 200) {
                                                MPMessage.MPCodeMsg.parseFrom(mpRespondMsg.respondData.toByteArray())
                                            } else {
                                                MPMessage.MPCodeMsg.parseFrom(mpRespondMsg.error.toByteArray())
                                            }
                                        )
                                    }

                                } else {
                                    bleNotifyCallBack?.invoke(data)
                                }
                            } catch (e: Exception) {
                                Logger.e("$TAG NOTIFY解析数据出错")
                            }

                        }
                    })
            }
        }


    }

    private suspend fun connectBLe(name: String, mac: String): FMBle? {
        return suspendCancellableCoroutine<FMBle?> { cancellableContinuation ->
            val callback = object : BleGattCallback() {
                override fun onStartConnect() {
                    Logger.d("$TAG,开始连接")
                    bleCallBack?.onStartConnect()
                }

                override fun onConnectFail(bleDevice: BleDevice, exception: BleException?) {
                    bleisConencted = false
                    // E  │ SyzBleManager>>>>>,连接失败FM226::失败原因==101===Gatt Exception Occurred!
                    Logger.e("$TAG,连接失败${bleDevice.device.name}::失败原因==${exception?.code}===${exception?.description}")
                    if (exception?.code == 100) {
                        if (bleDevice != null) {
                            BleManager.getInstance().disconnect(bleDevice)
                            Logger.e("$TAG 蓝牙连接超时，很有可能已经连接了")
                            bleCallBack?.onConnectFailNeedUserRestart(
                                bleDevice, BLE_CONNECT_ERROR_MSG
                            )
                        }
                    }
                    bleCallBack?.onConnectFail(bleDevice, BLE_CONNECT_ERROR_MSG2)
                    cancellableContinuation.resume(null) {
                        Logger.e("$TAG 协诚连接蓝牙返回数据异常:${it.message.toString()}")
                    }
                }

                override fun onConnectSuccess(
                    bleDevice: BleDevice?, gatt: BluetoothGatt?, status: Int
                ) {
                    Logger.d("$TAG,连接成功状态码${status}")
                    cancellableContinuation.resume(FMBle(bleDevice, gatt, status)) {
                        Logger.e("协诚连接蓝牙返回数据异常:${it.message.toString()}")
                    }
                }

                override fun onDisConnected(
                    isActiveDisConnected: Boolean,
                    device: BleDevice?,
                    gatt: BluetoothGatt?,
                    status: Int
                ) {
                    Logger.d("$TAG,断开连接")
                    bleCallBack?.onDisConnected(device)
                }

            }
            BleManager.getInstance().connect(mac, callback)
        }
    }


    //获取设备信息
    fun getDeviceInfo(
        callBack: DeviceInfoCall
    ) {
        bleNotifyCallBack = { dataArray ->
            val result = FmNotifyBeanUtils.getDeviceInfo(dataArray)
            when (result) {
                is NotifyResult.Success -> {
                    callBack.getDeviceInfo(result.data)
                }

                is NotifyResult.Error -> {
                    callBack.getDeviceInfoError(result.errorMsg)
                }
            }
        }
        currentOrderEventType = MPMessage.EventType.DEVICEINFO
        fmWriteABF1(FMPrinterOrder.orderForGetFmDevicesInfo())
    }

    //检查自检页
    fun writeSelfCheck(
        callBack: DeviceBleInfoCall
    ) {
        bleNotifyCallBack = { dataArray ->
            val result = FmNotifyBeanUtils.getSelfCheckInfo(dataArray)
            when (result) {
                is NotifyResult2.Success -> {
                    callBack.getBleNotifyInfo(true, result.msg ?: null)
                }

                is NotifyResult2.Error -> {
                    callBack.getBleNotifyInfo(false, result.errorMsg)
                }
            }
        }
        currentOrderEventType = MPMessage.EventType.SELFTEST
        fmWriteABF1(FMPrinterOrder.orderForGetFmSelfcheckingPage())
    }


    /**
     * 设置关机时间
     */
    fun writeShutdown(min: Int, callBack: DeviceBleInfoCall) {
        bleNotifyCallBack = { dataArray ->
            val result = FmNotifyBeanUtils.getShutDownPrinterResult(dataArray)
            when (result) {
                is NotifyResult2.Success -> {
                    callBack.getBleNotifyInfo(true, result.msg)
                }

                is NotifyResult2.Error -> {
                    callBack.getBleNotifyInfo(false, result.errorMsg)
                }
            }
        }
        currentOrderEventType = MPMessage.EventType.CLOSETIME

        fmWriteABF1(FMPrinterOrder.orderForGetFmSetShutdownTime(min))
    }


    /**
     * 取消打印
     */
    fun writeCancelPrinter(callBack: DeviceBleInfoCall) {
        bleNotifyCallBack = { dataArray ->
            val result = FmNotifyBeanUtils.getSelfCheckInfo(dataArray)
            when (result) {
                is NotifyResult2.Success -> {
                    callBack.getBleNotifyInfo(true, result.msg)
                }

                is NotifyResult2.Error -> {
                    callBack.getBleNotifyInfo(false, result.errorMsg)
                }
            }
        }
        currentOrderEventType = MPMessage.EventType.CANCELPRINTING
        fmWriteABF1(FMPrinterOrder.orderForGetFmCancelPrinter())
    }

    fun writePrintSpeed(
        speed: Int, callBack: DeviceBleInfoCall
    ) {
        bleNotifyCallBack = { dataArray ->
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
        currentOrderEventType = MPMessage.EventType.PRINTINGSPEED
        fmWriteABF1(FMPrinterOrder.orderForGetFmSetPrintSpeed(speed))
    }

    fun writePrintConcentration(
        Concentration: Int, callBack: DeviceBleInfoCall
    ) {
        bleNotifyCallBack = { dataArray ->
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
        currentOrderEventType = MPMessage.EventType.PRINTINCONCENTRATION
        fmWriteABF1(FMPrinterOrder.orderForGetFmSetPrintConcentration(Concentration))
    }


    fun writeBitmaps(bipmaps: MutableList<Bitmap>, page: Int, callBack: DeviceBleInfoCall) {
        bleNotifyCallBack = { dataArray ->
            val result = FmNotifyBeanUtils.getBitmapsPrint(dataArray)
            when (result) {
                is NotifyResult2.Success -> {
                    if (result.msg != null) {
                        callBack.getBleNotifyInfo(true, result.msg)
                    }
                }

                is NotifyResult2.Error -> {
                    callBack.getBleNotifyInfo(false, result.errorMsg)
                }
            }
        }
        if (bitListScope != null && bitListScope!!.isActive) {
            bitListScope?.cancel()
        }
        bitListScope = CoroutineScope(Dispatchers.IO)
        bitListScope!!.launch {
            var startTime = System.currentTimeMillis()
            val totalBipmapsDataList = mutableListOf<MutableList<MPMessage.MPSendMsg>>()
            bipmaps.forEachIndexed { index, bitmap ->
                Logger.d(
                    "${TAG}二值化之后第${index}张bitmap字节数${
                        BitmapExt.bitmapToByteArray(
                            bitmap
                        ).size
                    }"
                )
                val bitmapArray = BitmapUtils.print(bitmap, bitmap.width, bitmap.height)
                Logger.d("${TAG}print()之后第${index}张图片总字节数${bitmapArray.size}")
                //对bitmaparray进行每100个字节分包
                val aplitafter = FmBitmapOrDexPrinterUtils.splitByteArray(bitmapArray, 100)
                var total = aplitafter.size //总包数
                Logger.d("${TAG}第${index}张图片总包数${total}")
                val needSendDataList = mutableListOf<MPMessage.MPSendMsg>()
                aplitafter.forEachIndexed { index, bytes ->
                    val mPPrintMsg = MPMessage.MPPrintMsg.newBuilder().setPage(page)
                        .setDataLength(bitmapArray.size).setImgData(ByteString.copyFrom(bytes))
                        .setIndexPackage(index + 1).setTotalPackage(total).build()
                    needSendDataList.add(
                        MPMessage.MPSendMsg.newBuilder()
                            .setEventType(MPMessage.EventType.DEVICEPRINT)
                            .setSendData(mPPrintMsg.toByteString()).build()
                    )
                }
                totalBipmapsDataList.add(needSendDataList)
            }
            Logger.d("生成数据需要的时间>>>>${System.currentTimeMillis() - startTime}")
            currentOrderEventType = MPMessage.EventType.DEVICEPRINT
            PrintBimapUtils.getInstance().setBitmapTask(totalBipmapsDataList).doPrint()
        }
    }

    fun writeDex(filePath: String, callBack: DeviceBleInfoCall) {
        bleNotifyCallBack = { dataArray ->
            val result = FmNotifyBeanUtils.getSelfCheckInfo(dataArray)
            when (result) {
                is NotifyResult2.Success -> {
                    callBack.getBleNotifyInfo(true, result.msg ?: null)
                }

                is NotifyResult2.Error -> {
                    callBack.getBleNotifyInfo(false, result.errorMsg)
                }
            }
        }
        if (dexScope != null && dexScope!!.isActive) {
            dexScope!!.cancel()
        }
        dexScope = CoroutineScope(Dispatchers.IO)
        dexScope?.launch {
            val file = File(filePath)
            if (!file.exists()) {
                Logger.d("$TAG 找不到${filePath}目录下的文件")
                return@launch
            }
            if (!file.isExtension("bin")) {
                Logger.d("$TAG 该${filePath}文件不是Bin文件")
                return@launch
            }
            //转byte数组
            val fileArray = file.fileToByteArray()
            //分包
            val aplitafter = FmBitmapOrDexPrinterUtils.splitByteArray(fileArray, 100)
            var total = aplitafter.size //总包数
            Logger.d("${TAG}Dex文件总包数${total}")
            val needSendDataList = mutableListOf<MPMessage.MPSendMsg>()
            aplitafter.forEachIndexed { index, bytes ->
                val mpFirmwareMsg = MPMessage.MPFirmwareMsg.newBuilder()
                    .setBinData(ByteString.copyFrom(bytes))//分包数据
                    .setDataLength(bytes.size)//binData的分包长度
                    .setIndexPackage(index + 1)//分包序列号 第一包是 1 以
                    .setCrcCode(FmBitmapOrDexPrinterUtils.calculateCRC16(fileArray))
                    .setTotalPackage(total).build()
                needSendDataList.add(
                    MPMessage.MPSendMsg.newBuilder()
                        .setEventType(MPMessage.EventType.FIRMWAREUPGRADE)
                        .setSendData(mpFirmwareMsg.toByteString()).build()
                )
            }
            currentOrderEventType = MPMessage.EventType.FIRMWAREUPGRADE
            fmWriteDexABF4(needSendDataList)
        }
    }

    private fun fmWriteABF1(data: ByteArray) {
        if (fmBle != null) {
            val write_abf1 = fmBle?.getCharatersType(FMPrinter.Charac_ABF1)?.get(0)
            Logger.d(
                "$TAG=======ABF1准备写入serviceUUID=${write_abf1?.first}==CharaUUID=${write_abf1?.second}==写入长度${
                    Upacker.frameEncode(
                        data
                    ).size
                }"
            )
            BleManager.getInstance().writeWriteResponse(fmBle?.bleDevice,
                write_abf1?.first.toString(),
                write_abf1?.second.toString(),
                Upacker.frameEncode(data),
                false,
                object : BleWriteCallback() {
                    override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray) {
                        // 发送数据到设备成功（分包发送的情况下，可以通过方法中返回的参数可以查看发送进度）
                        Logger.d("$TAG=abf1写入成功==")
                    }

                    override fun onWriteFailure(exception: BleException) {
                        // 发送数据到设备失败
                        Logger.d("$TAG=abf1写入失败==${exception.code}===${exception.description}")
                    }
                })
        }


    }


    /**
     *   private var bitScope: CoroutineScope? = null
     */
    private var bitListScope: CoroutineScope? = null


    private suspend fun initBleMTU(bleDevice: BleDevice): Boolean {
        val MAX_MTU = 180
        return withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                BleManager.getInstance()
                    .setMtu(bleDevice, MAX_MTU, object : BleMtuChangedCallback() {
                        override fun onSetMTUFailure(exception: BleException) {
                            // 设置 MTU 失败
                            Logger.e("$TAG 设置MTU失败==${exception.description}")
                            if (isActive) {
                                continuation.resume(false) // 返回 false 表示初始化失败
                            }

                        }

                        override fun onMtuChanged(mtu: Int) {
                            // 设置 MTU 成功
                            Logger.d("$TAG 设置MTU成功==${mtu}")
                            if (isActive) {
                                continuation.resume(true) // 返回 true 表示初始化成功
                            }
                        }
                    })
            }
        }

    }

    // 递归写入函数
    suspend fun fmWriteABF4(dataList: MutableList<MPMessage.MPSendMsg>): Boolean {
        var success = false
        var start = System.currentTimeMillis()
        Logger.d("$TAG========总共要发${dataList.size}个包")
        for (index in 0 until dataList.size) {
            try {
                val data = dataList[index]
                val dataArray = data.toByteArray()
                Logger.d("$TAG========第${index}包===字节数${dataArray.size}")

                success = writeABF4(dataArray, index, dataList.size)
                if (success) {
                    Logger.d("$TAG========第${index}包打印}")
                    // 如果写入成功，继续递归写入
                    //延时写入
                    delay(1)
                    continue
                } else {
                    // 如果写入失败，停止递归写入
                    break
                }
            } catch (e: Exception) {
                break
            }
        }
        Logger.d("$TAG========发包耗时${System.currentTimeMillis() - start}")
        return success
    }


    fun writeBitmap(bitmap: Bitmap, page: Int) {
        if (bitScope != null && bitScope!!.isActive) {
            bitScope?.cancel()
        }
        bitScope = CoroutineScope(Dispatchers.IO)
        bitScope!!.launch {
            Logger.d("${TAG}二值化之后bitmap字节数${BitmapExt.bitmapToByteArray(bitmap).size}")
            //val bitmapArray = BitmapExt.bitmapToByteArray(bitmap, Bitmap.CompressFormat.PNG, 100)
            val bitmapArray = BitmapUtils.print(bitmap, bitmap.width, bitmap.height)
            Logger.d("${TAG}print()之后图片总字节数${bitmapArray.size}")
            //对bitmaparray进行每100个字节分包
            val aplitafter = FmBitmapOrDexPrinterUtils.splitByteArray(bitmapArray, 100)
            var total = aplitafter.size //总包数
            Logger.d("${TAG}图片总包数${total}")
            val needSendDataList = mutableListOf<MPMessage.MPSendMsg>()
            aplitafter.forEachIndexed { index, bytes ->
                val mPPrintMsg =
                    MPMessage.MPPrintMsg.newBuilder().setPage(page).setDataLength(bitmapArray.size)
                        .setImgData(ByteString.copyFrom(bytes)).setIndexPackage(index + 1)
                        .setTotalPackage(total).build()
                needSendDataList.add(
                    MPMessage.MPSendMsg.newBuilder().setEventType(MPMessage.EventType.DEVICEPRINT)
                        .setSendData(mPPrintMsg.toByteString()).build()
                )
            }
            fmWriteABF4(needSendDataList)
        }
    }


    private suspend fun writeABF4(data: ByteArray, index: Int, totalSize: Int): Boolean {
        return withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                if (fmBle != null) {
                    Logger.d("$TAG Upacker之前write的data数据大小:${data.size}")
                    val upackerData = Upacker.frameEncode(data)
                    Logger.d("$TAG Upacker之后write的data数据大小 ${upackerData.size}")
                    val write_abf1 = fmBle?.getCharatersType(FMPrinter.Charac_ABF4)?.get(0)
                    BleManager.getInstance().writeWithNoResponse2(fmBle?.bleDevice,
                        write_abf1?.first.toString(),
                        write_abf1?.second.toString(),
                        upackerData,
                        object : BleWriteCallback() {
                            override fun onWriteSuccess(
                                current: Int, total: Int, justWrite: ByteArray
                            ) {
                                if (isActive) {
                                    // 发送数据到设备成功（分包发送的情况下，可以通过方法中返回的参数可以查看发送进度）
                                    if (index < totalSize) {
                                        Logger.d("$TAG=abf4写入成功=第${index}个包===总共${totalSize}个包====")
                                    } else {
                                        Logger.d("$TAG=abf4完全写入成功=第${index}个包=总共${totalSize}个包======")
                                    }
                                    continuation.resume(true)
                                }
                            }

                            override fun onWriteFailure(exception: BleException) {
                                // 发送数据到设备失败
                                if (isActive) {
                                    Logger.e("$TAG=abf4写入失败=====第${index}个包===总共${totalSize}个包====${exception.code}===${exception.description}")
                                    continuation.resume(false)
                                }
                                //continuation.resumeWithException(Exception("Write abf4写入失败: ${exception.code}==${exception.description}"))
                            }
                        })
                }
            }
        }
    }


    private suspend fun fmWriteDexABF4(dataList: MutableList<MPMessage.MPSendMsg>) {
        var success = true
        Logger.d("$TAG========总共要发${dataList.size}个包")
        var writeCount = 0
        for (index in 0 until dataList.size) {
            try {
                val data = dataList[index]
                val dataArray = data.toByteArray()  //一次写入的数据量
                Logger.d("$TAG========第${index}包===字节数${dataArray.size}")
                success = writeABF4(dataArray, index, dataList.size)
                if (success) {
                    //如果写入成功，继续递归写入
                    //延时写入
                    writeCount += dataArray.size
                    if (writeCount >= 4 * 1024) {
                        delay(400)
                        writeCount = writeCount - (4 * 1024)//重新计算,有4k减去继续累加
                    } else {
                        delay(6)
                    }
                    continue
                } else {
                    // 如果写入失败，停止递归写入
                    break
                }
            } catch (e: Exception) {
                Logger.e("${TAG}fmWriteDexABF4异常>>>>${e.message}")
                break
            }
        }
    }
}