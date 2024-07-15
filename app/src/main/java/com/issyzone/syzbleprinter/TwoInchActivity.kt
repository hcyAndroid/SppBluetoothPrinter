package com.issyzone.syzbleprinter

import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.issyzone.classicblulib.bean.LogLiveData
import com.issyzone.classicblulib.bean.SyzPaperSize
import com.issyzone.classicblulib.utils.SYZFileUtils
import com.issyzone.common_work.mvi.BaseMviAppCompatActivity
import com.issyzone.syzbleprinter.databinding.ActivityTwoInchBinding
import com.issyzone.syzbleprinter.intent.SPPrinterUIState
import com.issyzone.syzbleprinter.intent.TwoInchItent
import com.issyzone.syzbleprinter.viewmodel.TwoInchViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class TwoInchActivity : BaseMviAppCompatActivity<TwoInchViewModel, ActivityTwoInchBinding>() {
    override val mViewModel : TwoInchViewModel by viewModel()
    override fun initData() {
        LogLiveData.showLogs(this, mBinding.tvLog)
        mBinding.etMac.setText("03:02:F3:D9:DA:4A")
        lifecycleScope.launchWhenStarted { }
        mViewModel.sendUiIntent(TwoInchItent.initSDk)
        mBinding.tvConnect.setOnClickListener {
            val mac = mBinding.etMac.text.toString()
            mViewModel.sendUiIntent(TwoInchItent.connectDevice(mac))
        }
        mBinding.tvDexUpdate.setOnClickListener {
            val path = SYZFileUtils.copyAssetGetFilePath("FM226_print_app(79).bin")
            mViewModel.sendUiIntent(TwoInchItent.updateDex(path))
        }

        mBinding.tvSelfChecking.setOnClickListener {
            mViewModel.sendUiIntent(TwoInchItent.printSelf)
        }

        mBinding.tvDisconnect.setOnClickListener {
            mViewModel.sendUiIntent(TwoInchItent.disconnect)
        }
        mBinding.tvSetPrintSpeed.setOnClickListener {
            mViewModel.sendUiIntent(TwoInchItent.setPrintSpeed(mBinding.etPrintSpeed.text.toString().toInt()))
        }

        mBinding.tvCheck.setOnClickListener {
            mViewModel.sendUiIntent(TwoInchItent.getDeviceInfo)
        }
        mBinding.tvShutdown.setOnClickListener {
            mViewModel.sendUiIntent(TwoInchItent.closeDevice(mBinding.etCloseTime.text.toString().toInt()))
        }
        mBinding.tvSetPrintConcentration.setOnClickListener {
            mViewModel.sendUiIntent(TwoInchItent.setPrintConcentration(mBinding.etPrintConcentration.text.toString().toInt()))

        }
        mBinding.tvJianxi.setOnClickListener {
            mViewModel.sendUiIntent(TwoInchItent.printBitmap(mBinding.etPrintPage.text.toString().toInt(), pageTYpe = SyzPaperSize.SYZPAPER_JIANXI))
        }

        mBinding.tvCancelPrinter.setOnClickListener {
            mViewModel.sendUiIntent(TwoInchItent.cancelPrint)
        }
        lifecycleScope.launch {
            //lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                mViewModel.uiStateFlow.collectLatest {
                    when (it.sppState) {
                        is SPPrinterUIState.setPaperSizeResult -> {
                            Log.i("TwoInchActivity", "设置纸张大小结果===${it.sppState.isSuccess}")
                            log("设置纸张大小结果===${it.sppState.isSuccess}===${it.sppState.msg.toString()}")
                        }
                        is SPPrinterUIState.checkPaperSizeBeforePrint -> {
                            Log.i("TwoInchActivity", "检查纸张大小===${it.sppState.isSame}==${it.sppState.printerSize}==${it.sppState.doPrintSize}")
                            log("打印前检查纸张===${it.sppState.isSame}==当前打印机的纸张类型${it.sppState.printerSize}==当前调用的纸张类型${it.sppState.doPrintSize}")
                            if (!it.sppState.isSame) {
                                log("纸张类型不一致")
                                mViewModel.sendUiIntent(TwoInchItent.fm226SetPaperSize(it.sppState.doPrintSize!!))
                            }
                        }
                        is SPPrinterUIState.printResult -> {
                            Log.i("TwoInchActivity", "打印结果===${it.sppState.isSuccess}")
                            log("打印结果===是否成功${it.sppState.isSuccess}===原因${it.sppState.msg.toString()}")
                        }
                        is SPPrinterUIState.printing->{
                            Log.i("TwoInchActivity", "打印中===${it.sppState.currentPrintPage}==${it.sppState.totalPage}")
                            log("打印中==当前打印=${it.sppState.currentPrintPage}==总份数${it.sppState.totalPage}")
                        }
                        is SPPrinterUIState.setCancelPrintResult -> {
                            Log.i("TwoInchActivity", "取消打印结果===${it.sppState.isSuccess}")
                            log("取消打印结果===${it.sppState.isSuccess}")
                        }
                        is SPPrinterUIState.printSelfResult -> {
                            Log.i("TwoInchActivity", "打印自检页结果===${it.sppState.isSuccess}")
                            log("打印自检页结果===${it.sppState.isSuccess}===${it.sppState.msg.toString()}")
                        }
                        is SPPrinterUIState.closeTimeResult->{
                            LogLiveData.clearLog(mBinding.tvLog)
                            log("设置关机时间结果=====${it.sppState.isSuccess}==${it.sppState.msg.toString()}")
                        }
                        is SPPrinterUIState.BLU_DISCONNECTED -> {
                            Log.i("TwoInchActivity", "蓝牙断开")
                            LogLiveData.clearLog(mBinding.tvLog)
                            log("蓝牙断开")
                        }
                        is SPPrinterUIState.getDeviceInfo -> {
                            Log.i("TwoInchActivity", "获取设备信息===${it.sppState.msg.toString()}")
                            log("${it.sppState.msg.toString()}")
                        }
                        is SPPrinterUIState.connectResult->{
                            Log.i("TwoInchActivity", "连接结果===${it.sppState.isSuccess}")
                            if (it.sppState.isSuccess){
                              log("连接成功")
                            }else{
                                log("连接失败")
                            }
                        }
                        is SPPrinterUIState.INIT -> {
                            Log.i("TwoInchActivity", "页面初始化")
                        }
                        is SPPrinterUIState.getDexUpdateResult -> {
                            Log.i("TwoInchActivity", "固件更新结果===${it.sppState.syzPrinterState}")
                            log("固件更新结果===${it.sppState.syzPrinterState}")
                        }

                        is SPPrinterUIState.setPrintSpeedResult -> {
                            Log.i("TwoInchActivity", "设置打印速度结果===${it.sppState.isSuccess}")
                            log("设置打印速度结果===${it.sppState.isSuccess}===${it.sppState.msg.toString()}")
                        }
                        is SPPrinterUIState.setDenistyResult -> {
                            Log.i("TwoInchActivity", "设置打印浓度结果===${it.sppState.isSuccess}")
                            log("设置打印浓度结果===${it.sppState.isSuccess}===${it.sppState.msg.toString()}")
                        }
                    }
                }
            }
        }


    private fun log(text:String){
        LogLiveData.addLogs(text)
    }

}
