package com.issyzone.common_work.webview

import android.content.Context
import android.content.MutableContextWrapper
import android.view.ViewGroup
import java.util.LinkedList


internal class WebViewPool private constructor() {
    private lateinit var mUserAgent: String
    private lateinit var mWebViewPool: LinkedList<SyzWebView?>
    lateinit var mParams: WebViewController.WebViewParams

    companion object {
        private var WEB_VIEW_COUNT = 3
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            WebViewPool()
        }

    }

    /**
     * 初始化WebView池
     */
    fun initWebViewPool(params: WebViewController.WebViewParams?) {
        if (params == null) return
        WEB_VIEW_COUNT = params.mWebViewCount
        mWebViewPool = LinkedList()
        mUserAgent = params.userAgent
        mParams = params
        for (i in 0 until WEB_VIEW_COUNT) {
            mWebViewPool.add(createWebView(params, mUserAgent))
        }
        registerEntities(params)
    }

    private fun registerEntities(params: WebViewController.WebViewParams) {
        params.mEntities?.let { WebViewEventManager.instance.registerEntities(*it) }
    }

    /**
     * 获取webView
     */
    fun getWebView(context: Context?): SyzWebView? {
        if (!::mWebViewPool.isInitialized) {
            return null
        }
        for (i in 0 until WEB_VIEW_COUNT) {
            if (mWebViewPool[i] != null) {
                val webView = mWebViewPool[i]
                val contextWrapper = webView?.context as MutableContextWrapper?
                contextWrapper?.baseContext = context
                mWebViewPool.remove()
                return webView
            }
        }
        return null
    }

    /**
     * Activity销毁时需要释放当前WebView
     */
    fun releaseWebView(webView: SyzWebView?) {
        LogWebViewUtils.e("释放当前WebView:$webView")
        webView?.apply {
            stopLoading()
            removeAllViews()
            clearHistory()
            clearCache(true)
            destroy()
            (parent as ViewGroup?)?.removeView(this)
            if (!::mWebViewPool.isInitialized) {
                return
            }
            if (mWebViewPool.size < WEB_VIEW_COUNT) {
                mWebViewPool.add(createWebView(mParams, mUserAgent))
            }
        }

    }

    private fun createWebView(
        params: WebViewController.WebViewParams, userAgent: String,
    ): SyzWebView {
        val webView = SyzWebView(MutableContextWrapper(params.application))
        LogWebViewUtils.e("创建webView:$webView")
        webView.setWebViewParams(params)
        params.apply {
            mWebViewSetting.initWebViewSetting(webView, userAgent)
            WebViewClientImpl(params.mWebViewClientCallback).initWebClient(webView)
            WebViewChromeClientImpl(params.mWebViewChromeClientCallback)
                .initWebChromeClient(webView)
        }
        return webView
    }

}