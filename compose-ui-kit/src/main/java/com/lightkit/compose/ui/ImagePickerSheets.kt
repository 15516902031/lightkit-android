package com.lightkit.compose.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.request.ImageRequest
import coil3.request.crossfade

@Composable
private fun imagePickerTheme(): UiKitTheme = LocalUiKitTheme.current

@Composable
private fun imagePickerStrings(): ImagePickerStrings = LocalUiKitTheme.current.imagePickerStrings

@Composable
internal fun ImagePickerVideoBadge(modifier: Modifier = Modifier) {
    val custom = LocalImagePickerVideoBadge.current
    if (custom != null) {
        custom()
    } else {
        Text(text = "▶", color = Color.White, fontSize = 10.sp, modifier = modifier)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ImagePickerOptionsSheet(
    sheetState: SheetState,
    config: ImagePickerConfig,
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onEmbeddedPickerClick: () -> Unit,
    onGalleryClick: () -> Unit = {},
) {
    val theme = imagePickerTheme()
    val strings = imagePickerStrings()

    UiKitBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        shape = RoundedCornerShape(0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White),
            ) {
                if (config.showCamera) {
                    PickerOptionButton(text = strings.camera, onClick = onCameraClick)
                }
                if (config.showEmbeddedPicker || config.showGallery) {
                    if (config.showCamera) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(theme.imagePickerOptionDividerColor),
                        )
                    }
                    PickerOptionButton(
                        text = strings.gallery,
                        onClick = if (config.showEmbeddedPicker) onEmbeddedPickerClick else onGalleryClick,
                    )
                }
            }
            SpacerV(10.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .clickableWithEffect(onClick = onDismiss)
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = strings.cancel,
                    fontSize = 16.sp,
                    color = theme.imagePickerMutedColor,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
internal fun PickerOptionButton(text: String, onClick: () -> Unit) {
    val theme = imagePickerTheme()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickableWithEffect(onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            color = theme.imagePickerTitleColor,
            textAlign = TextAlign.Center,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MediaPickerSheet(
    sheetState: SheetState,
    config: ImagePickerConfig,
    style: MediaPickerSheetStyle,
    selectedItems: List<MediaItem>,
    initialSelectedUris: List<Uri> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (List<MediaItem>) -> Unit,
) {
    val theme = imagePickerTheme()
    val strings = imagePickerStrings()
    val context = LocalContext.current
    var mediaItems by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val sheetHeightFraction = if (style == MediaPickerSheetStyle.Embedded) 0.8f else 0.76f
    val headerVerticalPadding = if (style == MediaPickerSheetStyle.Embedded) 16.dp else 12.dp
    val gridPadding = if (style == MediaPickerSheetStyle.Embedded) 8.dp else 4.dp
    val isMultiSelect = config.selectionMode == SelectionMode.Multiple
    val maxSelectCount = maxSelectionCount(config)

    LaunchedEffect(Unit) {
        isLoading = true
        mediaItems = loadMediaFromGallery(context, config.mediaType)
        isLoading = false
    }

    LaunchedEffect(initialSelectedUris, mediaItems) {
        syncInitialSelectedItems(initialSelectedUris, selectedItems, mediaItems)
    }

    val selectedUris by remember(selectedItems) {
        derivedStateOf { selectedItems.map { it.uri }.toSet() }
    }

    val singleSelectTitle = if (style == MediaPickerSheetStyle.Embedded) {
        strings.embeddedTitle
    } else {
        strings.selectPhoto
    }

    UiKitBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(sheetHeightFraction),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = headerVerticalPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = strings.cancel,
                        fontSize = 15.sp,
                        color = theme.imagePickerBodyColor,
                    )
                }
                Text(
                    text = if (isMultiSelect) {
                        strings.selectedCount(selectedItems.size, maxSelectCount)
                    } else {
                        singleSelectTitle
                    },
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = theme.imagePickerTitleColor,
                )
                TextButton(
                    onClick = { onConfirm(selectedItems) },
                    enabled = selectedItems.isNotEmpty(),
                ) {
                    Text(
                        text = strings.confirm,
                        fontSize = 15.sp,
                        color = if (selectedItems.isNotEmpty()) {
                            theme.imagePickerAccentColor
                        } else {
                            theme.imagePickerBodyColor
                        },
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            val gridModifier = if (style == MediaPickerSheetStyle.Embedded) {
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
            } else {
                Modifier.fillMaxSize()
            }

            when {
                isLoading -> {
                    Box(modifier = gridModifier, contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = theme.imagePickerAccentColor)
                    }
                }

                mediaItems.isEmpty() -> {
                    Box(modifier = gridModifier, contentAlignment = Alignment.Center) {
                        Text(
                            text = strings.noMedia,
                            fontSize = 15.sp,
                            color = theme.imagePickerBodyColor,
                        )
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(config.gridSpanCount),
                        modifier = gridModifier,
                        contentPadding = PaddingValues(gridPadding),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(mediaItems, key = { it.uri.toString() }) { item ->
                            MediaPickerGridItem(
                                item = item,
                                isSelected = item.uri in selectedUris,
                                style = style,
                                onClick = {
                                    applyMediaItemSelection(
                                        selectedItems = selectedItems,
                                        item = item,
                                        selectedUris = selectedUris,
                                        config = config,
                                        onSingleConfirm = onConfirm,
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun MediaPickerGridItem(
    item: MediaItem,
    isSelected: Boolean,
    style: MediaPickerSheetStyle,
    onClick: () -> Unit,
) {
    val theme = imagePickerTheme()
    val context = LocalContext.current
    val cornerRadius = if (style == MediaPickerSheetStyle.Embedded) 8.dp else 4.dp
    val frameBitmap by produceState<Bitmap?>(initialValue = null, item.uri, item.isVideo) {
        value = if (item.isVideo) extractPickerVideoFrame(context, item.uri) else null
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(cornerRadius))
            .clickableWithEffect(onClick = onClick),
    ) {
        if (frameBitmap != null) {
            AppImage(
                bitmap = frameBitmap!!.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            AppAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(item.uri)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        if (isSelected) {
            val scrimColor = if (style == MediaPickerSheetStyle.Embedded) {
                theme.imagePickerAccentColor.copy(alpha = 0.3f)
            } else {
                Color.Black.copy(alpha = 0.4f)
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(scrimColor),
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(theme.imagePickerAccentColor, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    AppIcon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color.Transparent, CircleShape)
                        .border(1.dp, theme.imagePickerBodyColor.copy(alpha = 0.6f), CircleShape),
                )
            }
        }

        if (item.isVideo) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .size(20.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (style == MediaPickerSheetStyle.Embedded) {
                    ImagePickerVideoBadge(modifier = Modifier.fillMaxSize())
                } else {
                    Text(text = "▶", color = Color.White, fontSize = 10.sp)
                }
            }
        }
    }
}
