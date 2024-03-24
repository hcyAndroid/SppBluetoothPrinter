package com.issyzone.blelibs.utils

import java.io.File
import java.io.FileInputStream


/**
 * 获取文件后缀名
 */
fun File.getExtension(): String? {
    val name = this.name
    val lastDotIndex = name.lastIndexOf('.')
    return if (lastDotIndex >= 0) name.substring(lastDotIndex + 1) else null
}

/**
 * 判断文件的后缀名是否等于xxx
 */
fun File.isExtension(houzhui:String="bin"):Boolean{
    return this.getExtension().equals(houzhui)
}

fun File.fileToByteArray(): ByteArray {
    val inputStream = FileInputStream(this)
    val size = this.length().toInt()
    val buffer = ByteArray(size)
    inputStream.read(buffer)
    inputStream.close()
    return buffer
}