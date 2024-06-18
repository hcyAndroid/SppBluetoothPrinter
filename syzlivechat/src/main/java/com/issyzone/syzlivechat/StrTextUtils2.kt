import com.issyzone.syzlivechat.StrRes
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

fun main() {
    //读取表格中的数据，去形成对应的xml
    val workbook = XSSFWorkbook(File("workbook.xlsx"))
    val sheet = workbook.getSheetAt(0)
    val data = mutableListOf<StrRes>()
    println("表格行数===${sheet.physicalNumberOfRows}")
    for (i in 1 until sheet.physicalNumberOfRows) {
        val row = sheet.getRow(i)
        val strRes = StrRes(
            id =row.getCell(0)?.stringCellValue,
            english =row.getCell(1)?.stringCellValue,
            zw = row.getCell(2)?.stringCellValue,
            es = row.getCell(6)?.stringCellValue,
            de = row.getCell(3)?.stringCellValue,
            fr = row.getCell(4)?.stringCellValue,
            jp = row.getCell(5)?.stringCellValue
        )
        data.add(strRes)
    }
    excelToString("app/src/main/res/values/strings.xml",data)
    excelToString("app/src/main/res/values-es-rES/strings.xml",data)
    excelToString("app/src/main/res/values-fr-rFR/strings.xml",data)
    excelToString("app/src/main/res/values-ja-rJP/strings.xml",data)
    excelToString("app/src/main/res/values-zh-rCN/strings.xml",data)
    excelToString("app/src/main/res/values-de-rCH/strings.xml",data)
    Thread.sleep(10000)
}

fun  excelToString(valuePath:String,data:MutableList<StrRes>){
    //去生成app/src/main/res/values/strings.xml
    val file = File(valuePath)
    val fos = FileOutputStream(file)
    fos.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n".toByteArray())
    fos.write("<resources>\n".toByteArray())
    data.forEach {
        fos.write("<string name=\"${it.id}\">${strParse(it.english)}</string>\n".toByteArray())
    }
    fos.write("</resources>".toByteArray())
    fos.close()
}

fun strParse(str: String?): String {
    //如果str中包含&，则替换为&amp;
    var result = str?:""
    if (result.contains("&")) {
        println("替换>>>>$result")
        result = result.replace("&", "&amp;")
        println("替换后>>>>$result")
    }
    return result
}