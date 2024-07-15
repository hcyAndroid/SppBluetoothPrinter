package com.issyzone.common_work.webview
import android.util.LruCache
class MemoryCacheIntercept : ICacheInterceptor {
    private var mLruCache: LruCache<String, WebResource>? = null

    init {
        val size = MemorySizeCalculator.instance.getSize()
        if (size > 0) {
            mLruCache = ResourceMemoryCache(size)
        }
    }

    override fun cacheInterceptor(chain: ICacheInterceptor.Chain): WebResource? {
        val request = chain.request()


        mLruCache?.let {
            val resource = it.get(request.key)
            if (checkResourceValid(resource)) {
                LogWebViewUtils.i("读取内存缓存:${request.url}")
                return resource
            }
        }
        val resource = chain.process(request)
        //内存缓存资源
        if (mLruCache != null && checkResourceValid(resource) && resource?.isCacheable == true){
            LogWebViewUtils.i("内存缓存缓存数据:${request.url}")
            mLruCache?.put(request.key, resource)
        }
        return resource
    }

    private fun checkResourceValid(resource: WebResource?): Boolean {
        if (resource == null) return false
        return resource.originBytes != null && resource.originBytes.isNotEmpty()
                && resource.responseHeaders != null
                && resource.responseHeaders.isNotEmpty()
    }

    class ResourceMemoryCache constructor(maxSize: Int) : LruCache<String, WebResource>(maxSize) {
        override fun sizeOf(key: String?, value: WebResource?): Int {
            return value?.originBytes?.size ?: 0
        }
    }

}