package com.issyzone.syzbleprinter

import android.app.Application
import com.issyzone.common_work.utils.MyCrashHandler
import com.issyzone.common_work.utils.MyCrashUtils
import com.issyzone.syzbleprinter.koin_test.normalModule
import com.issyzone.syzbleprinter.koin_test.singleModule
import com.issyzone.syzbleprinter.koin_test.viewModule
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin


class BaseApplication:Application() {

    // 创建一个协程作用域
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        //Logger.addLogAdapter(AndroidLogAdapter())
        startKoin {
            androidLogger()
            androidContext(this@BaseApplication)
            modules(mutableListOf(normalModule, singleModule,viewModule))
        }
       // MyCrashUtils.initCrashHandler(this@BaseApplication,"7cf71f9e75")


        Logger.addLogAdapter(AndroidLogAdapter())
    }
}