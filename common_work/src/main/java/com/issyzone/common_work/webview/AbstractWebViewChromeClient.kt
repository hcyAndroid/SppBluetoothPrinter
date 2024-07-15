package com.issyzone.common_work.webview

import android.webkit.WebChromeClient
import android.webkit.WebView

/**
 * author ：Peakmain
 * createTime：2023/04/07
 * mail:2726449200@qq.com
 * describe：
 */
abstract class AbstractWebViewChromeClient(val webViewChromeClientCallback: WebViewChromeClientCallback?) :
    WebChromeClient() {
    private var fragment: WebViewFragment? = null
    abstract fun initWebChromeClient(webView: WebView)
    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        if (fragment == null) {
            fragment = WebViewManager.instance.getFragment()
        }
        fragment?.onProgressChanged(view, newProgress)
        webViewChromeClientCallback?.onProgressChanged(view, newProgress, fragment)
    }

    override fun onReceivedTitle(view: WebView?, title: String) {
        if (fragment == null) {
            fragment = WebViewManager.instance.getFragment()
        }
        fragment?.onReceivedTitle(view, title)
        webViewChromeClientCallback?.onReceivedTitle(view, title, fragment)
    }

}