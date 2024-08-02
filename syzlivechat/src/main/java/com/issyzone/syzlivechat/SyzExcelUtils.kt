package com.issyzone.syzlivechat


import android.util.Log
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.SAXParserFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

object SyzExcelUtils {
    private val TAG = "SyzExcelUtils>>>>"
     val labelIdAndRoidINdex = 0   //Android 标签id再表格中的位置
     val labelTypeIndex = 2  //标签类型再表格中的位置
    fun writeToExcel() {
        val item = ITEM(
            mutableListOf(
                ExcelTag(tagName = "安卓的标签id", tagValue = ""),
                ExcelTag(tagName = "iOS_标签ID", tagValue = ""),
            )
        )
        for (language in enumValues<SupprotLanguage>()) {
            item.valueList.add(ExcelTag(tagName = language.excelName, tagValue = ""))
        }
        //读取XML
     /*   val data = mutableListOf<ITEM>()
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
                        it.valueList.get(labelIdAndRoidINdex).tagValue == id && it.valueList.get(
                            labelTypeIndex
                        ).tagValue == "string"
                    }
                    if (find == null) {
                        val newItem = deepCopy(item)
                        newItem.valueList.get(labelIdAndRoidINdex).tagValue = id
                        newItem.valueList.get(labelTypeIndex).tagValue = "string"
                        newItem.valueList.find {
                            it.tagName == xmlName
                        }?.tagValue = value
                        data.add(newItem)
                    } else {
                        find.valueList.find {
                            it.tagName == xmlName
                        }?.tagValue = value
                    }

                }
                arrayXml.forEach { (key, value) ->
                    value.forEach {
                        val id = "${key}$$${it.id}"
                        val value = it.value
                        val find = data.find {
                            it.valueList.get(labelIdAndRoidINdex).tagValue == id && it.valueList.get(
                                labelTypeIndex
                            ).tagValue == "string-array"
                        }
                        if (find == null) {
                            val newItem = deepCopy(item)
                            newItem.valueList.get(labelIdAndRoidINdex).tagValue = id
                            newItem.valueList.get(labelTypeIndex).tagValue = "string-array"
                            newItem.valueList.find {
                                it.tagName == xmlName
                            }?.tagValue = value
                            data.add(newItem)
                        } else {
                            find.valueList.find {
                                it.tagName == xmlName
                            }?.tagValue = value
                        }
                    }
                }
            }
        }

        data.forEach {
            println("${it.valueList}")
        }*/
        val data= MargeDataSta.readALLXmlData(item)
        println("从XML中读取了${data.size}条数据")
        //将数据写入到EXCEL,覆盖写入
        writeDataToExcel(data)
    }

