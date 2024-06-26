package com.issyzone.common_work

import android.view.LayoutInflater
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import java.lang.IllegalStateException
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

inline fun <reified VB : ViewBinding> AppCompatActivity.invokeViewBinding() =
    ActivityInflateBindingProperty(VB::class.java)

inline fun <reified VB : ViewBinding> ComponentActivity.invokeViewBinding() =
    ActivityInflateBindingProperty2(VB::class.java)

inline fun <reified VM : ViewModel> AppCompatActivity.invokeViewModel() =
    ActivityViewModelProperty(VM::class.java)


inline fun <reified VM : ViewModel> ComponentActivity.invokeViewModel() =
    ActivityViewModelProperty2(VM::class.java)


class ActivityViewModelProperty2<VM : ViewModel>(private val claz: Class<VM>) :
    ReadOnlyProperty<ComponentActivity, VM> {
    private var vm: VM? = null
    override fun getValue(thisAct: ComponentActivity, property: KProperty<*>): VM {
        if (vm == null) {
            vm = ViewModelProvider(thisAct, ViewModelProvider.NewInstanceFactory())[claz]
        }
        return vm!!
    }
}

class ActivityViewModelProperty<VM : ViewModel>(private val claz: Class<VM>) :
    ReadOnlyProperty<AppCompatActivity, VM> {
    private var vm: VM? = null
    override fun getValue(thisAct: AppCompatActivity, property: KProperty<*>): VM {
        if (vm == null) {
            vm = ViewModelProvider(thisAct, ViewModelProvider.NewInstanceFactory())[claz]
        }
        return vm!!
    }
}

class ActivityInflateBindingProperty<VB : ViewBinding>(private val claz: Class<VB>) :
    ReadOnlyProperty<AppCompatActivity, VB> {
    private var binding: VB? = null
    override fun getValue(thisAct: AppCompatActivity, property: KProperty<*>): VB {
        if (binding == null) {
            try {
                binding = (claz.getMethod("inflate", LayoutInflater::class.java)
                    .invoke(null, thisAct.layoutInflater)) as VB
            } catch (e: IllegalStateException) {
                e.printStackTrace()

            }
            thisAct.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    super.onDestroy(owner)
                    binding = null
                }
            })
        }
        return binding!!
    }
}

class ActivityInflateBindingProperty2<VB : ViewBinding>(private val claz: Class<VB>) :
    ReadOnlyProperty<ComponentActivity, VB> {
    private var binding: VB? = null
    override fun getValue(thisAct: ComponentActivity, property: KProperty<*>): VB {
        if (binding == null) {
            try {
                binding = (claz.getMethod("inflate", LayoutInflater::class.java)
                    .invoke(null, thisAct.layoutInflater) as VB)
            } catch (e: IllegalStateException) {
                e.printStackTrace()

            }
            thisAct.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    super.onDestroy(owner)
                    binding = null
                }
            })
        }
        return binding!!
    }
}