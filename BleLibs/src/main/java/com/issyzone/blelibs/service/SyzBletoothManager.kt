package com.issyzone.blelibs.service

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
import com.issyzone.blelibs.BleManager
import com.issyzone.blelibs.FMPrinter
import com.issyzone.blelibs.SYZBleUtils
import com.issyzone.blelibs.callback.BleGattCallback
import com.issyzone.blelibs.callback.BleMtuChangedCallback
import com.issyzone.blelibs.callback.BleNotifyCallback
import com.issyzone.blelibs.data.BleDevice
import com.issyzone.blelibs.exception.BleException
import com.issyzone.blelibs.upacker.MsgCallback
import com.issyzone.blelibs.upacker.Upacker
import com.issyzone.blelibs.utils.AppGlobels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


class SyzBletoothManager {
    private val TAG = "SyzBletoothManager>>>>>"

    companion object {
        private var instance: SyzBletoothManager? = null
        private var bleScope: CoroutineScope? = null
        private var notifyScope: CoroutineScope? = null
        private var notifyDataProcessor:NotifyDataProcessor?=null
        fun getInstance(): SyzBletoothManager {
            if (instance == null) {
                instance = SyzBletoothManager()
                bleScope=CoroutineScope(Dispatchers.IO)
                notifyScope=CoroutineScope(Dispatchers.IO)

            }
            return instance!!
        }
    }
    private inner class NotifyDataProcessor(private val myNotifyScope: CoroutineScope) {
        private val dataChannel = Channel<ByteArray>(Channel.UNLIMITED)

        init {
            // 启动一个单独的协程，专门用来按顺序处理数据
            myNotifyScope.launch {
                for (data in dataChannel) {
                    try {
                       // spp_read(data)
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

    private var myBleDevice:BleDevice?=null;
    private var myGatt:BluetoothGatt?=null
    private val connectBleGattBack = object : BleGattCallback() {
        override fun onStartConnect() {
            Log.e(TAG,"onStartConnect》》》")
        }

        override fun onConnectFail(bleDevice: BleDevice?, exception: BleException?) {
            Log.e(TAG,"onConnectFail》》》")
        }

        override fun onConnectSuccess(bleDevice: BleDevice, gatt: BluetoothGatt, status: Int) {
            Log.d(TAG,"onConnectSuccess》》》")
            myBleDevice=bleDevice
            myGatt=gatt
            setMtu()
        }

        override fun onDisConnected(
            isActiveDisConnected: Boolean,
            device: BleDevice?,
            gatt: BluetoothGatt?,
            status: Int
        ) {
           Log.d(TAG,"onDisConnected》》》")
        }
    }
    private val bleMtuChangedCallback=object: BleMtuChangedCallback(){
        override fun onSetMTUFailure(exception: BleException?) {
            Log.e(TAG,"MTU设置失败")
        }

        override fun onMtuChanged(mtu: Int) {
            Log.d(TAG,"MTU设置成功${mtu}")
            getChannels()
            initNotify()
        }

        private fun initNotify() {
            SYZBleUtils.findChannel(channelList,FMPrinter.Charac_ABF3)?.apply {
                BleManager.getInstance().notify2(myBleDevice,this.service.uuid.toString(),this,notifyCallback)
            }
        }
    }
    //
    private val notifyCallback=object :BleNotifyCallback(){
        override fun onNotifySuccess() {
            Log.i(TAG,"onNotifySuccess")
            notifyDataProcessor= NotifyDataProcessor(notifyScope!!)
        }

        override fun onNotifyFailure(exception: BleException?) {
            Log.e(TAG,"onNotifyFailure")
        }

        override fun onCharacteristicChanged(data: ByteArray?) {
            Log.d(TAG,"onCharacteristicChanged")
            notifyDataProcessor?.apply {

            }
        }
    }


    /**
     * 专门用来梳理接收的消息
     */









    private val channelList= mutableListOf<BluetoothGattCharacteristic>()
    private fun getChannels(){
        myGatt?.apply {
            channelList.clear()
            channelList.addAll(getFMPrinterCharacteristics(this))
        }
    }

    // 假设bluetoothGatt是已连接设备的BluetoothGatt实例
    private fun getFMPrinterCharacteristics(bluetoothGatt: BluetoothGatt): List<BluetoothGattCharacteristic> {
        val matchingCharacteristics = mutableListOf<BluetoothGattCharacteristic>()
        // 遍历所有服务
        bluetoothGatt.services.forEach { gattService ->
            FMPrinter.values().forEach { fmPrinter ->
                // 检查服务ID是否匹配
                if (gattService.uuid.toString().lowercase().startsWith(fmPrinter.serviceId)) {
                    // 遍历该服务的所有特征
                    gattService.characteristics.forEach { characteristic ->
                        // 检查特征ID是否匹配
                        if (characteristic.uuid.toString().lowercase().startsWith(fmPrinter.characterid)) {
                            matchingCharacteristics.add(characteristic)
                        }
                    }
                }
            }
        }
        return matchingCharacteristics
    }

    private fun setMtu() {
        val MAX_MTU = 180
        BleManager.getInstance().setMtu(myBleDevice,MAX_MTU,bleMtuChangedCallback)
    }

    fun  connectBle(mac: String){
        BleManager.getInstance().connect(mac,connectBleGattBack)
    }

    //初始化ble
    fun initBle(){
        BleManager.getInstance().init(AppGlobels.getApplication());
        BleManager.getInstance().enableLog(true)
            .setReConnectCount(1, 1000).operateTimeout = 5000
    }


}