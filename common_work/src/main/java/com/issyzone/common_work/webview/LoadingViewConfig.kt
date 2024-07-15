package com.issyzone.common_work.webview

import android.content.Context
import android.view.View

interface LoadingViewConfig {

    fun isShowLoading(): Boolean
    fun getLoadingView(context: Context): View?
    fun hideLoading()
    fun showLoading(context: Context?)
    fun setProgress(progress: Int){

    }
    fun onDestroy()
}