package com.issyzone.syzbleprinter

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.issyzone.syzbleprinter.databinding.ActivityWelcomeBinding

import com.issyzone.syzbleprinter.utils.invokeViewBinding

class WelcomeActivity : ComponentActivity() {
    private val vm: ActivityWelcomeBinding by invokeViewBinding()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(vm.root)
        vm.tvTwo.setOnClickListener {
            startActivity(Intent(WelcomeActivity@ this, MainActivity4::class.java))
        }

        vm.tvTwoNew.setOnClickListener {
            startActivity(Intent(WelcomeActivity@ this, MainActivity8::class.java))
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

    }
}