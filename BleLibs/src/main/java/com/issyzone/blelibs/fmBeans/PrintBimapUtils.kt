package com.issyzone.blelibs.fmBeans

import com.issyzone.blelibs.service.SyzBleManager
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


class PrintBimapUtils {

    companion object {
        private var bitMapPrintTaskList = mutableListOf<MutableList<MPMessage.MPSendMsg>>()
        private var instance: PrintBimapUtils? = null
        private var serviceScope: CoroutineScope? = null
        fun getInstance(): PrintBimapUtils {
            if (instance == null) {
                serviceScope= CoroutineScope(Dispatchers.IO)
                instance = PrintBimapUtils()
            }
            return instance!!
        }
    }

    fun setBitmapTask(bitmapdataList: MutableList<MutableList<MPMessage.MPSendMsg>>):PrintBimapUtils{
        bitMapPrintTaskList=bitmapdataList
        return this
    }

     fun doPrint(){
         if (serviceScope!=null&& serviceScope!!.isActive){
             serviceScope!!.cancel()
         }
         serviceScope= CoroutineScope(Dispatchers.IO)
         serviceScope!!.launch {
             val doFirst= bitMapPrintTaskList.firstOrNull()
             if (doFirst!=null){
                 SyzBleManager.getInstance().fmWriteABF4(doFirst)

             }else{
                 Logger.d("PrintBimapUtils>>>所有图片打印成功》》》")
             }
         }
    }
   fun removePrintWhenSuccess(){
        Logger.d("PrintBimapUtils>>>一张图片打印成功》》》")
       val doFistAlready=bitMapPrintTaskList.removeFirst()
       if (bitMapPrintTaskList.isNotEmpty()){
           Logger.e("开始下一张打印")
           doPrint()
       }else{
           Logger.e("bitmap全部成功")
       }
    }


    fun isCompleteBitmapPrinter():Boolean{
        return bitMapPrintTaskList.isNullOrEmpty()
    }



}