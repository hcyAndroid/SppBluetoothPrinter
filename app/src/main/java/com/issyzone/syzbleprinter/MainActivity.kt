package com.issyzone.syzbleprinter

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.issyzone.blelibs.data.SyzPrinterState
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.issyzone.blelibs.R
import com.issyzone.blelibs.callback.SyzBleCallBack
import com.issyzone.blelibs.classicblu.SyzClassicBluManager
import com.issyzone.blelibs.data.BleDevice
import com.issyzone.blelibs.data.SyzPrinterState2
import com.issyzone.blelibs.dataimp.BlePrinterInfoCall
import com.issyzone.blelibs.dataimp.BlePrinterInfoCall2
import com.issyzone.blelibs.dataimp.DeviceBleInfoCall
import com.issyzone.blelibs.dataimp.DeviceInfoCall
import com.issyzone.blelibs.fmBeans.MPMessage
import com.issyzone.blelibs.permission.SYZBlePermission
import com.issyzone.blelibs.service.SyzBleManager
import com.issyzone.blelibs.utils.BitmapExt
import com.issyzone.blelibs.utils.ImageUtilKt
import com.issyzone.blelibs.utils.SYZFileUtils
import com.issyzone.blelibs.utils.TextUtils
import com.issyzone.syzbleprinter.databinding.ActivityMainBinding
import com.issyzone.syzbleprinter.utils.invokeViewBinding
import com.jxprint.BTPrinterManager


class MainActivity : ComponentActivity() {
    private val vm: ActivityMainBinding by invokeViewBinding()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(vm.root)
        TextUtils.tv = vm.tvLog
        TextUtils.lifeScope = lifecycleScope

        initRecyclerView()

