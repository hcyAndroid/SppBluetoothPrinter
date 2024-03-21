package com.issyzone.syzbleprinter.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.issyzone.blelibs.data.BleDevice
import com.issyzone.blelibs.databinding.ItemScanBleBinding
import com.issyzone.blelibs.fmBeans.FMPrinterOrder
import com.issyzone.blelibs.fmBeans.FmBitmapPrinterUtils
import com.issyzone.blelibs.service.BleService
import com.issyzone.blelibs.utils.BitmapExt
import com.issyzone.blelibs.utils.BitmapUtils
import com.issyzone.blelibs.utils.ImageUtilKt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


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
                    click.invoke(this)

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


//            vm.tvSetPrintImg.setOnClickListener {
//               val bitmap= ImageUtilKt.convertBinary(BitmapExt.test(), 128)
//                FmBitmapPrinterUtils.test(
//                    ImageUtilKt.convertBinary(BitmapExt.test(), 128), bitmap.width, bitmap.height, 1
//                )
//            }

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