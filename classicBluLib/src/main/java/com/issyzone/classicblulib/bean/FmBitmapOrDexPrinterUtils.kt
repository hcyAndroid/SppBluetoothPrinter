package com.issyzone.classicblulib.bean
import android.graphics.Bitmap
import android.util.Log
import com.google.protobuf.ByteString
import com.issyzone.classicblulib.utils.BitmapExt
import com.issyzone.classicblulib.utils.BitmapUtils
import com.issyzone.classicblulib.utils.fileToByteArray
import com.issyzone.classicblulib.utils.isExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File

object FmBitmapOrDexPrinterUtils {
    private val TAG="FmBitmap>>>>>>"

    private var bitScope: CoroutineScope? = null
    private var dexScope: CoroutineScope? = null
    fun writeBitmap(bitmap: Bitmap, width: Int, height: Int, page: Int) {
        if (bitScope != null) {
            bitScope!!.cancel()
        }
        bitScope = CoroutineScope(Dispatchers.IO)
        bitScope!!.launch {
            Log.d("$TAG","二值化之后bitmap字节数${BitmapExt.bitmapToByteArray(bitmap).size}")
            //val bitmapArray = BitmapExt.bitmapToByteArray(bitmap, Bitmap.CompressFormat.PNG, 100)
            val bitmapArray = BitmapUtils.print(bitmap, width, height)
            Log.d("$TAG","print()之后图片总字节数${bitmapArray.size}")
            //对bitmaparray进行每100个字节分包
            val aplitafter = splitByteArray(bitmapArray,100)
            var total = aplitafter.size //总包数
            Log.d("$TAG","图片总包数${total}")
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
                Log.d("$TAG"," 找不到${filePath}目录下的文件")
                 return@launch
            }
            if (!file.isExtension("bin")){
                Log.d("$TAG","该${filePath}文件不是Bin文件")
                return@launch
            }
            //转byte数组
            val fileArray = file.fileToByteArray()
            //分包
            val aplitafter = splitByteArray(fileArray,100)
            var total = aplitafter.size //总包数
            Log.d("$TAG","Dex文件总包数${total}")
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

        }
    }

    fun splitByteArray(input: ByteArray, chunkSize: Int = 100): List<ByteArray> {
        return input.toList().chunked(chunkSize).map { it.toByteArray() }
    }




    fun calculateCRC16(data: ByteArray): Int {
        var crc = 0x0000

        for (byte in data) {
            crc = crc xor (byte.toInt() and 0xFF shl 8)
            for (j in 0 until 8) {
                if (crc and 0x8000 != 0) {
                    crc = crc shl 1 xor 0x1021
                } else {
                    crc = crc shl 1
                }
            }
        }

        // XMODEM使用的CRC16算法需要取反
        crc = crc.inv() and 0xFFFF

        return crc
    }








}