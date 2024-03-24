package com.issyzone.blelibs.dataimp

import com.issyzone.blelibs.fmBeans.MPMessage

interface DeviceInfoCall {
    fun getDeviceInfo(msg: MPMessage.MPDeviceInfoMsg)
    fun getDeviceInfoError(errorMsg: MPMessage.MPCodeMsg)
}

