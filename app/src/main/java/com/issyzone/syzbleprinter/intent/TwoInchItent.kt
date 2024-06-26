package com.issyzone.syzbleprinter.intent


import android.bluetooth.BluetoothDevice
import com.issyzone.classicblulib.bean.MPMessage
import com.issyzone.classicblulib.bean.SyzPaperSize
import com.issyzone.classicblulib.callback.SyzPrinterState
import com.issyzone.classicblulib.callback.SyzPrinterState2
import com.issyzone.common_work.mvi.IUIEffect
import com.issyzone.common_work.mvi.IUiIntent
import com.issyzone.common_work.mvi.IUiState
import com.issyzone.common_work.mvi.LoadUiState

sealed class TwoInchItent : IUiIntent {
    data class fm226SetPaperSize(val printerSize: SyzPaperSize):TwoInchItent()
    object initSDk : TwoInchItent()

    object disconnect : TwoInchItent()
    data class connectDevice(val mac: String) : TwoInchItent()

    data class updateDex(val dexFilePath: String?) : TwoInchItent()

    object getDeviceInfo : TwoInchItent()

    data class closeDevice(val time: Int) : TwoInchItent()
    data class setPrintSpeed(val speed: Int) : TwoInchItent()

    data class setPrintConcentration(val desnity: Int) : TwoInchItent()
    data class printBitmap(val page:Int,val pageTYpe:SyzPaperSize,var offsetX:Int=0,var offsetY: Int=0) : TwoInchItent()

    object printSelf : TwoInchItent()
    object cancelPrint : TwoInchItent()
}

data class TwoInchUIState(val loadUiState: LoadUiState, val sppState: SPPrinterUIState) :
    IUiState {}

sealed class SPPrinterUIState {
    object INIT : SPPrinterUIState()

    object BLU_DISCONNECTED : SPPrinterUIState()
    data class setPaperSizeResult(val isSuccess: Boolean, val msg: MPMessage.MPCodeMsg?) : SPPrinterUIState()

    data class checkPaperSizeBeforePrint(val isSame: Boolean, val printerSize: SyzPaperSize?, val doPrintSize: SyzPaperSize?) : SPPrinterUIState()
    data class printing(val currentPrintPage: Int, val totalPage: Int) : SPPrinterUIState()

    data class printResult(val isSuccess: Boolean, val msg: SyzPrinterState2) : SPPrinterUIState()
    data class getDeviceInfo(val msg: MPMessage.MPDeviceInfoMsg) : SPPrinterUIState()
    data class printSelfResult(val isSuccess: Boolean, val msg: SyzPrinterState2) :
        SPPrinterUIState()

    data class closeTimeResult(val isSuccess: Boolean, val msg: MPMessage.MPCodeMsg?) :
        SPPrinterUIState()

    data class setPrintSpeedResult(val isSuccess: Boolean, val msg: MPMessage.MPCodeMsg?) :
        SPPrinterUIState()

    data class setDenistyResult(val isSuccess: Boolean, val msg: MPMessage.MPCodeMsg?) :
        SPPrinterUIState()

    data class setCancelPrintResult(val isSuccess: Boolean) : SPPrinterUIState()
    data class getDexUpdateResult(val syzPrinterState: SyzPrinterState) : SPPrinterUIState()
    data class connectResult(
        val isSuccess: Boolean, val successDevice: BluetoothDevice?, val failedMsg: String?
    ) : SPPrinterUIState()
}

sealed class TwoInchUIEffect : IUIEffect {}