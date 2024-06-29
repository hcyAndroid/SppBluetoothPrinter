package com.issyzone.syzlivechat

import java.io.Serializable

data class ExcelTag (val tagName:String,var tagValue:String):Serializable
data class ITEM(val valueList:MutableList<ExcelTag>):Serializable
data class StringResource(val id: String, var value: String)