package com.issyzone.syzbleprinter

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.recyclerview.widget.LinearLayoutManager
import com.issyzone.blelibs.service.BleService
import com.issyzone.blelibs.utils.BitmapExt
import com.issyzone.blelibs.utils.BitmapUtils
import com.issyzone.blelibs.utils.ImageUtilKt
import com.issyzone.syzbleprinter.adapter.BlueScanAdapter
import com.issyzone.syzbleprinter.databinding.ActivityMain2Binding
import com.issyzone.syzbleprinter.utils.invokeViewBinding
import com.issyzone.syzbleprinter.utils.invokeViewModel
import com.issyzone.syzbleprinter.viewmodel.ScanBleViewModel
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity2 :  ComponentActivity() {
    private val vb:ActivityMain2Binding by invokeViewBinding()
    private val vm:ScanBleViewModel by invokeViewModel()
    val bleScanAdapter = BlueScanAdapter(){
        vm.connectBleDevice(it)
    }
    fun byteArrayToBitmap(byteArray: ByteArray): Bitmap? {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(vb.root)
        vm.startBleService()
        initRecyclerView()
        vm.getScanBleDevice(bleScanAdapter)
       // ImageUtilKt.convertBinary(BitmapExt.test(), 128)
        var bb=ImageUtilKt.convertBinary(BitmapExt.test(), 128)
        val bitmapArray = BitmapUtils.print(bb, bb.width, bb.height)
        //vb.iv.setImageBitmap(ImageUtilKt.convertBinary(BitmapExt.test(), 128))
        //vb.iv.setImageBitmap(ImageUtilKt.convertBinary(BitmapExt.test(), 128))
        vb.iv.setImageBitmap(byteArrayToBitmap(bitmapArray))

//        lifecycleScope.launch {
//            //delay(40000)
//            BleService.getInstance().getScanResultFlow()
//             .collect{
//                Logger.d("打印数据${it.size}")
//            }
//        }








//        BleService.getInstance().getScanResultFlow2().observe(this, Observer {
//            Logger.d("打印数据${it.size}")
////            Pager(PagingConfig(
////                pageSize = it.size, initialLoadSize = it.size, enablePlaceholders = false
////            ), null, pagingSourceFactory = {
////                ScanBlePageSource2(it)
////            }).flow.cachedIn(viewModelScope).collect {
////                bleScanAdapter.submitData(it)
////            }
//        })
    }

    private fun initRecyclerView() {
        vb.listBle.layoutManager = LinearLayoutManager(this@MainActivity2)
        vb.listBle.adapter = bleScanAdapter

    }
}