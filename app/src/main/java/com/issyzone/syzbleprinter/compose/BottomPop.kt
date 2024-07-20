//package com.issyzone.syzbleprinter.compose
//
//
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxHeight
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Button
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.ModalBottomSheet
//import androidx.compose.material3.Text
//import androidx.compose.material3.rememberModalBottomSheetState
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun BottomPop() {
////    val sheetState = rememberModalBottomSheetState()
////    val scope = rememberCoroutineScope()
////    var showBottomSheet by remember { mutableStateOf(false) }
//    ModalBottomSheet(
//        onDismissRequest = {
//
//        }
//    ) {
//        Text("Hide bottom sheet")
//        // Sheet content
////        Button(onClick = {
//////            scope.launch { sheetState.hide() }.invokeOnCompletion {
//////                if (!sheetState.isVisible) {
//////                    showBottomSheet = false
//////                }
//////            }
////        }) {
////            Text("Hide bottom sheet")
////        }
//    }
//}
//
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun PartialBottomSheet() {
//    var showBottomSheet by remember { mutableStateOf(false) }
//    val sheetState = rememberModalBottomSheetState(
//        skipPartiallyExpanded = false,
//    )
//
//    Column(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalAlignment = Alignment.CenterHorizontally,
//    ) {
//        Button(
//            onClick = { showBottomSheet = true }
//        ) {
//            Text("Display partial bottom sheet")
//        }
//
//        if (showBottomSheet) {
//            ModalBottomSheet(
//                modifier = Modifier.fillMaxHeight(),
//                sheetState = sheetState,
//                onDismissRequest = { showBottomSheet = false }
//            ) {
//                Text(
//                    "Swipe up to open sheet. Swipe down to dismiss.",
//                    modifier = Modifier.padding(16.dp)
//                )
//            }
//        }
//    }
//}