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

    /**
     * 亮度分界二值化算法
     * 使用了Bitmap.getPixel(i, j)获取像素值，这可能会导致性能问题，因为这个方法在循环中的性能较差。
     * 对于灰度值的计算，使用了(red * 38 + green * 75 + blue * 15) shr 7，这是一种降低浮点运算的方式。
     * 在二值化处理中，它提供了一个isReverse参数，允许用户反转黑白色。
     */
    fun grayBitmap2BinaryBitmap(graymap: Bitmap, isReverse: Boolean=false,threshold: Int=128): Bitmap {
        //得到图形的宽度和长度
        val width = graymap.width
        val height = graymap.height
        //创建二值化图像
        val binarymap = graymap.copy(Bitmap.Config.ARGB_8888, true)
        //依次循环，对图像的像素进行处理
        for (i in 0 until width) {
            for (j in 0 until height) {
                //得到当前像素的值
                val col = binarymap.getPixel(i, j)
                //得到alpha通道的值
                val alpha = Color.alpha(col)
                //得到图像的像素RGB的值
                val red = Color.red(col)
                val green = Color.green(col)
                val blue = Color.blue(col)
                // 用公式X = 0.3×R+0.59×G+0.11×B计算出X代替原来的RGB
                //val gray = (red * 0.3 + green * 0.59 + blue * 0.11).toInt()
                val gray = (red * 38 + green * 75 + blue * 15) shr 7  //降低浮点运算

                //对图像进行二值化处理
                val newGray = if (gray > threshold) {
                    if (isReverse) Color.BLACK else Color.WHITE
                } else {
                    if (isReverse) Color.WHITE else Color.BLACK
                }
                //设置新图像的当前像素值
                binarymap.setPixel(i, j, newGray)
            }
        }
        return binarymap
    }


    /**
     * 使用了Bitmap.getPixels(oldPx, 0, width, 0, 0, width, height)获取所有像素值，这在循环中的性能更好。
     * 对于灰度值的计算，使用了(r.toFloat() * 0.3 + g.toFloat() * 0.59 + b.toFloat() * 0.11).toInt()，这是更常见的计算灰度值的方式。
     * 二值化处理也提供了一个isReverse参数，允许用户反转黑白色。
     */


    fun convertBinary(bitmap: Bitmap, threshold: Int=128,isReverse:Boolean=false): Bitmap {
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
                if (isReverse) Color.WHITE else Color.BLACK

            } else {
                if (isReverse) Color.BLACK else Color.WHITE
            }
            newPx[j] = Color.argb(a, gray, gray, gray)
        }
        bmp.setPixels(newPx, 0, width, 0, 0, width, height)
        return bmp
    }

    /**
     * 平均值二值化算法
     */
    fun grayAverageBitmap2BinaryBitmap(srcBitmap: Bitmap,isReverse:Boolean=false): Bitmap? {
        val width = srcBitmap.width
        val height = srcBitmap.height

        val pixel_total = width * height  // 像素总数
        if (pixel_total == 0) return null

        val bitmap = srcBitmap.copy(Bitmap.Config.ARGB_8888, true)

        var sum: Long = 0  // 总灰度
        var threshold: Int   // 阈值

        for (i in 0 until pixel_total) {

            val x = i % width
            val y = i / width

            val pixel = bitmap.getPixel(x, y)

            // 分离三原色及透明度
            val alpha = Color.alpha(pixel)
            var red = Color.red(pixel)
            var green = Color.green(pixel)
            var blue = Color.blue(pixel)

            var gray = (red * 38 + green * 75 + blue * 15) shr 7
            if (alpha == 0 && gray == 0) {
                gray = 0xFF
            }

            if (gray > 0xFF) {
                gray = 0xFF
            }
            bitmap.setPixel(x, y, gray or -0x100)
            sum += gray.toLong()
        }
        // 计算平均灰度
        threshold = (sum / pixel_total).toInt()

        for (i in 0 until pixel_total) {

            val x = i % width
            val y = i / width
            val pixel = bitmap.getPixel(x, y) and 0x000000FF
            val color = if (pixel < threshold) {
                if (isReverse) Color.WHITE else Color.BLACK
            } else {
                if (isReverse) Color.BLACK else Color.WHITE

            }
            bitmap.setPixel(x, y, color)
        }
        return bitmap
    }



    fun bitmap2OTSUBitmap(srcBitmap: Bitmap,isReverse:Boolean=false): Bitmap?{
        val width = srcBitmap.width
        val height = srcBitmap.height

        val pixelTotal = width * height.toLong() // 总像素数量
        if (pixelTotal == 0L) return null

        val bitmap = srcBitmap.copy(Bitmap.Config.ARGB_8888, true)

        var sum1 = 0L   // 总灰度值
        var sumB = 0L   // 背景总灰度值
        var wB = 0.0 // 背景像素点比例
        var wF = 0.0 // 前景像素点比例
        var mB = 0.0 // 背景平均灰度值
        var mF = 0.0 // 前景平均灰度值
        var maxG = 0.0 // 最大类间方差
        var g = 0.0 // 类间方差
        var threshold = 0 // 阈值
        val histogram = DoubleArray(256)

        for (i in 0 until pixelTotal) {
            val x = (i % width).toInt()
            val y = (i / width).toInt()
            val pixel = bitmap.getPixel(x, y)
            // 分离三原色及透明度
            val alpha = Color.alpha(pixel)
            val red = Color.red(pixel)
            val green = Color.green(pixel)
            val blue = Color.blue(pixel)

            var gray = (red * 38 + green * 75 + blue * 15).ushr(7)
            if (alpha == 0 && gray == 0) {
                gray = 0xFF
            }
            if (gray > 0xFF) {
                gray = 0xFF
            }
            bitmap.setPixel(x, y, gray or -0x100)

            histogram[gray]++
            sum1 += gray
        }

        for (i in 0..255) {
            wB += histogram[i]
            wF = pixelTotal - wB
            if (wB == 0.0 || wF == 0.0) {
                continue
            }
            sumB += (i * histogram[i]).toLong()
            mB = sumB / wB
            mF = (sum1 - sumB) / wF
            g = wB * wF * (mB - mF) * (mB - mF)
            if (g >= maxG) {
                threshold = i
                maxG = g
            }
        }

        for (i in 0 until pixelTotal) {
            val x = (i % width).toInt()
            val y = (i / width).toInt()
            val pixel = bitmap.getPixel(x, y) and 0x000000FF
            val color = if (pixel < threshold) {
                if (isReverse) Color.WHITE else Color.BLACK

            } else {
                if (isReverse) Color.BLACK else Color.WHITE
            }
            bitmap.setPixel(x, y, color)
        }
        return bitmap
    }





}