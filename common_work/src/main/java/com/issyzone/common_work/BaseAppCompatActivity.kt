package com.issyzone.common_work

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

 abstract class BaseAppCompatActivity :AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView()
        initData()
    }
    abstract fun setContentView()
    abstract fun initData()
}