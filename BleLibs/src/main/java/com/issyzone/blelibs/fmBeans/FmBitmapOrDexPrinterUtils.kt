package com.issyzone.blelibs.fmBeans
import android.graphics.Bitmap
import com.google.protobuf.ByteString
import com.issyzone.blelibs.service.BleService
import com.issyzone.blelibs.utils.BitmapExt
import com.issyzone.blelibs.utils.BitmapUtils
import com.issyzone.blelibs.utils.fileToByteArray
import com.issyzone.blelibs.utils.isExtension
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File

object FmBitmapOrDexPrinterUtils {
    private val TAG="FmBitmapOrDexPrinterUtils>>>>>>"

    private var bitScope: CoroutineScope? = null
    private var dexScope: CoroutineScope? = null
    fun writeBitmap(bitmap: Bitmap, width: Int, height: Int, page: Int) {
        if (bitScope != null) {
            bitScope!!.cancel()
        }
        bitScope = CoroutineScope(Dispatchers.IO)
        bitScope!!.launch {
            Logger.d("${TAG}二值化之后bitmap字节数${BitmapExt.bitmapToByteArray(bitmap).size}")
            //val bitmapArray = BitmapExt.bitmapToByteArray(bitmap, Bitmap.CompressFormat.PNG, 100)
            val bitmapArray = BitmapUtils.print(bitmap, width, height)
            Logger.d("${TAG}print()之后图片总字节数${bitmapArray.size}")
            //对bitmaparray进行每100个字节分包
            val aplitafter = splitByteArray(bitmapArray,100)
            var total = aplitafter.size //总包数
            Logger.d("${TAG}图片总包数${total}")
            val needSendDataList = mutableListOf<MPMessage.MPSendMsg>()
            aplitafter.forEachIndexed { index, bytes ->
                val mPPrintMsg = MPMessage.MPPrintMsg.newBuilder().setPage(page)
                    .setDataLength(bitmapArray.size)
                    .setImgData(ByteString.copyFrom(bytes)).setIndexPackage(index+1)
                    .setTotalPackage(total).build()
                needSendDataList.add(
                    MPMessage.MPSendMsg.newBuilder().setEventType(MPMessage.EventType.DEVICEPRINT)
                        .setSendData(mPPrintMsg.toByteString()).build()
                )
            }
           BleService.getInstance().fmWriteABF4(needSendDataList)
        }
    }



     fun writeDex(filePath:String){
        if (dexScope != null) {
            dexScope!!.cancel()
        }
        dexScope = CoroutineScope(Dispatchers.IO)
        dexScope!!.launch {
            val file= File(filePath)
            if (!file.exists()){
                Logger.d("$TAG 找不到${filePath}目录下的文件")
                 return@launch
            }
            if (!file.isExtension("bin")){
                Logger.d("$TAG 该${filePath}文件不是Bin文件")
                return@launch
            }
            //转byte数组
            val fileArray = file.fileToByteArray()
            //分包
            val aplitafter = splitByteArray(fileArray,100)
            var total = aplitafter.size //总包数
            Logger.d("${TAG}Dex文件总包数${total}")
            val needSendDataList = mutableListOf<MPMessage.MPSendMsg>()
            aplitafter.forEachIndexed { index, bytes ->
                val mpFirmwareMsg = MPMessage.MPFirmwareMsg.newBuilder()
                    .setBinData(ByteString.copyFrom(bytes))//分包数据
                    .setDataLength(bytes.size)//binData的分包长度
                    .setIndexPackage(index+1)//分包序列号 第一包是 1 以
                    .setCrcCode(calculateCRC16(fileArray))
                    .setTotalPackage(total)
                    .build()
                needSendDataList.add(
                    MPMessage.MPSendMsg.newBuilder().setEventType(MPMessage.EventType.FIRMWAREUPGRADE)
                        .setSendData(mpFirmwareMsg.toByteString()).build()
                )
            }
           BleService.getInstance().fmWriteDexABF4(needSendDataList)
        }
    }

    fun splitByteArray(input: ByteArray, chunkSize: Int = 100): List<ByteArray> {
        return input.toList().chunked(chunkSize).map { it.toByteArray() }
    }

    fun calculateCRC16(bytes: ByteArray): Int {
        var crc = 0xFFFF
        for (i in bytes.indices) {
            crc = crc xor (bytes[i].toInt() and 0xFF)
            for (j in 0 until 8) {
                if ((crc and 0x0001) != 0) {
                    crc = (crc shr 1) xor 0xA001
                } else {
                    crc = crc shr 1
                }
            }
        }
        return crc and 0xFFFF
    }

}