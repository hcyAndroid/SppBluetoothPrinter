package com.issyzone.blelibs.dataimp

import com.issyzone.blelibs.data.SyzPrinterState
import com.issyzone.blelibs.data.SyzPrinterState2
import com.issyzone.blelibs.fmBeans.MPMessage

interface DeviceBleInfoCall {
    fun getBleNotifyInfo(isSuccess: Boolean,msg: MPMessage.MPCodeMsg?)

}


interface BlePrinterInfoCall {
    fun getBleNotifyInfo(isSuccess: Boolean,msg: SyzPrinterState)

}

interface BlePrinterInfoCall2 {
    fun getBleNotifyInfo(isSuccess: Boolean,msg: SyzPrinterState2)

}


