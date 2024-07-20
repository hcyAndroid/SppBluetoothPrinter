package com.issyzone.syzbleprinter.intent

import com.issyzone.common_work.mvi.IUIEffect
import com.issyzone.common_work.mvi.IUiIntent
import com.issyzone.common_work.mvi.IUiState
import com.issyzone.common_work.mvi.LoadUiState

sealed class BluScanIntent: IUiIntent {
    object registerScan : BluScanIntent()
    data class connectDevice(val mac: String) : BluScanIntent()
    object unRegisterScan : BluScanIntent()
}
data class   BluScanUIState(val loadUiState: LoadUiState, val scanState: ScanUIState) : IUiState {}
sealed class ScanUIState {
    object INIT : ScanUIState()
}
sealed class BluScanUIEffect : IUIEffect {}

