package com.issyzone.classicblulib.bean

/**
 * 0,1,2是区分当前升级的内容，像4寸就有外部文件系统用来做U盘的功能，我们现在做这个就是兼容以后的产品
 * 更具体来说，加这个012是属于一个类型，是丰富OTA这个接口，可以做更多远程升级的功能
 * 是的，升级固件传0过去就可以
 */
enum class SyzPaperSize(val paperSet: Int,funName: String,var height:Float,var offset:Float) {
    //0打印机固件，1字库，2文件系统
    SYZPAPER_JIANXI(1, "间隙纸",0.0f,0.0f),
    SYZPAPER_LIANXU(2, "连续纸",0.0f,0.0f),

    SYZPAPER_HEIBIAO(3, "黑标纸",0.0f,0.0f),
}