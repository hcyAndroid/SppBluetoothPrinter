package com.issyzone.blelibs.utils

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream



object SYZFileUtils {
    /**
     * 获取assets里的文件
     */
     fun copyAssetGetFilePath(fileName: String): String? {
        try {
            val cacheDir: File = AppGlobels.getApplication().cacheDir
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            val outFile = File(cacheDir, fileName)
            if (!outFile.exists()) {
                val res: Boolean = outFile.createNewFile()
                if (!res) {
                    return null
                }
            } else {
                if (outFile.length() > 10) { //表示已经写入一次
                    return outFile.getPath()
                }
            }
            val `is`: InputStream = AppGlobels.getApplication().assets.open(fileName)
            val fos: FileOutputStream = FileOutputStream(outFile)
            val buffer = ByteArray(1024)
            var byteCount: Int
            while (`is`.read(buffer).also { byteCount = it } != -1) {
                fos.write(buffer, 0, byteCount)
            }
            fos.flush()
            `is`.close()
            fos.close()
            return outFile.getPath()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}