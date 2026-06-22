package com.lightkit.compose.ui

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage
import coil3.request.ErrorResult
import coil3.request.ImageRequest

private const val TAG = "AppAsyncImage"

private fun imageRequestWithErrorLog(
    context: android.content.Context,
    model: Any?,
): Any? {
    if (model == null) return null
    val onError: (ImageRequest, ErrorResult) -> Unit = { request, result ->
        Log.e(
            TAG,
            "load failed, model=${request.data}, error=${result.throwable.message}",
            result.throwable,
        )
    }
    return when (model) {
        is ImageRequest -> model.newBuilder()
            .listener(onError = onError)
            .build()
        else -> ImageRequest.Builder(context)
            .data(model)
            .listener(onError = onError)
            .build()
    }
}

@Composable
private fun rememberImageRequestModel(model: Any?): Any? {
    val context = LocalContext.current
    return remember(model, context) {
        imageRequestWithErrorLog(context, model)
    }
}

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
        model = rememberImageRequestModel(model),
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = m,
        placeholder = placeholderResId?.let { painterResource(id = it) },
        error = errorResId?.let { painterResource(id = it) },
    )
}
