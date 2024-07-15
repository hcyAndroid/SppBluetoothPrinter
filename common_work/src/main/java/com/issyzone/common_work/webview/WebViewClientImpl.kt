package com.issyzone.common_work.webview

import android.webkit.WebView

internal class WebViewClientImpl(webViewClientCallback: WebViewClientCallback?) :
    AbstractWebViewClient(webViewClientCallback) {
    override fun initWebClient(webView: WebView) {
        val webViewClient = WebViewClientImpl(webViewClientCallback)
        webView.webViewClient = webViewClient
    }
}