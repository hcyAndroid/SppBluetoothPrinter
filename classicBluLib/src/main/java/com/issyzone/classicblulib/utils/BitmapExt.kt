package com.issyzone.classicblulib.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.issyzone.classicblulib.R

import java.io.ByteArrayOutputStream

object BitmapExt {
    fun test(drawableId: Int): Bitmap {
        val drawable = AppGlobels.getApplication().getDrawable(drawableId)
//        Logger.d(
//            "bitmap===宽${drawable?.intrinsicWidth}===高==${
//                drawable?.intrinsicHeight
//            }"
//        )
        val bitmap = BitmapFactory.decodeResource(AppGlobels.getApplication().resources, drawableId)

      //  Logger.d("bitmap Draable大小》》》${bitmapToByteArray(bitmap).size}")
        return BitmapFactory.decodeResource(AppGlobels.getApplication().resources, drawableId)
    }

    fun decodeBitmap(drawableId: Int ): Bitmap {
        // 通过资源 ID 获取原始图片的字节数组
        val inputStream = AppGlobels.getApplication().resources.openRawResource(drawableId)
        val byteArray = inputStream.readBytes()

        // 通过原始图片的字节数组创建 Bitmap 对象
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    fun splitByteArray(input: ByteArray, chunkSize: Int): List<ByteArray> {
        return input.toList().chunked(chunkSize).map { it.toByteArray() }
    }

    fun bitmapToByteArray(
        bitmap: Bitmap,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        quality: Int = 100
    ): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(format, quality, outputStream)
        return outputStream.toByteArray()
    }


}