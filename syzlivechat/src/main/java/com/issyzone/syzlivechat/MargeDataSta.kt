package com.issyzone.syzlivechat

import com.issyzone.syzlivechat.ITEM
import com.issyzone.syzlivechat.StringResource
import com.issyzone.syzlivechat.SupprotLanguage
import com.issyzone.syzlivechat.SyzExcelUtils.deepCopy
import com.issyzone.syzlivechat.SyzExcelUtils.labelIdAndRoidINdex
import com.issyzone.syzlivechat.SyzExcelUtils.labelTypeIndex
import com.issyzone.syzlivechat.SyzExcelUtils.readXMlFile


object MargeDataSta {
    private val totalData = mutableListOf<ITEM>()
    private val TAG = "合并数据:::"
    fun readALLXmlData(
        item: ITEM
    ): MutableList<ITEM> {
        val totalXMlData = mutableListOf<ITEM>()
        enumValues<SupprotLanguage>().forEachIndexed { indexLanguage, language ->
            println("语言: 路径: ${language.valuePath}, Excel 名称: ${language.excelName}")
            val currentXmlHandler = readXMlFile(language.valuePath)
            val currentLanguage = language.excelName
            currentXmlHandler?.apply {
                val strXmlList = currentXmlHandler.stringResources
                val strArrayXMlList = currentXmlHandler.arrayResources
                strXmlList.forEachIndexed { index, stringResource ->
                    val findStr = totalXMlData.find {
                        it.valueList.get(labelIdAndRoidINdex).tagValue == stringResource.id && it.valueList.get(
                            labelTypeIndex
                        ).tagValue == "string"
                    }
                    if (findStr == null) {
                        //插入,以英文为标准
                        if (indexLanguage==0){
                            val item = deepCopy(item)
                            item.valueList[labelIdAndRoidINdex].tagValue = stringResource.id
                            item.valueList[labelTypeIndex].tagValue = "string"
                            item.valueList.find {
                                it.tagName == currentLanguage
                            }?.tagValue = stringResource.value
                            totalXMlData.add(item)
                        }
                    } else {
                        findStr.valueList[labelIdAndRoidINdex].tagValue = stringResource.id
                        findStr.valueList[labelTypeIndex].tagValue = "string"
                        findStr.valueList.find {
                            it.tagName == currentLanguage
                        }?.tagValue = stringResource.value
                    }
                }
                strArrayXMlList.forEach { (key, value) ->
                    value.forEach {
                        val id = "${key}$$${it.id}"
                        val arrXmlValue = it.value
                        val find = totalXMlData.find {
                            it.valueList.get(labelIdAndRoidINdex).tagValue == id && it.valueList.get(
                                labelTypeIndex
                            ).tagValue == "string-array"
                        }
                        if (find == null) {
                            if (indexLanguage==0){
                                val newItem = deepCopy(item)
                                newItem.valueList.get(labelIdAndRoidINdex).tagValue = id
                                newItem.valueList.get(labelTypeIndex).tagValue = "string-array"
                                newItem.valueList.find {
                                    it.tagName == currentLanguage
                                }?.tagValue = arrXmlValue
                                totalXMlData.add(newItem)
                            }

                        } else {
                            find.valueList[labelIdAndRoidINdex].tagValue = id
                            find.valueList[labelTypeIndex].tagValue = "string-array"
                            find.valueList.find {
                                it.tagName == currentLanguage
                            }?.tagValue = arrXmlValue
                        }
                    }
                }
            }
        }
        totalXMlData.forEach {
            println("读取本地XML数据${totalXMlData.size}===${it}\n")
        }
        return totalXMlData
    }

    fun margeExcelData(
        excelData: MutableList<ITEM>,
        strXmlData: MutableList<ITEM>,
    ) : MutableList<ITEM>{
        val xmlcount=strXmlData.size
        val excelcount=excelData.size
        excelData.forEachIndexed { index, excelItem ->
            val find = strXmlData.find {
                it.valueList.get(labelIdAndRoidINdex).tagValue == excelItem.valueList.get(
                    labelIdAndRoidINdex
                ).tagValue && it.valueList.get(labelTypeIndex).tagValue == excelItem.valueList.get(
                    labelTypeIndex
                ).tagValue
            }
            if (find==null){
                strXmlData.add(excelItem)
            }else{
               excelItem.valueList.forEach {ex->
                   find.valueList.find {
                       it.tagName == ex.tagName
                   }?.tagValue = ex.tagValue
               }
            }
        }
        strXmlData.forEach {
            println("合并表格之后XML数据${strXmlData.size}=合并前xml${xmlcount}表格${excelcount}====${it}\n")
        }
        return  strXmlData
    }
}