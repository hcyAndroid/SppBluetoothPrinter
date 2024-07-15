package com.issyzone.common_work.webview
import android.webkit.WebView

internal class WebViewChromeClientImpl(webViewChromeClientCallback: WebViewChromeClientCallback?) :
    AbstractWebViewChromeClient(webViewChromeClientCallback) {
    override fun initWebChromeClient(webView: WebView) {
        webView.webChromeClient = WebViewChromeClientImpl(webViewChromeClientCallback)
    }

}