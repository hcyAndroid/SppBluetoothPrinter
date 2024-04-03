package com.issyzone.syzbleprinter


import android.bluetooth.BluetoothDevice

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity


import com.issyzone.classicblulib.R
import com.issyzone.blelibs.permission.SYZBlePermission
import com.issyzone.blelibs.utils.BitmapExt
import com.issyzone.blelibs.utils.ImageUtilKt
import com.issyzone.blelibs.utils.SYZFileUtils
import com.issyzone.classicblulib.bean.LogLiveData
import com.issyzone.classicblulib.bean.MPMessage
import com.issyzone.classicblulib.bean.SyzFirmwareType
import com.issyzone.classicblulib.bean.SyzPrinter
import com.issyzone.classicblulib.callback.BluPrinterInfoCall2
import com.issyzone.classicblulib.callback.CancelPrintCallBack
import com.issyzone.classicblulib.callback.DeviceBleInfoCall
import com.issyzone.classicblulib.callback.DeviceInfoCall
import com.issyzone.classicblulib.callback.SyzBluCallBack
import com.issyzone.classicblulib.callback.SyzPrinterState
import com.issyzone.classicblulib.callback.SyzPrinterState2
import com.issyzone.classicblulib.service.SyzClassicBluManager
import com.issyzone.classicblulib.tools.SpUtils
import com.issyzone.syzbleprinter.databinding.ActivityMain3Binding

import com.issyzone.syzbleprinter.utils.invokeViewBinding

/**
 * 2寸的页面
 */
class MainActivity4 : ComponentActivity() {
    private val vm: ActivityMain3Binding by invokeViewBinding()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(vm.root)
//        val sFscSppCentralApi = FscSppCentralApiImp.getInstance(MainActivity3@ this)
//        sFscSppCentralApi.initialize()

        val lo = "03:26:A0:AE:0B:57"
        val lo2 = "03:22:55:BF:00:0F"
        vm.tvType.text = "2寸demo"
        // TEst.test()
        val localmac = SpUtils.readData("mac2")
        if (localmac.isNullOrEmpty()) {
            vm.etMac.setText(lo2)
        } else {
            vm.etMac.setText(localmac)
        }


        LogLiveData.showLogs(this, vm.tvLog)

