package com.issyzone.syzbleprinter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import com.issyzone.blelibs.data.BleDevice
import com.issyzone.blelibs.fmBeans.FMPrinterOrder
import com.issyzone.blelibs.service.BleService
import com.issyzone.syzbleprinter.adapter.BlueScanAdapter
import com.orhanobut.logger.Logger
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
class ScanBleViewModel :ViewModel() {
    fun startBleService(){
        BleService.getInstance()
    }

    fun connectBleDevice(bleDevice: BleDevice){
        viewModelScope.launch {
            val connectDevice= BleService.getInstance().conenctBle2(bleDevice)
//            if (connectDevice!=null&&connectDevice.status!=null){
//                //连接成功
//                // connectDevice.getALLGatt()
//                Logger.d("连接成功")
//
//            }else{
//                //连接失败
//                Logger.d("连接失败")
//            }
        }

    }

    fun getScanBleDevice(bleScanAdapter: BlueScanAdapter){
        var  service=BleService.getInstance()
        service.getScanResultFlow()
        viewModelScope.launch {
           // delay(10000)
            BleService.getInstance().getScanResultFlow().catch {
                Logger.d("异常::${it.message}")
            }.collect {
                Logger.d("扫描到的FM设备个数：：${it.size}")
                if (it.size!=0){
                    Pager(PagingConfig(
                        pageSize = it.size, initialLoadSize = it.size
                    ), null, pagingSourceFactory = {
                        ScanBlePageSource2(it)
                    }).flow.cachedIn(viewModelScope).collect {
                        bleScanAdapter.submitData(it)
                    }
                }

            }
        }
    }

   inner class ScanBlePageSource2(var mutableList: List<BleDevice>) : PagingSource<Int, BleDevice>() {
        override fun getRefreshKey(state: PagingState<Int, BleDevice>): Int? {
            return null
        }

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, BleDevice> {
            return try {
                val page = params.key ?: 1
                val prevKey = if (page > 1) page - 1 else null
                // val nextKey = if (repoItems.isNotEmpty()) page + 1 else null
                LoadResult.Page(mutableList, prevKey, null)
            } catch (e: Exception) {
                LoadResult.Error(e)
            }

        }
    }
}