        SyzBleManager.getInstance().initBle()
        // BTPrinterManager.getInstance().connectBluetoothDevice()


    }

     val pair = Pair("RW402B", "DD:0D:30:00:02:E5")

    //val pair = Pair("FM226", "03:25:65:DF:0B:0C")

    var pair2 = Pair(48, 48)

    //var pair4 = Pair(108, 152)
    private fun initRecyclerView() {
        SyzBleManager.getInstance().setBleCallBack(object : SyzBleCallBack {
            override fun onStartConnect() {
                Log.d("", "当前线程名onStartConnect：${Thread.currentThread().name}")
            }

            override fun onConnectFail(bleDevice: BleDevice?, msg: String) {
                Log.d("", "当前线程名onConnectFail：${Thread.currentThread().name}")
            }

            override fun onConnectFailNeedUserRestart(bleDevice: BleDevice?, msg: String) {
                Log.d("", "当前线程名onConnectFailNeedUserRestart：${Thread.currentThread().name}")
            }

            override fun onConnectSuccess(bleDevice: BleDevice?) {
                Log.d("", "当前线程名onConnectSuccess：${Thread.currentThread().name}")
                TextUtils.log("ble连接成功::${bleDevice?.device}===${bleDevice?.mac}")
            }

            override fun onDisConnected(device: BleDevice?) {
                Log.d("", "当前线程名onDisConnected：${Thread.currentThread().name}")
            }

        })
        SyzBleManager.getInstance().setActivelyReportBack {
            Log.d("", "主动上报>>>>${it.toString()}")
            when (it) {
                SyzPrinterState2.PRINTER_LID_OPEN -> {
                    Log.d("", "主动上报>>>>开盖")
                }

                SyzPrinterState2.PRINTER_OK -> {
                    Log.d("", "主动上报>>>>打印OK")
                }

                else -> {

                }
            }
        }
        vm.tvConnect.setOnClickListener {
            //RW402B,DD:0D:30:00:02:E5
            //03:25:65:DF:0B:0C  FM226
            // SyzClassicBluManager.getInstance().connectToDevice(pair.second)
             SyzBleManager.getInstance().connectBle(pair.first, pair.second)
        }

        vm.tvDisconnect.setOnClickListener {
            if (SyzBleManager.getInstance().isBleConnected()) {
                Log.d("", "判断蓝牙是否已经连接>>>>${SyzBleManager.getInstance().isBleConnected()}")
                SyzBleManager.getInstance().disconnectBle()
            } else {
                Log.d("", "判断蓝牙没有连接")
                SyzBleManager.getInstance().connectBle(pair.first, pair.second)
            }

        }
        vm.tvCheck.setOnClickListener {
            SyzBleManager.getInstance().getDeviceInfo(object : DeviceInfoCall {
                override fun getDeviceInfo(msg: MPMessage.MPDeviceInfoMsg) {
                    Log.d("", "获取设备信息${msg.toString()}")
                    msg.paperStatus
                }

                override fun getDeviceInfoError(errorMsg: MPMessage.MPCodeMsg) {

                }
            })
        }


        vm.tvShutdown.setOnClickListener {
            SyzBleManager.getInstance().writeShutdown(2, object : DeviceBleInfoCall {
                override fun getBleNotifyInfo(isSuccess: Boolean, msg: MPMessage.MPCodeMsg?) {
                    if (isSuccess) {
                        Log.d("", "设置关机时间成功>>>>${msg.toString()}")
                    } else {
                        Log.d("", "设置关机时间失败>>>>${msg.toString()}")
                    }
                }
            })
        }
        vm.tvCancelPrinter.setOnClickListener {
            SyzBleManager.getInstance().writeCancelPrinter(object : DeviceBleInfoCall {
                override fun getBleNotifyInfo(isSuccess: Boolean, msg: MPMessage.MPCodeMsg?) {
                    if (isSuccess) {
                        Log.d("", "取消打印成功>>>>${msg.toString()}")
                    } else {
                        Log.d("", "取消打印失败>>>>${msg.toString()}")
                    }
                }
            })
        }
        vm.tvSetPrintSpeed.setOnClickListener {
            SyzBleManager.getInstance().writePrintSpeed(2, object : DeviceBleInfoCall {
                override fun getBleNotifyInfo(isSuccess: Boolean, msg: MPMessage.MPCodeMsg?) {
                    if (isSuccess) {
                        Log.d("", "设置打印速度成功>>>>${msg.toString()}")
                    } else {
                        Log.d("", "设置打印速度失败>>>>${msg.toString()}")
                    }
                }
            })
        }
        vm.tvSetPrintConcentration.setOnClickListener {
            SyzBleManager.getInstance().writePrintConcentration(4, object : DeviceBleInfoCall {
                override fun getBleNotifyInfo(isSuccess: Boolean, msg: MPMessage.MPCodeMsg?) {
                    if (isSuccess) {
                        Log.d("", "设置打印浓度成功>>>>${msg.toString()}")
                    } else {
                        Log.d("", "设置打印浓度失败>>>>${msg.toString()}")
                    }
                }
            })
        }

        vm.tvSelfChecking.setOnClickListener {
            SyzBleManager.getInstance().writeSelfCheck(object : BlePrinterInfoCall {
                override fun getBleNotifyInfo(isSuccess: Boolean, msg: SyzPrinterState) {
                    if (isSuccess) {
                        Log.d("", "打印自检页成功>>>>${msg.toString()}")
                    } else {
                        Log.d("", "打印自检页失败>>>>${msg.toString()}")
                    }
                }
            })
        }
        vm.tvSetPrintImg.setOnClickListener {
            Log.d(
                "", "FmBitmapPrinterUtils》》》bitmap字节数${
                    BitmapExt.bitmapToByteArray(
                        BitmapExt.decodeBitmap(R.drawable.test6)
                    ).size
                }"
            )
            Log.d("", "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT111")
            val bitmap = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test6), 128)
            Log.d("", "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT22222")
            val bitmap2 = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test7), 128)


            SyzBleManager.getInstance().writeBitmaps(
                mutableListOf(bitmap, bitmap2, bitmap),
                102,
                152,
                1,
                object : BlePrinterInfoCall2 {
                    override fun getBleNotifyInfo(isSuccess: Boolean, msg: SyzPrinterState2) {
                        if (isSuccess) {
                            Log.i("图片打印成功>>>", msg.toString())
                        } else {
                            Log.i("图片打印失败>>>", msg.toString())
                        }
                    }
                })
        }
        vm.tvDexUpdate.setOnClickListener {
            // val path = SYZFileUtils.copyAssetGetFilePath("FM226_print_app(1.1.0.0.8).bin")
            val path = SYZFileUtils.copyAssetGetFilePath("rw402_pa_v1.0.0.bin")
            path?.apply {
                SyzBleManager.getInstance().writeDex(this) {
                    if (it == SyzPrinterState.PRINTER_DEXUPDATE_SUCCESS) {
                        Log.d("", "固件更新成功")
                    } else if (it == SyzPrinterState.PRINTER_DEXUPDATE_FAILED) {
                        Log.d("", "固件更新失败")
                    } else {
                        Log.d("", "固件更新其他异常${it.toString()}==${it.code}===${it.info}")
                        // SyzBleManager.getInstance().stopWriteDex()
                    }
                }
            }
        }
    }


}