        SyzClassicBluManager.getInstance().initClassicBlu()
        SyzClassicBluManager.getInstance().setBluCallBack(object : SyzBluCallBack {
            override fun onStartConnect() {
                Log.i("SYZ2寸>>>", "开始连接")
                LogLiveData.addLogs("开始连接")
            }

            override fun onConnectFail(msg: String?) {
                Log.i("SYZ>>>", "onConnectFail")
                LogLiveData.addLogs("经典蓝牙连接失败==${msg}")

            }

            override fun onConnectSuccess(device: BluetoothDevice) {
                Log.i("SYZ>>>", "onConnectSuccess==${device.name}====${device.address}")
                LogLiveData.addLogs("经典蓝牙连接成功==${device.name}====${device.address}")
                SpUtils.saveData("mac2", device.address)
            }

            override fun onDisConnected() {
                Log.i("SYZ>>>", "onDisConnected")
                LogLiveData.addLogs("经典蓝牙已经断开")
                LogLiveData.clearLog(vm.tvLog)
            }
        })
        SYZBlePermission.checkBlePermission(this) {


            //  SyzClassicBluManager.getInstance().init()
//            val filter = IntentFilter()
//            filter.addAction(BluetoothDevice.ACTION_FOUND)
//            filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
//            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
//            Log.e("TGA", "注册扫描广播")
//            registerReceiver(MY(list), filter)
            // SyzClassicBluManager.getInstance().getBluetoothAdapter()?.startDiscovery()
        }
        vm.tvConnect.setOnClickListener {
            Log.i("点击", ">>>>")
            LogLiveData.addLogs("准备连接2寸adress=${vm.etMac.text.toString()}")
            SyzClassicBluManager.getInstance().connect(vm.etMac.text.toString())
            // SyzClassicBluManager.getInstance().connect("DC:1D:30:78:F7:7A")

//            /
            // SyzClassicBluManager.getInstance().connect("03:22:55:BF:00:0F")
        }
        vm.tvDisconnect.setOnClickListener {
            SyzClassicBluManager.getInstance().disConnectBlu()
        }
        vm.tvCheck.setOnClickListener {
            Log.i("当前是否连接》》》", "${SyzClassicBluManager.getInstance().isBluConnected()}")
            SyzClassicBluManager.getInstance().getDeviceInfo(object : DeviceInfoCall {
                override fun getDeviceInfo(msg: MPMessage.MPDeviceInfoMsg) {
                    Log.i("获取设备信息>>", "${msg.toString()}")
                }

                override fun getDeviceInfoError(errorMsg: MPMessage.MPCodeMsg) {

                }
            })
        }
        vm.tvDexUpdate.setOnClickListener {
            val path = SYZFileUtils.copyAssetGetFilePath("FM226_print_app(11).bin")
            path?.apply {
                SyzClassicBluManager.getInstance().writeDex(this) {
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
        vm.tvSelfChecking.setOnClickListener {
            SyzClassicBluManager.getInstance().writeSelfCheck()
        }
        vm.tvShutdown.setOnClickListener {
            SyzClassicBluManager.getInstance().writeShutdown(2, object : DeviceBleInfoCall {
                override fun getBleNotifyInfo(isSuccess: Boolean, msg: MPMessage.MPCodeMsg?) {
                    if (isSuccess) {
                        Log.d("", "设置关机时间成功>>>>${msg.toString()}")
                    } else {
                        Log.d("", "设置关机时间失败>>>>${msg.toString()}")
                    }
                }
            })
        }
        vm.tvSetPrintImg.setOnClickListener {
            Log.d(
                "", "FmBitmapPrinterUtils》》》bitmap字节数${
                    BitmapExt.bitmapToByteArray(
                        BitmapExt.decodeBitmap(R.drawable.test3)
                    ).size
                }"
            )
            Log.d("", "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT111")
            val bitmap = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test3), 128)
            val bitmap2 = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test8), 128)

            // val bitmap2 = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test7), 128)
            val page = vm.etPrintPage.text.toString().toInt()
            Log.d("", "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT${page}")
            SyzClassicBluManager.getInstance().writeBitmaps(mutableListOf(bitmap, bitmap2),
                48,
                48,
                page,
                SyzPrinter.SYZTWOINCH,
                object : BluPrinterInfoCall2 {
                    override fun getBluNotifyInfo(
                        isSuccess: Boolean, msg: SyzPrinterState2
                    ) {
                        if (isSuccess) {
                            Log.d("2寸图片打印》》》", "打印图片成功>>>>${msg.toString()}")
                        } else {
                            Log.e("2寸图片打印》》》", "打印图片失败>>>>${msg.toString()}")
                        }
                    }

                })
        }

        vm.tvSetPrintSpeed.setOnClickListener {
            SyzClassicBluManager.getInstance().writePrintSpeed(2, object : DeviceBleInfoCall {
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
            SyzClassicBluManager.getInstance()
                .writePrintConcentration(4, object : DeviceBleInfoCall {
                    override fun getBleNotifyInfo(isSuccess: Boolean, msg: MPMessage.MPCodeMsg?) {
                        if (isSuccess) {
                            Log.d("", "设置打印浓度成功>>>>${msg.toString()}")
                        } else {
                            Log.d("", "设置打印浓度失败>>>>${msg.toString()}")
                        }
                    }
                })
        }

        vm.tvCancelPrinter.setOnClickListener {
            SyzClassicBluManager.getInstance().writeCancelPrinter(object : CancelPrintCallBack {
                override fun cancelSuccess() {
                    Log.d("", "取消打印成功>>>>}")
                }

                override fun cancelFail() {
                    Log.d("", "取消打印失败>>>>}")
                }

            })
        }

        vm.tvDexZitiupdate.setOnClickListener {
            val path = SYZFileUtils.copyAssetGetFilePath("font(1).bin")
            path?.apply {
                SyzClassicBluManager.getInstance()
                    .writeDex(this, type = SyzFirmwareType.SYZFIRMWARETYPE02) {
                        if (it == SyzPrinterState.PRINTER_DEXUPDATE_SUCCESS) {
                            Log.d("", "字体固件更新成功")
                        } else if (it == SyzPrinterState.PRINTER_DEXUPDATE_FAILED) {
                            Log.d("", "字体固件更新失败")
                        } else {
                            Log.d(
                                "", "字体固件更新其他异常${it.toString()}==${it.code}===${it.info}"
                            )
                        }
                    }
            }
        }
    }

}