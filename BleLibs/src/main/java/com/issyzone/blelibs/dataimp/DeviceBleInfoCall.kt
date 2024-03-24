package com.issyzone.blelibs.dataimp

import com.issyzone.blelibs.fmBeans.MPMessage

interface DeviceBleInfoCall {
    fun getBleNotifyInfo(isSuccess: Boolean,msg: MPMessage.MPCodeMsg?)

}