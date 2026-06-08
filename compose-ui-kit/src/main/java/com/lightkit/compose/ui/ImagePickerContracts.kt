package com.lightkit.compose.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

enum class MediaType {
    Image,
    Video,
    ImageAndVideo,
}

enum class SelectionMode {
    Single,
    Multiple,
}

data class ImagePickerConfig(
    val mediaType: MediaType = MediaType.Image,
    val selectionMode: SelectionMode = SelectionMode.Single,
    val maxSelectionCount: Int = 9,
    val showCamera: Boolean = true,
    val showGallery: Boolean = true,
    val showEmbeddedPicker: Boolean = true,
    val gridSpanCount: Int = 3,
)

data class MediaItem(
    val uri: Uri,
    val isVideo: Boolean = false,
    val duration: Long? = null,
)

data class ImagePickerActions(
    val showPicker: () -> Unit,
    val openCamera: () -> Unit,
    val openGallery: () -> Unit,
)

data class ImagePickerStrings(
    val camera: String = "拍照",
    val gallery: String = "从相册选择",
    val cancel: String = "取消",
    val confirm: String = "确定",
    val selectPhoto: String = "选择照片",
    val noMedia: String = "暂无媒体文件",
    val embeddedTitle: String = "选择照片",
    val galleryPermissionDenied: String = "请授权相册权限后重试",
    val cameraPermissionDenied: String = "请授权相机权限后重试",
) {
    fun selectedCount(selected: Int, max: Int): String = "已选 $selected/$max"
}

data class ImagePickerHostConfig(
    val fileProviderAuthority: String,
    val showMessage: (String) -> Unit = {},
)

/** 嵌入式网格视频角标；未提供时显示默认 ▶ 文本。 */
val LocalImagePickerVideoBadge = staticCompositionLocalOf<(@Composable () -> Unit)?> { null }
