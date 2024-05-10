package com.issyzone.classicblulib.service
import android.util.Log
import com.google.protobuf.ByteString
import com.issyzone.classicblulib.bean.MPMessage
import com.issyzone.classicblulib.bean.SyzFirmwareType
import com.issyzone.classicblulib.bean.SyzPrinter
import com.issyzone.classicblulib.utils.SyzDexQueue
import com.issyzone.classicblulib.utils.SyzPrinterSetting
import com.issyzone.classicblulib.utils.fileToByteArray
import com.issyzone.classicblulib.utils.isExtension
import java.io.File
import java.util.ArrayList

class SyzDexProcessor private constructor(var builder: Builder) {
    private val TAG = "SyzDexProcessor>>>>"
    private val chunkSize = SyzPrinterSetting.getDexChunkSize(builder.printerType)
    private val currentSyzFirmwareType = builder.syzFirmwareType
    private val delaySize = 4 * 1024

    companion object {
        inline fun build(block: Builder.() -> Unit) = Builder().apply(block).build()
    }

    class Builder {
        var printerType: SyzPrinter = SyzPrinter.SYZTWOINCH // 默认值
        var syzFirmwareType: SyzFirmwareType = SyzFirmwareType.SYZFIRMWARETYPE01
        fun syzFirmwareType(syzFirmwareType: SyzFirmwareType) =
            apply { this.syzFirmwareType = syzFirmwareType }

        fun printerType(printerType: SyzPrinter) = apply { this.printerType = printerType }
        fun build() = SyzDexProcessor(this)
    }

    private var dexTaskQueue = SyzDexQueue<SyzDexQueue<MPMessage.MPSendMsg>>()
    suspend fun produceDex(filePath: String): SyzDexProcessor {
        val file = File(filePath)
        if (!file.exists()) {
            Log.d("$TAG", " 找不到${filePath}目录下的文件")
            return this
        }
        if (!file.isExtension("bin")) {
            Log.d("$TAG", " 该${filePath}文件不是Bin文件")
            return this
        }
        //转byte数组
        val fileArray = file.fileToByteArray()
        Log.d(TAG, "Dex文件总字节数${fileArray.size}===当前打印分包::${chunkSize}=")
        //val aplitafter = splitByteArray(fileArray, chunkSize)
        //
        //crc算法
        val crccode = CRC16_XMODEM(fileArray)
        //var total = aplitafter.size //总包数


        val fileArrays= splitByteArray(fileArray, delaySize)
        var total = 0 //总包数
        fileArrays.forEachIndexed { index1, fkbytes ->
            val fk=splitByteArray(fkbytes, chunkSize)
            fk.forEachIndexed { index2, bytes ->
                total++
            }
        }
        Log.d(TAG, "Dex文件按${chunkSize}分包的总包数${total}")
        var index=0
        dexTaskQueue.clear()
        fileArrays.forEachIndexed { index1, fkbytes ->
            val fk=splitByteArray(fkbytes, chunkSize)
            val fkqueue=SyzDexQueue<MPMessage.MPSendMsg>()
            fk.forEachIndexed { index2, bytes ->
                index++
                val mpFirmwareMsg =
                    MPMessage.MPFirmwareMsg.newBuilder().setBinData(ByteString.copyFrom(bytes))//分包数据
                        .setDataLength(fileArray.size)//
                        .setFirmwareType(currentSyzFirmwareType.funValue)//0打印机固件，1字库，2文件系统
                        .setIndexPackage(index)//分包序列号 第一包是 1 以
                        .setCrcCode(crccode)
                        .setTotalPackage(total).build()
                val sendMsg =
                    MPMessage.MPSendMsg.newBuilder().setEventType(MPMessage.EventType.FIRMWAREUPGRADE)
                        .setSendData(mpFirmwareMsg.toByteString()).build()
                Log.i(TAG,"第${index1}个4k,第${index}个包==文件数据量大小${bytes.size}")
                fkqueue.add(sendMsg)
            }
            dexTaskQueue.add(fkqueue)
        }

        return this
/*        aplitafter.forEachIndexed { index, bytes ->
            //再按尺寸分包
            val mpFirmwareMsg =
                MPMessage.MPFirmwareMsg.newBuilder().setBinData(ByteString.copyFrom(bytes))//分包数据
                    .setDataLength(fileArray.size)//
                    .setFirmwareType(currentSyzFirmwareType.funValue)//0打印机固件，1字库，2文件系统
                    .setIndexPackage(1 + index)//分包序列号 第一包是 1 以
                    .setCrcCode(crccode).setTotalPackage(total).build()
            val sendMsg =
                MPMessage.MPSendMsg.newBuilder().setEventType(MPMessage.EventType.FIRMWAREUPGRADE)
                    .setSendData(mpFirmwareMsg.toByteString()).build()
            dexTaskQueue.add(sendMsg)

        }*/




    }

