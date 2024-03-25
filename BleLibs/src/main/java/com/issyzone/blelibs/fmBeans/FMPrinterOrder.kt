package com.issyzone.blelibs.fmBeans

import android.util.Log
import com.issyzone.blelibs.fmBeans.MPMessage.MPSendMsg


object FMPrinterOrder {
    private val TAG="FMPrinterOrder"

    //获取打印信息
    fun orderForGetFmDevicesInfo(): ByteArray {
        var mSendMsg = MPSendMsg.newBuilder().setEventType(MPMessage.EventType.DEVICEINFO).build()
        Log.d("$TAG","发送检查设备的命令${mSendMsg.toString()}")
        return mSendMsg.toByteArray()
    }


    //打印自检页
    fun orderForGetFmSelfcheckingPage(): ByteArray {
        var mSendMsg = MPSendMsg.newBuilder().setEventType(MPMessage.EventType.SELFTEST).build()
        Log.d("$TAG","打印自检页的命令${mSendMsg.toString()}")
        return mSendMsg.toByteArray()
    }

    /**
     *  设置关机时间:意思就是设置多少分钟后关机
     *  设置的时间
     *  0:永远不关机
     *  其他: 根据传入的数值设定,单位 :分钟
     */
    fun orderForGetFmSetShutdownTime(min: Int = 0): ByteArray {
        var mSendMsg =
            MPSendMsg.newBuilder().setEventType(MPMessage.EventType.CLOSETIME).setSendInt(min)
                .build()
        Log.d("$TAG","设置关机时间的命令${mSendMsg.toString()}")
        return mSendMsg.toByteArray()
    }

    /**
     *设备取消打印
     */
    fun orderForGetFmCancelPrinter(): ByteArray {

        var mSendMsg =
            MPSendMsg.newBuilder().setEventType(MPMessage.EventType.CANCELPRINTING).build()
        Log.d("$TAG","设备取消打印的命令${mSendMsg.toString()}")
        return mSendMsg.toByteArray()
    }

    /**
     * 设置打印速度
     * @param speed (设置速度 1- 4)
     *
     */
    fun orderForGetFmSetPrintSpeed(speed: Int): ByteArray {

        if (speed in 1..4) {
            var mSendMsg = MPSendMsg.newBuilder().setEventType(MPMessage.EventType.PRINTINGSPEED)
                .setSendInt(speed).build()
            Log.d("$TAG","设置打印速度的命令${mSendMsg.toString()}")
            return mSendMsg.toByteArray()
        } else {
            val mySpeed = if (speed < 1) {
                1
            } else {
                4
            }
            var mSendMsg = MPSendMsg.newBuilder().setEventType(MPMessage.EventType.PRINTINGSPEED)
                .setSendInt(mySpeed).build()
            Log.d("$TAG","设置打印速度的命令${mSendMsg.toString()}")
            return mSendMsg.toByteArray()
        }
    }

    /**
     * 设置打印浓度
     * @param concentration 1到8
     */

    fun orderForGetFmSetPrintConcentration(
        concentration: Int
    ): ByteArray {
        if (concentration in 1..8) {
            var mSendMsg =
                MPSendMsg.newBuilder().setEventType(MPMessage.EventType.PRINTINCONCENTRATION)
                    .setSendInt(
                        concentration
                    ).build()
            Log.d("$TAG","设置打印浓度的命令${mSendMsg.toString()}")
            return mSendMsg.toByteArray()
        } else {
            val myConcentration = if (concentration < 1) {
                1
            } else {
                8
            }
            var mSendMsg = MPSendMsg.newBuilder().setEventType(MPMessage.EventType.PRINTINGSPEED)
                .setSendInt(myConcentration).build()
            Log.d("$TAG","设置打印浓度的命令${mSendMsg.toString()}")
            return mSendMsg.toByteArray()
        }
    }


}