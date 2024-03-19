package com.issyzone.syzbleprinter


import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.issyzone.blelibs.data.BleDevice
class ScanBlePageSource(var mutableList: MutableList<BleDevice>) : PagingSource<Int, BleDevice>() {




    override fun getRefreshKey(state: PagingState<Int, BleDevice>): Int? {
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, BleDevice> {
        return try {
            val page = params.key ?: 1
           // val pageSize = params.loadSize
           // val repoResponse = apiService.searRepos(page, pageSize)
            //val repoItems = repoResponse.items
            val prevKey = if (page > 1) page - 1 else null
           // val nextKey = if (repoItems.isNotEmpty()) page + 1 else null
            LoadResult.Page(mutableList, prevKey, null)
        } catch (e: Exception) {
            LoadResult.Error(e)
        }

    }
}