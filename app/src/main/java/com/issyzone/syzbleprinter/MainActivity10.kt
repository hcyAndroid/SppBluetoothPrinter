package com.issyzone.syzbleprinter


import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.graphics.Bitmap

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import com.issyzone.blelibs.utils.ImageUtilKt


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
import com.issyzone.classicblulib.utils.BitmapExt


import com.issyzone.classicblulib.utils.SYZFileUtils
import com.issyzone.syzbleprinter.databinding.ActivityMain9Binding
import com.issyzone.syzbleprinter.utils.OpenCVUtils

import com.issyzone.syzbleprinter.utils.invokeViewBinding



/**
 * 2寸的页面
 */
class MainActivity10 : ComponentActivity() {
    private val vm: ActivityMain9Binding by invokeViewBinding()
    private val TAG = "MAIN2>>>"


    override fun onDestroy() {
        super.onDestroy()
        SyzClassicBluManager.getInstance().onDestory()
    }

    private fun bitmapListFuns(originalList: MutableList<Bitmap>, page: Int): MutableList<Bitmap> {
        return originalList.map { bitmap ->
            (1..page).map { _ ->
                bitmap// 假设Bitmap有一个copy()方法来复制Bitmap对象
            }.toMutableList()
        }.flatten().toMutableList()
    }

