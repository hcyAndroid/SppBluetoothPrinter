package com.issyzone.syzbleprinter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.recyclerview.widget.LinearLayoutManager
import com.issyzone.blelibs.SYZBleUtils
import com.issyzone.blelibs.permission.SYZBlePermission
import com.issyzone.syzbleprinter.adapter.BlueScanAdapter
import com.issyzone.syzbleprinter.databinding.ActivityMainBinding
import com.issyzone.syzbleprinter.utils.invokeViewBinding
import com.orhanobut.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val vm: ActivityMainBinding by invokeViewBinding()
    companion object{}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(vm.root)
        testBle()
        initRecyclerView()
    }

    val bleScanAdapter = BlueScanAdapter(){}
    private fun initRecyclerView() {


        vm.rvBle.layoutManager = LinearLayoutManager(this@MainActivity)
        vm.rvBle.adapter = bleScanAdapter


    }

    fun testBle() {
        SYZBlePermission.checkBlePermission(this@MainActivity) {
            //权限开启，
            //开始扫描
            lifecycleScope.launch {
                SYZBleUtils.initBle()
                delay(500)
                Logger.d("蓝牙是否开启${SYZBleUtils.isSupportBle()}")
                if (SYZBleUtils.isBleOpen()) {
                    //蓝牙开启
                    //直接扫
                    SYZBleUtils.scanBle()
                }
                SYZBleUtils.scanBleResultLiveData.observe(this@MainActivity,
                    Observer { deviceList ->
                        Logger.d("蓝牙扫描结束${deviceList?.size}")
                        lifecycleScope.launch {
                            val needDeviceList = withContext(Dispatchers.IO) {
                                return@withContext deviceList?.filter {
                                    it.name.isNullOrEmpty()
                                }
                            }?.toMutableList()
                            Logger.d("蓝牙扫描结束真实需要的设备${needDeviceList?.size}")

                            Pager(PagingConfig(
                                pageSize = 10, initialLoadSize = 10, enablePlaceholders = false
                            ), null, pagingSourceFactory = {
                                ScanBlePageSource(needDeviceList!!)
                            }).flow.cachedIn(lifecycleScope).collect {
                                bleScanAdapter.submitData(it)
                            }
                        }
                    })
            }
        }
    }
}
