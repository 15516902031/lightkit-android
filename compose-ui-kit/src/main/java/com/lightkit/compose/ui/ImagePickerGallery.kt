package com.lightkit.compose.ui

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
internal fun rememberGalleryPermissions(config: ImagePickerConfig): List<String> {
    return remember(config) { ImagePickerPermissions.galleryPermissionsForConfig(config) }
}

internal fun loadMediaFromGallery(context: Context, mediaType: MediaType): List<MediaItem> {
    val mediaItems = mutableListOf<MediaItem>()
    val projection = arrayOf(
        MediaStore.MediaColumns._ID,
        MediaStore.MediaColumns.MIME_TYPE,
        MediaStore.Video.Media.DURATION,
    )

    val uri = when (mediaType) {
        MediaType.Image -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        MediaType.Video -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        MediaType.ImageAndVideo -> MediaStore.Files.getContentUri("external")
    }

    val selection = when (mediaType) {
        MediaType.Image -> "${MediaStore.MediaColumns.MIME_TYPE} LIKE 'image/%'"
        MediaType.Video -> "${MediaStore.MediaColumns.MIME_TYPE} LIKE 'video/%'"
        MediaType.ImageAndVideo -> "(${MediaStore.MediaColumns.MIME_TYPE} LIKE 'image/%' OR ${MediaStore.MediaColumns.MIME_TYPE} LIKE 'video/%')"
    }

    val sortOrder = "${MediaStore.MediaColumns.DATE_ADDED} DESC"

    context.contentResolver.query(
        uri,
        projection,
        selection,
        null,
        sortOrder,
    )?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
        val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
        val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)

        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            val mimeType = cursor.getString(mimeTypeColumn)
            val isVideo = mimeType?.startsWith("video/") == true
            val duration = if (isVideo) cursor.getLong(durationColumn) else null

            val contentUri = ContentUris.withAppendedId(
                if (isVideo) MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                else MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id,
            )

            mediaItems.add(MediaItem(contentUri, isVideo, duration))
        }
    }

    return mediaItems
}

internal fun createImageFile(context: Context): File {
    val timeStamp = System.currentTimeMillis()
    return File.createTempFile(
        "JPEG_${timeStamp}_",
        ".jpg",
        context.cacheDir,
    )
}

internal suspend fun extractPickerVideoFrame(context: Context, uri: Uri): Bitmap? {
    return withContext(Dispatchers.IO) {
        runCatching {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, uri)
                retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            } finally {
                runCatching { retriever.release() }
            }
        }.getOrNull()
    }
}
