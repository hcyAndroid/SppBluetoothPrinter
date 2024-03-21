package com.issyzone.blelibs.fmBeans
import android.graphics.Bitmap
import com.google.protobuf.ByteString
import com.issyzone.blelibs.service.BleService
import com.issyzone.blelibs.utils.BitmapUtils
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

object FmBitmapPrinterUtils {
    private var bitScope: CoroutineScope? = null
    fun test(bitmap: Bitmap, width: Int, height: Int, page: Int) {
        if (bitScope != null) {
            bitScope!!.cancel()
        }
        bitScope = CoroutineScope(Dispatchers.IO)
        bitScope!!.launch {
            val bitmapArray = BitmapUtils.print(bitmap, width, height)
            Logger.d("图片总字节数${bitmapArray.size}")
            //对bitmaparray进行每100个字节分包
            val aplitafter = splitByteArray(bitmapArray)
            var total = aplitafter.size //总包数
            Logger.d("图片总包数${total}")
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

    fun splitByteArray(input: ByteArray, chunkSize: Int = 100): List<ByteArray> {
        return input.toList().chunked(chunkSize).map { it.toByteArray() }
    }

}