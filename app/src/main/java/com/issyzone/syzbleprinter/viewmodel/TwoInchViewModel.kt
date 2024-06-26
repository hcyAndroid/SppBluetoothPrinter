package com.issyzone.syzbleprinter.viewmodel

import android.bluetooth.BluetoothDevice
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.issyzone.blelibs.utils.ImageUtilKt
import com.issyzone.classicblulib.bean.MPMessage
import com.issyzone.classicblulib.bean.SyzFirmwareType
import com.issyzone.classicblulib.bean.SyzPaperSize
import com.issyzone.classicblulib.callback.BluPrintingCallBack
import com.issyzone.classicblulib.callback.BluSelfCheckCallBack
import com.issyzone.classicblulib.callback.CancelPrintCallBack
import com.issyzone.classicblulib.callback.DeviceBleInfoCall
import com.issyzone.classicblulib.callback.DeviceInfoCall
import com.issyzone.classicblulib.callback.SyzBluCallBack
import com.issyzone.classicblulib.callback.SyzPrinterState
import com.issyzone.classicblulib.callback.SyzPrinterState2
import com.issyzone.classicblulib.service.SyzClassicBluManager
import com.issyzone.classicblulib.utils.BitmapExt
import com.issyzone.common_work.mvi.BaseMviViewModel
import com.issyzone.common_work.mvi.IUiIntent
import com.issyzone.common_work.mvi.LoadUiState
import com.issyzone.syzbleprinter.R
import com.issyzone.syzbleprinter.intent.SPPrinterUIState
import com.issyzone.syzbleprinter.intent.TwoInchItent
import com.issyzone.syzbleprinter.intent.TwoInchUIEffect
import com.issyzone.syzbleprinter.intent.TwoInchUIState
import kotlinx.coroutines.launch


class TwoInchViewModel : BaseMviViewModel<TwoInchItent, TwoInchUIState, TwoInchUIEffect>() {

    private val TAG = "TwoInchViewModel"
    override fun initUiState(): TwoInchUIState {
        return TwoInchUIState(LoadUiState.Idle, SPPrinterUIState.INIT)
    }

