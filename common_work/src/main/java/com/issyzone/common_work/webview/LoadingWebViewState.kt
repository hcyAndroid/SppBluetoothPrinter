package com.issyzone.common_work.webview

sealed class LoadingWebViewState {
    object NotLoading : LoadingWebViewState()
    object HorizontalProgressBarLoadingStyle : LoadingWebViewState()
    object ProgressBarLoadingStyle : LoadingWebViewState()
    object CustomLoadingStyle : LoadingWebViewState()
}