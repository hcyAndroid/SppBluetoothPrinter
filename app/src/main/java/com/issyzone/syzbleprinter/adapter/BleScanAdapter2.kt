package com.issyzone.syzbleprinter.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.issyzone.blelibs.data.BleDevice
import com.issyzone.syzbleprinter.databinding.ItemScanBleBinding


/**
 *蓝牙扫描适配器
 */
class BlueScanAdapter2 :BaseQuickAdapter<BleDevice,QuickViewHolder>(){
    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: BleDevice?) {
        TODO("Not yet implemented")
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): QuickViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemScanBleBinding.inflate(inflater, parent, false)

        return QuickViewHolder(binding.root)
    }

}