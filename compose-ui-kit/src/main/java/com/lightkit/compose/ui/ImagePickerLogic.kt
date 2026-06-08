package com.lightkit.compose.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider

enum class MediaPickerSheetStyle {
    Standard,
    Embedded,
}

internal enum class ImagePickerActiveSheet {
    None,
    Options,
    MediaGrid,
    Embedded,
}

internal enum class ImagePickerPendingGalleryAction {
    Gallery,
    Embedded,
}

internal object ImagePickerPermissions {
    val camera: String get() = Manifest.permission.CAMERA

    fun hasAll(context: Context, permissions: List<String>): Boolean {
        return permissions.all { perm ->
            ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun galleryPermissionsForConfig(config: ImagePickerConfig): List<String> {
        return galleryPermissionsForSdk(Build.VERSION.SDK_INT, config.mediaType)
    }

    fun galleryPermissionsForSdk(sdkInt: Int, mediaType: MediaType): List<String> {
        return buildList {
            if (sdkInt >= Build.VERSION_CODES.TIRAMISU) {
                when (mediaType) {
                    MediaType.Image -> add(Manifest.permission.READ_MEDIA_IMAGES)
                    MediaType.Video -> add(Manifest.permission.READ_MEDIA_VIDEO)
                    MediaType.ImageAndVideo -> {
                        add(Manifest.permission.READ_MEDIA_IMAGES)
                        add(Manifest.permission.READ_MEDIA_VIDEO)
                    }
                }
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }
}

internal fun maxSelectionCount(config: ImagePickerConfig): Int {
    return if (config.selectionMode == SelectionMode.Multiple) {
        config.maxSelectionCount
    } else {
        1
    }
}

internal fun urisToMediaItems(context: Context, uris: List<Uri>): List<MediaItem> {
    return uris.map { uri ->
        val isVideo = context.contentResolver.getType(uri)?.startsWith("video/") == true
        MediaItem(uri = uri, isVideo = isVideo, duration = null)
    }
}

internal fun createCameraPhotoUri(context: Context, fileProviderAuthority: String): Uri {
    val photoFile = createImageFile(context)
    return FileProvider.getUriForFile(context, fileProviderAuthority, photoFile)
}

internal fun toggleMultipleSelection(
    current: List<MediaItem>,
    item: MediaItem,
    selectedUris: Set<Uri>,
    maxCount: Int,
): List<MediaItem> {
    val next = current.toMutableList()
    if (item.uri in selectedUris) {
        next.removeAll { it.uri == item.uri }
    } else if (next.size < maxCount) {
        next.add(item)
    }
    return next
}

internal fun matchInitialSelectedItems(
    initialSelectedUris: List<Uri>,
    mediaItems: List<MediaItem>,
): List<MediaItem> {
    return initialSelectedUris.mapNotNull { uri ->
        mediaItems.find { it.uri == uri }
    }
}

internal fun applyMediaItemSelection(
    selectedItems: List<MediaItem>,
    item: MediaItem,
    selectedUris: Set<Uri>,
    config: ImagePickerConfig,
    onSingleConfirm: (List<MediaItem>) -> Unit,
) {
    when (config.selectionMode) {
        SelectionMode.Single -> onSingleConfirm(listOf(item))
        SelectionMode.Multiple -> {
            val next = toggleMultipleSelection(
                current = selectedItems,
                item = item,
                selectedUris = selectedUris,
                maxCount = maxSelectionCount(config),
            )
            (selectedItems as? SnapshotStateList<MediaItem>)?.apply {
                clear()
                addAll(next)
            }
        }
    }
}

internal fun syncInitialSelectedItems(
    initialSelectedUris: List<Uri>,
    selectedItems: List<MediaItem>,
    mediaItems: List<MediaItem>,
) {
    if (initialSelectedUris.isEmpty() || selectedItems.isNotEmpty() || mediaItems.isEmpty()) return
    val matchedItems = matchInitialSelectedItems(initialSelectedUris, mediaItems)
    if (matchedItems.isEmpty()) return
    (selectedItems as? SnapshotStateList<MediaItem>)?.apply {
        clear()
        addAll(matchedItems)
    }
}
