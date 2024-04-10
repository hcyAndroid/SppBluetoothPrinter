package com.issyzone.blelibs.utils


import android.graphics.Bitmap
import android.graphics.Color


object ImageUtilKt {
    //抖动
    fun convertGreyImgByFloyd(img: Bitmap): Bitmap {
        val width = img.width         //获取位图的宽
        val height = img.height       //获取位图的高
        val pixels = IntArray(width * height) //通过位图的大小创建像素点数组
        img.getPixels(pixels, 0, width, 0, 0, width, height)
        val gray = IntArray(height * width)
        for (i in 0 until height) {
            for (j in 0 until width) {
                val grey = pixels[width * i + j]
                val red = grey and 0x00FF0000 shr 16
                gray[width * i + j] = red
            }
        }
        var e = 0
        for (i in 0 until height) {
            for (j in 0 until width) {
                val g = gray[width * i + j]
                if (g >= 200) {
                    pixels[width * i + j] = -0x1
                    e = g - 255

                } else {
                    pixels[width * i + j] = -0x1000000
                    e = g - 0
                }
                if (j < width - 1 && i < height - 1) {
                    //右边像素处理
                    gray[width * i + j + 1] += 3 * e / 8
                    //下
                    gray[width * (i + 1) + j] += 3 * e / 8
                    //右下
                    gray[width * (i + 1) + j + 1] += e / 4
                } else if (j == width - 1 && i < height - 1) {//靠右或靠下边的像素的情况
                    //下方像素处理
                    gray[width * (i + 1) + j] += 3 * e / 8
                } else if (j < width - 1 && i == height - 1) {
                    //右边像素处理
                    gray[width * i + j + 1] += e / 4
                }
            }
        }
        val mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        mBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return mBitmap
    }
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