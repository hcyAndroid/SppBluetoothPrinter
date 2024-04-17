package com.issyzone.syzbleprinter


import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity

import com.issyzone.blelibs.utils.BitmapExt
import com.issyzone.syzbleprinter.databinding.ActivityMain5Binding
import com.issyzone.syzbleprinter.utils.OpenCVUtils
import com.issyzone.syzbleprinter.utils.invokeViewBinding
import org.opencv.android.OpenCVLoader

class MainActivity5 : ComponentActivity() {
    private val vm: ActivityMain5Binding by invokeViewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(vm.root)
        val tag = OpenCVLoader.initLocal()
        if (tag) {
            Log.i(">>>>>>>>", "opencv加载成功")
        } else {
            Log.i(">>>>>>>>", "opencv加载失败")
        }
        vm.iv.setImageBitmap(BitmapExt.decodeBitmap(R.drawable.test15))
        vm.iv2.setImageBitmap(OpenCVUtils.testOpencv2(R.drawable.test15,(48*8).toDouble(),(48*8).toDouble()))
        vm.iv3.setImageBitmap(OpenCVUtils.testOpencv3(R.drawable.test15,(48*8).toDouble(),(48*8).toDouble()))
    }
}