    //    private var currentDexQueue=ArrayList<MPMessage.MPSendMsg>()
    private var dexProcessed = 0 //dex的发包的进
     fun test(){
         (delaySize/chunkSize).toInt()*chunkSize+chunkSize
    }
    suspend fun consumeOne4K() {
        if (dexTaskQueue.isEmpty()) {
            Log.d(TAG, "固件更新已经发完")
        } else {
            //var count = 0
           // var index=0
            dexProcessed++
//            while (count < delaySize) {
//                val enum = dexTaskQueue.poll()
//                Log.i(TAG,"SPP_写入::::第${dexProcessed}个4k===${index}===")
//                SyzClassicBluManager.getInstance().writeDexABF4(enum)
//                count += chunkSize
//                index++
//            }

            val fkQueue = dexTaskQueue.poll()
            while (!fkQueue.isEmpty()){
                val nextItem= fkQueue.poll()
                SyzClassicBluManager.getInstance().writeDexABF4(nextItem)
            }



            /*  var count = 0
              var index=0
              while (count < delaySize) {
                  count += chunkSize
                  index++
                  val item = dexTaskQueue.poll()
                  Log.i(TAG,"SPP_写入::::第${dexProcessed}个4k===${index}===")
                  LogLiveData.addLogs("SPP_写入::::第${dexProcessed}个4k===${index}===")
                  SyzClassicBluManager.getInstance().writeDexABF4(item)
                  if (count<delaySize&&(count+chunkSize)>delaySize){
                      index++
                      count += chunkSize
                      val nextItem = dexTaskQueue.poll()
                      Log.i(TAG,"SPP2_写入::::第${dexProcessed}个4k===${index}===")
                      LogLiveData.addLogs("SPP2_写入::::第${dexProcessed}个4k===${index}===")
                      SyzClassicBluManager.getInstance().writeDexABF4(nextItem)
                  }
              }*/
        }
    }


    private suspend fun divideQueues(queue: SyzDexQueue<MPMessage.MPSendMsg>): SyzDexQueue<ArrayList<MPMessage.MPSendMsg>> {
        val dividedMessages = SyzDexQueue<ArrayList<MPMessage.MPSendMsg>>()
        val tempMessages = ArrayList<MPMessage.MPSendMsg>()
        var totalSize = 0
        while (!queue.isEmpty()) {
            val message = queue.poll()
            if (totalSize > delaySize) {
                // 如果添加当前消息会超过4k，则先将之前的消息添加到结果中
                dividedMessages.add(tempMessages)
                tempMessages.clear()
                totalSize = totalSize - delaySize
            }
            // 添加当前消息到临时列表中，并增加总大小
            tempMessages.add(message)
            totalSize += chunkSize
        }
        // 添加剩余的消息到结果中
        if (!tempMessages.isEmpty()) {
            dividedMessages.add(tempMessages)
        }
        return dividedMessages
    }


    private fun CRC16_XMODEM(buffer: ByteArray): Int {
        var wCRCin = 0x0000
        val wCPoly = 0x1021
        for (b in buffer) {
            for (i in 0..7) {
                val bit = b.toInt() shr 7 - i and 1 == 1
                val c15 = wCRCin shr 15 and 1 == 1
                wCRCin = wCRCin shl 1
                if (c15 xor bit) wCRCin = wCRCin xor wCPoly
            }
        }
        wCRCin = wCRCin and 0xffff
        return 0x0000.let { wCRCin = wCRCin xor it; wCRCin }
    }

    private fun splitByteArray(input: ByteArray, chunkSize: Int = 100): List<ByteArray> {
        return input.toList().chunked(chunkSize).map { it.toByteArray() }
    }

}