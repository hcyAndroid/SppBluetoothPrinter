package com.issyzone.classicblulib.bean

/**
 * 0,1,2是区分当前升级的内容，像4寸就有外部文件系统用来做U盘的功能，我们现在做这个就是兼容以后的产品
 * 更具体来说，加这个012是属于一个类型，是丰富OTA这个接口，可以做更多远程升级的功能
 * 是的，升级固件传0过去就可以
 */
enum class SyzFirmwareType(var funValue: Int, var funName: String) {
    //0打印机固件，1字库，2文件系统

    SYZFIRMWARETYPE01(0, "打印机固件"),
    SYZFIRMWARETYPE02(1, "字库"),
    SYZFIRMWARETYPE03(
        2,
        "文件系统"
    )
}