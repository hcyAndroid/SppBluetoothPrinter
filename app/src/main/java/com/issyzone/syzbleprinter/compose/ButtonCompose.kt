package com.issyzone.syzbleprinter.compose

import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState

@Composable
fun FilledButtonExample(onClick: () -> Unit, content: String,isEnable: Boolean) {
    Button(onClick = { onClick() }, enabled = isEnable) {
        Text(text = content)
    }
}
@Composable
fun FilledTonalButtonExample(onClick: () -> Unit, content: String,isEnable:Boolean) {
    FilledTonalButton(onClick = { onClick() }, enabled = isEnable) {
        Text(text = content)
    }
}
@Composable
fun OutlinedButtonExample(onClick: () -> Unit, content: String,isEnable: Boolean) {
    OutlinedButton(onClick = { onClick() }, enabled = isEnable) {
        Text(text = content)
    }
}
