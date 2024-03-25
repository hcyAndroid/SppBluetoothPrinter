package com.issyzone.blelibs

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.issyzone.blelibs.callback.BleMtuChangedCallback
import com.issyzone.blelibs.callback.BleScanCallback
import com.issyzone.blelibs.data.BleDevice
import com.issyzone.blelibs.exception.BleException
import com.issyzone.blelibs.scan.BleScanRuleConfig
import com.issyzone.blelibs.utils.AppGlobels
import java.nio.charset.Charset
import java.util.UUID


/**
 *
 */
object SYZBleUtils {
    private const val TAG = "SYZBleUtils"
    var scanBleResultLiveData= MutableLiveData<MutableList<BleDevice>?>()

   // var scanResultFlow = MutableStateFlow<MutableList<BleDevice>?>(mutableListOf())
    /**
     * 判定是否支持ble
     */
    fun isSupportBle(): Boolean {
        return BleManager.getInstance().isSupportBle;
    }

    fun initBle(needScan:Boolean=false) {
        BleManager.getInstance().init(AppGlobels.getApplication());
        BleManager.getInstance().enableLog(true)
            .setReConnectCount(0, 10000).operateTimeout = 5000

        if (needScan){
            val scanRuleConfig = BleScanRuleConfig.Builder()
                //.setServiceUuids(serviceUuids) // 只扫描指定的服务的设备，可选
                //.setDeviceName(true, names) // 只扫描指定广播名的设备，可选
                // .setDeviceMac(mac) // 只扫描指定mac的设备，可选
                // .setAutoConnect(isAutoConnect) // 连接时的autoConnect参数，可选，默认false
                .setScanTimeOut(10000) // 扫描超时时间，可选，默认10秒
                .build()
            BleManager.getInstance().initScanRule(scanRuleConfig)
        }
    }

    //手动判断蓝牙是否开启 ble open
    fun isBleOpen(): Boolean {
        return BleManager.getInstance().isBlueEnable
    }
    fun scanBle() {
       BleManager.getInstance().scan(object : BleScanCallback() {
           override fun onScanStarted(success: Boolean) {
               Log.d("$TAG","扫描开始${success}")
           }


           override fun onScanning(bleDevice: BleDevice?) {
               Log.d("$TAG","扫描中${bleDevice?.device?.name}===${bleDevice?.device?.address}")
           }

           override fun onScanFinished(scanResultList: MutableList<BleDevice>?) {
//                var scanTag = if (scanResultList.isNullOrEmpty()) {
//                    "没扫到任何BLE设备"
//                } else {
//                    var tag = StringBuffer()
//                    scanResultList?.forEach {
//                        tag.append("${it.device.name}=====${it.device.address}==\n")
//                    }
//                    tag.toString()
//                }
               Log.d("$TAG","扫描结束==${scanResultList?.size ?: 0}")

               if (scanResultList.isNullOrEmpty()){
                   scanBleResultLiveData.postValue(mutableListOf())
               }else{
                   scanBleResultLiveData.postValue(scanResultList)
               }

           }

       })
   }

    /**
     * 由于苹果ble无法直接获取外设的mac地址,所以需要外设在开启蓝牙的时候将设备mac地址广播出来.
     * 将mac地址的字符串写入蓝牙的ManufacturerDataKey 字段中,
     * 蓝牙Mac地址是由六个十六进制数字对组成的，每对数字之间用冒号分隔。例如，00:1A:2B:3C:4D:5E
     */
    fun advertiseMacForApple(mac: String) {
        var manufacturerDataKey = 0
        val bluetoothAdapter = BleManager.getInstance().bluetoothAdapter
        val bluetoothLeAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser
        val manufacturerData = mac.toByteArray(Charset.forName("UTF-8"))
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)//设置广播的模式-低延迟模式
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)//设置广播的传输功率水平-最高功率水平
            .setConnectable(true)//蓝牙广播的可连接性
            .build()
        val data = AdvertiseData.Builder().setIncludeDeviceName(true)//设置广播中是否标记这设备名称
            .addManufacturerData(
                manufacturerDataKey, manufacturerData
            ).build()
        bluetoothLeAdvertiser.startAdvertising(settings, data, object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                super.onStartSuccess(settingsInEffect)
            }

            override fun onStartFailure(errorCode: Int) {
                super.onStartFailure(errorCode)

            }
        })
    }



    fun isMyNeedNotify(desc: BluetoothGattDescriptor):Boolean{
      return  desc.characteristic.uuid.toString().lowercase().startsWith(FMPrinter.Charac_ABF3.characterid.lowercase())
    }


    fun getNotifyCharac(gatt: BluetoothGatt?):Pair<UUID, BluetoothGattCharacteristic>?{
        val serviceList= gatt?.services?.filter {
            it.uuid.toString().lowercase().startsWith(FMPrinter.Charac_ABF3.serviceId.lowercase())
        }?.toMutableList()
        if (!serviceList.isNullOrEmpty()){
            val characList= serviceList[0].characteristics.filter {
                it.uuid.toString().lowercase().startsWith(FMPrinter.Charac_ABF3.characterid.lowercase())
            }.toMutableList()
            if (!characList.isNullOrEmpty()){
               return Pair(serviceList[0].uuid,characList[0])
            }
        }
        return null
    }


    fun getABF4Charac(gatt: BluetoothGatt?):Pair<UUID, BluetoothGattCharacteristic>?{
        val serviceList= gatt?.services?.filter {
            it.uuid.toString().lowercase().startsWith(FMPrinter.Charac_ABF4.serviceId.lowercase())
        }?.toMutableList()
        if (!serviceList.isNullOrEmpty()){
            val characList= serviceList[0].characteristics.filter {
                it.uuid.toString().lowercase().startsWith(FMPrinter.Charac_ABF4.characterid.lowercase())
            }.toMutableList()
            if (!characList.isNullOrEmpty()){
                return Pair(serviceList[0].uuid,characList[0])
            }
        }
        return null
    }

}