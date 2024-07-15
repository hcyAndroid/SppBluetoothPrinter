package com.issyzone.common_work.webview

import android.webkit.WebView

interface InitWebViewSetting {
    fun initWebViewSetting(webView: WebView, userAgent: String? = null)
}