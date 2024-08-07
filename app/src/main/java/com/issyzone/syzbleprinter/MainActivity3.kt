package com.issyzone.syzbleprinter


import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat


import com.issyzone.blelibs.permission.SYZBlePermission
import com.issyzone.blelibs.utils.BitmapExt
import com.issyzone.blelibs.utils.ImageUtilKt
import com.issyzone.blelibs.utils.SYZFileUtils
import com.issyzone.classicblulib.bean.LogLiveData
import com.issyzone.classicblulib.bean.MPMessage
import com.issyzone.classicblulib.bean.SyzFirmwareType
import com.issyzone.classicblulib.bean.SyzPaperSize
import com.issyzone.classicblulib.bean.SyzPrinter
import com.issyzone.classicblulib.callback.BluPrintingCallBack
import com.issyzone.classicblulib.callback.BluSelfCheckCallBack
import com.issyzone.classicblulib.callback.CancelPrintCallBack
import com.issyzone.classicblulib.callback.DeviceBleInfoCall
import com.issyzone.classicblulib.callback.DeviceInfoCall
import com.issyzone.classicblulib.callback.SyzBluCallBack
import com.issyzone.classicblulib.callback.SyzPrinterState
import com.issyzone.classicblulib.callback.SyzPrinterState2

import com.issyzone.classicblulib.service.SyzClassicBluManager
import com.issyzone.classicblulib.tools.SpUtils
import com.issyzone.classicblulib.utils.AppGlobels
import com.issyzone.syzbleprinter.databinding.ActivityMain3Binding
import com.issyzone.syzbleprinter.utils.invokeViewBinding


class MainActivity3 : ComponentActivity() {
    private val TAG = "MAIN4>>>"
    private val vm: ActivityMain3Binding by invokeViewBinding()

