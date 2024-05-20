package com.issyzone.classicblulib.bean

import android.util.Log
import com.issyzone.classicblulib.utils.Upacker


/**
 * 打印机命令
 */
object FMPrinterOrder {
    private val TAG="FMPrinterOrder"

    //获取打印信息
    fun orderForGetFmDevicesInfo(): ByteArray {
        val mSendMsg = MPMessage.MPSendMsg.newBuilder().setEventType(MPMessage.EventType.DEVICEINFO).build()
        Log.d(TAG,"发送检查设备的命令${mSendMsg.toString()}===命令长度${Upacker.frameEncode(mSendMsg.toByteArray()).size}")
        return mSendMsg.toByteArray()
    }


    fun orderForEndPrint(): ByteArray {
        val mSendMsg = MPMessage.MPSendMsg.newBuilder().setEventType(MPMessage.EventType.PRINTINEND).build()
        Log.d(TAG,"发送结束打印的命令${mSendMsg.toString()}===命令长度${Upacker.frameEncode(mSendMsg.toByteArray()).size}")
        return mSendMsg.toByteArray()
    }


    //打印自检页
    fun orderForGetFmSelfcheckingPage(): ByteArray {
        val mSendMsg = MPMessage.MPSendMsg.newBuilder().setEventType(MPMessage.EventType.SELFTEST).build()
        Log.d(TAG,"打印自检页的命令${mSendMsg.toString()}")
        return mSendMsg.toByteArray()
    }

    /**
     *  设置关机时间:意思就是设置多少分钟后关机
     *  设置的时间
     *  0:永远不关机
     *  其他: 根据传入的数值设定,单位 :分钟
     */
    fun orderForGetFmSetShutdownTime(min: Int = 0): ByteArray {
        val mSendMsg =
            MPMessage.MPSendMsg.newBuilder().setEventType(MPMessage.EventType.CLOSETIME).setSendInt(min)
                .build()
        Log.d(TAG,"设置关机时间的命令${mSendMsg.toString()}")
        return mSendMsg.toByteArray()
    }

    /**
     *设备取消打印
     */
    fun orderForGetFmCancelPrinter(): ByteArray {

        val mSendMsg = MPMessage.MPSendMsg.newBuilder().setEventType(MPMessage.EventType.CANCELPRINTING).build()
        Log.d(TAG,"设备取消打印的命令${mSendMsg.toString()}")
        return mSendMsg.toByteArray()
    }

    /**
     * 设置打印速度
     * @param speed (设置速度 1- 4)  2寸1到4 4寸的1到8
     *
     */
    fun orderForGetFmSetPrintSpeed(speed: Int,printer: SyzPrinter): ByteArray {
        val speedTarget=if (printer==SyzPrinter.SYZTWOINCH){
            4
        }else{
            8
        }
        return if (speed in 1..speedTarget) {
            val mSendMsg = MPMessage.MPSendMsg.newBuilder().setEventType(MPMessage.EventType.PRINTINGSPEED)
                .setSendInt(speed).build()
            Log.d(TAG,"设置打印速度的命令${mSendMsg.toString()}")
             mSendMsg.toByteArray()
        } else {
            val mySpeed = if (speed < 1) {
                1
            } else {
                speedTarget
            }
            val mSendMsg = MPMessage.MPSendMsg.newBuilder().setEventType(MPMessage.EventType.PRINTINGSPEED)
                .setSendInt(mySpeed).build()
            Log.d(TAG,"设置打印速度的命令${mSendMsg.toString()}")
             mSendMsg.toByteArray()
        }
    }

    /**
     * 设置打印浓度
     * @param concentration 1到8 四寸的1..16
     */

    fun orderForGetFmSetPrintConcentration(
        concentration: Int,printer: SyzPrinter
    ): ByteArray {
        val concentrationTarget=if (printer==SyzPrinter.SYZTWOINCH){
            8
        }else{
            16
        }
        return if (concentration in 1..concentrationTarget) {
            val mSendMsg =
                MPMessage.MPSendMsg.newBuilder().setEventType(MPMessage.EventType.PRINTINCONCENTRATION)
                    .setSendInt(
                        concentration
                    ).build()
            Log.d(TAG,"设置打印浓度的命令${mSendMsg.toString()}")
            mSendMsg.toByteArray()
        } else {
            val myConcentration = if (concentration < 1) {
                1
            } else {
                concentrationTarget
            }
            val mSendMsg = MPMessage.MPSendMsg.newBuilder().setEventType(MPMessage.EventType.PRINTINGSPEED).setSendInt(myConcentration).build()
            Log.d(TAG,"设置打印浓度的命令${mSendMsg.toString()}")
            mSendMsg.toByteArray()
        }
    }


}