package com.lightkit.compose.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage

@Composable
fun AppImage(
    @DrawableRes resId: Int,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    cornerRadius: Dp? = null,
) {
    val m = if (cornerRadius != null) modifier.clip(RoundedCornerShape(cornerRadius)) else modifier
    Image(
        painter = painterResource(id = resId),
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = m,
    )
}

@Composable
fun AppImage(
    bitmap: ImageBitmap,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    cornerRadius: Dp? = null,
) {
    val m = if (cornerRadius != null) modifier.clip(RoundedCornerShape(cornerRadius)) else modifier
    Image(
        bitmap = bitmap,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = m,
    )
}

@Composable
fun AppAsyncImage(
    model: Any?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    cornerRadius: Dp? = null,
    @DrawableRes placeholderResId: Int? = null,
    @DrawableRes errorResId: Int? = null,
) {
    val m = if (cornerRadius != null) modifier.clip(RoundedCornerShape(cornerRadius)) else modifier
    if (LocalInspectionMode.current) {
        val previewRes = when {
            model is Int -> model
            placeholderResId != null -> placeholderResId
            else -> null
        }
        if (previewRes != null) {
            Image(
                painter = painterResource(id = previewRes),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = m,
            )
        } else {
            Box(modifier = m)
        }
        return
    }
    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = m,
        placeholder = placeholderResId?.let { painterResource(id = it) },
        error = errorResId?.let { painterResource(id = it) },
    )
}
