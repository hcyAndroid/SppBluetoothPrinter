package com.issyzone.blelibs.fmBeans

sealed class NotifyResult {
    data class Success(val data:  MPMessage.MPDeviceInfoMsg) : NotifyResult()
    data class Error(val errorMsg: MPMessage.MPCodeMsg) : NotifyResult()
}


sealed class NotifyResult2 {
    data class Success(val msg: MPMessage.MPCodeMsg?) : NotifyResult2()
    data class Error(val errorMsg: MPMessage.MPCodeMsg?) : NotifyResult2()
}