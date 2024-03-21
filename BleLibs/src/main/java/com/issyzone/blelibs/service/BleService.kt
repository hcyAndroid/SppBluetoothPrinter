package com.issyzone.blelibs.service

import android.app.Service
import android.bluetooth.BluetoothGatt
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.issyzone.blelibs.BleManager
import com.issyzone.blelibs.FMPrinter
import com.issyzone.blelibs.SYZBleUtils
import com.issyzone.blelibs.bluetooth.FMBle
import com.issyzone.blelibs.callback.BleGattCallback
import com.issyzone.blelibs.callback.BleMtuChangedCallback
import com.issyzone.blelibs.callback.BleNotifyCallback
import com.issyzone.blelibs.callback.BleScanCallback
import com.issyzone.blelibs.callback.BleWriteCallback
import com.issyzone.blelibs.data.BleDevice
import com.issyzone.blelibs.exception.BleException
import com.issyzone.blelibs.fmBeans.FmNotifyBean
import com.issyzone.blelibs.fmBeans.MPMessage
import com.issyzone.blelibs.upacker.Upacker
import com.issyzone.blelibs.utils.AppGlobels
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


/**
 *
 * 协程嵌套中的内部协程会随着外部协程的取消而自动取消。
 * 这是 Kotlin 协程的一个重要特性，称为协程的结构化并发。
 * 当您取消一个父协程时，它会递归地取消其所有子协程，确保资源得到正确释放并且不会产生悬挂的协程。
 * 这种行为使得协程之间的取消管理变得非常简单和可靠。
 */
class BleService : Service() {

    private val TAG = "BleService>>>>>"
    private var fmBle: FMBle? = null

    //只有一个service实例
    companion object {
        private var instance: BleService? = null
        private var serviceScope: CoroutineScope? = null
        private var scanScope: CoroutineScope? = null
        private val _scanResultFlow = MutableSharedFlow<List<BleDevice>>()
        fun getInstance(): BleService {
            if (instance == null) {
                instance = BleService()
                serviceScope = CoroutineScope(Dispatchers.IO)
                val serviceIntent = Intent(AppGlobels.getApplication(), BleService::class.java)
                AppGlobels.getApplication().startService(serviceIntent)
            }
            return instance!!
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    fun getScanResultFlow(): SharedFlow<List<BleDevice>> {
        val scanResultFlow = _scanResultFlow.asSharedFlow()
        Logger.d("${TAG}订阅数据")
        return scanResultFlow
    }


    /**
     * START_STICKY：如果 Service 进程被杀死，系统会尝试重新创建 Service 并调用 onStartCommand() 方法，但不会重新传递最后一个 Intent。这种模式适用于执行周期性任务的 Service。
     * START_NOT_STICKY：如果 Service 进程被杀死，系统不会重新创建 Service，除非有新的启动请求。这种模式适用于一次性任务的 Service。
     * START_REDELIVER_INTENT：如果 Service 进程被杀死，系统会重新创建 Service 并调用 onStartCommand() 方法，并重新传递最后一个 Intent。这种模式适用于执行需要确保不丢失数据的任务。
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Logger.d("${TAG}启动BLE服务")
        serviceScope?.launch {
            // 在后台线程中执行耗时操作
            withContext(Dispatchers.Main) {
                //初始化BLEManager
                SYZBleUtils.initBle()
                delay(500)
                //判断是否支持BLE
                if (!SYZBleUtils.isSupportBle()) {
                    stopSelf()
                    return@withContext
                }
                //判断蓝牙是否打开
                if (!SYZBleUtils.isBleOpen()) {
                    stopSelf()
                    return@withContext
                }
                //开启扫描
                scanBle()
            }

        }
        return START_STICKY
    }

    private suspend fun scanBle() {
        if (scanJob != null) {
            Logger.d("${TAG}>>>扫描协诚取消")
            scanJob?.cancel()
        }
        scanScope = CoroutineScope(Dispatchers.IO)
        scanJob = scanScope?.launch {
            // 在后台线程中执行蓝牙扫描等耗时操作
            val scanResultList = withContext(Dispatchers.IO) {
                // 执行蓝牙扫描等耗时操作
                startBleScan()
            }
            withContext(Dispatchers.IO) {
                // 扫描完成后，向activity暴露扫描的数据
                Logger.d("${TAG}>>>扫描到BLE设备个数:${scanResultList.size}")
                val needDeviceList = scanResultList.filter {

                    (!it.name.isNullOrEmpty()) && (!it.mac.isNullOrEmpty()) && (it.name.lowercase()
                        .startsWith("fm"))
                }
                Logger.d("${TAG}>>>BLE名字和mac地址不为null,且name开头为fm的设备的个数:${needDeviceList.size}")
                try {
                    withContext(Dispatchers.Main) {
                        Logger.d("${TAG}>>>FLOW发送数据")
                        _scanResultFlow.emit(needDeviceList)
                    }
                } catch (e: Exception) {
                    Logger.e("${TAG}>>>发送扫描数据异常${e.message}")
                }


            }
        }
    }


    private var scanJob: Job? = null

    // 带回复的可写特征(Write), 除了下发图片打印,和下发打印升级的指令, 其他的指令发下给打印机都用这个特征
    fun fmWriteABF1(data: ByteArray) {
        if (fmBle != null) {
            val write_abf1 = fmBle?.getCharatersType(FMPrinter.Charac_ABF1)?.get(0)
            Logger.d(
                "$TAG=======ABF1准备写入serviceUUID=${write_abf1?.first}==CharaUUID=${write_abf1?.second}==写入长度${
                    Upacker.frameEncode(
                        data
                    ).size
                }"
            )
            BleManager.getInstance().write(fmBle?.bleDevice,
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
     *  不带回复的可写特征(WriteWithoutResponse), 固件升级,图片发送这些需要分包的就使用这个特征来发.
     */

    private suspend fun writeABF4(data: ByteArray, index: Int, totalSize: Int): Boolean {
        return withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                if (fmBle != null) {
                    Logger.d("蓝牙TETST>>>>>>${data.size}")
                    Logger.d("蓝牙TETST>>>>>>  ${Upacker.frameEncode(data).size}")
                    val write_abf1 = fmBle?.getCharatersType(FMPrinter.Charac_ABF4)?.get(0)
                    BleManager.getInstance().write2(fmBle?.bleDevice,
                        write_abf1?.first.toString(),
                        write_abf1?.second.toString(),
                        Upacker.frameEncode(data),
                        object : BleWriteCallback() {
                            override fun onWriteSuccess(
                                current: Int, total: Int, justWrite: ByteArray
                            ) {
                                // 发送数据到设备成功（分包发送的情况下，可以通过方法中返回的参数可以查看发送进度）
                                if (index < totalSize) {
                                    Logger.d("$TAG=abf4写入成功=第${index}个包===总共${totalSize}个包====")
                                } else {
                                    Logger.d("$TAG=abf4完全写入成功=第${index}个包=总共${totalSize}个包======")
                                }
                                if (isActive) {
                                    continuation.resume(true)
                                }

                            }

                            override fun onWriteFailure(exception: BleException) {
                                // 发送数据到设备失败
                                Logger.e("$TAG=abf4写入失败=====第${index}个包===总共${totalSize}个包====${exception.code}===${exception.description}")
                                if (isActive) {
                                    continuation.resume(false)
                                }
                                //continuation.resumeWithException(Exception("Write abf4写入失败: ${exception.code}==${exception.description}"))
                            }
                        })
                }
            }
        }
    }


