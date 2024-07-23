# SPP协议蓝牙打印机SDK开发文档

## 1. 项目简介

SPP打印机是一款支持蓝牙打印的打印机，支持Android、iOS、Windows等多个平台，支持ESC/POS指令集，支持图片打印、文字打印、条码打印等功能。本文档主要介绍SPP打印机的使用方法，包括连接打印机、打印图片、打印文字、打印条码等功能。
## 版本历史和更新日志
| 版本号    | 更新日期       | 更新内容        |
|--------|------------|-------------|
| v1.5.6 | 2024-05-20 | SDK初步完成并且自测 |

## 2. 连接打印机

### 2.1 Android

#### 2.1.1 添加权限

在AndroidManifest.xml文件中添加蓝牙权限
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />
    <uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
</manifest>

#### 2.1.2 初始化SDK
```kotlin
 SyzClassicBluManager.getInstance().initClassicBlu()
```
#### 2.1.3 回调监听
##### 主动上报回调
```kotlin
        SyzClassicBluManager.getInstance().setActivelyReportBack {
            Log.i("主动上报的》》》》", it.toString())
        }
```

###### 主动回调枚举类介绍 SyzPrinterState2
| 索引   | 状态 | 描述 | 顺序 | 英文名 |
|------| ---- | ---- |----| ---- |
| 0    | true | 打印中 | 7  | PRINTER_PRINTING |
| 1    | true | 缺纸 | 2  | PRINTER_NO_PAPAER |
| 2    | true | 打印缓存满 | 8  | PRINTER_OOM |
| 3    | true | 开盖 | 1  | PRINTER_LID_OPEN |
| 4    | true | 卡纸 | 4  | PRINTER_STRUCK_PAPER |
| 5    | true | 打印头高温 | 3  | PRINTER_HIGH_TEMPERATURE |
| 6    | true | 电池电量低 | 5  | PRINTER_BATTERY_LOW |
| 7    | true | 马达过热 | 6  | PRINTER_MOTOR_HIGH_TEMPERATURE |
| 1000 | true | 打印机正常 | 9  | PRINTER_OK |
| 1001 | true | 打印机状态无法通过获取设备信息获取 | 10 | PRINTER_STATUS_UNKNOWN |
| 1002 | true | 取消打印回调 | 11 | PRINTER_CANCEL_PRINT |
| 1003 | true | 打印过程中解包失败 | 12 | PRINTER_UPACKER_FAILED |
| 1004 | true | 打印机学纸了 | 13 | PRINTER_HAS_STUDY_PAPER |
| 1005 | true | 打印机没有学纸 | 14 | PRINTER_NO_STUDY_PAPER |
| 1006 | true | 自检页打印失败 | 15 | PRINTER_SELF_PRINT_FAIL |
| 1007 | true | 设置纸张类型超时 | 16 | PRINTER_SET_PAPER_TYPE_OUTTIME |
| 1008 | true | 设置纸张类型成功 | 17 | PRINTER_SET_PAPER_TYPE_OK |
| 1009 | true | 设置纸张类型失败 | 18 | PRINTER_SET_PAPER_TYPE_FAILED |
| 1010 | true | 蓝牙接收打印数据异常 | 19 | PRINTER_PRINTING_DEVICE_ERROR |

前7个位打印机硬件返回的状态，后面为SDK返回的状态。顺序是指有一个权重

##### 纸张类型上报回调
```kotlin
        SyzClassicBluManager.getInstance().setPaperReportCallBack {
            Log.i("${TAG}纸张尺寸上报的》》》》", "width==${it.paper_width}===height==${it.pager_height}==printerType==${it.printerState2.string}")
            LogLiveData.addLogs("纸张尺寸上报的:>>>width==${it.paper_width}===height==${it.pager_height}==printerType==${it.printerState2.string}")
        }
```
###### 纸张类型上报回调 SyzPrinterPaper介绍
| 英文名 |  描述 |
| ---- | ---- |
| SyzPrinterState2 | 判断当前是否学纸 PRINTER_HAS_STUDY_PAPER or PRINTER_NO_STUDY_PAPER|
| paper_width | 纸张宽度 |
| pager_height | 纸张高度 |

