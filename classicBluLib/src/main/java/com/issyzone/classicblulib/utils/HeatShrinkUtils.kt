package com.issyzone.classicblulib.utils


import android.util.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * HeatShrink 压缩工具类
 */
object HeatShrinkUtils {
    private val TAG = "HeatShrinkUtils>>>"
    private val mutex = Mutex()
    suspend fun compress(
        data: ByteArray, windowSize: Int = 11, lookaheadSize: Int = 4
    ): ByteArray? = mutex.withLock {
        return try {
            ByteArrayOutputStream().use { byteArrayOutputStream ->
                // 创建新的 HsOutputStream 对象
                HsOutputStream(
                    byteArrayOutputStream, windowSize, lookaheadSize
                ).use { hsOutputStream ->
                    hsOutputStream.write(data)
                    // hsOutputStream.flush()// 刷新流
                    // 不需要显式关闭流，因为在 use 块中会自动关闭
                }
                byteArrayOutputStream.toByteArray()
            }
        } catch (e: IOException) {
            // 记录具体异常信息
            Log.e(
                TAG, "HeatShrinkUtils Compression failed due to an IO error: ${e.localizedMessage}"
            )
            null
        } catch (e: Exception) {
            // 捕获其他可能的异常并记录
            Log.e(TAG, "HeatShrinkUtils Compression failed: ${e.localizedMessage}")
            null
        }
    }


//    fun compress2(data: ByteArray, windowSize: Int = 11, lookaheadSize: Int = 4): Result<ByteArray, CompressionError> {
//        try {
//            ByteArrayOutputStream().use { byteArrayOutputStream ->
//                HsOutputStream(byteArrayOutputStream, windowSize, lookaheadSize).use { hsOutputStream ->
//                    hsOutputStream.write(data)
//                    hsOutputStream.flush()
//                }
//                return Result.success(byteArrayOutputStream.toByteArray())
//            }
//        } catch (e: IOException) {
//            return Result.failure(CompressionError.IO_ERROR)
//        } catch (e: Exception) {
//            return Result.failure(CompressionError.UNKNOWN_ERROR)
//        }
//    }


    enum class CompressionError {
        IO_ERROR, UNKNOWN_ERROR
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