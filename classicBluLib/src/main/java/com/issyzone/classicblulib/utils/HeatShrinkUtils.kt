package com.issyzone.classicblulib.utils


import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * HeatShrink 压缩工具类
 */
object HeatShrinkUtils {
    fun compress(data: ByteArray, windowSize: Int = 11, lookaheadSize: Int = 4): ByteArray {
        ByteArrayOutputStream().use { byteArrayOutputStream ->
            // 创建新的 HsOutputStream 对象
            HsOutputStream(byteArrayOutputStream, windowSize, lookaheadSize).use { hsOutputStream ->
                hsOutputStream.write(data)
                hsOutputStream.flush() // 刷新流
                // 不需要显式关闭流，因为在 use 块中会自动关闭
            }
            return byteArrayOutputStream.toByteArray()
        }
    }

    fun decompress(
        compressedData: ByteArray, windowSize: Int = 11, lookaheadSize: Int = 4
    ): ByteArray {
        ByteArrayInputStream(compressedData).use { byteArrayInputStream ->
            ByteArrayOutputStream().use { byteArrayOutputStream ->
                // 创建新的 HsInputStream 对象并解压数据
                HsInputStream(
                    byteArrayInputStream, windowSize, lookaheadSize
                ).use { hsInputStream ->
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (hsInputStream.read(buffer).also { bytesRead = it } != -1) {
                        byteArrayOutputStream.write(buffer, 0, bytesRead)
                    }
                }
                return byteArrayOutputStream.toByteArray()
            }
        }
    }

}