    override fun onDestroy() {
        super.onDestroy()
        SyzClassicBluManager.getInstance().onDestory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(vm.root)
        AppGlobels.getScreenResolution()
        AppGlobels.getDpi()
        AppGlobels.getPpi()
       // AppGlobels.getScreenInch()

        //val lo = "DC:0D:30:00:02:E2"
//        val lo = "DC:0D:30:00:02:E5"
        //val lo = "DC:0D:30:00:02:DE"
       // val lo = "DC:0D:30:00:02:DB"
        val lo = "DC:0D:30:98:95:E2"
        //val lo = "60:6E:41:A7:2D:D6"
        // val lo = "DC:0D:30:98:95:CF"//硬件那的mac
        // val lo = "DC:0D:30:00:02:DC"
        vm.tvType.text = "4寸demo"
        vm.etPicWidth.setText("102")
        vm.etPicHeight.setText("152")
        // TEst.test()
        val localmac = SpUtils.readData("mac4")
        if (localmac.isNullOrEmpty()) {
            vm.etMac.setText(lo)
        } else {
            vm.etMac.setText(lo)
        }

        LogLiveData.showLogs(this, vm.tvLog)
        vm.tvHeibiao.setOnClickListener {
            val size= SyzPaperSize.SYZPAPER_HEIBIAO
            size.height=vm.etHeibiaoHeight.text.toString().toFloat()
            size.offset=vm.etHeibiaoOffset.text.toString().toFloat()
            SyzClassicBluManager.getInstance().sendPaperSet(size,object :DeviceBleInfoCall{
                override fun getBleNotifyInfo(isSuccess: Boolean, msg: MPMessage.MPCodeMsg?) {

                }
            })
        }
        vm.tvJianxi.setOnClickListener {
            val size= SyzPaperSize.SYZPAPER_JIANXI
            size.height=vm.etHeibiaoHeight.text.toString().toFloat()
            size.offset=vm.etHeibiaoOffset.text.toString().toFloat()
            SyzClassicBluManager.getInstance().sendPaperSet(size,object :DeviceBleInfoCall{
                override fun getBleNotifyInfo(isSuccess: Boolean, msg: MPMessage.MPCodeMsg?) {

                }
            })
        }

        vm.tvLianxu.setOnClickListener {
            val size= SyzPaperSize.SYZPAPER_LIANXU
            size.height=vm.etHeibiaoHeight.text.toString().toFloat()
            size.offset=vm.etHeibiaoOffset.text.toString().toFloat()
            SyzClassicBluManager.getInstance().sendPaperSet(size,object :DeviceBleInfoCall{
                override fun getBleNotifyInfo(isSuccess: Boolean, msg: MPMessage.MPCodeMsg?) {

                }
            })
        }
        SyzClassicBluManager.getInstance().initClassicBlu()
        SyzClassicBluManager.getInstance().setActivelyReportBack {
            Log.i("${TAG}四寸主动上报的》》》》", it.toString())
        }
        SyzClassicBluManager.getInstance().setPaperReportCallBack {
            Log.i("${TAG}纸张尺寸上报的》》》》", "width==${it.paper_width}===height==${it.pager_height}==printerType==${it.printerState2.string}")
            //LogLiveData.addLogs("纸张尺寸上报的:>>>width==${it.paper_width}===height==${it.pager_height}==printerType==${it.printerState2.string}")
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
                // LogLiveData.clearLog(vm.tvLog)
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
            //LogLiveData.addLogs("准备连接4寸adress=${vm.etMac.text.toString()}")
            SyzClassicBluManager.getInstance().connect(vm.etMac.text.toString())
            // SyzClassicBluManager.getInstance().connect("DC:1D:30:78:F7:7A")

            // SyzClassicBluManager.getInstance().connect("03:22:55:BF:00:0F")
        }
        vm.tvDisconnect.setOnClickListener {
            SyzClassicBluManager.getInstance().disConnectBlu()
        }
        vm.tvCheck.setOnClickListener {
            LogLiveData.clearLog(vm.tvLog)
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
            LogLiveData.clearLog(vm.tvLog)
            val path = SYZFileUtils.copyAssetGetFilePath("rw402_pa_V01.01.17.bin")
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
            LogLiveData.clearLog(vm.tvLog)
            SyzClassicBluManager.getInstance().writeSelfCheck(object : BluSelfCheckCallBack {
                override fun getPrintResult(isSuccess: Boolean, msg: SyzPrinterState2) {

                }

            })
        }
        vm.tvShutdown.setOnClickListener {
            LogLiveData.clearLog(vm.tvLog)
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
        //打一张抖动
        vm.tvSetPrintImg.setOnClickListener {
            LogLiveData.clearLog(vm.tvLog)
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
           /* val bitmap5 = ImageUtilKt.convertBinary(
                ImageUtilKt.convertGreyImgByFloyd(BitmapExt.decodeBitmap(R.drawable.test20)), 128
            )*/

            val bitmap5 = BitmapExt.decodeBitmap(R.drawable.test21)

            val page = vm.etPrintPage.text.toString().toInt()
            val width = vm.etPicWidth.text.toString().toInt()
            val height = vm.etPicHeight.text.toString().toInt()
            Log.d("", "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT${page}==${width}===${height}")
            LogLiveData.clearLog(vm.tvLog)
            SyzClassicBluManager.getInstance().printBitmaps(mutableListOf(
                bitmap5
            ), width, height, page, object : BluPrintingCallBack {
                override fun printing(currentPrintPage: Int, totalPage: Int) {
                    Log.i("${TAG}>>>", "printing=====${currentPrintPage}=====${totalPage}")
                }

                override fun getPrintResult(isSuccess: Boolean, msg: SyzPrinterState2) {
                    if (isSuccess) {
                        Log.i("${TAG}>>>", "打印成功>>>>${isSuccess}===${msg}")
                    } else {
                        Log.i("${TAG}>>>", "打印失败>>>>${isSuccess}===${msg}")
                    }
                }

                override fun checkPaperSizeBeforePrint(
                    isSame: Boolean,
                    printerSize: SyzPaperSize?,
                    doPrintSize: SyzPaperSize?
                ) {

                }

                override fun checkPrinterBeforePrint(isOK: Boolean, msg: SyzPrinterState2) {

                }


            })
        }


        vm.tvSetPrintImg2.setOnClickListener {
            LogLiveData.clearLog(vm.tvLog)
            Log.d(
                "${TAG}", "FmBitmapPrinterUtils》》》bitmap字节数${
                    BitmapExt.bitmapToByteArray(
                        BitmapExt.decodeBitmap(com.issyzone.syzbleprinter.R.drawable.test3)
                    ).size
                }"
            )
            Log.d("", "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT111")
            val bitmap = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test6), 128)
            val bitmap2 = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test7), 128)


            val page = vm.etPrintPage.text.toString().toInt()
            val width = vm.etPicWidth.text.toString().toInt()
            val height = vm.etPicHeight.text.toString().toInt()
            LogLiveData.clearLog(vm.tvLog)
