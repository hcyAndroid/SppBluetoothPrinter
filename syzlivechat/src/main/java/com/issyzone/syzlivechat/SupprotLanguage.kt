package com.issyzone.syzlivechat

enum class SupprotLanguage(val valuePath:String,val excelName:String) {
    English("app/src/main/res/values/strings.xml","英语"),
    Spanish("app/src/main/res/values-es-rES/strings.xml","西班牙语"),
    French("app/src/main/res/values-fr-rFR/strings.xml","法语"),
    German("app/src/main/res/values-de-rCH/strings.xml","德语"),
    JAPAN("app/src/main/res/values-ja-rJP/strings.xml","日语"),
    CHINESE("app/src/main/res/values-zh-rCN/strings.xml","中文"),
//    YUENAN("app/src/main/res/values-vi-rVN/strings.xml","越南语"),
//    THAILLAND("app/src/main/res/values-th-rTH/strings.xml","泰语"),
//    KOREA("app/src/main/res/values-ko-rKR/strings.xml","韩语"),
    Italian("app/src/main/res/values-it-rCH/strings.xml","意大利语")
}