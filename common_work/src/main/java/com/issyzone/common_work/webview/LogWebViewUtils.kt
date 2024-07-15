package com.issyzone.common_work.webview

import android.util.Log
import org.koin.android.BuildConfig

object LogWebViewUtils {
    private const val TAG = "SyzWebView"
    @JvmStatic
    fun e(message: String) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, message)
        }
    }
    @JvmStatic
    fun i(message: String) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, message)
        }
    }
}