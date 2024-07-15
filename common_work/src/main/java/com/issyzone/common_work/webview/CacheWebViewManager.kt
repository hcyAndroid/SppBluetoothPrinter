package com.issyzone.common_work.webview

import android.content.Context
import android.os.Looper
import android.os.MessageQueue

class CacheWebViewManager private constructor() {

    private var mCacheConfig: CacheConfig? = null

    companion object {
        @JvmStatic
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            CacheWebViewManager()
        }

    }


    private var webViewPool = WebViewPool.instance

    fun getWebView(context: Context?): SyzWebView? {
        return webViewPool.getWebView(context)
    }

    fun releaseWebView(webView: SyzWebView?) {
        return webViewPool.releaseWebView(webView)
    }

    fun setCacheConfig(cacheConfig: CacheConfig): CacheWebViewManager {
        this.mCacheConfig = cacheConfig
        return this
    }

    fun getVersion(): Int {
        return mCacheConfig?.getVersion() ?: 0
    }

    fun clearDiskCache():CacheWebViewManager{
        mCacheConfig?.clearDiskCache(true)
        return this
    }
    fun getCacheConfig(): CacheConfig? {
        return mCacheConfig
    }

    /**
     * 预加载
     */
    fun preLoadUrl(context: Context?, url: String) {
        Looper.myQueue().addIdleHandler(object : MessageQueue.IdleHandler {
            override fun queueIdle(): Boolean {
                val webView = getWebView(context) ?: return true
                webView.preLoadUrl(url)
                WebViewPool.instance.releaseWebView(webView)
                return false
            }
        })
    }
}