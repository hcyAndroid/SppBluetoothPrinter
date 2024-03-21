package com.issyzone.blelibs.fmBeans

import com.orhanobut.logger.Logger


object FMPrinterResponse {
    private const val TAG = "FMPrinterResponse::>>>"
    fun responseForFmDevicesInfo(byteArray: ByteArray) {
        Logger.d("${TAG}收到蓝牙的消息了")
        val mpRespondMsg = MPMessage.MPRespondMsg.parseFrom(byteArray)
        Logger.json(mpRespondMsg.toString())
        val deviceInfo = MPMessage.MPDeviceInfoMsg.parseFrom(mpRespondMsg.respondData.toByteArray())
        Logger.json(deviceInfo.toString())
    }
}