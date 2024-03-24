package com.issyzone.syzbleprinter.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.issyzone.blelibs.callback.SyzBleCallBack
import com.issyzone.blelibs.data.BleDevice
import com.issyzone.blelibs.databinding.ItemScanBleBinding
import com.issyzone.blelibs.fmBeans.FMPrinterOrder
import com.issyzone.blelibs.fmBeans.FmBitmapOrDexPrinterUtils
import com.issyzone.blelibs.service.BleService
import com.issyzone.blelibs.service.SyzBleManager
import com.issyzone.blelibs.utils.BitmapExt
import com.issyzone.blelibs.utils.ImageUtilKt
import com.issyzone.blelibs.utils.SYZFileUtils
import com.issyzone.blelibs.utils.SYZFileUtils.copyAssetGetFilePath
import com.orhanobut.logger.Logger
import kotlinx.coroutines.GlobalScope


/**
 *蓝牙扫描适配器
 */
class BlueScanAdapter(var click: (ble: BleDevice) -> Unit) :
    PagingDataAdapter<BleDevice, BlueScanAdapter.BluetoothViewHolder>(object :
        DiffUtil.ItemCallback<BleDevice>() {
        override fun areItemsTheSame(oldItem: BleDevice, newItem: BleDevice): Boolean {
            return oldItem.mac == newItem.mac
        }

        override fun areContentsTheSame(oldItem: BleDevice, newItem: BleDevice): Boolean {
            return oldItem.mac == newItem.mac
        }
    }) {
    inner class BluetoothViewHolder(var vm: ItemScanBleBinding) : RecyclerView.ViewHolder(vm.root) {
        fun bind(item: BleDevice?) {

            vm.tvConnect.setOnClickListener {
                item?.apply {
                   // click.invoke(this)
                    BleService.getInstance().conenctBle2(this)

                }
            }
            vm.tvCheck.setOnClickListener {
                BleService.getInstance().fmWriteABF1(FMPrinterOrder.orderForGetFmDevicesInfo())
            }
            vm.tvSelfChecking.setOnClickListener {
                BleService.getInstance().fmWriteABF1(FMPrinterOrder.orderForGetFmSelfcheckingPage())
            }
            vm.tvShutdown.setOnClickListener {
                BleService.getInstance().fmWriteABF1(FMPrinterOrder.orderForGetFmSetShutdownTime(2))
            }
            vm.tvCancelPrinter.setOnClickListener {
                BleService.getInstance().fmWriteABF1(FMPrinterOrder.orderForGetFmCancelPrinter())
            }

            vm.tvSetPrintSpeed.setOnClickListener {
                BleService.getInstance().fmWriteABF1(FMPrinterOrder.orderForGetFmSetPrintSpeed(2))
            }

            vm.tvSetPrintConcentration.setOnClickListener {
                BleService.getInstance()
                    .fmWriteABF1(FMPrinterOrder.orderForGetFmSetPrintConcentration(3))
            }


            vm.tvSetPrintImg.setOnClickListener {
                Logger.d("FmBitmapPrinterUtils》》》bitmap字节数${BitmapExt.bitmapToByteArray(BitmapExt.decodeBitmap()).size}")
                val bitmap = ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(), 128)
                FmBitmapOrDexPrinterUtils.writeBitmap(
                    bitmap, bitmap.width, bitmap.height, 1
                )
            }
            vm.tvDexUpdate.setOnClickListener {

                val path =
                    SYZFileUtils.copyAssetGetFilePath("FM226_print_app(1.1.0.0.8).bin")
                path?.apply {
                    FmBitmapOrDexPrinterUtils.writeDex(
                        this
                    )
                }
            }

            vm.tvDeviceName.text = item?.name ?: ""
            vm.tvDeviceMac.text = item?.mac ?: ""
        }
    }

    override fun onBindViewHolder(holder: BluetoothViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BluetoothViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemScanBleBinding.inflate(inflater, parent, false)
        return BluetoothViewHolder(
            binding
        )
    }


}