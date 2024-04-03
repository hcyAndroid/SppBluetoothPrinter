package com.issyzone.blelibs.classicblu

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.IntentFilter
import android.util.Log
import com.issyzone.blelibs.upacker.Upacker
import com.issyzone.blelibs.utils.AppGlobels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class SyzClassicBluManager {
    private val TAG = "SyzClassicBluManager>>"
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var mmSocket: BluetoothSocket? = null
    private var MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")


    private fun registerReceiver() {
        val intent = IntentFilter()
        intent.addAction("android.bluetooth.device.action.FOUND")
        intent.addAction("android.bluetooth.device.action.BOND_STATE_CHANGED")
        intent.addAction("android.bluetooth.adapter.action.SCAN_MODE_CHANGED")
        intent.addAction("android.bluetooth.adapter.action.STATE_CHANGED")
        intent.addAction("android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED")
        intent.addAction("android.bluetooth.device.action.PAIRING_REQUEST")
        intent.addAction("android.bluetooth.a2dp.profile.action.PLAYING_STATE_CHANGED")
        intent.addAction("android.bluetooth.adapter.action.DISCOVERY_STARTED")
        intent.addAction("android.bluetooth.adapter.action.DISCOVERY_FINISHED")
        intent.addAction("android.bluetooth.device.action.ACL_DISCONNECT_REQUESTED")
        intent.addAction("android.bluetooth.device.action.ACL_DISCONNECTED")
        intent.addAction("android.bluetooth.device.action.ACL_CONNECTED")
        AppGlobels.getApplication().registerReceiver(SyzClassicBluReciver(), intent)
    }


    fun getBluetoothAdapter(): BluetoothAdapter? {
        return bluetoothAdapter
    }

    fun getTestBlue() {
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevices?.filter {
            (!it.name.isNullOrEmpty()) && (!it.address.isNullOrEmpty()) && ((it.name.lowercase()
                .startsWith("fm")) || (it.name.lowercase().startsWith("rw")))

        }?.forEach { device ->
            val deviceName = device.name
            val deviceHardwareAddress = device.address // MAC address
            Log.i("1经典蓝牙设备>>>", deviceName)
            Log.i("1经典蓝牙设备mac>>>", deviceHardwareAddress ?: "没有mac")
        }

    }

    companion object {
        private var instance: SyzClassicBluManager? = null
        fun getInstance(): SyzClassicBluManager {
            if (instance == null) {
                instance = SyzClassicBluManager()
            }
            return instance!!
        }
    }

    fun init() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        registerReceiver()
    }

    private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream
    private var mmInStream: DataInputStream? = null
    private var mmOutStream: DataOutputStream? = null

    fun connectToDevice(address: String) {
        val device: BluetoothDevice? = bluetoothAdapter?.getRemoteDevice(address)
        if (device == null) {
            Log.e(TAG, "Bluetooth device is null")
            return
        }
        try {
            // 创建 RFCOMM 套接字并连接到远程设备
            mmSocket = device.createRfcommSocketToServiceRecord(MY_UUID)
            mmSocket?.connect()
            mmInStream = DataInputStream(mmSocket?.inputStream)
            mmOutStream = DataOutputStream(mmSocket?.outputStream)
            // 连接成功
            Log.d(TAG, "Connected to device: $address")
            // 可以在这里进行数据通信等操作

            initReadChannel()
        } catch (e: IOException) {
            // 连接失败
            Log.e(TAG, "Connection failed: ${e.message}")
            try {
                mmSocket?.close()
            } catch (closeException: IOException) {
                Log.e(TAG, "Unable to close socket: ${closeException.message}")
            }
        }
    }

    private fun initReadChannel() {
        while (true){
            try {
                val buffer = ByteArray(1024)
                val bytesRead = mmInStream?.read(buffer)
                if (bytesRead != null && bytesRead > 0) {
                    val data = ByteArray(bytesRead)
                    System.arraycopy(buffer, 0, data, 0, bytesRead)
                    Log.d(TAG, "Data read: ${data.contentToString()}")
                   // return data
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error reading data: ${e.message}")
            }
        }
    }


    fun readData(): ByteArray? {
        try {
            val buffer = ByteArray(1024)
            val bytesRead = mmInStream?.read(buffer)
            if (bytesRead != null && bytesRead > 0) {
                val data = ByteArray(bytesRead)
                System.arraycopy(buffer, 0, data, 0, bytesRead)
                Log.d(TAG, "Data read: ${data.contentToString()}")
                return data
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error reading data: ${e.message}")
        }
        return null
    }

    fun write(bytes: ByteArray) {
        try {
            val data = Upacker.frameEncode(bytes)
            mmOutStream?.write(data)
            mmOutStream?.flush()
            Log.d(TAG, "Command sent:")
        } catch (e: IOException) {
            Log.e(TAG, "Error sending command: ${e.message}")
        }
    }

    fun disconnect() {
        try {
            mmOutStream?.close()
            mmSocket?.close()
            Log.d(TAG, "Disconnected from device")
        } catch (e: IOException) {
            Log.e(TAG, "Error closing socket: ${e.message}")
        }
    }
}


