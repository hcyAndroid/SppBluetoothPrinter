package com.issyzone.syzbleprinter.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.issyzone.blelibs.data.BleDevice
import com.issyzone.syzbleprinter.databinding.ItemScanBleBinding


/**
 *蓝牙扫描适配器
 */
class BlueScanAdapter() :
    PagingDataAdapter<BleDevice, BlueScanAdapter.BluetoothViewHolder>(object :
        DiffUtil.ItemCallback<BleDevice>() {
        override fun areItemsTheSame(oldItem: BleDevice, newItem: BleDevice): Boolean {
            return  oldItem.mac==newItem.mac
        }

        override fun areContentsTheSame(oldItem: BleDevice, newItem: BleDevice): Boolean {
            return  oldItem.mac==newItem.mac
        }
    }) {
    inner class BluetoothViewHolder(var vm: ItemScanBleBinding) :
        RecyclerView.ViewHolder(vm.root) {
        fun bind(item: BleDevice?) {
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