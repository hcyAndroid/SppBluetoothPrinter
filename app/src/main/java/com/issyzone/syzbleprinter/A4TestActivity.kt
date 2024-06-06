package com.issyzone.syzbleprinter

import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.issyzone.classicblulib.utils.AppGlobels
import com.issyzone.syzbleprinter.databinding.ActivityA4TestBinding
import com.issyzone.syzbleprinter.utils.invokeViewBinding
import com.jxprint.BTPrinterManager
import com.jxprint.converter.FloydBitmapTextModeConverter
import com.jxprint.enumdata.BTConnectionEnum
import com.jxprint.listener.BluetoothConnectionListener

class A4TestActivity : ComponentActivity() {
    private val vm: ActivityA4TestBinding by invokeViewBinding()
    val lo = "60:6E:41:A7:29:9B"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(vm.root)
        initA4()
        vm.tvConnect.setOnClickListener {

            val bluManager= AppGlobels.getApplication().getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            //var isSu=bluetooth?.startDiscovery()
            bluManager?.adapter?.startDiscovery()



            BTPrinterManager.getInstance()
                .connectBluetoothDevice(lo,"ITP02A-00234")

        }
        BTPrinterManager.getInstance().setBluetoothConnectionListener(object :BluetoothConnectionListener{
            override fun onConnectionState(p0: BTConnectionEnum, p1: String, p2: String) {
                when (p0) {
                    BTConnectionEnum.BT_Connecting -> {
                        Log.e("onConnectionState", "连接中。。。")

                    }

                    BTConnectionEnum.BT_Connected -> {
                        Log.e("onConnectionState", "已连接。。。")


                    }

                    BTConnectionEnum.BT_Pairing -> {
                        Log.e("onConnectionState", "配对中。。。")

                    }

                    BTConnectionEnum.BT_Paired -> {
                        Log.e("onConnectionState", "已配对。。。")

                    }

                    BTConnectionEnum.BT_PairFailed -> {
                        Log.e("onConnectionState", "配对失败。。。")

                    }

                    BTConnectionEnum.BT_Disconnected -> {
                        Log.e("onConnectionState", "已断开。。。")

                    }

                    BTConnectionEnum.BT_ConnectFailed -> {
                        Log.e("onConnectionState", "连接失败。。。")
                    }
                }
            }

            override fun onQRCodeInvalidIip() {

            }

        })
    }

    private fun initA4() {
        BTPrinterManager.getInstance().init(this@A4TestActivity)
           //文字增强 默认2档 总共5挡 0-4逐渐更黑  grayValue可调色值0-255


    }
}