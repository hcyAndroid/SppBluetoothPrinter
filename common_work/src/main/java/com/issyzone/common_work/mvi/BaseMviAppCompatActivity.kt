package com.issyzone.common_work.mvi
import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.issyzone.common_work.BaseAppCompatActivity
import java.lang.reflect.ParameterizedType


abstract class BaseMviAppCompatActivity<VM:ViewModel, VB: ViewBinding> :BaseAppCompatActivity(){
    protected lateinit var mViewModel: VM

    private var _binding: VB? = null
    protected val mBinding: VB
        get() = requireNotNull(_binding) { "ViewBinding对象为空" }

    //反射创建ViewModel
    open protected fun createViewModel(): VM {
        return ViewModelProvider(this).get(getVMCls(this))
    }
    protected fun getVMCls(obj: Any): Class<VM> {
        return (obj.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<VM>
    }

    //反射创建ViewBinding
    open protected fun createViewBinding() {
        val clazz: Class<*> =  (this.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[1] as Class<VB>
        try {
            _binding = clazz.getMethod(
                "inflate", LayoutInflater::class.java
            ).invoke(null, layoutInflater) as VB
        } catch (e: Exception) {
            e.printStackTrace()
            throw IllegalArgumentException("无法通过反射创建ViewBinding对象")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        createViewBinding()
        super.onCreate(savedInstanceState)
    }

    override fun setContentView() {
        setContentView(mBinding.root)
        mViewModel = createViewModel()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}