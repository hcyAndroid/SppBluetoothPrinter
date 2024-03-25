package com.issyzone.blelibs.dataimp

import SyzPrinterState
import com.issyzone.blelibs.fmBeans.MPMessage

interface DeviceBleInfoCall {
    fun getBleNotifyInfo(isSuccess: Boolean,msg: MPMessage.MPCodeMsg?)

}


interface BlePrinterInfoCall {
    fun getBleNotifyInfo(isSuccess: Boolean,msg: SyzPrinterState)

}


