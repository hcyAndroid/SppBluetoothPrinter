package com.issyzone.common_work.mvi

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseMviViewModel<UiItent:IUiIntent,UiState:IUiState,UIEffect:IUIEffect> : BaseViewModel() {
    //不是一次性事件，需要更新状态
    private val _uiStateFlow by lazy {
        MutableStateFlow(initUiState())
    }
    val uiStateFlow: StateFlow<UiState> by lazy {
        _uiStateFlow.asStateFlow()
    }
    //一次性事件，无需更新状态
    private val _uiEffectFlow= MutableSharedFlow<UIEffect>()
    val uiEffectFlow:SharedFlow<UIEffect> by lazy {
        _uiEffectFlow.asSharedFlow()
    }

    //页面事件分发
    private val _uiIntentFlow=Channel<IUiIntent>(Channel.UNLIMITED)

    //发送一次性事件
    protected fun sendEffect(builder:suspend ()->UIEffect?)=viewModelScope.launch {
        builder()?.let {
            _uiEffectFlow.emit(it)
        }
    }

    protected suspend fun  sendEffect(uiEffect: UIEffect){
        _uiEffectFlow.emit(uiEffect)
    }

    //更新页面状态
    fun updateUiState(reducer: UiState.() -> UiState) {
        _uiStateFlow.update { reducer(uiStateFlow.value) }
    }


    fun sendUiIntent(uiIntent: IUiIntent){
        viewModelScope.launch {
            _uiIntentFlow.send(uiIntent)
        }
    }
    init {
        viewModelScope.launch {
            _uiIntentFlow.consumeAsFlow().collect {
                handleIntent(it)
            }
        }
    }

    // 每个页面的 UiState 都不相同，必须实自己去创建
    protected abstract fun initUiState(): UiState

    //每个页面处理的 UiIntent 都不同，必须实现自己页面对应的状态处理
    protected abstract fun handleIntent(intent: IUiIntent)
}