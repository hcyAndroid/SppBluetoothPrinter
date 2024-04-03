package com.issyzone.blelibs.utils

import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object TextUtils {
    var tv: TextView? = null
    var text: String = ""
    var lifeScope: LifecycleCoroutineScope?=null
    fun log(str: String) {
        text=""
        text = text + "\n" + str

        lifeScope?.launch(Dispatchers.Main) {
            tv?.setText(text)
        }
    }



}