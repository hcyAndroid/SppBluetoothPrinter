package com.issyzone.classicblulib.service

import android.graphics.Bitmap
import android.util.Log
import com.google.protobuf.ByteString
import com.issyzone.classicblulib.bean.FMPrinterOrder
import com.issyzone.classicblulib.bean.MPMessage
import com.issyzone.classicblulib.bean.SyzPaperSize
import com.issyzone.classicblulib.bean.SyzPrinter
import com.issyzone.classicblulib.callback.BluPrintingCallBack
import com.issyzone.classicblulib.callback.DeviceBleInfoCall
import com.issyzone.classicblulib.callback.SyzPrinterState2
import com.issyzone.classicblulib.utils.AppGlobels
import com.issyzone.classicblulib.utils.BitmapUtils
import com.issyzone.classicblulib.utils.HeatShrinkUtils
import com.issyzone.classicblulib.utils.SyzBitmapQueue
import com.issyzone.classicblulib.utils.SyzPrinterSetting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.ArrayList


/**
 * 对bitmap进行处理
 */
class SyzBitmapProcessor private constructor(var builder: Builder) {
    companion object {
        inline fun build(block: Builder.() -> Unit) = Builder().apply(block).build()
    }

    class Builder {
        var printerType: SyzPrinter = SyzPrinter.SYZTWOINCH // 默认值
        var bitmapWidth: Int = 0
        var bitmapHeight: Int = 0
        var printPage: Int = 1
        var currentPaperSize: SyzPaperSize=SyzPaperSize.SYZPAPER_JIANXI
        fun printerType(printerType: SyzPrinter) = apply { this.printerType = printerType }
        fun bitmapWidth(bitmapWidth: Int) = apply { this.bitmapWidth = bitmapWidth }
        fun bitmapHeight(bitmapHeight: Int) = apply { this.bitmapHeight = bitmapHeight }
        fun printPage(printPage: Int) = apply { this.printPage = printPage }
        fun currentPaperSize(currentPaperSize: SyzPaperSize) =
            apply { this.currentPaperSize = currentPaperSize }

        fun build() = SyzBitmapProcessor(this)
    }

    private val TAG = "SyzBitmapProcessor>>>"
    private var isPrintingState = false //判断是否是打印中的状态
    private var isCancelPrinting = false  //判断是否取消打印
    private var bitmapProcessed = 0 //bitmap的发包的进度
    private val mutex = Mutex()
    private var printerIndex = 0   //打印好的张数
    private val duanChunkSize = SyzPrinterSetting.getfenDuanChunkSize(builder.printerType)
    private val chunkSizePack = SyzPrinterSetting.getchunkSizePack(builder.printerType)
    private val isCompress = SyzPrinterSetting.isSupportCompress(builder.printerType)
   // private val isSupportPageMore = SyzPrinterSetting.isSupportPageMore(builder.printerType)
    private var totalPage = 0   //需要打印的总份数
    private var isPrintPicToLocal=false  //是否把打印的图片保存到本地
    private val compressCode = if (isCompress) {
        1
    } else {
        0
    }

    //初始化打印参数,在打印之前
    private fun initPrinterParams() {
        printerIndex = 0
        bitmapProcessed = 0
        isCancelPrinting = false
        isPrintingState = false
        totalPage = 0
        releaseResources()
    }

    fun getPrinterState(): Boolean {
        return isPrintingState
    }

    //压缩
    private suspend fun compress(bitmapDataArray: ByteArray): ByteArray? {
        return try {
            // 直接调用 HeatShrinkUtils.compress 挂起函数
            HeatShrinkUtils.compress(bitmapDataArray)
        } catch (e: IOException) {
            //处理IOException异常
            Log.e(TAG, "压缩失败: ${e.localizedMessage}")
            null
        } catch (e: Exception) {
            // 处理其他类型的异常
            Log.e(TAG, "压缩失败: ${e.localizedMessage}")
            null
        }
    }


    private suspend fun decompress(bitmapDataArray: ByteArray): ByteArray? {
        return try {
            // 直接调用 HeatShrinkUtils.compress 挂起函数
            HeatShrinkUtils.decompress(bitmapDataArray)
        } catch (e: IOException) {
            //处理IOException异常
            Log.e(TAG, "解压缩失败: ${e.localizedMessage}")
            null
        } catch (e: Exception) {
            // 处理其他类型的异常
            Log.e(TAG, "解压缩失败: ${e.localizedMessage}")
            null
        }
    }

