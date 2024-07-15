package com.issyzone.common_work.webview

import android.content.Context
import android.webkit.WebView

open class WebViewEvent(
    var webView: WebView? = null,
    var context: Context? = null
)