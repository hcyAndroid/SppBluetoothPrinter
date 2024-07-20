package com.issyzone.syzbleprinter

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.issyzone.common_work.koin.get
import com.issyzone.common_work.utils.MyCrashUtils
import com.issyzone.syzbleprinter.compose.LabelCompose
import com.issyzone.syzbleprinter.compose.LabelCompose2
import com.issyzone.syzbleprinter.compose.divider
import com.issyzone.syzbleprinter.databinding.ActivityWelcomeBinding
import com.issyzone.syzbleprinter.koin_test.KoinSingleTest
import com.issyzone.syzbleprinter.koin_test.KoinTest

import com.issyzone.syzbleprinter.utils.invokeViewBinding
import com.tencent.bugly.crashreport.CrashReport
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class WelcomeActivity : ComponentActivity() {
    private val vm: ActivityWelcomeBinding by invokeViewBinding()
    private val koinTest = get<KoinTest>(parameters = { parametersOf(100) })
    private val koSingTes: KoinSingleTest by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(vm.root)
        lifecycleScope.launch {
            MyCrashUtils.readAllCrashLogs(this@WelcomeActivity).forEach {
                if (!it.isNullOrEmpty()) {
                    // Log.i(TAG, "bugly: $it")
                    CrashReport.postCatchedException(Throwable(it))
                }
            }
        }
        val clickTwo= {
            startActivity(Intent(WelcomeActivity@ this, TwoInchActivity::class.java))
        }
        vm.composeView.setContent {
            Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                Column(
                    modifier = Modifier.background(
                            color = colorResource(id = R.color.white),
                            shape = RoundedCornerShape(10.dp)
                        )
                ) {
                    LabelCompose2(label_name = "二寸机器", label_value = "FM226",click = clickTwo)
                    divider()
                    LabelCompose2(label_name = "四寸机器", label_value = "RW402B")
                    divider()
                    LabelCompose2(label_name = "调试", label_value = "双线性/三线性采样")
                    divider()
                    LabelCompose2(label_name = "调试", label_value = "自动化测试")
                }
            }

        }




        koinTest.test()
        koSingTes.test()
        vm.tvTwo.setOnClickListener {
            startActivity(Intent(WelcomeActivity@ this, MainActivity4::class.java))
        }

        vm.tvTwoNew.setOnClickListener {
            startActivity(Intent(WelcomeActivity@ this, TwoInchActivity::class.java))
        }

        vm.tvFour.setOnClickListener {
            startActivity(Intent(WelcomeActivity@ this, MainActivity3::class.java))
        }
        vm.tvFourNew.setOnClickListener {
            startActivity(Intent(WelcomeActivity@ this, MainActivity7::class.java))
        }
        vm.tvPic.setOnClickListener {
            startActivity(Intent(WelcomeActivity@ this, MainActivity5::class.java))

        }
        vm.tvTestA4.setOnClickListener {
            startActivity(Intent(WelcomeActivity@ this, A4TestActivity::class.java))
        }
        vm.tvTest5.setOnClickListener {
            startActivity(Intent(WelcomeActivity@ this, MainActivity10::class.java))
        }

        vm.tvTest6.setOnClickListener {
            startActivity(Intent(WelcomeActivity@ this, MainActivity9::class.java))
        }

    }
}