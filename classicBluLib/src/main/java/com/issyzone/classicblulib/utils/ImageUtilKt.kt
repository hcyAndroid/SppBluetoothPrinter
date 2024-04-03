package com.issyzone.classicblulib.utils


import android.graphics.Bitmap
import android.graphics.Color


object ImageUtilKt {

    fun convertBinary(bitmap: Bitmap, threshold: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        var color: Int
        var r: Int
        var g: Int
        var b: Int
        var a: Int
        val bmp = Bitmap.createBitmap(
            width, height, Bitmap.Config.ARGB_8888
        ) //创建一个图片对象
        val oldPx = IntArray(width * height)
        val newPx = IntArray(width * height)
        bitmap.getPixels(oldPx, 0, width, 0, 0, width, height) //获取图片的颜色像素
        for (j in 0 until width * height) {
            //获取单个颜色的argb数据
            color = oldPx[j]
            r = Color.red(color)
            g = Color.green(color)
            b = Color.blue(color)
            a = Color.alpha(color)
            //计算单点的灰度值
            var gray = (r.toFloat() * 0.3 + g.toFloat() * 0.59 + b.toFloat() * 0.11).toInt()
            //根据阈值对比，低于的设置为黑色，高于的设置为白色
            gray = if (gray < threshold) {
                0
            } else {
                255
            }
            newPx[j] = Color.argb(a, gray, gray, gray)
        }
        bmp.setPixels(newPx, 0, width, 0, 0, width, height)
        return bmp
    }
}