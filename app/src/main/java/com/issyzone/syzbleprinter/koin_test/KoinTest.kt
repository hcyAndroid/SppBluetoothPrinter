package com.issyzone.syzbleprinter.koin_test

import android.content.Context
import android.util.Log
import com.issyzone.syzbleprinter.R
import org.koin.dsl.module

class KoinTest(val context: Context,val number:Int) {
    private val TAG="KoinTest>>>"
    fun test() {
       Log.i(TAG,"KoinTest test==${context.resources.getString(R.string.app_name)}====${number}")
    }
}