    //按字节分包
    private fun splitByteArray(input: ByteArray, chunkSize: Int = 100): List<ByteArray> {
        return input.toList().chunked(chunkSize).map { it.toByteArray() }
    }

    private val MAX_CROP_WIDTH_FM226=50*8 //模版宽度最大不超过50*8,超过了就裁剪
    private val MAX_CROP_WIDTH_RW402B=102*8
   suspend fun bitmap2Path(bitmap: Bitmap, path: String?): String? {
       if (isPrintPicToLocal){
           try {
               val file = File(path)
               file.parentFile?.mkdirs()
               val os: OutputStream = FileOutputStream(file)
               bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
               os.flush()
               os.close()
           } catch (e: Exception) {
               Log.e(TAG, "", e)
           }
           return path
       }else{
           return ""
       }
    }
    //app一股脑把bitmap集合丢过来，bitmap里会重复,page可能多余2
    //不会有 ABx2  AA BB的情况  只有AB AB的情况
    suspend fun produceBitmaps2(bitmapList: MutableList<Bitmap>): SyzBitmapProcessor {

        initPrinterParams()
        val printTaskLength = bitmapList.size * (builder.printPage)
        totalPage = printTaskLength
        bitMapTaskQueue = SyzBitmapQueue(printTaskLength)
        Log.d(
            TAG,
            "准备打印机任务>>${bitmapList.size}张bitmap,一张${builder.printPage}份==总共要打印${totalPage}份"
        )
        val isPrintMore=bitmapList.size!=1 //判断多图还是单图
        var isSupportPageMore=false
        bitmapList.forEachIndexed { index, bitmap ->
            val newBitmap = if (builder.printerType == SyzPrinter.SYZTWOINCH) {
                //二寸打印机，加了排废，模版宽度最大不超过52*8,超过了就裁剪
                val bitmapWidth = bitmap.width
                if (bitmapWidth > MAX_CROP_WIDTH_FM226) {
                    Log.e(TAG, "宽度${bitmapWidth}超过了2寸最大宽度${MAX_CROP_WIDTH_FM226}开始裁剪")
                    val cropBitmap= cropBitmapCenter(bitmap, MAX_CROP_WIDTH_FM226)
                    val filePath=File(AppGlobels.getApplication().externalCacheDir, "sdk_print").apply { mkdirs() }.absolutePath + File.separator+"print-" + System.currentTimeMillis() + ".png"

                    bitmap2Path(cropBitmap,filePath)
                    cropBitmap
                } else {
                    Log.i(TAG, "宽度${bitmapWidth}没有超过了2寸最大宽度${MAX_CROP_WIDTH_FM226}")
                    bitmap
                }
            } else if (builder.printerType == SyzPrinter.SYZFOURINCH) {
                val bitmapWidth = bitmap.width
                if (bitmapWidth > MAX_CROP_WIDTH_RW402B) {
                    Log.e(TAG, "宽度超过了4寸最大宽度开始裁剪")
                    val cropBitmap=cropBitmapCenter(bitmap, MAX_CROP_WIDTH_RW402B)
                    cropBitmap
                } else {
                    Log.i(TAG, "宽度没有超过了4寸最大宽度")
                    bitmap
                }
            } else {
                bitmap
            }
            if (!isPrintMore) {
                //单图
                val duanREsult= turnBitmapToDuanList(newBitmap, builder.printPage, index)
                val duanListQueue = duanREsult.second
                isSupportPageMore=duanREsult.first
                bitMapTaskQueue.add(duanListQueue)
            } else {
                //多张图
                val duanREsult=turnBitmapToDuanList(newBitmap, 1, index)
                val duanListQueue = duanREsult.second
                isSupportPageMore=duanREsult.first
                bitMapTaskQueue.add(duanListQueue)
            }
        }
        //图片回收
        bitmapList.forEach {
            if (it != null && !it.isRecycled) {
                it.recycle()
            }
        }
        bitmapList.clear()

        if (!isPrintMore) {
            //单图打印，分两种情况，2寸超过10k不支持page,不超过就支持page
            if (isSupportPageMore){
                Log.i(TAG, "打印机${builder.printerType}打印任务>>${bitMapTaskQueue.size()}张bitmap,一张${builder.printPage}份==总共要打印${totalPage}份==是否支持page多份${isSupportPageMore}")
            }else{
                if (builder.printPage > 1) {
                    bitMapTaskQueue.duplicateElements(builder.printPage)
                }
                Log.i(TAG, "打印机${builder.printerType}打印任务>>${bitMapTaskQueue.size()}张bitmap,一张1份==总共要打印${totalPage}份==是否支持page多份${isSupportPageMore}")
            }
        } else {
            //多图打印,PDF
            if (builder.printPage > 1) {
                bitMapTaskQueue.duplicateElements(builder.printPage)
            }
            Log.i(TAG,
                "打印机${builder.printerType}多图打印任务>>${bitMapTaskQueue.size()}张bitmap,一张1份==总共要打印${totalPage}份"
            )

            /*//四寸支持打印多份
            if (isAction4PrintMore) {
                //不需要复制，因为硬件的page已经设置了

                Log.i(
                    TAG,
                    "打印机${builder.printerType}单张图任务>>${bitMapTaskQueue.size()}张bitmap,一张${builder.printPage}份==总共要打印${totalPage}份"
                )


            } else {
                //打印PDF


            }*/

        }



        return this
    }


