package com.issyzone.blelibs.fmBeans

import com.orhanobut.logger.Logger

object FmNotifyBeanUtils {
    private val TAG = "FmNotifyBeanUtils>>>"

    fun getDeviceInfo(byteArray: ByteArray): NotifyResult {
        val mpRespondMsg = MPMessage.MPRespondMsg.parseFrom(byteArray)
        Logger.i("$TAG NOTIFY返回的信息 ${mpRespondMsg.toString()}")
        //打印设备信息
        return if (mpRespondMsg.code == 200) {
            val deviceInfo =
                MPMessage.MPDeviceInfoMsg.parseFrom(mpRespondMsg.respondData.toByteArray())
            Logger.d("$TAG 获取设备信息>>>>${deviceInfo.toString()}")
            return NotifyResult.Success(deviceInfo)
        } else {
            val deviceErrorInfo = MPMessage.MPCodeMsg.parseFrom(mpRespondMsg.error.toByteArray())
            Logger.e("设备问题>>>>${deviceErrorInfo.toString()}")
            NotifyResult.Error(deviceErrorInfo)
        }
    }

    fun getShutDownPrinterResult(byteArray: ByteArray): NotifyResult2 {
        val mpRespondMsg = MPMessage.MPRespondMsg.parseFrom(byteArray)
        Logger.i("$TAG NOTIFY返回的信息 ${mpRespondMsg.toString()}")
        return if (mpRespondMsg.eventType == MPMessage.EventType.CLOSETIME) {
            if (mpRespondMsg.code == 200) {
                val deviceInfo =
                    MPMessage.MPDeviceInfoMsg.parseFrom(mpRespondMsg.respondData.toByteArray())
                Logger.d("BLE设置关机成功>>>>${deviceInfo.toString()}")
                NotifyResult2.Success(null)
            } else {
                val deviceErrorInfo =
                    MPMessage.MPCodeMsg.parseFrom(mpRespondMsg.error.toByteArray())
                Logger.e("BLE设置关机失败>>>>${deviceErrorInfo.toString()}")
                NotifyResult2.Error(deviceErrorInfo)
            }
        } else {
            NotifyResult2.Error(null)
        }
    }

    fun getSetSpeedPrinterResult(byteArray: ByteArray): NotifyResult2 {
        val mpRespondMsg = MPMessage.MPRespondMsg.parseFrom(byteArray)
        Logger.i("$TAG NOTIFY返回的信息 ${mpRespondMsg.toString()}")
        return if (mpRespondMsg.eventType == MPMessage.EventType.PRINTINGSPEED) {
            if (mpRespondMsg.code == 200) {
                val deviceInfo =
                    MPMessage.MPDeviceInfoMsg.parseFrom(mpRespondMsg.respondData.toByteArray())
                Logger.d("BLE设置打印速度成功>>>>${deviceInfo.toString()}")
                NotifyResult2.Success(null)
            } else {
                val deviceErrorInfo =
                    MPMessage.MPCodeMsg.parseFrom(mpRespondMsg.error.toByteArray())
                Logger.e("BLE设置打印速度失败>>>>${deviceErrorInfo.toString()}")
                NotifyResult2.Error(deviceErrorInfo)
            }
        } else {
            NotifyResult2.Error(null)
        }
    }


    fun getSetConcentrationPrinterResult(byteArray: ByteArray): NotifyResult2 {
        val mpRespondMsg = MPMessage.MPRespondMsg.parseFrom(byteArray)
        Logger.i("$TAG NOTIFY返回的信息 ${mpRespondMsg.toString()}")
        return if (mpRespondMsg.eventType == MPMessage.EventType.PRINTINCONCENTRATION) {
            if (mpRespondMsg.code == 200) {
                val deviceInfo =
                    MPMessage.MPDeviceInfoMsg.parseFrom(mpRespondMsg.respondData.toByteArray())
                Logger.d("BLE设置打印浓度成功>>>>${deviceInfo.toString()}")
                NotifyResult2.Success(null)
            } else {
                val deviceErrorInfo =
                    MPMessage.MPCodeMsg.parseFrom(mpRespondMsg.error.toByteArray())
                Logger.e("BLE设置打印浓度失败>>>>${deviceErrorInfo.toString()}")
                NotifyResult2.Error(deviceErrorInfo)
            }
        } else {
            NotifyResult2.Error(null)
        }
    }


