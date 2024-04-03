package com.issyzone.classicblulib.tools

import android.content.Context

import android.content.SharedPreferences
import com.issyzone.classicblulib.utils.AppGlobels


object SpUtils {
    // 存储数据到SharedPreferences
     fun saveData(key: String, value: String) {
        val sharedPreferences: SharedPreferences =
            AppGlobels.getApplication().getSharedPreferences("classic_blu", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    // 从SharedPreferences读取数据
     fun readData(key: String): String? {
        val sharedPreferences: SharedPreferences =
            AppGlobels.getApplication().getSharedPreferences("classic_blu", Context.MODE_PRIVATE)
        return sharedPreferences.getString(key,"") // 如果key不存在，则返回默认值
    }
}