package com.issyzone.syzbleprinter.viewmodel

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.issyzone.classicblulib.bean.SyzPrinter
import com.issyzone.classicblulib.service.SyzClassicBluManager
import com.issyzone.classicblulib.tools.BTManager
import com.issyzone.classicblulib.tools.DiscoveryListener
import com.issyzone.common_work.mvi.BaseMviViewModel
import com.issyzone.common_work.mvi.IUiIntent
import com.issyzone.common_work.mvi.LoadUiState
import com.issyzone.syzbleprinter.intent.BluScanIntent
import com.issyzone.syzbleprinter.intent.BluScanUIEffect
import com.issyzone.syzbleprinter.intent.BluScanUIState
import com.issyzone.syzbleprinter.intent.ScanUIState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScanBluViewModel : BaseMviViewModel<BluScanIntent, BluScanUIState, BluScanUIEffect>() {
    override fun initUiState(): BluScanUIState {
        return BluScanUIState(loadUiState = LoadUiState.Idle, scanState = ScanUIState.INIT)
    }

    private val _devices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val devices: StateFlow<List<BluetoothDevice>> = _devices

    private suspend fun addDevice(device: BluetoothDevice, rssi: Int) =
        withContext(Dispatchers.IO) {
            Log.i(TAG, "onDeviceFound${device.name}====${device.address}===${rssi}")
            if (device.name.isNullOrEmpty() || device.address.isNullOrEmpty()) {
                return@withContext
            }
            SyzPrinter.values().find {
                    device.name.lowercase().startsWith(it.device.lowercase())
                } ?: return@withContext
            _devices.value.find {
                it.address == device.address && it.name == device.name
            }?.let {
                return@withContext
            }
            _devices.value = _devices.value + device
        }

    private val TAG = "ScanBluViewModel"
    override fun handleIntent(intent: IUiIntent) {
        when (intent) {
            is BluScanIntent.registerScan -> {
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        BTManager.getInstance().registerBluReciver()
                        BTManager.getInstance().addDiscoveryListener(object : DiscoveryListener {
                            override fun onDiscoveryStart() {
                                Log.i(TAG, "onDiscoveryStart")
                            }

                            override fun onDiscoveryStop() {
                                Log.i(TAG, "onDiscoveryStop")
                            }

                            override fun onDeviceFound(device: BluetoothDevice, rssi: Int) {
                                viewModelScope.launch {
                                    addDevice(device,rssi)
                                }
                            }

                            override fun onDiscoveryError(errorCode: Int, errorMsg: String) {
                                Log.i(TAG, "onDiscoveryError$errorCode====$errorMsg")
                            }
                        })
                        if (BTManager.getInstance().isDiscovering) {
                            BTManager.getInstance().stopDiscovery()
                        }
                        BTManager.getInstance().startDiscovery()
                    }
                }
            }
            is BluScanIntent.unRegisterScan -> {
                Log.i(TAG, "unRegisterScan")
                if (BTManager.getInstance().isDiscovering) {
                    BTManager.getInstance().stopDiscovery()
                }
                BTManager.getInstance().unRegisterBroadCaster()
            }
            is  BluScanIntent.connectDevice -> {
                Log.i(TAG, "connectDevice${intent.mac}")
                connect(intent.mac)
            }
        }
    }
    private fun connect(mac: String) {
        SyzClassicBluManager.getInstance().connect(mac)
    }
}