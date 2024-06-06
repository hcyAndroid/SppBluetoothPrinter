/*
package com.issyzone.syzbleprinter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseEISViewModel<E : IUIEffect, I : IUiIntent, S : IUiState> : ViewModel() {

    private var _uiStateFlow = MutableStateFlow(initUiState())
    val uiStateFlow: StateFlow<S> = _uiStateFlow

    //页面事件的 Channel 分发
    private val _uiIntentFlow = Channel<I>(Channel.UNLIMITED)

    //更新State页面状态 (data class 类型)
    fun updateUiState(reducer: S.() -> S) {
        _uiStateFlow.update { reducer(_uiStateFlow.value) }
    }

    //更新State页面状态 (sealed class 类型)
    fun sendUiState(s: S) {
        _uiStateFlow.value = s
    }

    //发送页面事件
    fun sendUiIntent(uiIntent: I) {
        viewModelScope.launch {
            _uiIntentFlow.send(uiIntent)
        }
    }

    init {
        // 这里是通过Channel的方式自动分发的。
        viewModelScope.launch {
            //收集意图 （观察者模式改变之后就自动更新）用于协程通信的，所以需要在协程中调用
            _uiIntentFlow.consumeAsFlow().collect { intent ->
                handleIntent(intent)
            }
        }

    }

    //每个页面的 UiState 都不相同，必须实自己去创建
    protected abstract fun initUiState(): S

    //每个页面处理的 UiIntent 都不同，必须实现自己页面对应的状态处理
    protected abstract fun handleIntent(intent: I)

    //一次性事件，无需更新
    private val _effectFlow = MutableSharedFlow<E>()
    val uiEffectFlow: SharedFlow<E> by lazy { _effectFlow.asSharedFlow() }

    //两种方式发射，在协程外用viewModelScope发射
    protected fun sendEffect(builder: suspend () -> E?) = viewModelScope.launch {
        builder()?.let { _effectFlow.emit(it) }
    }

    //两种方式发射,suspend 协程中直接发射
    protected suspend fun sendEffect(effect: E) = _effectFlow.emit(effect)

}
*/
