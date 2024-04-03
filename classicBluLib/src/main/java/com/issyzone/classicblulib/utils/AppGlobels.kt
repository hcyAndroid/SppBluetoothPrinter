package com.issyzone.classicblulib.utils

import android.app.Application
import android.util.Log


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
}