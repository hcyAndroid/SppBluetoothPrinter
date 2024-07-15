package com.issyzone.syzbleprinter

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.issyzone.common_work.koin.get
import com.issyzone.common_work.utils.MyCrashUtils
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
    private val koinTest=get<KoinTest>(parameters = { parametersOf(100) })
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