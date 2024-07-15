package com.issyzone.common_work.utils

import android.content.Context
import android.util.Log
import com.tencent.bugly.crashreport.CrashReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader

/**
 *
 */
object MyCrashUtils {
    private val TAG="MyCrashUtils"
      fun initCrashHandler(context: Context,buglyId:String) {
        initBugly(context,buglyId)
        val crashHandler = MyCrashHandler.getInstance()
        crashHandler.init(context)
        //
//        readAllCrashLogs(context).forEach {
//            if (!it.isNullOrEmpty()) {
//                Log.i(TAG, "bugly: $it")
//                CrashReport.postCatchedException(Throwable(it))
//            }
//        }
    }

    fun initBugly(context: Context, buglyId: String) {
        val strategy = CrashReport.UserStrategy(context)
        strategy.setCrashHandleCallback(object : CrashReport.CrashHandleCallback() {
            override fun onCrashHandleStart(crashType: Int, errorType: String?, errorMessage: String?, errorStack: String?): Map<String, String>? {
                // 崩溃处理开始时调用，可以在这里打印日志或执行其他操作
                Log.i(TAG, "Crash handle start: $errorType, $errorMessage")
                return super.onCrashHandleStart(crashType, errorType, errorMessage, errorStack)
            }

            override fun onCrashHandleStart2GetExtraDatas(
                p0: Int,
                p1: String?,
                p2: String?,
                p3: String?
            ): ByteArray {
                return super.onCrashHandleStart2GetExtraDatas(p0, p1, p2, p3)
                Log.i(TAG, "Crash handle start2: $p0, $p1")
            }

        })

        CrashReport.initCrashReport(context, buglyId, true, strategy)
    }
   suspend fun readAllCrashLogs(context: Context): MutableList<String?> = withContext(Dispatchers.IO){
        val logsContents = mutableListOf<String?>()
        // 获取外部存储目录下的应用缓存目录
        val externalCacheDir = context.externalCacheDir ?: return@withContext logsContents
        // 文件存储位置
        val path = "${externalCacheDir.path}/crash_logInfo/"
        val directory = File(path)
        // 检查目录是否存在
        if (!directory.exists() || !directory.isDirectory) {
            return@withContext logsContents
        }
        // 获取所有的日志文件
        val logFiles = directory.listFiles { file -> file.extension == "log" } ?: return@withContext logsContents
        // 读取每个日志文件的内容
        for (logFile in logFiles) {
            val content = try {
                val fileInputStream = FileInputStream(logFile)
                val inputStreamReader = InputStreamReader(fileInputStream)
                val bufferedReader = inputStreamReader.buffered()
                val fileContent = bufferedReader.use { it.readText() }
                fileInputStream.close()
                fileContent
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
            logsContents.add(content)
            // 删除文件
            if (logFile.exists()) {
                logFile.delete()
            }
        }
       return@withContext logsContents
    }
}