package com.issyzone.blelibs.fmBeans

import android.util.Log
import com.issyzone.blelibs.service.SyzBleManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


class PrintBimapUtils {
   private val TAG="PrintBimapUtils"
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
                 Log.d("$TAG","PrintBimapUtils>>>所有图片打印成功》》》")
             }
         }
    }
   fun removePrintWhenSuccess(){
       Log.d("$TAG","PrintBimapUtils>>>一张图片打印成功》》》")
       val doFistAlready=bitMapPrintTaskList.removeFirst()
       if (bitMapPrintTaskList.isNotEmpty()){
           Log.d("$TAG","开始下一张打印")
           doPrint()
       }else{
           Log.d("$TAG","bitmap全部成功")
       }
    }


    fun isCompleteBitmapPrinter():Boolean{
        return bitMapPrintTaskList.isNullOrEmpty()
    }



}