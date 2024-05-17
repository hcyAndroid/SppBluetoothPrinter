package com.issyzone.syzbleprinter.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.issyzone.blelibs.R
import com.issyzone.blelibs.utils.AppGlobels
import org.opencv.android.Utils
import org.opencv.core.Core

import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfFloat
import org.opencv.core.MatOfInt
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.lang.Math.pow

import kotlin.math.*


object OpenCVUtils {
    fun decodeBitmap(drawableId: Int = R.drawable.test5): Bitmap {
        // 通过资源 ID 获取原始图片的字节数组
        val inputStream = AppGlobels.getApplication().resources.openRawResource(drawableId)
        val byteArray = inputStream.readBytes()

        // 通过原始图片的字节数组创建 Bitmap 对象
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    fun testOpencv(drawableId: Int = R.drawable.test5): Bitmap {
        val bitmap = decodeBitmap(drawableId)
        val src = Mat()
        val dst = Mat() // 用于存放结果的Mat对象
        val size = Size((src.width() / 2).toDouble(), (src.height() / 2).toDouble())
        Utils.bitmapToMat(bitmap, src)
        Imgproc.resize(src, dst, size, 0.0, 0.0, Imgproc.INTER_CUBIC)
        // 创建一个空的Bitmap对象，其大小和类型匹配Mat
        // 创建一个空的Bitmap对象，其大小和类型匹配Mat
        val bmp = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(dst, bmp)
        return bmp
    }


    //双三次采样
    fun testOpencv2(drawableId: Int = R.drawable.test5, width: Double, height: Double): Bitmap {
        val bitmap = decodeBitmap(drawableId)
        val src = Mat()
        Utils.bitmapToMat(bitmap, src) // 现在src包含了图像数据
        val size = Size(width, height)
        val dst = Mat(size, src.type()) // 使用新尺寸和原图像的类型创建dst
        /**
         * src: 这是一个 Mat 对象，包含了你想要调整大小的源图像。
         * dst: 这是另一个 Mat 对象，将用于存放调整大小后的输出图像。
         * 对象定义了输出图像的新尺寸。在此情况下，尺寸被设置为原始图像宽度和高度的一半。
         * Size 的构造函数接受宽度和高度作为参数
         * fx: 这是沿x轴的缩放因子。因为已经使用 size 对象指定了输出图像的尺寸，所以这里设置为0.0，表示不使用缩放因子。
         * fy: 这是沿y轴的缩放因子。同样的，因为用了 size 来确定输出图像的大小，所以这个也设置为0.0
         *  这是一个标志，指定了在调整图像尺寸时使用的插值方法。Imgproc.INTER_CUBIC 是一种插值方法，
         *  它使用了一个4x4像素邻域的双三次插值。这种方法通常比简单的最近邻插值（Imgproc.INTER_NEAREST）产生更平滑的图像，
         *  但是比其他插值方法（如 Imgproc.INTER_LINEAR 或 Imgproc.INTER_AREA）要慢一些。不过，
         *  对于缩放操作而言，Imgproc.INTER_CUBIC 为了获得更高质量的结果，在性能上的付出通常是值得的。
         */
        Imgproc.resize(src, dst, size, 0.0, 0.0, Imgproc.INTER_CUBIC)
        val bmp = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(dst, bmp)

        return bmp
    }


    fun testOpencv2(drawableId: Int = R.drawable.test5): Bitmap {
        val bitmap = decodeBitmap(drawableId)
        val src = Mat()
        Utils.bitmapToMat(bitmap, src) // 现在src包含了图像数据
        val size = Size(bitmap.width.toDouble(), bitmap.height.toDouble())
        val dst = Mat(size, src.type()) // 使用新尺寸和原图像的类型创建dst
        /**
         * src: 这是一个 Mat 对象，包含了你想要调整大小的源图像。
         * dst: 这是另一个 Mat 对象，将用于存放调整大小后的输出图像。
         * 对象定义了输出图像的新尺寸。在此情况下，尺寸被设置为原始图像宽度和高度的一半。
         * Size 的构造函数接受宽度和高度作为参数
         * fx: 这是沿x轴的缩放因子。因为已经使用 size 对象指定了输出图像的尺寸，所以这里设置为0.0，表示不使用缩放因子。
         * fy: 这是沿y轴的缩放因子。同样的，因为用了 size 来确定输出图像的大小，所以这个也设置为0.0
         *  这是一个标志，指定了在调整图像尺寸时使用的插值方法。Imgproc.INTER_CUBIC 是一种插值方法，
         *  它使用了一个4x4像素邻域的双三次插值。这种方法通常比简单的最近邻插值（Imgproc.INTER_NEAREST）产生更平滑的图像，
         *  但是比其他插值方法（如 Imgproc.INTER_LINEAR 或 Imgproc.INTER_AREA）要慢一些。不过，
         *  对于缩放操作而言，Imgproc.INTER_CUBIC 为了获得更高质量的结果，在性能上的付出通常是值得的。
         */
        Imgproc.resize(src, dst, size, 0.0, 0.0, Imgproc.INTER_CUBIC)
        val bmp = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(dst, bmp)

        return bmp
    }


    fun testOpencv3(drawableId: Int = R.drawable.test5, width: Double, height: Double): Bitmap {
        val bitmap = decodeBitmap(drawableId)
        val src = Mat()
        Utils.bitmapToMat(bitmap, src) // 现在src包含了图像数据
        val size = Size(width, height)
        val dst = Mat(size, src.type()) // 使用新尺寸和原图像的类型创建dst
        /**
         * src: 这是一个 Mat 对象，包含了你想要调整大小的源图像。
         * dst: 这是另一个 Mat 对象，将用于存放调整大小后的输出图像。
         * 对象定义了输出图像的新尺寸。在此情况下，尺寸被设置为原始图像宽度和高度的一半。
         * Size 的构造函数接受宽度和高度作为参数
         * fx: 这是沿x轴的缩放因子。因为已经使用 size 对象指定了输出图像的尺寸，所以这里设置为0.0，表示不使用缩放因子。
         * fy: 这是沿y轴的缩放因子。同样的，因为用了 size 来确定输出图像的大小，所以这个也设置为0.0
         *  这是一个标志，指定了在调整图像尺寸时使用的插值方法。Imgproc.INTER_CUBIC 是一种插值方法，
         *  它使用了一个4x4像素邻域的双三次插值。这种方法通常比简单的最近邻插值（Imgproc.INTER_NEAREST）产生更平滑的图像，
         *  但是比其他插值方法（如 Imgproc.INTER_LINEAR 或 Imgproc.INTER_AREA）要慢一些。不过，
         *  对于缩放操作而言，Imgproc.INTER_CUBIC 为了获得更高质量的结果，在性能上的付出通常是值得的。
         */
        Imgproc.resize(src, dst, size, 0.0, 0.0, Imgproc.INTER_LINEAR)
        val bmp = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(dst, bmp)

        return bmp
    }


    fun testOpencv3(drawableId: Int = R.drawable.test5): Bitmap {
        val bitmap = decodeBitmap(drawableId)
        val src = Mat()
        Utils.bitmapToMat(bitmap, src) // 现在src包含了图像数据
        val size = Size(bitmap.width.toDouble(), bitmap.height.toDouble())
        val dst = Mat(size, src.type()) // 使用新尺寸和原图像的类型创建dst
        /**
         * src: 这是一个 Mat 对象，包含了你想要调整大小的源图像。
         * dst: 这是另一个 Mat 对象，将用于存放调整大小后的输出图像。
         * 对象定义了输出图像的新尺寸。在此情况下，尺寸被设置为原始图像宽度和高度的一半。
         * Size 的构造函数接受宽度和高度作为参数
         * fx: 这是沿x轴的缩放因子。因为已经使用 size 对象指定了输出图像的尺寸，所以这里设置为0.0，表示不使用缩放因子。
         * fy: 这是沿y轴的缩放因子。同样的，因为用了 size 来确定输出图像的大小，所以这个也设置为0.0
         *  这是一个标志，指定了在调整图像尺寸时使用的插值方法。Imgproc.INTER_CUBIC 是一种插值方法，
         *  它使用了一个4x4像素邻域的双三次插值。这种方法通常比简单的最近邻插值（Imgproc.INTER_NEAREST）产生更平滑的图像，
         *  但是比其他插值方法（如 Imgproc.INTER_LINEAR 或 Imgproc.INTER_AREA）要慢一些。不过，
         *  对于缩放操作而言，Imgproc.INTER_CUBIC 为了获得更高质量的结果，在性能上的付出通常是值得的。
         */
        Imgproc.resize(src, dst, size, 0.0, 0.0, Imgproc.INTER_LINEAR)
        val bmp = Bitmap.createBitmap(dst.cols(), dst.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(dst, bmp)

        return bmp
    }


    /**
     * 固定阙值二值化
     */
    fun convertBinary(bitmap: Bitmap, threshold: Int): Bitmap {
        // 将Bitmap转换为OpenCV的Mat对象
        val src = Mat(bitmap.width, bitmap.height, CvType.CV_8UC1)
        Utils.bitmapToMat(bitmap, src)

        // 将彩色图像转换为灰度图像
        val gray = Mat()
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGB2GRAY)

        // 创建目标Mat对象用于存储二值化的图像
        val binary = Mat()

        // 应用阈值进行二值化
        Imgproc.threshold(gray, binary, threshold.toDouble(), 255.0, Imgproc.THRESH_BINARY)

        // 创建Bitmap对象，将二值化后的Mat转换回Bitmap
        val binaryBitmap =
            Bitmap.createBitmap(binary.cols(), binary.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(binary, binaryBitmap)

        // 释放资源
        src.release()
        gray.release()
        binary.release()

        return binaryBitmap
    }


    /**
     * OTSU 二值化
     */
    fun convertBinaryOtsu(srcBitmap: Bitmap): Bitmap {
        // 将 Bitmap 转换为 Mat
        val srcMat = Mat()
        Utils.bitmapToMat(srcBitmap, srcMat)

        // 创建一个新的 Mat 对象来存储结果
        val dstMat = Mat(srcMat.rows(), srcMat.cols(), srcMat.type())

        // 将源图像转换为灰度图像
        Imgproc.cvtColor(srcMat, srcMat, Imgproc.COLOR_RGB2GRAY)

        // 应用 OTSU 阈值
        Imgproc.threshold(srcMat, dstMat, 0.0, 255.0, Imgproc.THRESH_BINARY or Imgproc.THRESH_OTSU)

        // 将结果转换为 Bitmap
        val dstBitmap = Bitmap.createBitmap(dstMat.cols(), dstMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(dstMat, dstBitmap)

        return dstBitmap
    }


    /**
     * 双峰法二值化
     */
    fun convertBinaryDoublePeaks(srcBitmap: Bitmap): Bitmap {
        // 将 Bitmap 转换为 Mat
        val srcMat = Mat()
        Utils.bitmapToMat(srcBitmap, srcMat)

        // 将源图像转换为灰度图像
        Imgproc.cvtColor(srcMat, srcMat, Imgproc.COLOR_RGB2GRAY)

        // 计算图像的灰度直方图
        val hist = Mat()
        val histSize = MatOfInt(256)
        val ranges = MatOfFloat(0f, 256f)
        Imgproc.calcHist(listOf(srcMat), MatOfInt(0), Mat(), hist, histSize, ranges)

        // 找出两个最高峰
        val maxVal1 = Core.minMaxLoc(hist).maxVal
        hist.put(hist.rows() - 1, 0, maxVal1)
        val maxVal2 = Core.minMaxLoc(hist).maxVal

        // 计算最小值阈值
        val thresholdValue = if (maxVal1 > maxVal2) maxVal2 else maxVal1

        // 创建一个新的 Mat 对象来存储结果
        val dstMat = Mat(srcMat.rows(), srcMat.cols(), srcMat.type())

        // 应用阈值
        Imgproc.threshold(srcMat, dstMat, thresholdValue, 255.0, Imgproc.THRESH_BINARY)

        // 将结果转换为 Bitmap
        val dstBitmap = Bitmap.createBitmap(dstMat.cols(), dstMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(dstMat, dstBitmap)

        return dstBitmap
    }


    /**
     * 三角形法二值化
     */
    fun triangleThreshold(srcBitmap: Bitmap): Bitmap {
        // 将 Bitmap 转换为 Mat
        val srcMat = Mat()
        Utils.bitmapToMat(srcBitmap, srcMat)

        // 将源图像转换为灰度图像
        Imgproc.cvtColor(srcMat, srcMat, Imgproc.COLOR_RGB2GRAY)

        // 计算图像的灰度直方图
        val hist = Mat()
        val histSize = MatOfInt(256)
        val ranges = MatOfFloat(0f, 256f)
        Imgproc.calcHist(listOf(srcMat), MatOfInt(0), Mat(), hist, histSize, ranges)

        // 找出直方图的最高峰
        val maxLoc = Core.minMaxLoc(hist).maxLoc
        val peak = Point(maxLoc.y, maxLoc.x)

        // 找出直方图的最左端和最右端
        val leftEnd = Point(0.0, 0.0)
        val rightEnd = Point(255.0, 0.0)

        // 计算直角三角形的直角边
        val a = sqrt(pow(peak.x - leftEnd.x, 2.0) + pow(peak.y - leftEnd.y, 2.0))
        val b = sqrt(pow(peak.x - rightEnd.x, 2.0) + pow(peak.y - rightEnd.y, 2.0))
        val c = sqrt(pow(rightEnd.x - leftEnd.x, 2.0) + pow(rightEnd.y - leftEnd.y, 2.0))

        // 计算阈值
        val thresholdValue = (pow(a, 2.0) + pow(c, 2.0) - pow(b, 2.0)) / (2 * c)

        // 创建一个新的 Mat 对象来存储结果
        val dstMat = Mat(srcMat.rows(), srcMat.cols(), srcMat.type())

        // 应用阈值
        Imgproc.threshold(srcMat, dstMat, thresholdValue, 255.0, Imgproc.THRESH_BINARY)

        // 将结果转换为 Bitmap
        val dstBitmap = Bitmap.createBitmap(dstMat.cols(), dstMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(dstMat, dstBitmap)

        return dstBitmap
    }


    /**
     * 最小错误法
     */
    fun minimumErrorThreshold(srcBitmap: Bitmap): Bitmap {
        // 将 Bitmap 转换为 Mat
        val srcMat = Mat()
        Utils.bitmapToMat(srcBitmap, srcMat)

        // 将源图像转换为灰度图像
        Imgproc.cvtColor(srcMat, srcMat, Imgproc.COLOR_RGB2GRAY)

        // 计算图像的灰度直方图
        val hist = Mat()
        val histSize = MatOfInt(256)
        val ranges = MatOfFloat(0f, 256f)
        Imgproc.calcHist(listOf(srcMat), MatOfInt(0), Mat(), hist, histSize, ranges)

        // 初始化阈值和最小误差平方和
        var threshold = 0
        var minError = Double.MAX_VALUE

        // 遍历所有可能的阈值
        for (t in 0..255) {
            // 计算两个类的平均灰度值
            val mean1 = Core.mean(srcMat.submat(0, t, 0, srcMat.cols())).`val`[0]
            val mean2 = Core.mean(srcMat.submat(t, srcMat.rows(), 0, srcMat.cols())).`val`[0]

            // 计算误差平方和
            var error = 0.0
            for (i in 0 until t) {
                error += pow(i - mean1, 2.0) * hist.get(i, 0)[0]
            }
            for (i in t until 256) {
                error += pow(i - mean2, 2.0) * hist.get(i, 0)[0]
            }

            // 更新最小误差平方和和对应的阈值
            if (error < minError) {
                minError = error
                threshold = t
            }
        }

        // 创建一个新的 Mat 对象来存储结果
        val dstMat = Mat(srcMat.rows(), srcMat.cols(), srcMat.type())

        // 应用阈值
        Imgproc.threshold(srcMat, dstMat, threshold.toDouble(), 255.0, Imgproc.THRESH_BINARY)

        // 将结果转换为 Bitmap
        val dstBitmap = Bitmap.createBitmap(dstMat.cols(), dstMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(dstMat, dstBitmap)

        return dstBitmap
    }

}