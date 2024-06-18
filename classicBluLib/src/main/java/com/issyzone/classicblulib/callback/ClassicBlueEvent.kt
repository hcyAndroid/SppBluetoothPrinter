package com.issyzone.classicblulib.callback

enum class SyzPrinterState2(
    var index: Int, var status: Boolean, var string: String, var order: Int
) {
    PRINTER_PRINTING(0, true, "打印中", 7),
    PRINTER_NO_PAPAER(1, true, "缺纸", 2),
    PRINTER_OOM(2, true, "打印缓存满", 8),
    PRINTER_LID_OPEN(3, true, "开盖", 1),
    PRINTER_STRUCK_PAPER(4, true, "卡纸", 4),//卡纸
    PRINTER_HIGH_TEMPERATURE(5, true, "打印头高温", 3),//打印头高温
    PRINTER_BATTERY_LOW(6, true, "电池电量低", 5),//低电量
    PRINTER_MOTOR_HIGH_TEMPERATURE(7, true, "马达过热", 6),//马达高温
    PRINTER_OK(1000, true, "打印机正常", 9),//马达高温
    PRINTER_STATUS_UNKNOWN(1001, true, "打印机状态无法通过获取设备信息获取", 10),//马达高温
    PRINTER_CANCEL_PRINT(1002,true,"取消打印回调",11),
    PRINTER_UPACKER_FAILED(1003,true,"打印过程中解包失败",12),
    PRINTER_HAS_STUDY_PAPER(1004,true,"打印机学纸了",13),
    PRINTER_NO_STUDY_PAPER(1005,true,"打印机没有学纸",14),
    PRINTER_SELF_PRINT_FAIL(1006,true,"自检页打印失败",15),
//  PRINTER_LID_CLOSE(3, false),//关盖
//    //开盖
//    PRINTER_HAS_PAPER(11, "1"), PRINTER_NO_PAPER(11, "2"), PRINTER_STRUCK_PAPER(
//        11, "3"
//    ),
//    PRINTER_GETRIGHT(12, "1"), PRINTER_OVERHEATING(12, "2"), PRINTER_BATTERY(
//        13, "save_battery"
//    ),
//    PRINTER_SOME_UNKNOW(999, "999"), PRINTER_PRINT_SUCCESS(300, "1"), PRINTER_PRINT_CANCEL(
//        300, "2"
//    ),
//    PRINTER_PRINT_FAILED(300, "3"), PRINTER_DEXUPDATE_SUCCESS(400, "1"),//升级陈工,三秒后打印机重启
//    PRINTER_DEXUPDATE_FAILED(400, "2")
}

enum class SyzPrinterState(var code: Int, var info: String) {
    PRINTER_LID_CLOSE(10, "1"),  //关盖
    PRINTER_LID_OPEN(10, "2"),   //开盖
    PRINTER_HAS_PAPER(11, "1"), PRINTER_NO_PAPER(11, "2"), PRINTER_STRUCK_PAPER(
        11, "3"
    ),
    PRINTER_GETRIGHT(12, "1"), PRINTER_OVERHEATING(12, "2"), PRINTER_BATTERY(
        13, "save_battery"
    ),
    PRINTER_SOME_UNKNOW(999, "999"), PRINTER_PRINT_SUCCESS(300, "1"), PRINTER_PRINT_CANCEL(
        300, "2"
    ),
    PRINTER_PRINT_FAILED(300, "3"), PRINTER_DEXUPDATE_SUCCESS(400, "1"),//升级陈工,三秒后打印机重启
    PRINTER_DEXUPDATE_FAILED(400, "2")
}