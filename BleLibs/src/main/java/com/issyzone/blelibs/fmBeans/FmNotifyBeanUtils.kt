package com.issyzone.blelibs.fmBeans

import SyzPrinterState
import android.util.Log


object FmNotifyBeanUtils {
    private val TAG = "FmNotifyBeanUtils>>>"

    fun getDeviceInfo(byteArray: ByteArray): NotifyResult {
        val mpRespondMsg = MPMessage.MPRespondMsg.parseFrom(byteArray)
        Log.d("$TAG", " NOTIFY返回的信息 ${mpRespondMsg.toString()}")
        //打印设备信息
        return if (mpRespondMsg.code == 200) {
            val deviceInfo =
                MPMessage.MPDeviceInfoMsg.parseFrom(mpRespondMsg.respondData.toByteArray())
            Log.d("$TAG", "获取设备信息>>>>${deviceInfo.toString()}")
            return NotifyResult.Success(deviceInfo)
        } else {
            val deviceErrorInfo = MPMessage.MPCodeMsg.parseFrom(mpRespondMsg.error.toByteArray())
            Log.d("$TAG", "设备问题>>>>${deviceErrorInfo.toString()}")
            NotifyResult.Error(deviceErrorInfo)
        }
    }

    fun getShutDownPrinterResult(byteArray: ByteArray): NotifyResult2 {
        val mpRespondMsg = MPMessage.MPRespondMsg.parseFrom(byteArray)
        Log.d("$TAG", " NOTIFY返回的信息 ${mpRespondMsg.toString()}")
        return if (mpRespondMsg.eventType == MPMessage.EventType.CLOSETIME) {
            if (mpRespondMsg.code == 200) {
                val deviceInfo =
                    MPMessage.MPDeviceInfoMsg.parseFrom(mpRespondMsg.respondData.toByteArray())
                Log.d("$TAG", "BLE设置关机成功>>>>${deviceInfo.toString()}")
                NotifyResult2.Success(null)
            } else {
                val deviceErrorInfo =
                    MPMessage.MPCodeMsg.parseFrom(mpRespondMsg.error.toByteArray())
                Log.d("$TAG", "BLE设置关机失败>>>>${deviceErrorInfo.toString()}")
                NotifyResult2.Error(deviceErrorInfo)
            }
        } else {
            NotifyResult2.Error(null)
        }
    }

    fun getSetSpeedPrinterResult(byteArray: ByteArray): NotifyResult2 {
        val mpRespondMsg = MPMessage.MPRespondMsg.parseFrom(byteArray)
        Log.d("$TAG", "$TAG NOTIFY返回的信息 ${mpRespondMsg.toString()}")
        return if (mpRespondMsg.eventType == MPMessage.EventType.PRINTINGSPEED) {
            if (mpRespondMsg.code == 200) {
                val deviceInfo =
                    MPMessage.MPDeviceInfoMsg.parseFrom(mpRespondMsg.respondData.toByteArray())
                Log.d("$TAG", "BLE设置打印速度成功>>>>${deviceInfo.toString()}")
                NotifyResult2.Success(null)
            } else {
                val deviceErrorInfo =
                    MPMessage.MPCodeMsg.parseFrom(mpRespondMsg.error.toByteArray())
                Log.d("$TAG", "BLE设置打印速度失败>>>>${deviceErrorInfo.toString()}")
                NotifyResult2.Error(deviceErrorInfo)
            }
        } else {
            NotifyResult2.Error(null)
        }
    }


    fun getSetConcentrationPrinterResult(byteArray: ByteArray): NotifyResult2 {
        val mpRespondMsg = MPMessage.MPRespondMsg.parseFrom(byteArray)
        Log.d("$TAG", " NOTIFY返回的信息 ${mpRespondMsg.toString()}")
        return if (mpRespondMsg.eventType == MPMessage.EventType.PRINTINCONCENTRATION) {
            if (mpRespondMsg.code == 200) {
                val deviceInfo =
                    MPMessage.MPDeviceInfoMsg.parseFrom(mpRespondMsg.respondData.toByteArray())
                Log.d("$TAG", "BLE设置打印浓度成功>>>>${deviceInfo.toString()}")
                NotifyResult2.Success(null)
            } else {
                val deviceErrorInfo =
                    MPMessage.MPCodeMsg.parseFrom(mpRespondMsg.error.toByteArray())
                Log.d("$TAG", "BLE设置打印浓度失败>>>>${deviceErrorInfo.toString()}")
                NotifyResult2.Error(deviceErrorInfo)
            }
        } else {
            NotifyResult2.Error(null)
        }
    }


