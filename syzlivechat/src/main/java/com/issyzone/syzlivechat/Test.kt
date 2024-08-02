package com.issyzone.common_work.excel

import com.issyzone.syzlivechat.SyzExcelUtils

fun main(){
    //SyzExcelUtils.writeToExcel()//写入表格
   // SyzExcelUtils.getExcelData("workbook.xlsx")
    SyzExcelUtils.getExcelData("workbook(2).xlsx")  //读取产品表格,需要读取的注明id和类型

    Thread.sleep(10000)
}