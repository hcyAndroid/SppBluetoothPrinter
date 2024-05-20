package com.issyzone.classicblulib.bean

import android.util.Log
import com.issyzone.classicblulib.callback.SyzPrinterState
import com.issyzone.classicblulib.callback.SyzPrinterState2

import kotlinx.coroutines.delay

import java.nio.ByteBuffer
import java.util.BitSet


object FmNotifyBeanUtils {
    private val TAG = "FmNotifyBeanUtils>>>"

    fun getDeviceInfo(byteArray: ByteArray): NotifyResult {
        val mpRespondMsg = MPMessage.MPRespondMsg.parseFrom(byteArray)
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
                Log.d("$TAG", "经典蓝牙设置关机成功>>>>${deviceInfo.toString()}")
                NotifyResult2.Success(null)
            } else {
                val deviceErrorInfo =
                    MPMessage.MPCodeMsg.parseFrom(mpRespondMsg.error.toByteArray())
                Log.d("$TAG", "经典蓝牙设置关机失败>>>>${deviceErrorInfo.toString()}")
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
                Log.d("$TAG", "经典蓝牙设置打印速度成功>>>>${deviceInfo.toString()}")
                NotifyResult2.Success(null)
            } else {
                val deviceErrorInfo =
                    MPMessage.MPCodeMsg.parseFrom(mpRespondMsg.error.toByteArray())
                Log.d("$TAG", "经典蓝牙设置打印速度失败>>>>${deviceErrorInfo.toString()}")
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
                Log.d("$TAG", "经典蓝牙设置打印浓度成功>>>>${deviceInfo.toString()}")
                NotifyResult2.Success(null)
            } else {
                val deviceErrorInfo =
                    MPMessage.MPCodeMsg.parseFrom(mpRespondMsg.error.toByteArray())
                Log.d("$TAG", "经典蓝牙设置打印浓度失败>>>>${deviceErrorInfo.toString()}")
                NotifyResult2.Error(deviceErrorInfo)
            }
        } else {
            NotifyResult2.Error(null)
        }
    }


    /**
     * 打印自检页返回
     */
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


    /**
     * 取消打印
     */
    fun getCancelPrintingInfo(byteArray: ByteArray): NotifyResult2 {
        val mpRespondMsg = MPMessage.MPRespondMsg.parseFrom(byteArray)
        Log.d("$TAG", "取消打印回调》》 ${mpRespondMsg.toString()}")
        return  if (mpRespondMsg.eventType == MPMessage.EventType.CANCELPRINTING) {
            if (mpRespondMsg.code == 200) {
                val deviceInfo =
                    MPMessage.MPDeviceInfoMsg.parseFrom(mpRespondMsg.respondData.toByteArray())
                Log.d("$TAG", ">>>>收到了取消打印的通知${deviceInfo.toString()}")
                NotifyResult2.Success(null)
            } else {
                val deviceErrorInfo =
                    MPMessage.MPCodeMsg.parseFrom(mpRespondMsg.error.toByteArray())
                Log.d("$TAG", "收到了取消打印的通知Code!=200>>>>${deviceErrorInfo.toString()}")
                NotifyResult2.Error(deviceErrorInfo)
            }
        } else {
            NotifyResult2.Error(null)
        }
    }

    fun getPrintStatus(device: MPMessage.MPDeviceInfoMsg): SyzPrinterState2 {
        Log.i("获取打印机状态>>>", device.printStatus)
        /**
         * code:  10: 打印开盖关盖状态    info:   bit 0 --> 打印中,
         * bit 1 --> 没纸, bit 2 --> 打印缓存满 ,bit 3 --> 开盖,bit 4 --> 卡纸,bit 5 --> 打印头高温,bit 6 --> 电池电量低,bit 7 --> 马达过热
         *        13: 打印机电量状态      info:   电池电量,如果当前电量只剩下20%,那么传20
         *        400: 打印机升级状态     info:    1: 升级陈工,三秒后打印机重启 2.升级失败
         */
        val infoArray = pasrsePrinterStatus(device.printStatus.toInt())
        val errorLIst = mutableListOf<SyzPrinterState2>()
        infoArray?.forEachIndexed { index, b ->
            val obj = SyzPrinterState2.values().find {
                it.status == b && it.index == index
            }
            if (obj != null) {
                errorLIst.add(obj)
            }
        }
        errorLIst.sortBy {
            it.order
        }
        Log.i(TAG, "异常：${errorLIst.toString()}")
        LogLiveData.addLogs("异常：${errorLIst.toString()}")
        return if (errorLIst.isEmpty()) {
            SyzPrinterState2.PRINTER_OK
        } else {
            errorLIst[0]
        }
    }
    fun getActivelyReport(code: MPMessage.MPCodeMsg): SyzPrinterState2 {
        Log.i("主动上报200解析>>>", code.toString())
        // LogLiveData.addLogs("主动上报200解析>>>${code.toString()}")
        /**
         * code:  10: 打印开盖关盖状态    info:   bit 0 --> 打印中,
         * bit 1 --> 没纸, bit 2 --> 打印缓存满 ,bit 3 --> 开盖,bit 4 --> 卡纸,bit 5 --> 打印头高温,bit 6 --> 电池电量低,bit 7 --> 马达过热
         *        13: 打印机电量状态      info:   电池电量,如果当前电量只剩下20%,那么传20
         *        400: 打印机升级状态     info:    1: 升级陈工,三秒后打印机重启 2.升级失败
         */
        val infoArray = pasrsePrinterStatus(code.info.toInt())
        val errorLIst = mutableListOf<SyzPrinterState2>()
        infoArray?.forEachIndexed { index, b ->
            val obj = SyzPrinterState2.values().find {
                it.status == b && it.index == index
            }
            if (obj != null) {
                errorLIst.add(obj)
            }
        }
        errorLIst.sortBy {
            it.order
        }
        Log.i(TAG, "异常：${errorLIst.toString()}")
        LogLiveData.addLogs("异常：${errorLIst.toString()}")
        return if (errorLIst.isEmpty()) {
            SyzPrinterState2.PRINTER_OK
        } else {
            errorLIst[0]
        }
    }


    /*   fun getPrintState(code: MPMessage.MPCodeMsg): SyzPrinterState {
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

    */

    fun convertToBinary(num: Int): String {
        return String.format("%8s", Integer.toBinaryString(num)).replace(' ', '0')
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

    fun getByteAtPosition(value: Int, position: Int): Byte {
        // 计算要获取的字节在整数中的位置
        val shiftBits = position * 8
        // 将整数右移相应的位数，然后与 0xFF 进行按位与运算，获取特定字节的值
        val byteValue = (value shr shiftBits) and 0xFF
        // 将获取到的值转换为字节类型并返回
        return byteValue.toByte()
    }

    fun iterateBits(byteValue: Byte) {
        // 使用一个循环逐一获取每个比特的值
        for (i in 0..7) {  // 从最低位到最高位遍历
            val bit = (byteValue.toInt() shr i) and 1
            Log.i("iterateBits========", "====${bit}====")
        }
    }

    fun pasrsePrinterStatus(number: Int): BooleanArray {
        //iterateBits()




//
        val bytes = ByteArray(4)
        // 使用 ByteBuffer 将 int 转换为字节数组
        val buffer = ByteBuffer.allocate(4)
        buffer.putInt(number)
        buffer.flip()
        buffer[bytes]
        val b = buffer[buffer.limit() - 1] // 要获取bit数组的字节
       // val bits = BitSet.valueOf(byteArrayOf(getByteAtPosition(number,0)))
        val bits = BitSet.valueOf(byteArrayOf(b))
        val length = 8 // bit数组的长度
        val bitArray = BooleanArray(length)
        Log.i("info解析start========", "========")
        // LogLiveData.addLogs("info解析start========")
        for (i in 0 until length) {
            bitArray[i] = bits[i]
            val tagresult = when (i) {
                0 -> {
                    "打印中"
                }

                1 -> {
                    "缺纸"
                }

                2 -> {
                    "打印缓存满"
                }

                3 -> {
                    "开盖"
                }

                4 -> {
                    "卡纸"
                }

                5 -> {
                    "打印头高温"
                }

                6 -> {
                    "电池电量低"
                }

                7 -> {
                    "马达过热"
                }

                else -> {
                    "其他状态"
                }
            }
            Log.i("info解析======${i}==${tagresult}", "========" + bitArray[i])
            // LogLiveData.addLogs("info解析======${i}==${tagresult}===${bitArray[i]}")
        }
        Log.i("info解析end========", "========")
        // LogLiveData.addLogs("info解析end======")
        return bitArray
    }

    /*  fun isPrinterError(printStatus: Int): SyzPrinterState2 {
          val infoArray = test(printStatus)
          val errorLIst = mutableListOf<SyzPrinterState2>()
          infoArray?.forEachIndexed { index, b ->
              val obj = SyzPrinterState2.values().find {
                  it.status == b && it.index == index
              }
              if (obj != null) {
                  errorLIst.add(obj)
              }
          }
          Log.i(TAG, "异常：${errorLIst.toString()}")
          return if (errorLIst.isEmpty()) {
              SyzPrinterState2.PRINTER_OK
          } else {
              errorLIst[0]
          }
      }*/

    /*   fun getActivelyReport(code: MPMessage.MPCodeMsg): SyzPrinterState2 {
           Log.i("主动上报200解析>>>", code.toString())

           */
    /**
     * code:  10: 打印开盖关盖状态    info:   bit 0 --> 打印中,
     * bit 1 --> 没纸, bit 2 --> 打印缓存满 ,bit 3 --> 开盖,bit 4 --> 卡纸,bit 5 --> 打印头高温,bit 6 --> 电池电量低,bit 7 --> 马达过热
     *        13: 打印机电量状态      info:   电池电量,如果当前电量只剩下20%,那么传20
     *        400: 打印机升级状态     info:    1: 升级陈工,三秒后打印机重启 2.升级失败
     *//*
        val infoArray = test(code.info.toInt())
        val errorLIst = mutableListOf<SyzPrinterState2>()
        infoArray?.forEachIndexed { index, b ->
            val obj = SyzPrinterState2.values().find {
                it.status == b && it.index == index
            }
            if (obj != null) {
                errorLIst.add(obj)
            }
        }
        errorLIst.sortBy {
            it.order
        }
        Log.i(TAG, "异常：${errorLIst.toString()}")
        return if (errorLIst.isEmpty()) {
            SyzPrinterState2.PRINTER_OK
        } else {
            errorLIst[0]
        }


        *//* return when {
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
         }*//*

    }

    fun getBitmapsPrint(byteArray: ByteArray): NotifyResult2 {
        val mpRespondMsg = MPMessage.MPRespondMsg.parseFrom(byteArray)
        Log.d("$TAG", " NOTIFY返回的信息 ${mpRespondMsg.toString()}")
        */
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
     *//*
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
            */
    /**
     *    code:1  info busy
     *//*
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
        */
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
     *//*
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


    */
    /**
     * 获取纸张的类型
     *//*
    fun getPaperStatus(deviceInfo: MPMessage.MPDeviceInfoMsg): SyzPrinterPaper? {
        //这个变量改为纸张类型 2：2寸纸； 3：2.5寸纸； 4：3寸纸 ；5：4寸纸
        return enumValues<SyzPrinterPaper>().find {
            it.paperSize == deviceInfo.paperSize
        }
    }
*/

    /**
     * 获取打印的状态
     *//*    fun getPrintStatus(deviceInfo: MPDeviceInfoMsg) {
            deviceInfo.printStatus
        }*/


}