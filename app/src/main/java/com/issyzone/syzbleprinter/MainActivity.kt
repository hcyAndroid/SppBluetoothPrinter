package com.issyzone.syzbleprinter

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.issyzone.blelibs.BleManager
import com.issyzone.blelibs.callback.SyzBleCallBack
import com.issyzone.blelibs.data.BleDevice
import com.issyzone.blelibs.dataimp.DeviceBleInfoCall
import com.issyzone.blelibs.dataimp.DeviceInfoCall
import com.issyzone.blelibs.fmBeans.MPMessage
import com.issyzone.blelibs.service.SyzBleManager
import com.issyzone.blelibs.utils.BitmapExt
import com.issyzone.blelibs.utils.ImageUtilKt
import com.issyzone.blelibs.utils.SYZFileUtils
import com.issyzone.syzbleprinter.databinding.ActivityMainBinding
import com.issyzone.syzbleprinter.utils.invokeViewBinding
import com.orhanobut.logger.Logger

class MainActivity : ComponentActivity() {
    private val vm: ActivityMainBinding by invokeViewBinding()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(vm.root)
        initRecyclerView()
        SyzBleManager.getInstance().initBle()
    }

    val pair = Pair("RW402B", "DD:0D:30:00:02:E5")
    val pair1 = Pair("FM226", "03:25:65:DF:0B:0C")
    private fun initRecyclerView() {
        SyzBleManager.getInstance().setBleCallBack(object : SyzBleCallBack {
            override fun onStartConnect() {
                Logger.d("当前线程名onStartConnect：${Thread.currentThread().name}")
            }

            override fun onConnectFail(bleDevice: BleDevice?, msg: String) {
                Logger.d("当前线程名onConnectFail：${Thread.currentThread().name}")
            }

            override fun onConnectFailNeedUserRestart(bleDevice: BleDevice?, msg: String) {
                Logger.d("当前线程名onConnectFailNeedUserRestart：${Thread.currentThread().name}")
            }

            override fun onConnectSuccess(bleDevice: BleDevice?) {
                Logger.d("当前线程名onConnectSuccess：${Thread.currentThread().name}")
            }

            override fun onDisConnected(device: BleDevice?) {
                Logger.d("当前线程名onDisConnected：${Thread.currentThread().name}")
            }

        })
        SyzBleManager.getInstance().setActivelyReportBack {
            Logger.d("主动上报>>>>${it.toString()}")
        }
        vm.tvConnect.setOnClickListener {
            //RW402B,DD:0D:30:00:02:E5
            //03:25:65:DF:0B:0C  FM226

            SyzBleManager.getInstance().connectBle(pair.first, pair.second)
        }

        vm.tvDisconnect.setOnClickListener {
            if (SyzBleManager.getInstance().isBleConnected()){
                Logger.d("判断蓝牙是否已经连接>>>>${SyzBleManager.getInstance().isBleConnected()}")
                SyzBleManager.getInstance().disconnectBle()
            }else{
                Logger.d("判断蓝牙没有连接")
                SyzBleManager.getInstance().connectBle(pair.first, pair.second)
            }

        }
        vm.tvCheck.setOnClickListener {
            SyzBleManager.getInstance().getDeviceInfo(object : DeviceInfoCall {
                override fun getDeviceInfo(msg: MPMessage.MPDeviceInfoMsg) {
                    Logger.d("获取设备信息${msg.toString()}")
                }

                override fun getDeviceInfoError(errorMsg: MPMessage.MPCodeMsg) {

                }
            })
        }
        vm.tvSelfChecking.setOnClickListener {
            SyzBleManager.getInstance().writeSelfCheck(object : DeviceBleInfoCall {
                override fun getBleNotifyInfo(isSuccess: Boolean, msg: MPMessage.MPCodeMsg?) {
                    if (isSuccess) {
                        Logger.d("打印自检页成功>>>>${msg.toString()}")
                    } else {
                        Logger.d("打印自检页失败>>>>${msg.toString()}")
                    }
                }
            })
        }
        vm.tvShutdown.setOnClickListener {
            SyzBleManager.getInstance().writeShutdown(2, object : DeviceBleInfoCall {
                override fun getBleNotifyInfo(isSuccess: Boolean, msg: MPMessage.MPCodeMsg?) {
                    if (isSuccess) {
                        Logger.d("设置关机时间成功>>>>${msg.toString()}")
                    } else {
                        Logger.d("设置关机时间失败>>>>${msg.toString()}")
                    }
                }
            })
        }
        vm.tvCancelPrinter.setOnClickListener {
            SyzBleManager.getInstance().writeCancelPrinter(object : DeviceBleInfoCall {
                override fun getBleNotifyInfo(isSuccess: Boolean, msg: MPMessage.MPCodeMsg?) {
                    if (isSuccess) {
                        Logger.d("取消打印成功>>>>${msg.toString()}")
                    } else {
                        Logger.d("取消打印失败>>>>${msg.toString()}")
                    }
                }
            })
        }
        vm.tvSetPrintSpeed.setOnClickListener {
            SyzBleManager.getInstance().writePrintSpeed(2, object : DeviceBleInfoCall {
                override fun getBleNotifyInfo(isSuccess: Boolean, msg: MPMessage.MPCodeMsg?) {
                    if (isSuccess) {
                        Logger.d("设置打印速度成功>>>>${msg.toString()}")
                    } else {
                        Logger.d("设置打印速度失败>>>>${msg.toString()}")
                    }
                }
            })
        }
        vm.tvSetPrintConcentration.setOnClickListener {
            SyzBleManager.getInstance().writePrintConcentration(4, object : DeviceBleInfoCall {
                override fun getBleNotifyInfo(isSuccess: Boolean, msg: MPMessage.MPCodeMsg?) {
                    if (isSuccess) {
                        Logger.d("设置打印浓度成功>>>>${msg.toString()}")
                    } else {
                        Logger.d("设置打印浓度失败>>>>${msg.toString()}")
                    }
                }
            })
        }
        vm.tvSetPrintImg.setOnClickListener {
            Logger.d(
                "FmBitmapPrinterUtils》》》bitmap字节数${
                    BitmapExt.bitmapToByteArray(
                        BitmapExt.decodeBitmap()
                    ).size
                }"
            )
            Logger.d("TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT111")
            val bitmap = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(), 128)
            Logger.d("TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT22222")
//            SyzBleManager.getInstance().writeBitmap(
//                bitmap,  1
//            )

            SyzBleManager.getInstance()
                .writeBitmaps(mutableListOf(bitmap), 1, object : DeviceBleInfoCall {
                    override fun getBleNotifyInfo(
                        isSuccess: Boolean, msg: MPMessage.MPCodeMsg?
                    ) {
                        if (isSuccess) {
                            Logger.d("全部图片下载成功")
                        } else {
                            Logger.e("全部图片下载失败>>>${msg?.toString()}")
                        }
                    }
                })
        }
        vm.tvDexUpdate.setOnClickListener {
            val path = SYZFileUtils.copyAssetGetFilePath("FM226_print_app(1.1.0.0.8).bin")
            path?.apply {
                SyzBleManager.getInstance().writeDex(this, object : DeviceBleInfoCall {
                    override fun getBleNotifyInfo(
                        isSuccess: Boolean, msg: MPMessage.MPCodeMsg?
                    ) {
                        if (isSuccess) {
                            Logger.d("固件更新成功")
                        }
                    }

                })
            }
        }


    }

}