//            val data= mutableListOf<Bitmap>()
//            for (i in 1..100){
//                data.add(bitmap4)
//            }
            SyzClassicBluManager.getInstance().printBitmaps(mutableListOf(
                bitmap, bitmap2
            ), width, height, page, object : BluPrintingCallBack {
                override fun printing(currentPrintPage: Int, totalPage: Int) {
                    Log.i("${TAG}>>>", "printing=====${currentPrintPage}=====${totalPage}")
                }

                override fun getPrintResult(isSuccess: Boolean, msg: SyzPrinterState2) {
                    if (isSuccess) {
                        Log.i("${TAG}>>>", "打印成功>>>>${isSuccess}===${msg}")
                    } else {
                        Log.i("${TAG}>>>", "打印失败>>>>${isSuccess}===${msg}")
                    }
                }

                override fun checkPaperSizeBeforePrint(
                    isSame: Boolean,
                    printerSize: SyzPaperSize?,
                    doPrintSize: SyzPaperSize?
                ) {

                }

                override fun checkPrinterBeforePrint(isOK: Boolean, msg: SyzPrinterState2) {

                }


            })


        }

        vm.tvSetPrintImg3.setOnClickListener {
            LogLiveData.clearLog(vm.tvLog)
            Log.d(
                "${TAG}", "FmBitmapPrinterUtils》》》bitmap字节数${
                    BitmapExt.bitmapToByteArray(
                        BitmapExt.decodeBitmap(com.issyzone.syzbleprinter.R.drawable.test3)
                    ).size
                }"
            )
            Log.d("", "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT111")
       /*     val bitmap = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test6), 128)
            val bitmap2 = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test7), 128)
            val bitmap3 = ImageUtilKt.convertBinary(
                ImageUtilKt.convertGreyImgByFloyd(BitmapExt.decodeBitmap(R.drawable.test6)), 128
            )
            val bitmap4 = ImageUtilKt.convertBinary(
                ImageUtilKt.convertGreyImgByFloyd(BitmapExt.decodeBitmap(R.drawable.test7)), 128
            )
            val bitmap5 = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test4_1), 128)
            val bitmap6 = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test4_2), 128)
            val bitmap7 = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test4_3), 128)
            val bitmap8 = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test4_4), 128)
*/
             val bitmap9=ImageUtilKt.convertBinary(  BitmapExt.testBitmap(this@MainActivity3,R.drawable.test222)!!)




            LogLiveData.clearLog(vm.tvLog)

            val page = vm.etPrintPage.text.toString().toInt()
            val width = vm.etPicWidth.text.toString().toInt()
            val height = vm.etPicHeight.text.toString().toInt()

