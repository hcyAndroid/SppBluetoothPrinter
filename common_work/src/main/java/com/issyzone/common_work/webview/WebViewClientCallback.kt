package com.issyzone.common_work.webview

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView

interface WebViewClientCallback {
    fun onPageStarted(view: WebView, url: String, fragment: WebViewFragment?)
    fun onPageFinished(view: WebView, url: String, fragment: WebViewFragment?)
    fun shouldOverrideUrlLoading(view: WebView, url: String,fragment: WebViewFragment?): Boolean?
    fun onReceivedError(view: WebView?, err: Int, des: String?, url: String?,fragment: WebViewFragment?)
    fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest): WebResourceResponse?
}