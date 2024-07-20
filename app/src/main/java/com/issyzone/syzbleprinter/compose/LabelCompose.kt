package com.issyzone.syzbleprinter.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import com.issyzone.syzbleprinter.R

@Composable
fun LabelCompose(label_name: String, label_value: String = "") {
    Box(modifier = Modifier.padding(16.dp)) {
        Text(
            text = label_name,
            fontSize = 17.sp,
            color = colorResource(id = R.color.ff252525),
            modifier = Modifier.align(Alignment.CenterStart)
        )
        Image(
            painter = painterResource(id = R.mipmap.h5_back),
            contentDescription = "",
            modifier = Modifier.align(Alignment.CenterEnd)
        )
        Text(
            text = label_value,
            fontSize = 17.sp,
            color = colorResource(id = R.color.ff252525),
            modifier = Modifier.align(Alignment.CenterStart)
        )
    }
}

@Composable
fun divider() {
    Divider(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp),
        color = colorResource(id = R.color.ffeaeaea)
    )
}

@Composable
fun ActivityTopBar(title: String) {
    val constraints = ConstraintSet {
        val back_img = createRefFor("back_img")
        val content = createRefFor("content")
        constrain(back_img) {
            top.linkTo(parent.top)
            bottom.linkTo(parent.bottom)
            start.linkTo(parent.start, 18.dp)
        }
        constrain(content) {
            top.linkTo(parent.top)
            bottom.linkTo(parent.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }
    }
    ConstraintLayout(
        constraints,
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(color = colorResource(id = R.color.white))
    ) {
        Image(painter = painterResource(id = R.mipmap.btn_close),
            contentDescription = "",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .size(20.dp)
                .layoutId("back_img")
                .clickable {

                })
        Text(
            text = title,
            fontSize = 24.sp,
            color = colorResource(id = R.color.ff252525),
            modifier = Modifier.layoutId("content")
        )
    }

}

@Composable
fun LabelCompose2(label_name: String, label_value: String, click: (() -> Unit)? = null) {
    ConstraintLayout(modifier = Modifier.run {
        fillMaxWidth()
            .height(44.dp)
            .clickable(interactionSource = remember { MutableInteractionSource() },
                indication = null, // 去掉水波纹动画
                onClick = click ?: {})
    }) {
        val (text1, text2, img) = createRefs()
        Text(text = label_name,
            fontSize = 17.sp,
            color = colorResource(id = R.color.ff252525),
            modifier = Modifier.constrainAs(text1) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start, margin = 16.dp)
            })
        Text(text = label_value,
            fontSize = 17.sp,
            color = colorResource(id = R.color.ff252525),
            modifier = Modifier.constrainAs(text2) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                end.linkTo(img.start, margin = 5.dp)
            })
        Image(painter = painterResource(id = R.mipmap.h5_back),
            contentDescription = "",
            modifier = Modifier.constrainAs(img) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                end.linkTo(parent.end, margin = 16.dp)
            })
    }
}


@Composable
fun LabelCompose3(
    label_name: String,
    label_value: String,
    isVisibility:Boolean,
    click: (() -> Unit)? = null
) {
    // val isVisibility=remember { mutableStateOf(true) }
    ConstraintLayout(modifier = Modifier.run {
        fillMaxWidth()
            .height(44.dp)
            .clickable(interactionSource = remember { MutableInteractionSource() },
                indication = null, // 去掉水波纹动画
                onClick = if (isVisibility) {
                    {}
                } else {
                    click ?: {}
                })
    }) {
        val (text1, text2, img) = createRefs()
        Text(text = label_name,
            fontSize = 17.sp,
            color = colorResource(id = R.color.ff252525),
            modifier = Modifier.constrainAs(text1) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start, margin = 16.dp)
            })
        Text(text = label_value,
            fontSize = 17.sp,
            color = colorResource(id = R.color.ff252525),
            modifier = Modifier.constrainAs(text2) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                if (isVisibility) {
                    end.linkTo(parent.end, margin = 16.dp)
                } else {
                    end.linkTo(img.start, margin = 5.dp)
                }
            })
        if (!isVisibility) {
            Image(painter = painterResource(id = R.mipmap.h5_back),
                contentDescription = "",
                modifier = Modifier.constrainAs(img) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end, margin = 16.dp)
                })
        }
    }
}

