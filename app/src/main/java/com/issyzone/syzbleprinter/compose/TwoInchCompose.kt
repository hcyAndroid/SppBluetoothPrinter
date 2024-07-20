package com.issyzone.syzbleprinter.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.issyzone.syzbleprinter.R
import com.issyzone.syzbleprinter.intent.TwoInchItent
import com.issyzone.syzbleprinter.viewmodel.TwoInchViewModel
import androidx.compose.runtime.*
import kotlinx.coroutines.launch

@Composable
fun TwoInchCompose(
    mViewModel: TwoInchViewModel, clickScan: (() -> Unit)? = null, clickDisconnect: (() -> Unit)
) {
    var showBottomPop by remember { mutableStateOf(false) }
    if (showBottomPop){
        //BottomPop(isShow = true)
    }else{
       // BottomPop(isShow = false)
    }
    Surface(modifier = Modifier.fillMaxWidth()) {
        Column {
            val isConnect by mViewModel.isConnected.collectAsState()
            val connectDeviceTxt by mViewModel.connectStateText.collectAsState()
            ActivityTopBar(title = "Fm226")
            Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
                Column(
                    modifier = Modifier.background(
                        color = colorResource(id = R.color.white), shape = RoundedCornerShape(10.dp)
                    )
                ) {
                    LabelCompose3(
                        label_name = "当前设备",
                        label_value = connectDeviceTxt,
                        click = clickScan,
                        isVisibility = isConnect
                    )
                    divider()
                    Row {
                        FilledButtonExample({
                            clickDisconnect()
                        }, "断开连接", isConnect)
                        FilledTonalButtonExample(
                            onClick = {
                                showBottomPop=true
                                mViewModel.sendUiIntent(TwoInchItent.getDeviceInfo)
                            }, content = "获取设备信息", isEnable = isConnect
                        )
                    }
                    divider()
                    Row {
                        OutlinedButtonExample(
                            onClick = {
                                mViewModel.sendUiIntent(TwoInchItent.printSelf)
                            }, content = "打印自检页", isEnable = isConnect
                        )
                    }

                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomPop(isShow:Boolean) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    LaunchedEffect(sheetState) {
        if (isShow){
            sheetState.show()
        }else{
            sheetState.hide()
        }

    }
    if (isShow) {
        ModalBottomSheet(
            onDismissRequest = {

            }, sheetState = sheetState
        ) {
            // Sheet content
            Button(onClick = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible){

                    }
                }
            }) {
                Text("Hide bottom sheet")
            }
        }
    }
}