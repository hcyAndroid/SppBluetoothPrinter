package com.issyzone.common_work.webview
import android.content.Context
class NetworkCacheInterceptor(val context: Context?) : ICacheInterceptor {
    override fun cacheInterceptor(chain: ICacheInterceptor.Chain): WebResource? {
        val request = chain.request()

        val mimeType = request.mimeType
        val isCacheContentType = WebViewUtils.instance.isCacheContentType(mimeType)
        return context?.let {
            if (WebViewUtils.instance.isImageType(request.mimeType)) {
                InterceptRequestManager.instance.loadImage(context,request)
            } else{
                LogWebViewUtils.i("开始网络缓存:${request.url}")
                OKHttpManager(it).getResource(request,isCacheContentType)
            }

        }
    }
}