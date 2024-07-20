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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.ui.unit.sp


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
                //scaffordExample()
                 topbar()
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
            Image(
                painter = painterResource(id = R.drawable.test11),
                contentDescription = "test3",
                modifier = Modifier
                    .clip(
                        CircleShape
                    )
                    .size(100.dp)
            )
            Text(
                text = "连接蓝牙", modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
            )
            Text(text = "断开蓝牙",
                modifier = Modifier
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
                Text(
                    text = "打印速度", modifier = Modifier.weight(1f), textAlign = TextAlign.Center
                )
                Text(
                    text = "打印浓度", modifier = Modifier.weight(1f), textAlign = TextAlign.Center
                )
            }
            Box {
                Text(
                    text = "打印机状态",
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Blue)
                )
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

val modifier = Modifier
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


@Composable
fun PreviewGreeting() {
    MyComposeActivity().MyComposeActivityContent()
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = false)
@Composable
fun topbar() {
    Box (modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)){
        Text(
            text = "文本1",
            color = Color.Blue,
            fontSize = 30.sp,
            modifier = Modifier.align(
                Alignment.TopStart
            )
        )
        Divider(modifier= Modifier
            .width(8.dp)
            .fillMaxHeight().align(Alignment.Center), color = Color.Red)

        Text(text = "文本2", color = Color.LightGray, fontSize = 30.sp, modifier = Modifier.align(Alignment.TopEnd))
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun scaffordExample() {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    var presses by remember {
        mutableStateOf(1)
    }
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(text = "TopAppBar") },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary,
            ),
            navigationIcon = {
                IconButton(onClick = { /* do something */ }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Localized description"
                    )
                }
            },
            actions = {
                IconButton(onClick = { /* do something */ }) {
                    Icon(
                        imageVector = Icons.Filled.List,
                        contentDescription = "Localized description"
                    )
                }
                IconButton(onClick = { /* do something */ }) {
                    Icon(
                        imageVector = Icons.Filled.Add, contentDescription = "Localized description"
                    )
                }
            },
            scrollBehavior = scrollBehavior,
            modifier = Modifier.height(30.dp)
        )
    }) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(text = "text01", modifier = modifier.clickable {
                presses++
            })
            Text(text = "text${presses}")
        }
    }
}