//            val data= mutableListOf<Bitmap>()
//            for (i in 1..100){
//                data.add(bitmap4)
//            }
            SyzClassicBluManager.getInstance().printBitmaps(mutableListOf(
                bitmap9
            ), width, height, page, object : BluPrintingCallBack {
                override fun printing(currentPrintPage: Int, totalPage: Int) {
                    Log.i("${TAG}>>>", "printing=====${currentPrintPage}=====${totalPage}")
                }

                override fun getPrintResult(isSuccess: Boolean, msg: SyzPrinterState2) {
                    if (isSuccess) {
                        Log.i("${TAG}>>>", "打印成功>>>>${isSuccess}===${msg}")
                    } else {
                        Log.i("${TAG}>>>", "打印失败>>>>${isSuccess}===${msg}")
                    }
                }

                override fun checkPaperSizeBeforePrint(
                    isSame: Boolean,
                    printerSize: SyzPaperSize?,
                    doPrintSize: SyzPaperSize?
                ) {

                }

                override fun checkPrinterBeforePrint(isOK: Boolean, msg: SyzPrinterState2) {

                }

            })
        }

        vm.tvSetPrintImg0.visibility = View.GONE
        vm.tvSetPrintImg0.setOnClickListener {
            /*Log.d(
                "${TAG}", "FmBitmapPrinterUtils》》》bitmap字节数${
                    BitmapExt.bitmapToByteArray(
                        BitmapExt.decodeBitmap(com.issyzone.syzbleprinter.R.drawable.test3)
                    ).size
                }"
            )
            Log.d("", "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT111")
            //val bitmap = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test3), 128)
            //val bitmap2 = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test8), 128)
            //val bitmap3 = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test11), 128)


            val bitmap4 = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(com.issyzone.syzbleprinter.R.drawable.test16), 128)
            val bitmap5 = OpenCVUtils.convertBinary(OpenCVUtils.testOpencv2(com.issyzone.syzbleprinter.R.drawable.test16), 128)
            val bitmap6 = OpenCVUtils.convertBinary(OpenCVUtils.testOpencv3(com.issyzone.syzbleprinter.R.drawable.test16), 128)


            // val bitmap4 = BitmapExt.decodeBitmap(R.drawable.test10)

//            val list= mutableListOf(bitmap3,bitmap3,bitmap3,bitmap3,bitmap3,bitmap3,bitmap3,bitmap3)
//            lifecycleScope.launch {
//                list.forEach {
//                    val bitmapPrintArray = BitmapUtils.print(it, it.width, it.height)
//                    val quicklzCompressTask = async { SyzClassicBluManager.getInstance().compress(bitmapPrintArray) }
//                    val bitmapCompress = quicklzCompressTask.await()
//                    Log.i(
//                        TAG,
//                        "压缩后bitmap大小==${bitmapCompress.size}=="
//                    )
//                }
//            }
            LogLiveData.clearLog(vm.tvLog)

            val page = vm.etPrintPage.text.toString().toInt()
            val width = vm.etPicWidth.text.toString().toInt()
            val height = vm.etPicHeight.text.toString().toInt()

//            val data= mutableListOf<Bitmap>()
//            for (i in 1..100){
//                data.add(bitmap4)
//            }
            SyzClassicBluManager.getInstance().printBitmaps(mutableListOf(
                bitmap4, bitmap5, bitmap6
            ), width, height, 1, SyzPrinter.SYZTWOINCH, object : BluPrintingCallBack {
                override fun printing(currentPrintPage: Int, totalPage: Int) {
                    Log.i("${TAG}>>>", "printing=====${currentPrintPage}=====${totalPage}")
                }

                override fun getPrintResult(isSuccess: Boolean, msg: SyzPrinterState2) {
                    if (isSuccess) {
                        Log.i("${TAG}>>>", "打印成功>>>>${isSuccess}===${msg}")
                    } else {
                        Log.i("${TAG}>>>", "打印失败>>>>${isSuccess}===${msg}")
                    }
                }
            })*/
        }
        vm.tvSetPrintImg4.setOnClickListener {
            LogLiveData.clearLog(vm.tvLog)
            val width = vm.etPicWidth.text.toString().toInt()
            val height = vm.etPicHeight.text.toString().toInt()
            val bitmap = ImageUtilKt.convertBinary(
                BitmapExt.decodeBitmap(com.issyzone.syzbleprinter.R.drawable.test3), 128
            )
            val bitmap2 = ImageUtilKt.convertBinary(
                BitmapExt.decodeBitmap(com.issyzone.syzbleprinter.R.drawable.test8), 128
            )
            val bitmap3 = ImageUtilKt.convertBinary(
                BitmapExt.decodeBitmap(com.issyzone.syzbleprinter.R.drawable.test11), 128
            )
            val bitmap4 = ImageUtilKt.convertBinary(
                BitmapExt.decodeBitmap(com.issyzone.syzbleprinter.R.drawable.test13), 128
            )
            val page = vm.etPrintPage.text.toString().toInt()

            SyzClassicBluManager.getInstance().printBitmaps(mutableListOf(
                bitmap4,
//                bitmap2,
//                bitmap3,
//                bitmap4,
//                bitmap5,
//                bitmap,
//                bitmap2,
//                bitmap3,
//                bitmap4,
//                bitmap5
            ), width, height, page, object : BluPrintingCallBack {
                override fun printing(currentPrintPage: Int, totalPage: Int) {
                    Log.i("${TAG}>>>", "printing=====${currentPrintPage}=====${totalPage}")
                }

                override fun getPrintResult(isSuccess: Boolean, msg: SyzPrinterState2) {
                    if (isSuccess) {
                        Log.i("${TAG}>>>", "打印成功>>>>${isSuccess}===${msg}")
                    } else {
                        Log.i("${TAG}>>>", "打印失败>>>>${isSuccess}===${msg}")
                    }
                }

                override fun checkPaperSizeBeforePrint(
                    isSame: Boolean,
                    printerSize: SyzPaperSize?,
                    doPrintSize: SyzPaperSize?
                ) {

                }

                override fun checkPrinterBeforePrint(isOK: Boolean, msg: SyzPrinterState2) {

                }
            })
        }





        vm.tvSetPrintSpeed.setOnClickListener {
            LogLiveData.clearLog(vm.tvLog)
            SyzClassicBluManager.getInstance().writePrintSpeed(
                vm.etPrintSpeed.text.toString().toInt(),
                object : DeviceBleInfoCall {
                    override fun getBleNotifyInfo(
                        isSuccess: Boolean, msg: MPMessage.MPCodeMsg?
                    ) {
                        if (isSuccess) {
                            Log.d("", "设置打印速度成功>>>>${msg.toString()}")
                        } else {
                            Log.d("", "设置打印速度失败>>>>${msg.toString()}")
                        }
                    }
                })
        }

        vm.tvSetPrintConcentration.setOnClickListener {
            LogLiveData.clearLog(vm.tvLog)
            SyzClassicBluManager.getInstance()
                .writePrintConcentration(vm.etPrintConcentration.text.toString().toInt(),
                    object : DeviceBleInfoCall {
                        override fun getBleNotifyInfo(
                            isSuccess: Boolean, msg: MPMessage.MPCodeMsg?
                        ) {
                            if (isSuccess) {
                                Log.d("", "设置打印浓度成功>>>>${msg.toString()}")
                            } else {
                                Log.d("", "设置打印浓度失败>>>>${msg.toString()}")
                            }
                        }
                    })
        }

        vm.tvCancelPrinter.setOnClickListener {
            LogLiveData.clearLog(vm.tvLog)
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
            LogLiveData.clearLog(vm.tvLog)
            val path = SYZFileUtils.copyAssetGetFilePath("font.bin")
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