package com.issyzone.classicblulib.bean

import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

object LogLiveData {
    private val liveData = MutableSharedFlow<String>()
    private var log = ""
    private var isTest=true


    fun addLogs(value: String) {
        if (isTest){
            CoroutineScope(Dispatchers.IO).launch {
                liveData.emit(value)
            }
        }

    }
    fun clearLog(view: TextView){
        if (isTest){
            log=""
            view.text = log
        }
    }

    fun showLogs(lifecycleOwner: LifecycleOwner, view: TextView) {
        if (isTest){
            CoroutineScope(Dispatchers.Main).launch {
                liveData?.apply {
                    this.asSharedFlow().collectLatest {
                        log = log + "\n" + it
                        view.text = log
                    }
                }

            }
        }
    }
}