    private suspend fun  bitmapFactory():MutableList<Bitmap>{
        val bitmap = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test3), 128)
        val bitmap2 = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test8), 128)
        val bitmap3 = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test11), 128)
        val bitmap4 = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test13), 128)
        return mutableListOf(bitmap,bitmap2,bitmap3,bitmap4)
    }

    override fun handleIntent(intent: IUiIntent) {
        when (intent) {
            is TwoInchItent.printBitmap -> {
                Log.i("TwoInchViewModel", "打印图片")
                viewModelScope.launch {
                   val bitmapList= bitmapFactory()
                    printBitmaps(bitmapList,intent.page,intent.pageTYpe)
                }
            }
            is TwoInchItent.fm226SetPaperSize -> {
                Log.i("TwoInchViewModel", "设置纸张大小${intent.printerSize}")
               setPaperSize(intent.printerSize)
            }
            is TwoInchItent.cancelPrint -> {
                Log.i("TwoInchViewModel", "取消打印")
                cancelPrint2()
            }

            is TwoInchItent.disconnect -> {
                Log.i("TwoInchViewModel", "断开连接")
                SyzClassicBluManager.getInstance().disConnectBlu()
            }

            is TwoInchItent.connectDevice -> {
                Log.i("TwoInchViewModel", "连接设备")
                connect(intent.mac)
            }

            is TwoInchItent.initSDk -> {
                Log.i("TwoInchViewModel", "初始化SDK")
                initSDK()
            }

            is TwoInchItent.getDeviceInfo -> {
                Log.i("TwoInchViewModel", "获取设备信息")
                getDeviceInfo()
            }

            is TwoInchItent.updateDex -> {
                Log.i("TwoInchViewModel", "更新Dex")
                updateDex(intent.dexFilePath)
            }

            is TwoInchItent.closeDevice -> {
                Log.i("TwoInchViewModel", "关闭设备==${intent.time}")
                closeDevice(intent.time)
            }

            is TwoInchItent.printSelf -> {
                Log.i("TwoInchViewModel", "打印自检页")
                printSelf()

            }

            is TwoInchItent.setPrintSpeed -> {
                Log.i("TwoInchViewModel", "设置打印速度==${intent.speed}")
                setPrintSpeed2(intent.speed)
            }

            is TwoInchItent.setPrintConcentration -> {
                Log.i("TwoInchViewModel", "设置打印浓度==${intent.desnity}")
                setDenisty(intent.desnity)
            }
        }
    }

    /**
     * 设置纸张大小
     */
    private  fun setPaperSize(printerSize: SyzPaperSize) {
        SyzClassicBluManager.getInstance().sendPaperSet(printerSize,object:DeviceBleInfoCall{
            override fun getBleNotifyInfo(isSuccess: Boolean, msg: MPMessage.MPCodeMsg?) {
                updateUiState {
                    copy(sppState = SPPrinterUIState.setPaperSizeResult(isSuccess,msg))
                }
            }
        })
    }

    private fun printBitmaps(bitmapList: MutableList<Bitmap>,page:Int,currentPaperSize: SyzPaperSize?){
        SyzClassicBluManager.getInstance().printBitmaps(bitmapList,50*8,50*8,page,object:BluPrintingCallBack{
            override fun printing(currentPrintPage: Int, totalPage: Int) {
                updateUiState {
                    copy(sppState = SPPrinterUIState.printing(currentPrintPage,totalPage))
                }
            }

            override fun getPrintResult(isSuccess: Boolean, msg: SyzPrinterState2) {
               updateUiState {
                     copy(sppState = SPPrinterUIState.printResult(isSuccess,msg))
               }
            }

            override fun checkPaperSizeBeforePrint(
                isSame: Boolean,
                printerSize: SyzPaperSize?,
                doPrintSize: SyzPaperSize?
            ) {
                updateUiState {
                    copy(sppState = SPPrinterUIState.checkPaperSizeBeforePrint(isSame,printerSize,doPrintSize))
                }

            }
        },currentPaperSize)
    }

    private fun cancelPrint2() {
        SyzClassicBluManager.getInstance().writeCancelPrinter(object : CancelPrintCallBack {
            override fun cancelSuccess() {
                updateUiState {
                    copy(sppState = SPPrinterUIState.setCancelPrintResult(true))
                }
            }

            override fun cancelFail() {
                updateUiState {
                    copy(sppState = SPPrinterUIState.setCancelPrintResult(false))
                }
            }
        })

    }

    private fun setDenisty(desnity: Int) {
        SyzClassicBluManager.getInstance()
            .writePrintConcentration(desnity, object : DeviceBleInfoCall {
                override fun getBleNotifyInfo(isSuccess: Boolean, msg: MPMessage.MPCodeMsg?) {
                    updateUiState {
                        copy(sppState = SPPrinterUIState.setDenistyResult(isSuccess, msg))
                    }
                }
            })
    }

    private fun setPrintSpeed2(speed: Int) {
        SyzClassicBluManager.getInstance().writePrintSpeed(speed, object : DeviceBleInfoCall {
            override fun getBleNotifyInfo(isSuccess: Boolean, msg: MPMessage.MPCodeMsg?) {
                updateUiState {
                    copy(sppState = SPPrinterUIState.setPrintSpeedResult(isSuccess, msg))
                }
            }
        })
    }


    //打印自检页
    private fun printSelf() {
        SyzClassicBluManager.getInstance().writeSelfCheck(object : BluSelfCheckCallBack {
            override fun getPrintResult(isSuccess: Boolean, msg: SyzPrinterState2) {
                updateUiState {
                    copy(sppState = SPPrinterUIState.printSelfResult(isSuccess, msg))
                }
            }
        })
    }

    private fun closeDevice(time: Int) {
        SyzClassicBluManager.getInstance().writeShutdown(time, object : DeviceBleInfoCall {
            override fun getBleNotifyInfo(isSuccess: Boolean, msg: MPMessage.MPCodeMsg?) {
                updateUiState {
                    copy(sppState = SPPrinterUIState.closeTimeResult(isSuccess, msg))
                }
            }
        })
    }

    private fun updateDex(dexFilePath: String?) {
        dexFilePath?.apply {
            SyzClassicBluManager.getInstance().writeDex(this, SyzFirmwareType.SYZFIRMWARETYPE01) {
                if (it == SyzPrinterState.PRINTER_DEXUPDATE_SUCCESS) {
                    Log.d("${TAG}", "固件更新成功")
                } else if (it == SyzPrinterState.PRINTER_DEXUPDATE_FAILED) {
                    Log.d("${TAG}", "固件更新失败")
                } else {
                    Log.d("${TAG}", "固件更新其他异常${it.toString()}==${it.code}===${it.info}")
                }
                updateUiState {
                    copy(sppState = SPPrinterUIState.getDexUpdateResult(it))
                }
            }
        }

    }

    private fun getDeviceInfo() {
        SyzClassicBluManager.getInstance().getDeviceInfo(object : DeviceInfoCall {
            override fun getDeviceInfo(msg: MPMessage.MPDeviceInfoMsg) {
                updateUiState {
                    copy(sppState = SPPrinterUIState.getDeviceInfo(msg))
                }
            }

            override fun getDeviceInfoError(errorMsg: MPMessage.MPCodeMsg) {

            }
        })
    }

    private fun connect(mac: String) {
        SyzClassicBluManager.getInstance().connect(mac)
    }

    private fun initSDK() {
        SyzClassicBluManager.getInstance().initClassicBlu()
        SyzClassicBluManager.getInstance().setBluCallBack(object : SyzBluCallBack {
            override fun onStartConnect() {
                Log.i(TAG, "开始连接")
                updateUiState {
                    copy(loadUiState = LoadUiState.Loading(true))
                }
            }

            override fun onConnectFail(msg: String?) {
                Log.i(TAG, "连接失败")
                updateUiState {
                    copy(sppState = SPPrinterUIState.connectResult(false, null, msg))
                }
            }

            override fun onConnectSuccess(device: BluetoothDevice) {
                Log.i(TAG, "连接成功")
                updateUiState {
                    copy(sppState = SPPrinterUIState.connectResult(true, device, null))
                }
            }

            override fun onDisConnected() {
                Log.i(TAG, "断开连接")
                updateUiState {
                    copy(sppState = SPPrinterUIState.BLU_DISCONNECTED)
                }
            }

        })
    }


}