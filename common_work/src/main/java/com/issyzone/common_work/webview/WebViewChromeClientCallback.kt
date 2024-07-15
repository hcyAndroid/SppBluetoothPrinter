package com.issyzone.common_work.webview

import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebView


interface  WebViewChromeClientCallback {
    fun onReceivedTitle(view: WebView?, title: String?, fragment: WebViewFragment?)
    fun openFileInput(
        fileUploadCallbackFirst: ValueCallback<Uri>?,
        fileUploadCallbackSecond: ValueCallback<Array<Uri>>?,
        acceptType: String?
    )
    fun onProgressChanged(view: WebView?, newProgress: Int, fragment: WebViewFragment?)
}