    private suspend fun cropBitmapCenter(bitmap: Bitmap, newWidth: Int): Bitmap {
        // 计算裁剪的起始点
        val startX = if (bitmap.width > newWidth) (bitmap.width - newWidth) / 2 else 0
        val startY = 0

        // 计算裁剪的宽度和高度
        val width = if (bitmap.width > newWidth) newWidth else bitmap.width
        val height = bitmap.height

        // 创建新的位图
        return Bitmap.createBitmap(bitmap, startX, startY, width, height)
    }

    /**
     * 方法是将位图列表转换为打印任务队列，每个打印任务是一个分解后的位图数据队列
     * ABC  page=2  ABC ABC
     */
 /*   suspend fun produceBitmaps(bitmapList: MutableList<Bitmap>): SyzBitmapProcessor {
        initPrinterParams()
        val printTaskLength = bitmapList.size * (builder.printPage)
        totalPage = printTaskLength
        bitMapTaskQueue = SyzBitmapQueue(printTaskLength)
        Log.d(
            TAG,
            "准备打印机任务>>${bitmapList.size}张bitmap,一张${builder.printPage}份==总共要打印${totalPage}份"
        )
        bitmapList.forEachIndexed { index, bitmap ->
            if (isSupportPageMore) {
                val duanListQueue = turnBitmapToDuanList(bitmap, builder.printPage, index)
                bitMapTaskQueue.add(duanListQueue)
            } else {
                val duanListQueue = turnBitmapToDuanList(bitmap, 1, index)
                bitMapTaskQueue.add(duanListQueue, builder.printPage)
            }
            if (bitmap != null && !bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
        if (isSupportPageMore) {
            Log.i(
                TAG,
                "打印机任务>>${bitMapTaskQueue.size()}张bitmap,一张${builder.printPage}份==总共要打印${totalPage}份"
            )
        } else {
            Log.i(
                TAG,
                "打印机任务>>${bitMapTaskQueue.size()}张bitmap,一张1份==总共要打印${totalPage}份"
            )
        }
        return this
    }
*/
    /**
     * 把图片变成段的集合
     */
    private suspend fun turnBitmapToDuanList(
        bitmap: Bitmap, page: Int, index: Int
    ): Pair<Boolean,SyzBitmapQueue<ArrayList<MPMessage.MPSendMsg>>> {
        Log.i(
            TAG,
            "Bitmap${index}===开始处理图片===width==${bitmap.width}==height==${bitmap.height}===${builder.bitmapWidth}"
        )
        val bitmapPrintArray = BitmapUtils.print(bitmap, bitmap.width, bitmap.height)
        val bitMapFenDuanList = splitByteArray(bitmapPrintArray, duanChunkSize)
        //存放一张图片所有的段数据
        val bitmapDuanListQueue =
            SyzBitmapQueue<ArrayList<MPMessage.MPSendMsg>>(bitMapFenDuanList.size)
        val duanArrayList= mutableListOf<ByteArray>() //一张图片所有的段集合
        var oneBitmapTotalSize=0
        bitMapFenDuanList.forEachIndexed { indexDuan, duanBytes ->
            Log.i(TAG, "Bitmap${index}第${indexDuan}段压缩前的大小${duanBytes.size}")
            val duanByteArray = if (isCompress) {
                val duanCompress = compress(duanBytes)
                Log.i(TAG, "Bitmap${index}第${indexDuan}段压缩后的大小${duanCompress?.size}")
                duanCompress?.apply {
                    val tstData = decompress(duanCompress)
                    Log.i(TAG, "Bitmap${index}第${indexDuan}段解压缩后的大小${tstData?.size}")
                }


                if (duanCompress != null) {
                    duanCompress
                } else {
                    //希望这永远不要执行
                    duanBytes
                }
            } else {
                Log.i(TAG, "Bitmap${index}===第${indexDuan}段不压缩的大小${duanBytes.size}")
                duanBytes
            }
            oneBitmapTotalSize+=duanByteArray.size
            duanArrayList.add(duanByteArray)
        }
        val isSupportPage=when(builder.printerType){
              SyzPrinter.SYZTWOINCH->{
                  oneBitmapTotalSize < 10*1024
              }
              SyzPrinter.SYZFOURINCH->{
                true
             }
             SyzPrinter.SYZZEROFIVE->{
                false
             }
             else->{
                false
             }
        }
        val truePage=if (isSupportPage){
            page
        }else{
            1
        }
        Log.i(TAG,"当前设备${builder.printerType}是否支持page多份==${isSupportPage}==图片分段压缩后的合计大小${oneBitmapTotalSize}")
        //每段遍历处理数据
        duanArrayList.forEachIndexed { indexDuan, duanByteArray ->
            //每段再分包
            val bitMapFenBaoList = splitByteArray(duanByteArray, chunkSizePack)
            val totalBaoEachDuan = bitMapFenBaoList.size
            Log.i(
                TAG,
                "Bitmap${index}===第${indexDuan}段按${chunkSizePack}分了${bitMapFenBaoList.size}包==page=${page}"
            )
            val baoDataEachDuanList = arrayListOf<MPMessage.MPSendMsg>()

            bitMapFenBaoList.forEachIndexed { baoIndex, baoBytes ->
                Log.i(
                    TAG,
                    "Bitmap${index}===第${indexDuan}段==第${baoIndex}包==数据大小==${baoBytes.size}==当前图片分段数>>>${bitMapFenDuanList.size}==="
                )
                if (baoIndex == 0) {
                    //第一包传宽高
                    val mPPrintMsg = MPMessage.MPPrintMsg.newBuilder().setPage(truePage)
                        .setDataLength(bitmapPrintArray.size)
                        .setImgData(ByteString.copyFrom(baoBytes))
                        .setTotalSection(bitMapFenDuanList.size).setIndexPackage(baoIndex + 1)
                        .setTotalPackage(totalBaoEachDuan).setWidth(bitmap.width / 8)
                        .setCompression(compressCode).setSectionLength(duanByteArray.size).build()
                    val baoData = MPMessage.MPSendMsg.newBuilder()
                        .setEventType(MPMessage.EventType.DEVICEPRINT)
                        .setSendData(mPPrintMsg.toByteString()).build()
                    baoDataEachDuanList.add(baoData)
                } else {
                    val mPPrintMsg = MPMessage.MPPrintMsg.newBuilder().setPage(truePage)
                        .setDataLength(bitmapPrintArray.size)
                        .setTotalSection(bitMapFenDuanList.size)
                        .setImgData(ByteString.copyFrom(baoBytes)).setIndexPackage(baoIndex + 1)
                        .setTotalPackage(totalBaoEachDuan).setCompression(compressCode)
                        .setSectionLength(duanByteArray.size).build()
                    val baoData = MPMessage.MPSendMsg.newBuilder()
                        .setEventType(MPMessage.EventType.DEVICEPRINT)
                        .setSendData(mPPrintMsg.toByteString()).build()
                    baoDataEachDuanList.add(baoData)
                }
            }
            bitmapDuanListQueue.add(baoDataEachDuanList)
        }


        return Pair(isSupportPage,bitmapDuanListQueue)
    }


