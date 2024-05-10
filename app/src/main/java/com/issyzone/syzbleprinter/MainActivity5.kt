package com.issyzone.syzbleprinter


import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity

import com.issyzone.blelibs.utils.BitmapExt
import com.issyzone.blelibs.utils.ImageUtilKt
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
        vm.iv.setImageBitmap( ImageUtilKt.convertBinary(BitmapExt.decodeBitmap(R.drawable.test222)))

        vm.iv2.setImageBitmap( ImageUtilKt.bitmap2OTSUBitmap(BitmapExt.decodeBitmap(R.drawable.test222)))
        vm.iv3.setImageBitmap(ImageUtilKt.grayAverageBitmap2BinaryBitmap(BitmapExt.decodeBitmap(R.drawable.test222)))
    }
}