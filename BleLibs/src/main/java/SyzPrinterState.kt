enum class SyzPrinterState(var code: Int, var info: String) {
    PRINTER_LID_CLOSE(10, "1"),  //关盖
    PRINTER_LID_OPEN(10, "2"),   //开盖
    PRINTER_HAS_PAPER(11, "1"), PRINTER_NO_PAPER(11, "2"), PRINTER_STRUCK_PAPER(
        11, "3"
    ),
    PRINTER_GETRIGHT(12, "1"), PRINTER_OVERHEATING(12, "2"), PRINTER_BATTERY(
        13, "save_battery"
    ),
    PRINTER_SOME_UNKNOW(999, "999"),
    PRINTER_PRINT_SUCCESS(300, "1"),
    PRINTER_PRINT_CANCEL(300, "2"),
    PRINTER_PRINT_FAILED(300, "3"),
    PRINTER_DEXUPDATE_SUCCESS(400,"1"),//升级陈工,三秒后打印机重启
    PRINTER_DEXUPDATE_FAILED(400,"2")
}