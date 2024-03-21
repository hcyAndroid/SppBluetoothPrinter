package com.issyzone.blelibs.utils

import android.app.Application
import com.orhanobut.logger.Logger

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
                Logger.e(TAG, "反射application失败")
            }
        }
        return applications!!
    }
}