##### 蓝牙状态上报回调
```kotlin
    SyzClassicBluManager.getInstance().setBluCallBack(object : SyzBluCallBack {
    override fun onStartConnect() {
        Log.i("SYZ>>>", "开始连接")
        LogLiveData.addLogs("开始连接")
    }

    override fun onConnectFail(msg: String?) {
        Log.i("SYZ>>>", "onConnectFail")
        LogLiveData.addLogs("经典蓝牙连接失败==${msg}")

    }

    override fun onConnectSuccess(device: BluetoothDevice) {
        if (ContextCompat.checkSelfPermission(this@MainActivity3, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
            Log.i("SYZ>>>", "onConnectSuccess==${device.name}====${device.address}")
            LogLiveData.addLogs("经典蓝牙连接成功==${device.name}====${device.address}")
            SpUtils.saveData("mac4", device.address)
        }

    }

    override fun onDisConnected() {
        Log.i("SYZ>>>", "onDisConnected")
        LogLiveData.addLogs("经典蓝牙已经断开")
        // LogLiveData.clearLog(vm.tvLog)
    }
})
```
#### 2.1.4 连接打印机
```kotlin
SyzClassicBluManager.getInstance().connect(mac)
```
mac :spp 打印机的adress地址
#### 2.1.5 断开连接
```kotlin
SyzClassicBluManager.getInstance().disConnectBlu()
```
#### 2.1.6 协议约定
| MPMessage.EventType | 描述 | 方法调用名 |
| --- | --- | --- |
| DEVICEINFO | 获取打印信息 | `getDeviceInfo()` |
| PRINTINEND | 发送结束打印的命令 | `orderForEndPrint()` |
| SELFTEST | 打印自检页的命令 | `writeSelfCheck()` |
| CLOSETIME | 设置关机时间的命令 | `writeShutdown(min)` |
| CANCELPRINTING | 设备取消打印的命令 | `writeCancelPrinter()` |
| PRINTINGSPEED | 设置打印速度的命令 | `writePrintSpeed(speed, printer)` |
| PRINTINCONCENTRATION | 设置打印浓度的命令 | `writePrintConcentration(concentration, printer)` |
#### 2.1.7 获取设备信息
```kotlin
    SyzClassicBluManager.getInstance().getDeviceInfo(object : DeviceInfoCall {
    override fun getDeviceInfo(msg: MPMessage.MPDeviceInfoMsg) {
        Log.i("获取设备信息>>", "${msg.toString()}")
    }

    override fun getDeviceInfoError(errorMsg: MPMessage.MPCodeMsg) {

    }
})
```
MPMessage.MPDeviceInfoMsg介绍
| 参数名称 | 类型 | 描述 |
| ---- | ---- | ---- |
| mac | String | 设备的MAC地址 |
| sn | String | 设备的序列号（SN） |
| firmwareVer | String | 设备的固件版本 |
| paperStatus | int | 纸张状态，可能的值根据设备的具体实现可能会有所不同 |
| elec | int | 设备的电量 |
| concentration | int | 打印的浓度设置，范围通常是1-8 |
| speed | int | 打印的速度设置，范围通常是1-4 |
| paperSize | int | 纸张尺寸类型，具体的值和含义根据设备的具体实现可能会有所不同 |
| printStatus | String | 打印机的状态 |
#### 2.1.8 打印自检页
```kotlin
   SyzClassicBluManager.getInstance().writeSelfCheck(object :BluSelfCheckCallBack{
    override fun getPrintResult(isSuccess: Boolean, msg: SyzPrinterState2) {
        Log.e("打印自检页的结果::","${isSuccess}==${msg}")
    }

})
```
该方法会先调取设备信息查询打印状态，如果通过才会发送打印自检页指令