    fun test11(){
        val deviceINfo=  MPMessage.MPDeviceInfoMsg
            .newBuilder()
            .setMac("15:6D:DC:1A:26:03")
            .setSn("15:6D:DC:1A:26:03")
            .setElec(0)
            .setSpeed(1)
            .setConcentration(4)
            .setFirmwareVer("01.01.15")
            .build()
        val  copyByteArray=deviceINfo.toByteArray()

        val data= MPMessage.MPDeviceInfoMsg.parseFrom(copyByteArray)
        Log.d(
            "$TAG", " NOTIFY返回respondData ${
                data.toString()
            }"
        )
    }

    fun setBluCallBack(){
        SyzClassicBluManager.getInstance().setBluCallBack(object : SyzBluCallBack {
            override fun onStartConnect() {
                Log.i("${TAG}寸>>>", "开始连接")
                // LogLiveData.addLogs("开始连接")
            }

            override fun onConnectFail(msg: String?) {
                Log.i("${TAG}>>>", "onConnectFail")
                LogLiveData.addLogs("经典蓝牙连接失败==${msg}")

            }

            override fun onConnectSuccess(device: BluetoothDevice) {
                if (ContextCompat.checkSelfPermission(this@MainActivity10, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
                    Log.i("${TAG}>>>", "onConnectSuccess==${device.name}====${device.address}")
                    LogLiveData.addLogs("经典蓝牙连接成功==${device.name}====${device.address}")
                    SpUtils.saveData("mac2", device.address)
                } else {
                    // Handle the lack of permission
                }
            }

            override fun onDisConnected() {
                Log.i("SYZ>>>", "onDisConnected")
                LogLiveData.addLogs("经典蓝牙已经断开")
                LogLiveData.clearLog(vm.tvLog)
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(vm.root)

       // val tag = OpenCVLoader.initLocal()

//        val sFscSppCentralApi = FscSppCentralApiImp.getInstance(MainActivity3@ this)
//        sFscSppCentralApi.initialize()

        //val lo = "03:22:5A:2F:EE:86"  //硬件的机器
         //val lo = "03:26:A0:AE:0B:57"// ios
        //val lo = "03:03:46:3D:A2:DE"//二寸新机器
        //val lo = "03:03:99:F3:2D:15"//二寸新机器
       // val lo="03:02:F3:D9:DA:4A"
        val lo="03:E1:A8:FF:82:FD"
       // 03:02::::
        // val lo = "03:25:70:6A:BF:45"
        // 03:25:70:6A:BF:45
        //val lo = "03:26:14:57:DF:7C"//android
        // val lo2 = "03:22:55:BF:00:0F"
        //val lo= "45:BF:6A:70:25:03"//qiang
        vm.tvType.text = "2寸demo"
        // TEst.test()
        val localmac = SpUtils.readData("mac2")
        if (localmac.isNullOrEmpty()) {
            vm.etMac.setText(lo)
        } else {
            vm.etMac.setText(lo)
        }
        SyzClassicBluManager.getInstance().setActivelyReportBack {
            Log.i("${TAG}主动上报的》》》》", it.toString())
        }
        SyzClassicBluManager.getInstance().setPaperReportCallBack {
            Log.i("${TAG}纸张尺寸上报的》》》》", "width==${it.paper_width}===height==${it.pager_height}")
        }
        LogLiveData.showLogs(this, vm.tvLog)

        SyzClassicBluManager.getInstance().initClassicBlu()
        setBluCallBack()
//        SYZBlePermission.checkBlePermission(this) {
//
//
//            //  SyzClassicBluManager.getInstance().init()
////            val filter = IntentFilter()
////            filter.addAction(BluetoothDevice.ACTION_FOUND)
////            filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
////            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
////            Log.e("TGA", "注册扫描广播")
////            registerReceiver(MY(list), filter)
//            // SyzClassicBluManager.getInstance().getBluetoothAdapter()?.startDiscovery()
//        }

        vm.tvHeibiao.setOnClickListener {
            val size=SyzPaperSize.SYZPAPER_HEIBIAO
            size.height=vm.etHeibiaoHeight.text.toString().toFloat()
            size.offset=vm.etHeibiaoOffset.text.toString().toFloat()
            SyzClassicBluManager.getInstance().sendPaperSet(size,object :DeviceBleInfoCall{
                override fun getBleNotifyInfo(isSuccess: Boolean, msg: MPMessage.MPCodeMsg?) {

                }
            })
        }
        vm.tvJianxi.setOnClickListener {
            val size=SyzPaperSize.SYZPAPER_JIANXI
            size.height=vm.etHeibiaoHeight.text.toString().toFloat()
            size.offset=vm.etHeibiaoOffset.text.toString().toFloat()
            SyzClassicBluManager.getInstance().sendPaperSet(size,object :DeviceBleInfoCall{
                override fun getBleNotifyInfo(isSuccess: Boolean, msg: MPMessage.MPCodeMsg?) {

                }
            })
        }

        vm.tvLianxu.setOnClickListener {
            val size=SyzPaperSize.SYZPAPER_LIANXU
            size.height=vm.etHeibiaoHeight.text.toString().toFloat()
            size.offset=vm.etHeibiaoOffset.text.toString().toFloat()
            SyzClassicBluManager.getInstance().sendPaperSet(size,object :DeviceBleInfoCall{
                override fun getBleNotifyInfo(isSuccess: Boolean, msg: MPMessage.MPCodeMsg?) {

                }
            })
        }
        vm.tvConnect.setOnClickListener {
            /*val obj = MPMessage.MPDeviceInfoMsg.newBuilder().setMac("57:0B:AE:A0:26:03")
                .setPrintStatus("4").setElec(0).setMac("dddd").setPrintStatus("0").build()
            val data = obj.toByteArray()
            val datas = Upacker.frameEncode(data)
            Log.i("点击", ">>>>${StringUtils.toHex(data)}")
            Upacker(object : MsgCallback {
                override fun onMsgPrased(data: ByteArray?, len: Int) {
                    Log.e(
                        TAG, "Upacker>>>>解包成功${StringUtils.toHex(data)}==${
                            MPMessage.MPDeviceInfoMsg.parseFrom(data)
                        }"
                    )

                }

                override fun onMsgFailed() {
                    Log.e(TAG, "Upacker>>>>解包失败")

                }

            }).unpack(datas)*/
            //LogLiveData.addLogs("准备连接2寸adress=${vm.etMac.text.toString()}")
            SyzClassicBluManager.getInstance().connect(vm.etMac.text.toString())
            // SyzClassicBluManager.getInstance().connect("DC:1D:30:78:F7:7A")

//            /
            // SyzClassicBluManager.getInstance().connect("03:22:55:BF:00:0F")
        }
        vm.tvDisconnect.setOnClickListener {
            SyzClassicBluManager.getInstance().disConnectBlu()
        }
        vm.tvCheck.setOnClickListener {
            Log.i("${TAG}当前是否连接》》》", "${SyzClassicBluManager.getInstance().isBluConnected()}")
            SyzClassicBluManager.getInstance().getDeviceInfo(object : DeviceInfoCall {
                override fun getDeviceInfo(msg: MPMessage.MPDeviceInfoMsg) {
                    Log.i("${TAG}获取设备信息>>", "${msg.toString()}")
                }

                override fun getDeviceInfoError(errorMsg: MPMessage.MPCodeMsg) {

                }
            })
        }
        vm.tvDexUpdate.setOnClickListener {

            val path = SYZFileUtils.copyAssetGetFilePath("FM226_print_app(69).bin")
            path?.apply {
                SyzClassicBluManager.getInstance().writeDex(this) {
                    if (it == SyzPrinterState.PRINTER_DEXUPDATE_SUCCESS) {
                        Log.d("${TAG}", "固件更新成功")
                    } else if (it == SyzPrinterState.PRINTER_DEXUPDATE_FAILED) {
                        Log.d("${TAG}", "固件更新失败")
                    } else {
                        Log.d("${TAG}", "固件更新其他异常${it.toString()}==${it.code}===${it.info}")
                        // SyzBleManager.getInstance().stopWriteDex()
                    }
                }
            }
        }
        vm.tvSelfChecking.setOnClickListener {
            SyzClassicBluManager.getInstance().writeSelfCheck(object :BluSelfCheckCallBack{
                override fun getPrintResult(isSuccess: Boolean, msg: SyzPrinterState2) {
                    Log.e("打印自检页的结果::","${isSuccess}==${msg}")
                }

            })
        }
        vm.tvShutdown.setOnClickListener {
            SyzClassicBluManager.getInstance().writeShutdown(2, object : DeviceBleInfoCall {
                override fun getBleNotifyInfo(isSuccess: Boolean, msg: MPMessage.MPCodeMsg?) {
                    if (isSuccess) {
                        Log.d("${TAG}", "设置关机时间成功>>>>${msg.toString()}")
                    } else {
                        Log.d("${TAG}", "设置关机时间失败>>>>${msg.toString()}")
                    }
                }
            })
        }
        //打印1张抖动
        vm.tvSetPrintImg.setOnClickListener {

            //val bitmap9=ImageUtilKt.convertBinary(com.issyzone.blelibs.utils.BitmapExt.testBitmap(this@MainActivity4,R.drawable.test222)!!)
            val bitmap4 = BitmapExt.decodeBitmap(R.drawable.zidong3)


            val page = vm.etPrintPage.text.toString().toInt()
            val width = vm.etPicWidth.text.toString().toInt()
            val height = vm.etPicHeight.text.toString().toInt()
            LogLiveData.clearLog(vm.tvLog)
            SyzClassicBluManager.getInstance().printBitmaps(mutableListOf(
                bitmap4
            ), width, height, page,  object : BluPrintingCallBack {
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

            Log.d(
                "${TAG}", "FmBitmapPrinterUtils》》》bitmap字节数${
                    BitmapExt.bitmapToByteArray(
                        BitmapExt.decodeBitmap(R.drawable.test3)
                    ).size
                }"
            )
            Log.d("", "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT111")
            val bitmap4 =BitmapExt.decodeBitmap(R.drawable.zidong4)
//
//            val bitmap2 = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test8), 128)
//            val bitmap3 = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test11), 128)
//            val bitmap4 = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test13), 128)
//
//
//            val bitmap0 = BitmapExt.decodeBitmap(R.drawable.test16)
//            val bitmap5 = ImageUtilKt.convertBinary(bitmap0, 128)
//            val bitmap6 = ImageUtilKt.convertBinary(
//                OpenCVUtils.testOpencv2(
//                    R.drawable.test16, bitmap0.width.toDouble(), bitmap.height.toDouble()
//                ), 128
//            )
//            val bitmap7 = ImageUtilKt.convertBinary(
//                OpenCVUtils.testOpencv3(
//                    R.drawable.test16, bitmap0.width.toDouble(), bitmap.height.toDouble()
//                ), 128
//            )


            val page = vm.etPrintPage.text.toString().toInt()
            val width = vm.etPicWidth.text.toString().toInt()
            val height = vm.etPicHeight.text.toString().toInt()
            LogLiveData.clearLog(vm.tvLog)
//            val data= mutableListOf<Bitmap>()
//            for (i in 1..100){
//                data.add(bitmap4)
//            }
            SyzClassicBluManager.getInstance().printBitmaps(mutableListOf(
                bitmap4
            ), width, height, 1,  object : BluPrintingCallBack {
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
            Log.d(
                "${TAG}", "FmBitmapPrinterUtils》》》bitmap字节数${
                    BitmapExt.bitmapToByteArray(
                        BitmapExt.decodeBitmap(R.drawable.test3)
                    ).size
                }"
            )
            Log.d("", "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT111")
            val bitmap = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test3), 128)
            val bitmap2 = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test8), 128)
            val bitmap3 = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test11), 128)
            val bitmap4 = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test13), 128)
            val bitmap6 = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test14), 128)

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
                bitmap,
                bitmap2,
                bitmap3,
                bitmap4,
               // bitmap5,
                bitmap6,
                bitmap2,
                bitmap3,
                bitmap4,
                //bitmap5,
                bitmap6
            ), width, height, 1,  object : BluPrintingCallBack {
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
        //打印PDF
        vm.tvSetPrintImg0.setOnClickListener {
            Log.d(
                "${TAG}", "FmBitmapPrinterUtils》》》bitmap字节数${
                    BitmapExt.bitmapToByteArray(
                        BitmapExt.decodeBitmap(R.drawable.test3)
                    ).size
                }"
            )
            Log.d("", "TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT111")
            //val bitmap = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test3), 128)
            //val bitmap2 = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test8), 128)
            //val bitmap3 = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test11), 128)


//            val bitmap4 = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test16), 128)
//            val bitmap5 = OpenCVUtils.convertBinary(OpenCVUtils.testOpencv2(R.drawable.test16), 128)
//            val bitmap6 = OpenCVUtils.convertBinary(OpenCVUtils.testOpencv3(R.drawable.test16), 128)
            val bitmap = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test3), 128)
            val bitmap2 = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test8), 128)
            val bitmap3 = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test11), 128)
            val bitmap4 = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test13), 128)
            val bitmap5 = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test13), 128)


            LogLiveData.clearLog(vm.tvLog)

            val page = vm.etPrintPage.text.toString().toInt()
            val width = vm.etPicWidth.text.toString().toInt()
            val height = vm.etPicHeight.text.toString().toInt()
            SyzClassicBluManager.getInstance().printBitmaps(mutableListOf(
                bitmap,bitmap2,bitmap3,bitmap4,bitmap5
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
        vm.tvSetPrintImg4.setOnClickListener {
            val width = vm.etPicWidth.text.toString().toInt()
            val height = vm.etPicHeight.text.toString().toInt()
            val bitmap = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test3), 128)
            val bitmap2 = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test8), 128)
            val bitmap3 = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test11), 128)
            val bitmap4 = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test13), 128)
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
            ), width, height, page,  object : BluPrintingCallBack {
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

            SyzClassicBluManager.getInstance().writePrintSpeed(
                vm.etPrintPage.text.toString().toInt(),
                object : DeviceBleInfoCall {
                    override fun getBleNotifyInfo(
                        isSuccess: Boolean, msg: MPMessage.MPCodeMsg?
                    ) {
                        if (isSuccess) {
                            Log.d("${TAG}", "设置打印速度成功>>>>${msg.toString()}")
                        } else {
                            Log.d("${TAG}", "设置打印速度失败>>>>${msg.toString()}")
                        }
                    }
                })
        }

        vm.tvSetPrintConcentration.setOnClickListener {
            SyzClassicBluManager.getInstance()
                .writePrintConcentration(1,object : DeviceBleInfoCall {
                    override fun getBleNotifyInfo(isSuccess: Boolean, msg: MPMessage.MPCodeMsg?) {
                        if (isSuccess) {
                            Log.d("${TAG}", "设置打印浓度成功>>>>${msg.toString()}")
                        } else {
                            Log.d("${TAG}", "设置打印浓度失败>>>>${msg.toString()}")
                        }
                    }
                })
        }

        vm.tvCancelPrinter.setOnClickListener {
            SyzClassicBluManager.getInstance()
                .writeCancelPrinter( object : CancelPrintCallBack {
                    override fun cancelSuccess() {
                        Log.d("${TAG}", "取消打印成功>>>>}")
                    }

                    override fun cancelFail() {
                        Log.d("${TAG}", "取消打印失败>>>>}")
                    }

                })
        }

        vm.tvDexZitiupdate.setOnClickListener {
            val path = SYZFileUtils.copyAssetGetFilePath("font(1).bin")
            path?.apply {
                SyzClassicBluManager.getInstance()
                    .writeDex(this, type = SyzFirmwareType.SYZFIRMWARETYPE02) {
                        if (it == SyzPrinterState.PRINTER_DEXUPDATE_SUCCESS) {
                            Log.d("${TAG}", "字体固件更新成功")
                        } else if (it == SyzPrinterState.PRINTER_DEXUPDATE_FAILED) {
                            Log.d("${TAG}", "字体固件更新失败")
                        } else {
                            Log.d(
                                "${TAG}",
                                "字体固件更新其他异常${it.toString()}==${it.code}===${it.info}"
                            )
                        }
                    }
            }
        }
    }


}