    private var bitMapTaskQueue = SyzBitmapQueue<SyzBitmapQueue<ArrayList<MPMessage.MPSendMsg>>>(1)

    /**
     * A,B,C  page=2  ABC ABC
     * 方法是将PDF文件的位图转换为打印任务队列。
     */
    suspend fun producePDFFile(bitmapList: MutableList<Bitmap>): SyzBitmapProcessor {
        initPrinterParams()
        val printTaskLength = bitmapList.size * (builder.printPage)
        bitMapTaskQueue = SyzBitmapQueue(printTaskLength)
        totalPage = printTaskLength
        Log.d(
            TAG,
            "准备PDF打印机任务>>${bitmapList.size}张bitmap,打印${builder.printPage}份==总共要打印${totalPage}份"
        )
        bitmapList.forEachIndexed { index, bitmap ->
            val duanListQueue = turnBitmapToDuanList(bitmap, 1, index).second
            if (bitmap != null && !(bitmap.isRecycled)) {
                bitmap.recycle()//回收
            }
            bitMapTaskQueue.add(duanListQueue)
        }
        //重点复制多份
        bitMapTaskQueue.duplicateElements(builder.printPage)
        Log.i(
            TAG,
            "PDF打印文件份数${builder.printPage},产生的数据源数量${bitMapTaskQueue.size()}==总页数${totalPage}"
        )
        return this
    }