#### 2.1.9 设置关机时间
```kotlin
            SyzClassicBluManager.getInstance().writeShutdown(2, object : DeviceBleInfoCall {
                override fun getBleNotifyInfo(isSuccess: Boolean, msg: MPMessage.MPCodeMsg?) {
                    if (isSuccess) {
                        Log.d("", "设置关机时间成功>>>>${msg.toString()}")
                    } else {
                        Log.d("", "设置关机时间失败>>>>${msg.toString()}")
                    }
                }
            })
```
2寸有关机时间，4寸没有关机时间
#### 2.1.10 设备取消打印
2寸是点击取消打印按钮，发送取消指令，立马就显示取消成功，不用等指令回调。同时阻止下一张图片
4寸是点击取消打印按钮，发送取消指令，立马就显示取消成功，不用等指令回调。同时阻止下一包数据
```kotlin
    SyzClassicBluManager.getInstance().writeCancelPrinter(object : CancelPrintCallBack {
                override fun cancelSuccess() {
                    Log.d("", "取消打印成功>>>>}")
                }

                override fun cancelFail() {
                    Log.d("", "取消打印失败>>>>}")
                }

            })
```
#### 2.1.11 设置打印速度
```kotlin
    SyzClassicBluManager.getInstance().writePrintSpeed(2, object : DeviceBleInfoCall {
                override fun getBleNotifyInfo(isSuccess: Boolean, msg: MPMessage.MPCodeMsg?) {
                    if (isSuccess) {
                        Log.d("", "设置打印速度成功>>>>${msg.toString()}")
                    } else {
                        Log.d("", "设置打印速度失败>>>>${msg.toString()}")
                    }
                }
            })
```
2寸没有打印速度，设备不接受打印速度，统一设置了速度.4寸有打印速度（ 2寸1到4【理论】， 4寸的1到8）

#### 2.1.12 设置打印浓度
```kotlin
     SyzClassicBluManager.getInstance()
    .writePrintConcentration(vm.etPrintConcentration.text.toString().toInt(),
        object : DeviceBleInfoCall {
            override fun getBleNotifyInfo(
                isSuccess: Boolean, msg: MPMessage.MPCodeMsg?
            ) {
                if (isSuccess) {
                    Log.d("", "设置打印浓度成功>>>>${msg.toString()}")
                } else {
                    Log.d("", "设置打印浓度失败>>>>${msg.toString()}")
                }
            }
        })
```
2寸1..8 四寸的1..16

#### 2.1.13 打印图片
##### 2寸打印图片
2寸打印图片有检查设备信息，查询设备信息的纸张类型和当前app的模版的纸张类型做匹配，如果不匹配，会返回纸张类型错误的回调(此时app选择性调用设置纸张类型的命令)

```kotlin
    SyzClassicBluManager.getInstance().printBitmaps(mutableListOf(
    bitmap5,
), width, height, page, object : BluPrintingCallBack {
    override fun printing(currentPrintPage: Int, totalPage: Int) {
        Log.i("${TAG}>>>", "printing=====${currentPrintPage}=====${totalPage}")
    }

    override fun getPrintResult(isSuccess: Boolean, msg: SyzPrinterState2) {
        if (isSuccess) {
            Log.i("${TAG}>>>", "打印成功>>>>${isSuccess}===${msg}")
        } else {
            Log.i("${TAG}>>>", "打印失败>>>>${isSuccess}===${msg}")
        }
    }
})

```kotlin
   SyzClassicBluManager.getInstance().printBitmaps(mutableListOf(
    bitmap5,
), width, height, page, object : BluPrintingCallBack {
    override fun printing(currentPrintPage: Int, totalPage: Int) {
        Log.i("${TAG}>>>", "printing=====${currentPrintPage}=====${totalPage}")
    }

    override fun getPrintResult(isSuccess: Boolean, msg: SyzPrinterState2) {
        if (isSuccess) {
            Log.i("${TAG}>>>", "打印成功>>>>${isSuccess}===${msg}")
        } else {
            Log.i("${TAG}>>>", "打印失败>>>>${isSuccess}===${msg}")
        }
    }
})
```
* bitmap5:图片,传入bitmap集合（bitmap集合里的bitmap，如果重复，请深度复制，不能传入多个相同的bitmap）
* width:图片宽度 （纸张尺寸 50*50）
* height:图片高度 （无效，随便填）
* page:打印页数（2寸请写死1,4寸可以传入多页,呈现AABB形式打印，想要PDF打印请传1）
* BluPrintingCallBack:打印回调  成功会返回OK枚举，否则出错：一般是开盖，缺纸，没电，高温等等
#### 2.1.14 发送纸张类型【二寸】
```kotlin
    suspend fun setPaperType(syzPaperSize: SyzPaperSize): SyzPrinterState2 {
        return withTimeoutOrNull(ORDER_TIME_OUT) {
            val setPaperSetTask = async { setPaperSet(syzPaperSize) }
            setPaperSetTask.await()
        } ?: SyzPrinterState2.PRINTER_SET_PAPER_TYPE_OUTTIME
    }
```


# SPP打印的硬件逻辑
## 打印机连接后需要获取设备信息，来触发主动上报
## 打印机图片打印 code11 info2 



