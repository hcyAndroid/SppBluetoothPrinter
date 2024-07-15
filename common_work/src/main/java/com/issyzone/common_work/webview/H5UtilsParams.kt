package com.issyzone.common_work.webview

import android.view.View
import android.webkit.WebSettings

class H5UtilsParams private constructor() {
    var preLoadUrl: String = ""
    var mCacheMode: Int = WebSettings.LOAD_NO_CACHE
    var updateToolBarBar: ((String, BaseWebViewActivity?) -> Unit)? = null
    var isShowToolBar: Boolean = true
    var updateStatusBar: ((String, BaseWebViewActivity?) -> Unit)? = null
    var mLoadingWebViewState: LoadingWebViewState? = null
    var mLoadingViewConfig: LoadingViewConfig? = null
    var mHandleUrlParamsCallback: HandleUrlParamsCallback<out WebViewEvent>? = null
    var mHeadContentView: View? = null
    var mHeadContentViewId: Int = 0
    var mHeadViewBlock: ((View) -> Unit)? = null
    var mExecuteJsPair: Triple<String, String, ((SyzWebView?, WebViewFragment?) -> Unit)?>? = null
    var mCommonWeResourceResponsePair: Triple<String, String, ((String) -> Boolean)?>? = null

    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            H5UtilsParams()
        }
    }

    fun clear() {
        updateToolBarBar = null
        isShowToolBar = true
        updateToolBarBar = null
        mLoadingViewConfig = null
        mLoadingWebViewState = null
        mCacheMode = WebSettings.LOAD_DEFAULT
        mExecuteJsPair = null
    }
}