    fun getSelfCheckInfo(byteArray: ByteArray): NotifyResult2 {
        val mpRespondMsg = MPMessage.MPRespondMsg.parseFrom(byteArray)
        Logger.i("$TAG NOTIFY返回的信息 ${mpRespondMsg.toString()}")
        /**
         *
        10: 打印开盖关盖状态    info:   1: 合盖,  2:开盖
        11: 打印机纸张状态      info:   1: 上纸,  2:缺纸  3:卡纸
        12: 打印机打印头状态    info:   1: 恢复正常 2:过热
        13: 打印机电量状态      info:   电池电量,如果当前电量只剩下20%,那么传20
        300: 打印机打印状态     info:    1: 打印成功  2: 打印取消 3.打印失败
        400: 打印机升级状态     info:    1: 升级陈工,三秒后打印机重启 2.升级失败
         */
        return if (mpRespondMsg.code == 200) {
            val deviceInfo = MPMessage.MPCodeMsg.parseFrom(mpRespondMsg.respondData.toByteArray())
            Logger.d("打印自检页成功>>>>${deviceInfo.toString()}")
            NotifyResult2.Success(deviceInfo)
        } else {
            val deviceErrorInfo = MPMessage.MPCodeMsg.parseFrom(mpRespondMsg.error.toByteArray())
            Logger.e("打印自检页失败>>>>${deviceErrorInfo.toString()}")
            NotifyResult2.Error(deviceErrorInfo)
        }
    }


    fun getBitmapsPrint(byteArray: ByteArray): NotifyResult2 {
        val mpRespondMsg = MPMessage.MPRespondMsg.parseFrom(byteArray)
        Logger.i("$TAG NOTIFY返回的信息 ${mpRespondMsg.toString()}")
        /**
         *
         *
        4
        10: 打印开盖关盖状态    info:   1: 合盖,  2:开盖
        11: 打印机纸张状态      info:   1: 上纸,  2:缺纸  3:卡纸
        12: 打印机打印头状态    info:   1: 恢复正常 2:过热
        13: 打印机电量状态      info:   电池电量,如果当前电量只剩下20%,那么传20
        300: 打印机打印状态     info:    1: 打印成功  2: 打印取消 3.打印失败
        400: 打印机升级状态     info:    1: 升级陈工,三秒后打印机重启 2.升级失败
         */
        return if (mpRespondMsg.code == 200) {
            val deviceInfo = MPMessage.MPCodeMsg.parseFrom(mpRespondMsg.respondData.toByteArray())
            if (deviceInfo.code == 300 && deviceInfo.info == "1") {
                Logger.d("图片打印成功>>>>${deviceInfo.toString()}")
                PrintBimapUtils.getInstance().removePrintWhenSuccess()
                if (PrintBimapUtils.getInstance().isCompleteBitmapPrinter()) {
                    NotifyResult2.Success(deviceInfo)
                } else {
                    NotifyResult2.Success(null)
                }
            } else {
                NotifyResult2.Error(deviceInfo)
            }
        } else {
            /**
             *    code:1  info busy
             */
            if (mpRespondMsg.code == 4) {
                val deviceErrorInfo =
                    MPMessage.MPCodeMsg.parseFrom(mpRespondMsg.respondData.toByteArray())
                Logger.e("打印Bitmap问题>>>>${deviceErrorInfo.toString()}")
                NotifyResult2.Error(deviceErrorInfo)
            } else {
                val deviceErrorInfo =
                    MPMessage.MPCodeMsg.parseFrom(mpRespondMsg.error.toByteArray())
                Logger.e("设备问题>>>>${deviceErrorInfo.toString()}")
                NotifyResult2.Error(deviceErrorInfo)
            }

        }
    }


}