package com.issyzone.syzbleprinter.compose

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.imageResource

@Composable
fun ImageBitmap(bitmap: Bitmap, contentDescription: String = "",modifier: Modifier) {
    Image(bitmap = bitmap.asImageBitmap(), contentDescription = contentDescription, modifier=modifier)
}

@Composable
fun ImageBitmapByLocalId(bitmapId: Int, contentDescription: String = "",modifier: Modifier) {
    Image(bitmap = ImageBitmap.imageResource(id = bitmapId), contentDescription = contentDescription, modifier=modifier)
}