    fun getSelfCheckInfo(byteArray: ByteArray): NotifyResult2 {
        val mpRespondMsg = MPMessage.MPRespondMsg.parseFrom(byteArray)
        Log.d("$TAG", " NOTIFY返回的信息 ${mpRespondMsg.toString()}")
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
            Log.d("$TAG", "打印自检页成功>>>>${deviceInfo.toString()}")
            NotifyResult2.Success(deviceInfo)
        } else {
            val deviceErrorInfo = MPMessage.MPCodeMsg.parseFrom(mpRespondMsg.error.toByteArray())
            Log.d("$TAG", "打印自检页失败>>>>${deviceErrorInfo.toString()}")
            NotifyResult2.Error(deviceErrorInfo)
        }
    }

    fun getPrintState(code: MPMessage.MPCodeMsg): SyzPrinterState {
        return when {
            (code.code == 300 && code.info == "1") -> {
                //打印成功
                SyzPrinterState.PRINTER_PRINT_SUCCESS
            }

            (code.code == 300 && code.info == "2") -> {
                //打印取消
                SyzPrinterState.PRINTER_PRINT_CANCEL
            }

            (code.code == 300 && code.info == "3") -> {
                //打印失败
                SyzPrinterState.PRINTER_PRINT_FAILED
            }

            else -> {
                SyzPrinterState.PRINTER_SOME_UNKNOW.code = code.code
                SyzPrinterState.PRINTER_SOME_UNKNOW.info = code.info
                SyzPrinterState.PRINTER_SOME_UNKNOW
            }
        }
    }

    fun getDexUpdateReport(code: MPMessage.MPCodeMsg): SyzPrinterState {
        return when {
            code.code == 400 && code.info == "1" -> {
                SyzPrinterState.PRINTER_DEXUPDATE_SUCCESS
            }

            code.code == 400 && code.info == "2" -> {
                SyzPrinterState.PRINTER_DEXUPDATE_FAILED
            }

            else -> {
                SyzPrinterState.PRINTER_SOME_UNKNOW.code = code.code
                SyzPrinterState.PRINTER_SOME_UNKNOW.info = code.info
                SyzPrinterState.PRINTER_SOME_UNKNOW
            }
        }
    }

    fun getActivelyReport(code: MPMessage.MPCodeMsg): SyzPrinterState {
        return when {
            (code.code == 10 && code.info == "1") -> {
                //合盖
                SyzPrinterState.PRINTER_LID_CLOSE
            }

            (code.code == 10 && code.info == "2") -> {
                //开盖
                SyzPrinterState.PRINTER_LID_OPEN
            }

            (code.code == 11 && code.info == "1") -> {
                //上纸
                SyzPrinterState.PRINTER_HAS_PAPER
            }

            (code.code == 11 && code.info == "2") -> {
                //缺纸
                SyzPrinterState.PRINTER_NO_PAPER
            }

            (code.code == 11 && code.info == "3") -> {
                //卡纸
                SyzPrinterState.PRINTER_STRUCK_PAPER
            }

            (code.code == 12 && code.info == "1") -> {
                //恢复正常
                SyzPrinterState.PRINTER_GETRIGHT
            }

            (code.code == 12 && code.info == "2") -> {
                //过热
                SyzPrinterState.PRINTER_OVERHEATING
            }

            (code.code == 13) -> {
                //info里电量 电量小于二十才会有
                SyzPrinterState.PRINTER_BATTERY.info = code.info
                SyzPrinterState.PRINTER_BATTERY
            }

            else -> {
                SyzPrinterState.PRINTER_SOME_UNKNOW.code = code.code
                SyzPrinterState.PRINTER_SOME_UNKNOW.info = code.info
                SyzPrinterState.PRINTER_SOME_UNKNOW
            }
        }

    }

    fun getBitmapsPrint(byteArray: ByteArray): NotifyResult2 {
        val mpRespondMsg = MPMessage.MPRespondMsg.parseFrom(byteArray)
        Log.d("$TAG", " NOTIFY返回的信息 ${mpRespondMsg.toString()}")
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
                Log.d("$TAG", "图片打印成功>>>>${deviceInfo.toString()}")
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
                Log.d("$TAG", "打印Bitmap问题>>>>${deviceErrorInfo.toString()}")
                NotifyResult2.Error(deviceErrorInfo)
            } else {
                val deviceErrorInfo =
                    MPMessage.MPCodeMsg.parseFrom(mpRespondMsg.error.toByteArray())
                Log.d("$TAG", "设备问题>>>>${deviceErrorInfo.toString()}")
                NotifyResult2.Error(deviceErrorInfo)
            }

        }
    }


    fun getSelfCheck(byteArray: ByteArray): NotifyResult2 {
        val mpRespondMsg = MPMessage.MPRespondMsg.parseFrom(byteArray)
        Log.d("$TAG", " NOTIFY返回的信息 ${mpRespondMsg.toString()}")
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
                NotifyResult2.Success(deviceInfo)
            } else {
                NotifyResult2.Error(deviceInfo)
            }
        } else {
            if (mpRespondMsg.code == 4) {
                val deviceErrorInfo =
                    MPMessage.MPCodeMsg.parseFrom(mpRespondMsg.respondData.toByteArray())
                Log.d("$TAG", "打印Bitmap问题>>>>${deviceErrorInfo.toString()}")
                NotifyResult2.Error(deviceErrorInfo)
            } else {
                val deviceErrorInfo =
                    MPMessage.MPCodeMsg.parseFrom(mpRespondMsg.error.toByteArray())
                Log.d("$TAG", "设备问题>>>>${deviceErrorInfo.toString()}")
                NotifyResult2.Error(deviceErrorInfo)
            }

        }
    }

}