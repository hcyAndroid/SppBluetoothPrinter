package com.issyzone.classicblulib.utils

import android.app.Application
import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.WindowManager


private var applications: Application? = null

object AppGlobels {
    private const val TAG = "AppGlobels>>>"
    //反射获取application
    fun getApplication(): Application {
        if (applications == null) {
            kotlin.runCatching {
                applications =
                    Class.forName("android.app.ActivityThread").getMethod("currentApplication")
                        .invoke(null, *emptyArray()) as Application
            }.onFailure {
                it.printStackTrace()
                Log.e(TAG, "反射application失败")
            }
        }
        return applications!!
    }


    /**
     * 获取屏幕分辨率的方法。
     * @param context 上下文。
     * @return 返回一个 Pair，第一个元素是屏幕宽度(px)，第二个元素是屏幕高度(px)。
     */
    fun getScreenResolution(): Pair<Int, Int> {
        val windowManager = getApplication().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        Log.i(TAG,"当前屏幕分辨率===${width}px=====${height}px===dpi==${getDpi()}===${getApplication().resources.displayMetrics.densityDpi}")

        Log.i(TAG,"当前屏幕分辨率222===${calculateDpFromDpm(width)}px=====${calculateDpFromDpm(height)}px")


        //Log.i(TAG,"当前屏幕分辨率===${pxToDp(width.toFloat())}dp=====${pxToDp(height.toFloat())}dp")
        return Pair(width, height)
    }
    fun getPpi(): Float {
        val metrics = getApplication().resources.displayMetrics
        val ppi=metrics.densityDpi.toFloat()
        Log.i(TAG,"当前屏幕ppi===${ppi}")
        return ppi
    }

    fun getDpi():Int{
        val displayMetrics = DisplayMetrics()
        val windowManager = getApplication().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val dpi= displayMetrics.densityDpi
       // Log.i(TAG,"当前屏幕dpi===${dpi}")
        return  dpi
    }

    fun calculateDpFromDpm(widthPx: Int): Int {
        val dpi = getDpi() * 25.4f // Convert dpm to dpi
        val densityRatio = dpi / 160f
        return (widthPx / getApplication().resources.displayMetrics.density).toInt()
    }

    fun calculateDpFromPx(px: Int, dpi: Int): Double {
        return px / (dpi / 160.0)
    }

//    fun  getScreenInch(){
//        //屏幕尺寸（dp）= 屏幕尺寸（px）/ 设备密度（dpi/160）
//         val deviceSity= getApplication().resources.displayMetrics.densityDpi/160
//         val width= getScreenResolution().first/deviceSity
//        val height= getScreenResolution().second/deviceSity
//        Log.i(TAG,"当前屏幕尺寸===${width}=====${height}")
//    }



    /**
     * 将像素(px)转换为设备独立像素(dp)。
     * @param context 上下文，用于获取屏幕密度。
     * @param px 像素值(px)。
     * @return 转换后的设备独立像素(dp)。
     */
    fun pxToDp( px: Float): Float {
        Log.i(TAG,"当前屏幕Dpi:::${getApplication().resources.displayMetrics.density}")
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_PX,
            px,
            getApplication().resources.displayMetrics
        ) /  getApplication().resources.displayMetrics.density
    }
}