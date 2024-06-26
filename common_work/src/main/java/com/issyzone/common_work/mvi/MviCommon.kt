package com.issyzone.common_work.mvi

import androidx.annotation.Keep

//MVI  页面事件管理的基类
@Keep
interface  IUiIntent

@Keep  //页面状态管理的基类
interface  IUiState

@Keep  //用shareflow来实现一次性状态
interface  IUIEffect

sealed class  LoadUiState{
    object Idle:LoadUiState()
    data class Loading(val isShow:Boolean):LoadUiState()
    object showContent:LoadUiState()
    data class showError(val error:Throwable):LoadUiState()
}