    suspend fun updatePrintProcess() = withContext(Dispatchers.IO) {
        mutex.withLock {
            printerIndex++
            bitmapCall?.printing(currentPrintPage = printerIndex, totalPage = totalPage)
            if (printerIndex == totalPage) {
                isPrintingState = false
                Log.d(TAG, "打印机任务结束>>==总共要打印${totalPage}份==已打印${printerIndex}")
                bitmapCall?.getPrintResult(true, SyzPrinterState2.PRINTER_OK)
            } else {
                if (isCancelPrinting) {
                    Log.d(
                        TAG,
                        "打印机任务结束(取消打印)>>==总共要打印${totalPage}份==已打印${printerIndex}"
                    )
                } else {
                    Log.d(TAG, "打印机任务ing>>==总共要打印${totalPage}份==已打印${printerIndex}")
                }

            }
        }
    }

    fun handlePrintingMistakes(state: SyzPrinterState2) {
        //处理打印过程中的错误
        Log.e(TAG, "打印机打印出错${state.toString()}==当前的打印状态${isPrintingState}")
        if (isPrintingState) {
            bitmapCall?.getPrintResult(false, state)
            isPrintingState = false
        }
    }

    //收到取消答应的通知
    fun setPrinterCancel() {
        isCancelPrinting = true;
        Log.i(TAG, "收到取消打印的指令，不会再发包${isCancelPrinting}")
        bitmapCall?.getPrintResult(false, SyzPrinterState2.PRINTER_CANCEL_PRINT)
    }

    //解包失败
    fun upackerFaiLed() {
        bitmapCall?.getPrintResult(false, SyzPrinterState2.PRINTER_UPACKER_FAILED)
    }

    private var bitmapCall: BluPrintingCallBack? = null //打印回调
    fun setBimapCallBack(callBack: BluPrintingCallBack): SyzBitmapProcessor {
        this.bitmapCall = callBack
        return this
    }

    suspend fun doPrint() {
        //   bitmapCall?.printing(currentPrintPage = printerIndex, totalPage = totalPage)
        bitmapCall?.printing(currentPrintPage = printerIndex, totalPage = totalPage)
        consumeOneBitmap()
    }

    private suspend fun consumeOneBitmap() {
        if (bitMapTaskQueue.isEmpty()) {
//            SyzClassicBluManager.getInstance().writeABF1(FMPrinterOrder.orderForEndPrint(), "${TAG}=orderForEndPrint>>>>")
//            delay(50)
//            //这里只释放
//            releaseResources()
        } else {
            if (isCancelPrinting) {
                //取消打印
                Log.i(TAG, "收到取消打印的标记，清空所有的任务=已经发送的图片数==${bitmapProcessed}==已经打印的图片数==${printerIndex}")

                closePrinterTask()
            } else {
                //currentBitmapQueue.clear()
                currentBitmapQueue = bitMapTaskQueue.poll()  //拿出第一张bitmap
                bitmapProcessed++
                Log.i(
                    TAG, "打印bitmap==发包进度${bitmapProcessed}==已经打印的图片数==${printerIndex}"
                )
                processBitmap(currentBitmapQueue) // 处理Bitmap的函数
            }

        }
    }

