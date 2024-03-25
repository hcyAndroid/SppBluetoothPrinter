package com.issyzone.blelibs.permission

import android.Manifest
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts



object SYZBlePermission {
    private const val TAG = "SYZBlePermission>>>:"
    private val permissionToRequest =if (Build.VERSION.SDK_INT>30){
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
        )
    }else{
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    }

    private fun requestBlePermissionLauncherFun(
        context: ComponentActivity, call: () -> Unit
    ): ActivityResultLauncher<Array<String>> {
        return context.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.all { it.value }
            if (allGranted) {
                // 所有权限均已授予
               // Logger.d(">>>>>所有的权限已经授予")
                //startBluetoothScan()
                call.invoke()
            } else {
                // 有权限被拒绝，您可以向用户显示一个提示或者禁用相应功能
                //Logger.d(">>>>>所有的权限没有授予")
                if (permissionToRequest.any { !permissions[it]!! }) {
                    // AppGlobels.showAppSettings(context)
                    //Logger.d("${TAG}需要手动设置权限")
                }
            }
        }
    }

    // 检查并请求位置权限
    fun checkBlePermission(context: ComponentActivity, call: () -> Unit) {
        requestBlePermissionLauncherFun(context, call).launch(permissionToRequest)
    }


}