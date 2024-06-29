package com.issyzone.syzlivechat
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import javax.xml.parsers.SAXParserFactory

object SyzExcelUtils {
    private val TAG = "SyzExcelUtils>>>>"
    private val labelIdAndRoidINdex=0   //Android 标签id再表格中的位置
    private val labelTypeIndex=2   //标签类型再表格中的位置
    fun writeToExcel() {
        val item = ITEM(
            mutableListOf(
                ExcelTag(tagName = "安卓的标签id", tagValue = ""),
                ExcelTag(tagName = "ios的标签id", tagValue = ""),
                ExcelTag(tagName = "安卓的标签类型", tagValue = ""),
            )
        )
        for (language in enumValues<SupprotLanguage>()) {
            item.valueList.add(ExcelTag(tagName = language.excelName, tagValue = ""))
        }
        //读取XML
        val data = mutableListOf<ITEM>()
        for (language in enumValues<SupprotLanguage>()) {
            val xmlPath = language.valuePath
            val xmlName = language.excelName
            val xmlHandler = readXMlFile(xmlPath)
            xmlHandler?.apply {
                val arrayXml = this.arrayResources
                val strXml = this.stringResources
                strXml.forEach {
                    val id = it.id
                    val value = it.value
                    val find = data.find {
                        it.valueList.get(labelIdAndRoidINdex).tagValue == id && it.valueList.get(labelTypeIndex).tagValue == "string"
                    }
                    if (find==null){
                        val newItem=deepCopy(item)
                        newItem.valueList.get(labelIdAndRoidINdex).tagValue=id
                        newItem.valueList.get(labelTypeIndex).tagValue="string"
                        newItem.valueList.find {
                            it.tagName==xmlName
                        }?.tagValue=value
                        data.add(newItem)
                    }else{
                        find.valueList.find {
                            it.tagName==xmlName
                        }?.tagValue=value
                    }

                }
                arrayXml.forEach { (key, value) ->
                    value.forEach {
                        val id = "${key}$$${it.id}"
                        val value=it.value
                        val find = data.find {
                            it.valueList.get(labelIdAndRoidINdex).tagValue == id && it.valueList.get(labelTypeIndex).tagValue == "string-array"
                        }
                        if (find==null){
                            val newItem=deepCopy(item)
                            newItem.valueList.get(labelIdAndRoidINdex).tagValue=id
                            newItem.valueList.get(labelTypeIndex).tagValue="string-array"
                            newItem.valueList.find {
                                it.tagName==xmlName
                            }?.tagValue=value
                            data.add(newItem)
                        }else{
                            find.valueList.find {
                                it.tagName==xmlName
                            }?.tagValue=value
                        }
                    }
                }
            }
        }

        data.forEach {
            println("${it.valueList}")
        }
        println("从XML中读取了${data.size}条数据")
        //将数据写入到EXCEL,覆盖写入
        writeDataToExcel(data)
    }
   private  fun writeDataToExcel(data: MutableList<ITEM>) {
        val workbook = XSSFWorkbook() // Create a new Workbook
        val sheet = workbook.createSheet("Data") // Create a Sheet
        val colNUms=data.get(0).valueList.size
        val header_Row = sheet.createRow(0)
        for (i in 0 until colNUms){
            header_Row.createCell(i).setCellValue(data.get(0).valueList.get(i).tagName)
            if (i==0){
                sheet.setColumnWidth(i, 40 * 256)
            }else if (i==1){
                sheet.setColumnWidth(i, 10 * 256)
            }else if (i==2){
                sheet.setColumnWidth(i, 40 * 256)
            }else{
                sheet.setColumnWidth(i, 100 * 256)
            }
        }
        // Populate the data rows
        data.forEachIndexed { index, valueList ->
            val row = sheet.createRow(index + 1)
            for (i in 0 until colNUms){
                row.createCell(i).setCellValue(valueList.valueList.get(i).tagValue)
            }
        }
        println("写入${data.size}条数据到Excel")
        // Write the output to a file
        val fileOut = FileOutputStream("workbook.xlsx")
        workbook.write(fileOut)
        fileOut.close()
        // Closing the workbook
        workbook.close()
    }

