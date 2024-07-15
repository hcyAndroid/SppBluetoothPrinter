package com.issyzone.common_work.webview

import android.net.Uri

interface HandleUrlParamsCallback<T : WebViewEvent> {
    fun handleUrlParamsCallback(uri: Uri?, path: String?): T
}