package com.issyzone.syzbleprinter

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class MyComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyComposeActivityContent()
        }
    }

    @Composable
    fun MyComposeActivityContent() {
        MaterialTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                greeting(name = "")
            }
        }
    }

    @Composable
    fun greeting(name: String) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier.padding(16.dp)
        ) {
            Image(painter = painterResource(id = R.drawable.test11), contentDescription = "test3", modifier = Modifier.clip(
                CircleShape).size(100.dp))
            Text(
                text = "连接蓝牙",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
            )
            Text(text = "断开蓝牙", modifier = Modifier
                .clip(CircleShape)
                .requiredSize(84.dp, 28.dp)
                .clickable {
                    Log.i("TAG", "点击了断开蓝牙")
                })
            Text(text = "打印自检页",
                modifier = Modifier
                    .size(width = 84.dp, height = 28.dp)
                    .clickable {
                        Log.i("TAG", "点击了打印自检页")
                    })
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = "打印速度", modifier = Modifier.weight(1f), textAlign = TextAlign.Center )
                Text(text = "打印浓度",modifier = Modifier.weight(1f) ,textAlign = TextAlign.Center)
            }
            Box {
                Text(text = "打印机状态",modifier = Modifier
                    .matchParentSize()
                    .background(Color.Blue))
                Text(text = "error")
            }
            ArtistCardModifiers {
                Log.i("TAG", "点击了>>>>")
            }
            BoxWithConstraints {
                val constraints = constraints
                Text("Max width: ${constraints.maxWidth}, Max height: ${constraints.maxHeight}")
            }
        }
    }
}
val modifier= Modifier
    .fillMaxWidth()
    .background(Color.Red)
    .padding(2.dp)


@Composable
fun ArtistCardModifiers(
    onClick: () -> Unit
) {
    val animatedState = animateFloatAsState(targetValue = 1f)
    //
    val padding = 16.dp
    Column(
        Modifier
            .clickable(onClick = onClick)
            .padding(padding)
            .fillMaxWidth()

    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "打印机状态")
        }
        Spacer(modifier)
        Card(
            modifier = Modifier.offset(x = 16.dp, y = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 100.dp),
        ) {
            Text(text = "打印机状态", modifier = Modifier.paddingFromBaseline(top = 16.dp))
        }
    }
}

@Preview(showBackground = false)
@Composable
fun PreviewGreeting() {
    MyComposeActivity().MyComposeActivityContent()
}