    private fun writeDataToExcel(data: MutableList<ITEM>) {
        val workbook = XSSFWorkbook() // Create a new Workbook
        val sheet = workbook.createSheet("Data") // Create a Sheet
        val colNUms = data.get(0).valueList.size
        val header_Row = sheet.createRow(0)
        for (i in 0 until colNUms) {
            header_Row.createCell(i).setCellValue(data.get(0).valueList.get(i).tagName)
            if (i == 0) {
                sheet.setColumnWidth(i, 40 * 256)
            } else if (i == 1) {
                sheet.setColumnWidth(i, 10 * 256)
            } else if (i == 2) {
                sheet.setColumnWidth(i, 40 * 256)
            } else {
                sheet.setColumnWidth(i, 100 * 256)
            }
        }
        // Populate the data rows
        data.forEachIndexed { index, valueList ->
            val row = sheet.createRow(index + 1)
            for (i in 0 until colNUms) {
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

    fun getCellValue(cell: Cell?): String {
        if (cell == null) {
            return ""
        }
        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue
            CellType.NUMERIC -> {
                val numericValue = cell.numericCellValue
                if (numericValue % 1 == 0.0) {
                    numericValue.toInt().toString()
                } else {
                    numericValue.toString()
                }
            }

            else -> cell?.stringCellValue?:""
        }
    }

    /**
     *
     */
    fun getExcelData2(fileName: String) {
        val workbook = XSSFWorkbook(File(fileName))
        val sheet = workbook.getSheetAt(0)
        val Exceldata = mutableListOf<ITEM>()
        val rowNums = sheet.physicalNumberOfRows
        println("表格行数====${rowNums}")
        val rowZero = sheet.getRow(1)
        val zeroCellNUms = rowZero.physicalNumberOfCells
        val item = ITEM(mutableListOf())
        for (zero in 0 until zeroCellNUms) {
            item.valueList.add(
                ExcelTag(
                    tagName = rowZero.getCell(zero)?.stringCellValue ?: "", tagValue = ""
                )
            )
        }
        for (i in 2..(rowNums - 2)) {
            val row = sheet.getRow(i)
            val cellNums = row.physicalNumberOfCells
            //println("第${i}行有${cellNums}列")
//            if (cellNums != zeroCellNUms) {
//                throw Exception("第${i}内容列数和开头标准不一致")
//            }
            //判断id 和 type 是否存在 数据是否存在
            val id = getCellValue(row.getCell(labelIdAndRoidINdex))
            val type = getCellValue(row.getCell(labelTypeIndex))
            if (id.isNullOrEmpty() || type.isNullOrEmpty()) {
                println("第${i}行的id或者type为空")
                continue
            } else {
                println("第${i}行的数据正常==${id}==${type}")
                val currentItem = deepCopy(item)
                for (j in 0 until cellNums) {
                    currentItem.valueList.get(j).tagValue = "${getCellValue(row.getCell(j))}"
                    // println("第${i}行第${j}列的数据是${row.getCell(j)?.stringCellValue}")
                }
                Exceldata.add(currentItem)
            }
        }

        Exceldata.forEach {
            println("${it.valueList.toString()}")
        }
        println("读到了表格${Exceldata.size}条合理数据")

       val data= MargeDataSta.margeExcelData(Exceldata, MargeDataSta.readALLXmlData(item))
//        for (language in enumValues<SupprotLanguage>()) {
//            println("语言: ${language.name}, 路径: ${language.valuePath}, Excel 名称: ${language.excelName}")
//            val currentXmlHandler = readXMlFile(language.valuePath)
//            currentXmlHandler?.apply {
//                mergeData(currentXmlHandler, data, language.excelName, item)
//                println("合并了${data.size}====${language.excelName}")
//                //data.addAll(newList)
//            }
//        }

//        data.forEach {
//            println("合并之后的数据::${it.valueList}")
//        }

        for (language in enumValues<SupprotLanguage>()) {
            val docFactory = DocumentBuilderFactory.newInstance()
            val docBuilder = docFactory.newDocumentBuilder()
            // 根元素
            val doc = docBuilder.newDocument()
            val rootElement = doc.createElement("resources")
            doc.appendChild(rootElement)
            // 写入 string-array 数据
            val stringArrayList = data.filter {
                it.valueList[labelTypeIndex].tagValue == "string-array"
            }.toMutableList()
            val groupedStringArray = stringArrayList.groupBy {
                it.valueList[labelIdAndRoidINdex].tagValue!!.split("$$")[0]
            }
            groupedStringArray.forEach { (key, value) ->
                val arrayElement = doc.createElement("string-array")
                arrayElement.setAttribute("name", key)
                value.forEach {
                    val id = it.valueList[labelIdAndRoidINdex].tagValue
                    val itemValue =
                        it.valueList.find { tag -> tag.tagName == language.excelName }?.tagValue
                    val itemElement = doc.createElement("item")
                    itemElement.setAttribute("name", id!!.split("$$")[1])
                    itemElement.appendChild(doc.createTextNode(strParse(itemValue)))
                    arrayElement.appendChild(itemElement)
                }
                rootElement.appendChild(arrayElement)
            }
            // 写入 string 数据
            data.forEach {
                val id = it.valueList[labelIdAndRoidINdex].tagValue
                val type = it.valueList[labelTypeIndex].tagValue
                val value = it.valueList.find { tag -> tag.tagName == language.excelName }?.tagValue
                if (type == "string") {
                    val stringElement = doc.createElement("string")
                    stringElement.setAttribute("name", id)
                    stringElement.appendChild(doc.createTextNode(strParse(value)))
                    rootElement.appendChild(stringElement)
                }
            }
            // 创建文件路径和文件
            val file = File(language.valuePath)
            file.parentFile.mkdirs()
            // 格式化并写入 XML 文件
            val transformerFactory = TransformerFactory.newInstance()
            val transformer = transformerFactory.newTransformer()
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes") // 省略XML声明
            val source = DOMSource(doc)
            val result = StreamResult(File(language.valuePath))
            transformer.transform(source, result)
            println("文件已生成: ${language.valuePath}")
        }
    }

    fun getExcelData(fileName: String) {
        val workbook = XSSFWorkbook(File(fileName))
        val sheet = workbook.getSheetAt(0)
        val data = mutableListOf<ITEM>()
        val rowNums = sheet.physicalNumberOfRows
        println("表格行数====${rowNums}")
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
        for (i in 1..(rowNums - 1)) {
            val row = sheet.getRow(i)
            val cellNums = row.getLastCellNum()
            println("第${i}行有${cellNums}列")
//            if (cellNums != zeroCellNUms) {
//                throw Exception("第${i}内容列数和开头标准不一致")
//            }
            //判断id 和 type 是否存在 数据是否存在
            val id = getCellValue(row.getCell(labelIdAndRoidINdex))
            val type = getCellValue(row.getCell(labelTypeIndex))
            if (id.isNullOrEmpty() || type.isNullOrEmpty()) {
                println("第${i}行的id或者type为空")
                continue
            } else {
                println("第${i}行的数据正常==")
                val currentItem = deepCopy(item)
                for (j in 0 until cellNums) {
                     println("表格数据获取第${i}行第${j}列的数据是${row.getCell(j)?.stringCellValue}========${getCellValue(row.getCell(j))}")
                    currentItem.valueList.get(j).tagValue = "${getCellValue(row.getCell(j))}"
                    // println("第${i}行第${j}列的数据是${row.getCell(j)?.stringCellValue}")
                }
                data.add(currentItem)
            }
        }

        data.forEach {
            println("表格数据::${it.valueList.toString()}\n")
        }
        println("读到了表格${data.size}条合理数据")
//        for (language in enumValues<SupprotLanguage>()) {
//            println("语言: ${language.name}, 路径: ${language.valuePath}, Excel 名称: ${language.excelName}")
//            val currentXmlHandler = readXMlFile(language.valuePath)
//            currentXmlHandler?.apply {
//                val newList = mergeData(currentXmlHandler, data, language.excelName, item)
//                println("合并了${newList.size}====${language.excelName}")
//                data.addAll(newList)
//            }
//        }
        MargeDataSta.margeExcelData(data, MargeDataSta.readALLXmlData(item))
        for (language in enumValues<SupprotLanguage>()) {
            val docFactory = DocumentBuilderFactory.newInstance()
            val docBuilder = docFactory.newDocumentBuilder()
            // 根元素
            val doc = docBuilder.newDocument()
            val rootElement = doc.createElement("resources")
            doc.appendChild(rootElement)
            // 写入 string-array 数据
            val stringArrayList = data.filter {
                it.valueList[labelTypeIndex].tagValue == "string-array"
            }.toMutableList()
            val groupedStringArray = stringArrayList.groupBy {
                it.valueList[labelIdAndRoidINdex].tagValue!!.split("$$")[0]
            }
            groupedStringArray.forEach { (key, value) ->
                val arrayElement = doc.createElement("string-array")
                arrayElement.setAttribute("name", key)
                value.forEach {
                    val id = it.valueList[labelIdAndRoidINdex].tagValue
                    val itemValue =
                        it.valueList.find { tag -> tag.tagName == language.excelName }?.tagValue
                    val itemElement = doc.createElement("item")
                    itemElement.setAttribute("name", id!!.split("$$")[1])
                    itemElement.appendChild(doc.createTextNode(strParse(itemValue)))
                    arrayElement.appendChild(itemElement)
                }
                rootElement.appendChild(arrayElement)
            }
            // 写入 string 数据
            data.forEach {
                val id = it.valueList[labelIdAndRoidINdex].tagValue
                val type = it.valueList[labelTypeIndex].tagValue
                val value = it.valueList.find { tag -> tag.tagName == language.excelName }?.tagValue
                if (type == "string") {
                    val stringElement = doc.createElement("string")
                    stringElement.setAttribute("name", id)
                    stringElement.appendChild(doc.createTextNode(strParse(value)))
                    rootElement.appendChild(stringElement)
                }
            }
            // 创建文件路径和文件
            val file = File(language.valuePath)
            file.parentFile.mkdirs()
            // 格式化并写入 XML 文件
            val transformerFactory = TransformerFactory.newInstance()
            val transformer = transformerFactory.newTransformer()
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes") // 省略XML声明
            val source = DOMSource(doc)
            val result = StreamResult(File(language.valuePath))
            transformer.transform(source, result)
            println("文件已生成: ${language.valuePath}")
        }

     /*   for (language in enumValues<SupprotLanguage>()) {
            *//*  val file = File(language.valuePath)
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
              fos.close()*//*
            val docFactory = DocumentBuilderFactory.newInstance()
            val docBuilder = docFactory.newDocumentBuilder()
            // 根元素
            val doc = docBuilder.newDocument()
            val rootElement = doc.createElement("resources")
            doc.appendChild(rootElement)
            // 写入 string-array 数据
            val stringArrayList = data.filter {
                it.valueList[labelTypeIndex].tagValue == "string-array"
            }.toMutableList()
            val groupedStringArray = stringArrayList.groupBy {
                it.valueList[labelIdAndRoidINdex].tagValue!!.split("$$")[0]
            }
            groupedStringArray.forEach { (key, value) ->
                val arrayElement = doc.createElement("string-array")
                arrayElement.setAttribute("name", key)
                value.forEach {
                    val id = it.valueList[labelIdAndRoidINdex].tagValue
                    val itemValue =
                        it.valueList.find { tag -> tag.tagName == language.excelName }?.tagValue
                    val itemElement = doc.createElement("item")
                    itemElement.setAttribute("name", id!!.split("$$")[1])
                    itemElement.appendChild(doc.createTextNode(strParse(itemValue)))
                    arrayElement.appendChild(itemElement)
                }
                rootElement.appendChild(arrayElement)
            }
            // 写入 string 数据
            data.forEach {
                val id = it.valueList[labelIdAndRoidINdex].tagValue
                val type = it.valueList[labelTypeIndex].tagValue
                val value = it.valueList.find { tag -> tag.tagName == language.excelName }?.tagValue
                if (type == "string") {
                    val stringElement = doc.createElement("string")
                    stringElement.setAttribute("name", id)
                    stringElement.appendChild(doc.createTextNode(strParse(value)))
                    rootElement.appendChild(stringElement)
                }
            }
            // 创建文件路径和文件
            val file = File(language.valuePath)
            file.parentFile.mkdirs()
            // 格式化并写入 XML 文件
            val transformerFactory = TransformerFactory.newInstance()
            val transformer = transformerFactory.newTransformer()
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes") // 省略XML声明
            val source = DOMSource(doc)
            val result = StreamResult(File(language.valuePath))
            transformer.transform(source, result)
            println("文件已生成: ${language.valuePath}")
        }*/
    }

    fun strParse(str: String?): String {
        //如果str中包含&，则替换为&amp;
        var result = str ?: ""
//        if (result.contains("&")) {
//            //println("替换>>>>$result")
//            result = result.replace("&", "&amp;")
//            //println("替换后>>>>$result")
//        }
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
        println("合并语言${excelName},${data.size}")
        currentXmlData.forEach {
            println("XML数据：：${it.id}====${it.value}")
            val id = it.id
            val value=it.value
            val find = data.find {
                it.valueList.get(labelIdAndRoidINdex).tagValue == id && it.valueList.get(
                    labelTypeIndex
                ).tagValue == "string"
            }
            if (find == null) {
                val newITem = deepCopy(item)
                newITem.valueList.get(labelIdAndRoidINdex).tagValue = it.id
                newITem.valueList.get(labelTypeIndex).tagValue = "string"
                newITem.valueList.find {
                    it.tagName == excelName
                }?.tagValue = it.value
                data.add(newITem)
            }else{
                find.valueList.get(labelIdAndRoidINdex).tagValue = id
                find.valueList.get(labelTypeIndex).tagValue = "string"
                find.valueList.find {
                    it.tagName == excelName
                }?.tagValue = value
                println("dayin:${find?.valueList}====${value}")
               // newItemList.add(newITem)
            }
        }
    /*    currentXmlArrayData.forEach { (key, value) ->
            value.forEach {
                val flag = "${key}$$${it.id}"
                var find =
                    data.find {
                        flag == it.valueList.get(labelIdAndRoidINdex).tagValue && it.valueList.get(
                            labelTypeIndex
                        ).tagValue == "string-array"
                    }
                if (find == null) {
                    val newITem = deepCopy(item)
                    newITem.valueList.get(labelIdAndRoidINdex).tagValue = flag
                    newITem.valueList.get(labelTypeIndex).tagValue = "string-array"
                    newITem.valueList.find {
                        it.tagName == excelName
                    }?.tagValue = it.value
                    data.add(newITem)
                }
            }
        }*/
        return data
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