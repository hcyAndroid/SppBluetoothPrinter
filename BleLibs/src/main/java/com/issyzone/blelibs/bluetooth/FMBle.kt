package com.issyzone.blelibs.bluetooth

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
import com.issyzone.blelibs.FMPrinter
import com.issyzone.blelibs.data.BleDevice
import com.issyzone.blelibs.utils.AppGlobels
import com.orhanobut.logger.Logger
import java.util.UUID


data class FMBle(
    var bleDevice: BleDevice?,
    var gatt: BluetoothGatt?,
    var status: Int?
){
    /**
     * 匹配service id  ABF0
     *///it.uuid.toString().
    private fun getALLGatt():MutableList<Pair<UUID,UUID>>{
        val gattList= mutableListOf<Pair<UUID,UUID>>()
        gatt?.apply {
            val serviceList: List<BluetoothGattService> = this.services
            for (service in serviceList) {
                val uuid_service = service.uuid
                val characteristicList = service.characteristics
                for (characteristic in characteristicList) {
                    val uuid_chara = characteristic.uuid
                   // Logger.d("蓝牙GATT协议:::service_uuid==${uuid_service}===character_uuid==${uuid_chara}")
                    gattList.add(Pair(uuid_service,uuid_chara))
                }
            }
        }
        return  gattList
    }

    private fun getFMPrintGatt():MutableList<Pair<UUID,UUID>>{
        val allGatt= getALLGatt()
        val fmPrintGatt=allGatt.filter {
            it.first.toString().lowercase().startsWith(FMPrinter.Charac_ABF1.serviceId.lowercase())
        }.toMutableList()
        fmPrintGatt.forEach {
           // Logger.d("FM蓝牙GATT协议:::service_uuid==${it.first}===character_uuid==${it.second}")
        }
        return fmPrintGatt
    }

    /**
     * 根据类型获取service_id character_id
     */
    fun getCharatersType(type:FMPrinter):MutableList<Pair<UUID,UUID>>{
        val allFmGatt= getFMPrintGatt()
       return when(type){
            FMPrinter.Charac_ABF1->{
                allFmGatt.filter {
                    it.second.toString().lowercase().startsWith(FMPrinter.Charac_ABF1.characterid.lowercase())
                }.toMutableList()
            }
            FMPrinter.Charac_ABF2->{
                allFmGatt.filter {
                    it.second.toString().lowercase().startsWith(FMPrinter.Charac_ABF2.characterid.lowercase())
                }.toMutableList()
            }
            FMPrinter.Charac_ABF3->{
                allFmGatt.filter {
                    it.second.toString().lowercase().startsWith(FMPrinter.Charac_ABF3.characterid.lowercase())
                }.toMutableList()
            }
            FMPrinter.Charac_ABF4->{
                allFmGatt.filter {
                    it.second.toString().toLowerCase().startsWith(FMPrinter.Charac_ABF4.characterid.toLowerCase())
                }.toMutableList()
            }
        }
    }

}