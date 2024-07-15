package com.issyzone.syzbleprinter.adapter
import android.bluetooth.BluetoothDevice
import androidx.paging.PagingSource
import androidx.paging.PagingState
import java.io.IOException

private const val STARTING_PAGE_INDEX = 1
class BlueScanPagingSource():PagingSource<Int, BluetoothDevice>() {
    override fun getRefreshKey(state: PagingState<Int, BluetoothDevice>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, BluetoothDevice> {
        val position = params.key ?: STARTING_PAGE_INDEX
        return try {
//            val response = service.searchRepos(apiQuery, position, params.loadSize)
//            val repos = response.items
            LoadResult.Page(
                data = emptyList(),
                prevKey = if (position == STARTING_PAGE_INDEX) null else position - 1,
                nextKey = null
            )
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: Exception) {
            return LoadResult.Error(exception)
        }
    }
}