    private suspend fun closePrinterTask() {
        releaseResources()
        Log.i(TAG, "结束打印任务")
        isPrintingState = false
    }

    private var currentBitmapQueue = SyzBitmapQueue<ArrayList<MPMessage.MPSendMsg>>(10)


    private suspend fun processBitmap(
        bitmapQueue: SyzBitmapQueue<ArrayList<MPMessage.MPSendMsg>>,
    ) {
        //开始消费段,检查打印机是否具备打印条件

      /*  if (bitmapProcessed == 1) {
            Log.i(TAG, "打印机打印前检查打印状态${bitmapProcessed}")
            val printerStatus = checkPrinterState()
            //检查打印机状态
            val stateFlag = printerStatus.second
            if (stateFlag == SyzPrinterState2.PRINTER_OK) {
                Log.i(TAG, "打印机状态正常==${stateFlag}")
                Log.i(TAG, "下发第一张图片的第一段，总共${bitmapQueue.size()}段")
                //只有开始打印的时候才下发进度
                bitmapCall?.printing(currentPrintPage = printerIndex, totalPage = totalPage)
                bitmapCall?.checkPrinterBeforePrint(true, SyzPrinterState2.PRINTER_OK)
                duanProcessed = 0
                consumeDuansOneBitmap()
            } else {
                Log.e(TAG, "打印机状态异常不能打印==${stateFlag}")
                // bitmapCall?.getPrintResult(false, stateFlag)
                bitmapCall?.checkPrinterBeforePrint(false, stateFlag)
            }
        } else {
            Log.i(
                TAG,
                "不检查状态直接打印,下发第${bitmapProcessed}张图片的第一段，总共${bitmapQueue.size()}段"
            )
            duanProcessed = 0
            consumeDuansOneBitmap()
        }*/
        Log.i(
            TAG,
            "下发第${bitmapProcessed}张图片的第一段，总共${bitmapQueue.size()}段"
        )
        duanProcessed = 0
        consumeDuansOneBitmap()
    }

    private var duanProcessed = 0
    suspend fun consumeDuansOneBitmap() = withContext(Dispatchers.IO) {
        if (currentBitmapQueue.isEmpty()) {
            Log.d(TAG, "当前第${bitmapProcessed}bitmap所有duan已经消费完毕")
            if (!bitMapTaskQueue.isEmpty()) {
                Log.d(TAG, "当前第${bitmapProcessed}bitmap所有duan已经消费完毕==处理下一张图片")
                consumeOneBitmap()
            } else {
                Log.e(TAG, "没有图片任务了")/*                SyzClassicBluManager.getInstance()
                                    .writeABF1(FMPrinterOrder.orderForEndPrint(), "${TAG}=orderForEndPrint>>>>")
                                delay(50)
                                //这里只释放
                                releaseResources()*/
            }

            // recycleCurrentPrintBitmap()

        } else {
            //现在取消打印直接把分段的停了

            duanProcessed++
            val duanData = currentBitmapQueue.poll()
            Log.d(
                TAG,
                "向蓝牙设备写入第${bitmapProcessed}图=====第${duanProcessed}段的数据==包数=${duanData.size}"
            )
            isPrintingState = true
            SyzClassicBluManager.getInstance().fmWriteABF4(duanData)
            if (currentBitmapQueue.isEmpty() && bitMapTaskQueue.isEmpty()) {
                Log.d(TAG, "所有图片都发完了，发送结束命令>>>")
                SyzClassicBluManager.getInstance()
                    .writeABF1(FMPrinterOrder.orderForEndPrint(), "${TAG}=orderForEndPrint>>>>")
                delay(50)
                //这里只释放
                releaseResources()
            } else {

            }
        }
    }

