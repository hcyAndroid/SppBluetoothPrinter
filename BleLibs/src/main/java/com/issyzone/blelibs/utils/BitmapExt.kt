package com.issyzone.blelibs.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.issyzone.blelibs.R
import com.orhanobut.logger.Logger

object BitmapExt {
    fun test(drawableId: Int = R.drawable.test01): Bitmap {
        val drawable = AppGlobels.getApplication().getDrawable(drawableId)
        Logger.d(
            "bitmap===宽${drawable?.intrinsicWidth}===高==${
                drawable?.intrinsicHeight
            }"
        )
        return BitmapFactory.decodeResource(AppGlobels.getApplication().resources, drawableId)
    }

    fun splitByteArray(input: ByteArray, chunkSize: Int): List<ByteArray> {
        return input.toList().chunked(chunkSize).map { it.toByteArray() }
    }


}