package com.issyzone.syzlivechat

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import javax.xml.parsers.SAXParserFactory
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.FileOutputStream

data class StrRes(
    val id: String?,
    val english: String?,
    var es: String?,
    var de: String?,
    var fr: String?,
    var jp: String?,
    var zw: String?
)

data class StringResource(val id: String, val value: String)
class StringsXmlHandler : DefaultHandler() {
    private var currentValue = ""
    private var currentId = ""
    val resources = mutableListOf<StringResource>()

    override fun startElement(
        uri: String, localName: String, qName: String, attributes: Attributes
    ) {
        if (qName == "string") {
            currentId = attributes.getValue("name")
        }
        currentValue = ""
    }

    override fun characters(ch: CharArray, start: Int, length: Int) {
        currentValue += String(ch, start, length)
    }

    override fun endElement(uri: String, localName: String, qName: String) {
        if (qName == "string") {
            resources.add(StringResource(currentId, currentValue))
        }
    }
}

fun readXMlFile(fileString: String): MutableList<StringResource> {
    val file = File(fileString)
    if (file.exists()) {
        val factory = SAXParserFactory.newInstance()
        val saxParser = factory.newSAXParser()
        val userhandler = StringsXmlHandler()
        saxParser.parse(file, userhandler)
//        userhandler.resources.forEach {
//            println("ID: ${it.id}, Content: ${it.value}")
//        }
        return userhandler.resources
    } else {
        println("文件不存在")
        return mutableListOf()
    }
}

fun main() {
    //读取app目录下的文件
//        GlobalScope.launch(Dispatchers.IO) {
    val totalData = mutableListOf<StrRes>()
    val res = readXMlFile("app/src/main/res/values/strings.xml")
    res.forEach {
        totalData.add(StrRes(it.id, it.value, "", "", "", "", ""))
    }
    var res_de = readXMlFile("app/src/main/res/values-de-rCH/strings.xml")
    res_de.forEach { de ->
        var find = totalData.find { de.id == it.id }
        if (find == null) {
            totalData.add(StrRes(de.id, "", "", de.value, "", "", ""))
        } else {
            find?.de = de.value
        }
    }
    var res_es = readXMlFile("app/src/main/res/values-es-rES/strings.xml")
    res_es.forEach { es ->
        var find = totalData.find { es.id == it.id }
        if (find == null) {
            totalData.add(StrRes(es.id, "", es.value, "", "", "", ""))
        } else {
            find?.es = es.value
        }
    }

    var res_fr = readXMlFile("app/src/main/res/values-fr-rFR/strings.xml")
    res_fr.forEach { fr ->
        var find = totalData.find { fr.id == it.id }
        if (find == null) {
            totalData.add(StrRes(fr.id, "", "", "", fr.value, "", ""))
        } else {
            find?.fr = fr.value
        }
    }

    var res_jp = readXMlFile("app/src/main/res/values-ja-rJP/strings.xml")
    res_jp.forEach { jp ->
        var find = totalData.find { jp.id == it.id }
        if (find == null) {
            totalData.add(StrRes(jp.id, "", "", "", "", jp.value, ""))
        } else {
            find?.jp = jp.value
        }
    }
    var res_zw = readXMlFile("app/src/main/res/values-zh-rCN/strings.xml")
    res_zw.forEach { zw ->
        var find = totalData.find { zw.id == it.id }
        if (find == null) {
            totalData.add(StrRes(zw.id, "", "", "", "", "", zw.value))
        } else {
            find?.zw = zw.value
        }
    }
    totalData.forEach {
        println("ID: ${it.id}, English: ${it.english}, ES: ${it.es}, DE: ${it.de}, FR: ${it.fr}, JP: ${it.jp}, ZW: ${it.zw}")
    }
    //把获得的数据写入到excel表格中,每一个字段对应一列
    writeDataToExcel(totalData)
    Thread.sleep(10000)
}

fun writeDataToExcel(data: List<StrRes>) {
    val workbook = XSSFWorkbook() // Create a new Workbook
    val sheet = workbook.createSheet("Data") // Create a Sheet
    sheet.setColumnWidth(0, 30 * 256)
    sheet.setColumnWidth(1, 100 * 256)
    sheet.setColumnWidth(2, 100 * 256)
    sheet.setColumnWidth(3, 100 * 256)
    sheet.setColumnWidth(4, 100 * 256)
    sheet.setColumnWidth(5, 100 * 256)
    sheet.setColumnWidth(6, 100 * 256)
    // Create the header row
    val headerRow = sheet.createRow(0)
    headerRow.createCell(0).setCellValue("标签ID")
    headerRow.createCell(1).setCellValue("英语")
    headerRow.createCell(2).setCellValue("中文")
    headerRow.createCell(3).setCellValue("德语")
    headerRow.createCell(4).setCellValue("法语")
    headerRow.createCell(5).setCellValue("日语")
    headerRow.createCell(6).setCellValue("西班牙语")

    // Populate the data rows
    data.forEachIndexed { index, strRes ->
        val row = sheet.createRow(index + 1)
        row.createCell(0).setCellValue(strRes.id)
        row.createCell(1).setCellValue(strRes.english)
        row.createCell(2).setCellValue(strRes.zw)
        row.createCell(3).setCellValue(strRes.de)
        row.createCell(4).setCellValue(strRes.fr)
        row.createCell(5).setCellValue(strRes.jp)
        row.createCell(6).setCellValue(strRes.es)
    }

    // Write the output to a file
    val fileOut = FileOutputStream("workbook.xlsx")
    workbook.write(fileOut)
    fileOut.close()
    // Closing the workbook
    workbook.close()
}