    suspend fun checkPrinterStatus():Boolean {
        val printer_status = SyzClassicBluManager.getInstance().getPrintStatus()
        var paperSize = printer_status.first
        var printerState = printer_status.second
        if (builder.printerType == SyzPrinter.SYZTWOINCH) {
            //二寸
            Log.i(TAG, "二寸检查纸张类型==设备纸张类型${paperSize}==当前打印纸张类型${builder.currentPaperSize}")
            if (paperSize == builder.currentPaperSize) {
                //纸张类型一致
                Log.i(TAG, "纸张类型一致")
                if (printerState == SyzPrinterState2.PRINTER_OK) {
                    Log.i(TAG, "打印机状态正常==${printerState}")
                    //只有开始打印的时候才下发进度
                    //bitmapCall?.printing(currentPrintPage = printerIndex, totalPage = totalPage)
                    bitmapCall?.checkPrinterBeforePrint(true, SyzPrinterState2.PRINTER_OK)
                    return true
                } else {
                    Log.e(TAG, "打印机状态异常不能打印==${printerState}")
                    bitmapCall?.checkPrinterBeforePrint(false, printerState)
                }
                bitmapCall?.checkPaperSizeBeforePrint(true,paperSize,builder.currentPaperSize)
            }else{
                //纸张类型不一致,二寸要弹窗提示
                Log.e(TAG, "纸张类型不一致${printerState}====${paperSize}====${builder.currentPaperSize}")
                bitmapCall?.checkPaperSizeBeforePrint(false,paperSize,builder.currentPaperSize)
                //纸张类型不一致的时候，要弹窗提示，发送当前类型
              /*  val setPaperStatus=SyzClassicBluManager.getInstance().setPaperType(builder.currentPaperSize)
                if (setPaperStatus==SyzPrinterState2.PRINTER_SET_PAPER_TYPE_OK){
                    bitmapCall?.checkPaperSizeBeforePrint(true,builder.currentPaperSize,builder.currentPaperSize)
                    if (printerState == SyzPrinterState2.PRINTER_OK) {
                        Log.i(TAG, "打印机状态正常==${printerState}")
                        //只有开始打印的时候才下发进度
                        bitmapCall?.checkPrinterBeforePrint(true, SyzPrinterState2.PRINTER_OK)
                        return true
                    } else {
                        Log.e(TAG, "打印机状态异常不能打印==${printerState}")
                        bitmapCall?.checkPrinterBeforePrint(false, printerState)
                    }
                }else{
                    Log.e(TAG, "设置纸张类型失败==${setPaperStatus}")
                     bitmapCall?.checkPrinterBeforePrint(false, setPaperStatus)
                }*/
            }
        } else {
            //四寸
            Log.i(TAG, "四寸不用检查纸张类型")
            if (printerState == SyzPrinterState2.PRINTER_OK) {
                Log.i(TAG, "打印机状态正常==${printerState}")
                //只有开始打印的时候才下发进度
                //bitmapCall?.printing(currentPrintPage = printerIndex, totalPage = totalPage)
                bitmapCall?.checkPrinterBeforePrint(true, SyzPrinterState2.PRINTER_OK)
                return true
            } else {
                Log.e(TAG, "打印机状态异常不能打印==${printerState}")
                bitmapCall?.checkPrinterBeforePrint(false, printerState)
            }
        }
        return false
    }




    fun releaseResources() {
        currentBitmapQueue.forEach { duanList ->
            duanList?.clear()
        }
        // 释放段队列持有的引用
        currentBitmapQueue.clear()

        bitMapTaskQueue.forEach { innerQueue ->
            innerQueue.forEach { arrayList ->
                arrayList.clear()
            }
            // 释放每个内部队列持有的引用
            innerQueue.clear()
        }
        // 释放外部队列持有的引用
        bitMapTaskQueue.clear()
        // 日志输出，表示已释放资源
        Log.i(TAG, "All resources have been released.")
    }

    fun handlePrinterError(code: Int) {
        Log.e(TAG,"打印过程中发生了错误${code}==当前的打印状态${isPrintingState}")
        if (isPrintingState) {
            bitmapCall?.getPrintResult(false, SyzPrinterState2.PRINTER_PRINTING_DEVICE_ERROR)
            isPrintingState = false
        }
    }

}


