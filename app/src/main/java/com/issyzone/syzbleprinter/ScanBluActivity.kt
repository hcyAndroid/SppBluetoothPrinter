package com.issyzone.syzbleprinter

import android.bluetooth.BluetoothDevice
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.issyzone.common_work.mvi.BaseMviAppCompatActivity
import com.issyzone.syzbleprinter.databinding.ActivityMain2Binding
import com.issyzone.syzbleprinter.intent.BluScanIntent
import com.issyzone.syzbleprinter.viewmodel.ScanBluViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ScanBluActivity : BaseMviAppCompatActivity<ScanBluViewModel, ActivityMain2Binding>() {
    override val mViewModel: ScanBluViewModel by viewModel()
    private val TAG = "ScanBluActivity"
    override fun onDestroy() {
        super.onDestroy()
        mViewModel.sendUiIntent(BluScanIntent.unRegisterScan)
    }


    override fun initData() {
        lifecycleScope.launch {
            mViewModel.sendUiIntent(BluScanIntent.registerScan)
            mBinding.composeView.setContent {
                test()
            }
        }
    }

    @Composable
    fun test() {
        val list by mViewModel.devices.collectAsState()
        Box(
            modifier = Modifier
                .background(colorResource(id = R.color.white))
                .padding(start = 16.dp, end = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(list) { device ->
                    BluDeviceCompose(device) {
                        Log.i(TAG, "点击了===>$device")
                        setResult(RESULT_OK, intent.putExtra("mac", device.address))
                        finish()
                        //mViewModel.sendUiIntent(BluScanIntent.connectDevice(device.address))

                    }
                }
            }
        }
    }

    @Composable
    fun BluDeviceCompose(device: BluetoothDevice, click: () -> Unit) {
        Log.i(TAG, "设备3333>>>$device")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(
                    color = colorResource(id = R.color.fff8f8f8), shape = RoundedCornerShape(15.dp)
                )
                .clickable {
                    click.invoke()
                }
                .padding(start = 15.dp, end = 15.dp),

            ) {
            ConstraintLayout {
                val (img, col) = createRefs()
                Image(painter = painterResource(id = R.mipmap.icon_device_fm226),
                    contentScale = androidx.compose.ui.layout.ContentScale.FillBounds,
                    contentDescription = "fm226",
                    modifier = Modifier
                        .constrainAs(img) {
                            start.linkTo(parent.start)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        }
                        .size(80.dp, 80.dp))
                Column(modifier = Modifier.constrainAs(col) {
                    start.linkTo(img.end, 20.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }) {
                    Text(
                        text = device.name,
                        fontSize = 19.sp,
                        color = colorResource(id = R.color.ff252525)
                    )
                    Box(modifier = Modifier.padding(top = 10.dp)) {
                        Text(
                            text = device.address,
                            fontSize = 16.sp,
                            color = colorResource(id = R.color.ff898989)
                        )
                    }
                }
            }

        }
    }
}