    // 递归写入函数
    suspend fun fmWriteABF4(dataList: MutableList<MPMessage.MPSendMsg>) {
        var success = true
        Logger.d("$TAG========总共要发${dataList.size}个包")
        for (index in 0 until dataList.size) {
            try {
                val data = dataList[index]

                val dataArray = data.toByteArray()
                Logger.d("$TAG========第${index}===字节数${dataArray.size}")
                success = writeABF4(dataArray, index, dataList.size)
                if (success) {
                    // 如果写入成功，继续递归写入
                    delay(6)//延时写入
                    // continue
                } else {
                    // 如果写入失败，停止递归写入
                    // break
                }
            } catch (e: Exception) {

            }
        }
    }


    private var abf3NotifyCallback: ((FmNotifyBean) -> Unit)? = null


    //暴露一个读取通知的回调吧
    fun setFmAbf3NotifyCallback(call: (FmNotifyBean) -> Unit) {
        this.abf3NotifyCallback = call
    }

    private fun initFmNotify() {
        if (fmBle != null) {
            val notify = fmBle?.getCharatersType(FMPrinter.Charac_ABF3)?.get(0)
            Logger.d("$TAG=======正在打开通知serviceUUID=${notify?.first}==CharaUUID=${notify?.second}")
            BleManager.getInstance().notify(fmBle?.bleDevice,
                notify?.first.toString(),
                notify?.second.toString(),
                object : BleNotifyCallback() {
                    override fun onNotifySuccess() {
                        // 打开通知操作成功
                        Logger.d("$TAG=======打开通知操作成功==")
                        abf3NotifyCallback?.invoke(FmNotifyBean(1))
                    }

                    override fun onNotifyFailure(exception: BleException) {
                        // 打开通知操作失败
                        Logger.d("$TAG=======打开通知操作失败==${exception.code}====${exception.description}")
                        abf3NotifyCallback?.invoke(FmNotifyBean(0, exception))
                    }

                    override fun onCharacteristicChanged(data: ByteArray) {
                        // 打开通知后，设备发过来的数据将在这里出现
                        Logger.d(
                            "$TAG===ABF3====收到蓝牙数据${
                                String(data, Charsets.UTF_8)
                            }==>字节数据长度>>>>>${data.size}"
                        )

                        val fmNotifyBean = FmNotifyBean(2, byteArray = data)
                        fmNotifyBean.getFmDeviceInfo()
                        abf3NotifyCallback?.invoke(fmNotifyBean)
                    }
                })
        }
    }


    /**
     * 设置最大传输单元，设置里是默认20左右
     */
    private suspend fun initBleMTU(bleDevice: BleDevice): Boolean {
        val MAX_MTU = 180
        return suspendCoroutine { continuation ->
            BleManager.getInstance().setMtu(bleDevice, MAX_MTU, object : BleMtuChangedCallback() {
                override fun onSetMTUFailure(exception: BleException) {
                    // 设置 MTU 失败
                    Logger.e("$TAG 设置MTU失败==${exception.description}")

                    continuation.resume(false) // 返回 false 表示初始化失败

                }

                override fun onMtuChanged(mtu: Int) {
                    // 设置 MTU 成功
                    Logger.d("$TAG 设置MTU成功==${mtu}")

                    continuation.resume(true) // 返回 true 表示初始化成功


                }
            })

        }


    }

    fun conenctBle2(bleDevice: BleDevice) {
        serviceScope?.launch {
            withContext(Dispatchers.IO) {
                if (fmBle != null) {
                    Logger.d("$TAG fmBle!=null")
                }
                val connectBleTask = async { connectBLe(bleDevice) }
                fmBle = connectBleTask.await()
                if (fmBle != null && fmBle?.bleDevice != null) {
                    initBleMTU(fmBle!!.bleDevice!!)
                    val setMTUTask = async { initBleMTU(fmBle!!.bleDevice!!) }
                    val mtuflag = setMTUTask.await()
                    Logger.d("$TAG 协诚设置MTU成功==${mtuflag}")
                    if (mtuflag) {
                        //注测读取通道
                        initFmNotify()
                    }
                }
            }
        }
    }

    /**
     * 连接蓝牙
     */
    private suspend fun connectBLe(bleDevice: BleDevice): FMBle? {
        return suspendCancellableCoroutine<FMBle?> { cancellableContinuation ->
            val callback = object : BleGattCallback() {
                override fun onStartConnect() {
                    Logger.d("$TAG,开始连接")
                }

                override fun onConnectFail(bleDevice: BleDevice, exception: BleException?) {
                    Logger.e("$TAG,连接失败${bleDevice.device.name}::失败原因==${exception?.code}===${exception?.description}")





                    if (exception?.code == 100) {
                        if (bleDevice != null) {
                            BleManager.getInstance().disconnect(bleDevice)
                            Logger.e("$TAG 蓝牙连接超时，很有可能已经连接了")
                        }
                    }

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
                    //建立读通道
                    // 3.	ABF3: 通知的特征(Notify), 打印机上报任何信息都通过这个特征来读取
                    initFmNotify()
                }

                override fun onDisConnected(
                    isActiveDisConnected: Boolean,
                    device: BleDevice?,
                    gatt: BluetoothGatt?,
                    status: Int
                ) {
                    Logger.d("$TAG,断开连接")
                }

            }
            BleManager.getInstance().connect(bleDevice, callback)
        }
    }


    suspend fun startBleScan(): List<BleDevice> {
        return suspendCancellableCoroutine { continuation ->
            val callback = object : BleScanCallback() {
                override fun onScanStarted(success: Boolean) {
                    // 扫描开始时的回调
                }

                override fun onLeScan(bleDevice: BleDevice?) {
                    // 扫描到设备时的回调
                }

                override fun onScanning(bleDevice: BleDevice?) {
                    // 正在扫描中的回调
                }

                override fun onScanFinished(scanResultList: MutableList<BleDevice>?) {
                    // 扫描完成时的回调
                    if (scanResultList != null) {
                        continuation.resume(scanResultList) {
                            Logger.e("协诚返回数据异常:${it.message.toString()}")
                        } // 将扫描结果列表作为结果传递给协程
                    } else {
                        continuation.resume(emptyList()) {
                            Logger.e("协诚返回数据异常:${it.message.toString()}")
                        } // 如果扫描结果列表为空，则返回空列表
                    }
                }
            }
            BleManager.getInstance().scan(callback)
            // 在挂起函数中注册取消回调，以便在协程取消时取消蓝牙扫描
            continuation.invokeOnCancellation {
                BleManager.getInstance().cancelScan()
            }
        }
    }


    private fun UpBleUI() {


    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.d("${TAG}销毁BLE服务")
        serviceScope?.cancel()
    }
}