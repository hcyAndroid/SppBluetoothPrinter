package com.issyzone.blelibs.data

/**
 * 这个变量改为纸张类型 2：2寸纸； 3：2.5寸纸； 4：3寸纸 ；5：4寸纸@Hyun禤崇全
 */
enum class SyzPrinterPaper(var paperSize: Int) {
    PaperSize2Inch(2), PaperSize2_5Inch(3), PaperSize3Inch(4), PaperSize4Inch(5),
}
