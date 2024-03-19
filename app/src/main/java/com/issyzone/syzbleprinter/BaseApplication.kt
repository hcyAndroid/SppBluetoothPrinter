package com.issyzone.syzbleprinter

import android.app.Application
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger

class BaseApplication:Application() {
    override fun onCreate() {
        super.onCreate()
        //Logger.addLogAdapter(AndroidLogAdapter())
        Logger.addLogAdapter(AndroidLogAdapter())
    }
}