package com.issyzone.syzbleprinter


import android.bluetooth.BluetoothDevice

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity


import com.issyzone.blelibs.R


import com.issyzone.blelibs.permission.SYZBlePermission
import com.issyzone.blelibs.utils.BitmapExt
import com.issyzone.blelibs.utils.ImageUtilKt
import com.issyzone.blelibs.utils.SYZFileUtils
import com.issyzone.classicblulib.bean.LogLiveData
import com.issyzone.classicblulib.bean.MPMessage
import com.issyzone.classicblulib.bean.SyzFirmwareType
import com.issyzone.classicblulib.bean.SyzPrinter
import com.issyzone.classicblulib.callback.BluPrinterInfoCall2
import com.issyzone.classicblulib.callback.BluPrintingCallBack
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


class MainActivity3 : ComponentActivity() {
    private val vm: ActivityMain3Binding by invokeViewBinding()

    override fun onDestroy() {
        super.onDestroy()
        SyzClassicBluManager.getInstance().onDestory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(vm.root)

        val lo1 = "DC:0D:30:00:02:E2"
        val lo2 = "DC:0D:30:00:02:E5"
        val deviceMac = "DC:0D:30:00:02:DB" //硬件那的mac
        vm.tvType.text = "4寸demo"
        vm.etPicWidth.setText("102")
        vm.etPicHeight.setText("152")
        // TEst.test()
        val localmac = SpUtils.readData("mac4")
        if (localmac.isNullOrEmpty()) {
            vm.etMac.setText(deviceMac)
        } else {
            vm.etMac.setText(localmac)
        }

        LogLiveData.showLogs(this, vm.tvLog)

        SyzClassicBluManager.getInstance().initClassicBlu()
        SyzClassicBluManager.getInstance().setActivelyReportBack {
            Log.i("2寸主动上报的》》》》", it.toString())
        }
        SyzClassicBluManager.getInstance().setBluCallBack(object : SyzBluCallBack {
            override fun onStartConnect() {
                Log.i("SYZ>>>", "开始连接")
                LogLiveData.addLogs("开始连接")
            }

            override fun onConnectFail(msg: String?) {
                Log.i("SYZ>>>", "onConnectFail")
                LogLiveData.addLogs("经典蓝牙连接失败==${msg}")

            }

            override fun onConnectSuccess(device: BluetoothDevice) {
                Log.i("SYZ>>>", "onConnectSuccess==${device.name}====${device.address}")
                LogLiveData.addLogs("经典蓝牙连接成功==${device.name}====${device.address}")
                SpUtils.saveData("mac4", device.address)
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
            LogLiveData.addLogs("准备连接4寸adress=${vm.etMac.text.toString()}")
            SyzClassicBluManager.getInstance().connect(vm.etMac.text.toString())
            // SyzClassicBluManager.getInstance().connect("DC:1D:30:78:F7:7A")

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
            val path = SYZFileUtils.copyAssetGetFilePath("rw402_pa_v1.0.0(12).bin")
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
            val bitmap = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test6), 128)
            val bitmap2 = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test7), 128)
            Log.d("", "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT22222")
            // val bitmap2 = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test7), 128)
            val page = vm.etPrintPage.text.toString().toInt()
            val width = vm.etPicWidth.text.toString().toInt()
            val height = vm.etPicHeight.text.toString().toInt()
            Log.d("", "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT${page}==${width}===${height}")
//            SyzClassicBluManager.getInstance().writeBitmaps(mutableListOf(bitmap,bitmap2),
//                width,
//                height,
//                page,
//                SyzPrinter.SYZFOURINCH,
//                object : BluPrinterInfoCall2 {
//                    override fun getBluNotifyInfo(
//                        isSuccess: Boolean, msg: SyzPrinterState2
//                    ) {
//                        if (isSuccess) {
//                            Log.d("", "打印图片成功>>>>${msg.toString()}")
//                        } else {
//                            Log.d("", "打印图片失败>>>>${msg.toString()}")
//                        }
//                    }
//
//                })

            SyzClassicBluManager.getInstance().writeBitmaps(mutableListOf(bitmap, bitmap2),
                    width,
                    height,
                    page,
                    SyzPrinter.SYZTWOINCH,
                false,
                    object : BluPrintingCallBack {
                        override fun printing(currentPrintPage: Int, totalPage: Int) {
                            Log.i("MAIN>>>", "printing=====${currentPrintPage}=====${totalPage}")
                        }

                        override fun getPrintResult(isSuccess: Boolean, msg: SyzPrinterState2) {
                            if (isSuccess) {
                                Log.i("MAIN>>>", "打印成功>>>>${isSuccess}===${msg}")
                            } else {
                                Log.i("MAIN>>>", "打印失败>>>>${isSuccess}===${msg}")
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
            SyzClassicBluManager.getInstance().writeCancelPrinter(SyzPrinter.SYZFOURINCH,object : CancelPrintCallBack {
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