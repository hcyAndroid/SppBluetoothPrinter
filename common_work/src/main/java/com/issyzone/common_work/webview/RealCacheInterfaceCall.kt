package com.issyzone.common_work.webview

import android.content.Context

class RealCacheInterfaceCall(private val context: Context?, private val cacheRequest: CacheRequest) : ICall {
    override fun call(): WebResource? {
        val cacheInterceptorList = ArrayList<ICacheInterceptor>()
        cacheInterceptorList.add(MemoryCacheIntercept())
        cacheInterceptorList.add(DiskCacheInterceptor(context))
        cacheInterceptorList.add(NetworkCacheInterceptor(context))
        val realCacheInterfaceChain = RealCacheInterfaceChain(cacheInterceptorList, 0, cacheRequest)
        return realCacheInterfaceChain.process(cacheRequest)
    }

}