    fun getExcelData() {
        val workbook = XSSFWorkbook(File("workbook.xlsx"))
        val sheet = workbook.getSheetAt(0)
        val data = mutableListOf<ITEM>()
        val rowNums = sheet.physicalNumberOfRows
        // Log.d(TAG,"表格行数====${rowNums}")
        val rowZero = sheet.getRow(0)
        val zeroCellNUms = rowZero.physicalNumberOfCells
        val item = ITEM(mutableListOf())
        for (zero in 0 until zeroCellNUms) {
            item.valueList.add(
                ExcelTag(
                    tagName = rowZero.getCell(zero)?.stringCellValue ?: "", tagValue = ""
                )
            )
        }
        println("表格行数====${rowNums}")
        for (i in 1 until rowNums) {
            val row = sheet.getRow(i)
            val cellNums = row.physicalNumberOfCells
            println("第${i}行有${cellNums}列")
            if (cellNums != zeroCellNUms) {
                throw Exception("内容列数和开头标准不一致")
            }
            val currentItem = deepCopy(item)
            for (j in 0 until cellNums) {
                currentItem.valueList.get(j).tagValue = "${row.getCell(j)?.stringCellValue}"
               // println("第${i}行第${j}列的数据是${row.getCell(j)?.stringCellValue}")
            }
            data.add(currentItem)
        }

        data.forEach {
            println("${it.valueList.toString()}")
        }
        println("读到了表格${data.size}行数据")
        for (language in enumValues<SupprotLanguage>()) {
            println("语言: ${language.name}, 路径: ${language.valuePath}, Excel 名称: ${language.excelName}")
            val currentXmlHandler = readXMlFile(language.valuePath)
            currentXmlHandler?.apply {
                val newList = mergeData(currentXmlHandler, data, language.excelName, item)
                println("合并了${newList.size}====${language.excelName}")
                data.addAll(newList)
            }
        }

       /* for (language in enumValues<SupprotLanguage>()) {
            val file = File(language.valuePath)
            file.parentFile.mkdirs()
            val fos = FileOutputStream(file)
            fos.write("<resources>\n".toByteArray())
            //写入数据
            val stringArrayList = data.filter {
                it.valueList.get(labelTypeIndex).tagValue == "string-array"
            }.toMutableList()
            val groupedStringArray = stringArrayList.groupBy {
                it.valueList.get(labelIdAndRoidINdex).tagValue!!.split("$$")[0]
            }
            groupedStringArray.forEach {
                val key = it.key
                val value = it.value
                fos.write("<string-array name=\"$key\">\n".toByteArray())
                value.forEach {
                    val id = it.valueList.get(labelIdAndRoidINdex).tagValue
                    val value = it.valueList.find {
                        it.tagName == language.excelName
                    }?.tagValue

                    fos.write("<item name=\"${id!!.split("$$")[1]}\">${strParse(value)}</item>\n".toByteArray())
                }
                fos.write("</string-array>\n".toByteArray())
            }


            data.forEach {
                val id = it.valueList.get(labelIdAndRoidINdex).tagValue
                val type = it.valueList.get(labelTypeIndex).tagValue
                val value = it.valueList.find {
                    it.tagName == language.excelName
                }?.tagValue
                if (type == "string") {
                    fos.write("<string name=\"${id}\">${strParse(value)}</string>\n".toByteArray())
                }
            }
            fos.write("</resources>".toByteArray())
            fos.close()
        }*/
    }

    fun strParse(str: String?): String {
        //如果str中包含&，则替换为&amp;
        var result = str ?: ""
        if (result.contains("&")) {
            //println("替换>>>>$result")
            result = result.replace("&", "&amp;")
            //println("替换后>>>>$result")
        }
        //如果str中包含单引号且单引号前一位不是\，则替换为\'
        //判定str中含有单引号且不含有双引号
        str?.apply {
            val containsSingleQuote = str.contains("'")
            val containsDoubleQuote = str.contains("\"")

            if (containsSingleQuote) {
                //判定单引号前一位不是\或者前一位没有数据,并且后一位不为单引号
                val indexOfSingleQuote = str.indexOf("'")
                if ((indexOfSingleQuote == 0 || str[indexOfSingleQuote - 1] != '\\') && (str[indexOfSingleQuote + 1] != '\'')) {
                    // println("替换>>>>$result")
                    result = result.replace("'", "\\'")
                    //println("替换后>>>>$result")
                } else {
                    // println("The string does not meet the conditions.")
                }

            } else {
                println("The string does not meet the conditions.")
            }
        }


        return result
    }


    private fun mergeData(
        currentXmlHandler: StringsXmlHandler, data: MutableList<ITEM>, excelName: String, item: ITEM
    ): MutableList<ITEM> {
        val currentXmlData = currentXmlHandler.stringResources
        val currentXmlArrayData = currentXmlHandler.arrayResources
        val newItemList = mutableListOf<ITEM>()
        currentXmlData.forEach {
            val id = it.id
            val find = data.find {
                it.valueList.get(labelIdAndRoidINdex).tagValue == id && it.valueList.get(
                    labelTypeIndex).tagValue == "string"
            }
            if (find == null) {
                val newITem = deepCopy(item)
                newITem.valueList.get(labelIdAndRoidINdex).tagValue = it.id
                newITem.valueList.get(labelTypeIndex).tagValue = "string"
                newITem.valueList.find {
                    it.tagName == excelName
                }?.tagValue = it.value
                newItemList.add(newITem)
            }
        }
        currentXmlArrayData.forEach { (key, value) ->
            value.forEach {
                val flag = "${key}$$${it.id}"
                var find =
                    data.find { flag == it.valueList.get(labelIdAndRoidINdex).tagValue && it.valueList.get(labelTypeIndex).tagValue == "string-array" }
                if (find == null) {
                    val newITem = deepCopy(item)
                    newITem.valueList.get(labelIdAndRoidINdex).tagValue = flag
                    newITem.valueList.get(labelTypeIndex).tagValue = "string-array"
                    newITem.valueList.find {
                        it.tagName == excelName
                    }?.tagValue = it.value
                    newItemList.add(newITem)
                }
            }
        }
        return newItemList
    }

    fun <T : Serializable> deepCopy(obj: T): T {
        // 将对象写入到字节流
        val baos = ByteArrayOutputStream()
        ObjectOutputStream(baos).use { oos ->
            oos.writeObject(obj)
        }

        // 从字节流中读取对象，实现深度复制
        val bais = ByteArrayInputStream(baos.toByteArray())
        ObjectInputStream(bais).use { ois ->
            @Suppress("unchecked_cast") return ois.readObject() as T
        }
    }

    fun readXMlFile(fileString: String): StringsXmlHandler? {
        val file = File(fileString)
        if (file.exists()) {
            val factory = SAXParserFactory.newInstance()
            val saxParser = factory.newSAXParser()
            val userhandler = StringsXmlHandler()
            saxParser.parse(file, userhandler)

            return userhandler
        } else {
            println("文件不存在")
            return null
        }
    }


}