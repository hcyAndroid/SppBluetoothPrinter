package com.issyzone.blelibs.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import com.issyzone.blelibs.R
import java.io.ByteArrayOutputStream


object BitmapExt {
    fun test(drawableId: Int = R.drawable.test3): Bitmap {
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

    fun decodeBitmap(drawableId: Int = R.drawable.test5): Bitmap {
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

    /**
     * BitmapFactory.Options类用于控制解码图片时的行为。其中的inSampleSize是一个非常重要的字段，它可以用来让BitmapFactory在解码过程中对图片进行下采样（减小解析度和内存占用）。
     * inSampleSize可以定义为一个正整数，值越大，说明越多的像素将被过滤掉，最终Bitmap的宽度和高度将分别为原图片的1/inSampleSize比例。这个选项只能接受2的幂（例如，1、2、4、8、16...）作为值。比如：
     * 如果 inSampleSize 设为 1，则位图将会解码成原始图片大小。
     * 如果 inSampleSize 设为 2，则位图宽度和高度都将是原始图片的1/2，为原始像素数的1/4。
     * 如果 inSampleSize 设为 4，则位图宽度和高度都将是原始图片的1/4，为原始像素数的1/16。
     * 使用较高的inSampleSize值可以显著减小图片的尺寸，以及解码过程中需要的内存，这对处理高分辨率的图片时非常有用，可以避免OutOfMemoryError。
     * 而inDensity和inTargetDensity是BitmapFactory.Options的另外两个字段，它们通常用于处理图片在不同屏幕密度（DPI）上的显示问题：
     * inDensity：表示位图的像素密度。
     * inTargetDensity：屏幕的像素密度。
     * Nearest Neighbour Resampling（邻近采样）
     */

    fun bitmapCompress(id: Int, inSampleSize: Int = 2): Bitmap {
        val options = BitmapFactory.Options()
        //或者 inDensity 搭配 inTargetDensity 使用，算法和 inSampleSize 一样
        //或者 inDensity 搭配 inTargetDensity 使用，算法和 inSampleSize 一样
        options.inSampleSize = inSampleSize
        // val bitmap = BitmapFactory.decodeFile("/sdcard/test.png")
        return BitmapFactory.decodeResource(AppGlobels.getApplication().resources, id, options)
    }

    /**
     * 双线性采样（Bilinear Resampling）
     * 邻近采样字的显示失真对比双线性采样来说要严重很多
     */
    fun bitmapCompress2(id: Int): Bitmap {
        val bitmap = decodeBitmap(id)
        val compress =
            Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 2, bitmap.getHeight() / 2, true);
        return compress

    }

    /**
     * 双线性采样（Bilinear Resampling）
     * 邻近采样字的显示失真对比双线性采样来说要严重很多
     */
    fun bitmapCompress3(id: Int): Bitmap {
        val bitmap = decodeBitmap(id)
        val matrix = Matrix();
        matrix.setScale(0.5f, 0.5f);
        return Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true
        )
    }





}