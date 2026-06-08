package com.lightkit.compose.ui

import android.net.Uri
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePicker(
    config: ImagePickerConfig = ImagePickerConfig(),
    hostConfig: ImagePickerHostConfig,
    initialSelectedUris: List<Uri> = emptyList(),
    onMediaSelected: (List<Uri>) -> Unit,
    content: @Composable (showPicker: () -> Unit) -> Unit,
) {
    ImagePickerHost(
        config = config,
        hostConfig = hostConfig,
        initialSelectedUris = initialSelectedUris,
        onMediaSelected = onMediaSelected,
        withActions = false,
        actionsContent = null,
        pickerContent = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePickerWithActions(
    config: ImagePickerConfig = ImagePickerConfig(),
    hostConfig: ImagePickerHostConfig,
    initialSelectedUris: List<Uri> = emptyList(),
    onMediaSelected: (List<Uri>) -> Unit,
    content: @Composable (ImagePickerActions) -> Unit,
) {
    ImagePickerHost(
        config = config,
        hostConfig = hostConfig,
        initialSelectedUris = initialSelectedUris,
        onMediaSelected = onMediaSelected,
        withActions = true,
        actionsContent = content,
        pickerContent = null,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImagePickerHost(
    config: ImagePickerConfig,
    hostConfig: ImagePickerHostConfig,
    initialSelectedUris: List<Uri>,
    onMediaSelected: (List<Uri>) -> Unit,
    withActions: Boolean,
    actionsContent: (@Composable (ImagePickerActions) -> Unit)?,
    pickerContent: (@Composable (showPicker: () -> Unit) -> Unit)?,
) {
    val controller = rememberImagePickerController(
        config = config,
        hostConfig = hostConfig,
        initialSelectedUris = initialSelectedUris,
        onMediaSelected = onMediaSelected,
    )

    if (withActions) {
        actionsContent?.invoke(controller.actions)
    } else {
        pickerContent?.let { content ->
            content { controller.showPicker() }
        }
    }

    when (controller.activeSheet) {
        ImagePickerActiveSheet.Options -> {
            ImagePickerOptionsSheet(
                sheetState = controller.optionsSheetState,
                config = config,
                onDismiss = controller.dismissOptions,
                onCameraClick = controller.launchCameraFlow,
                onEmbeddedPickerClick = controller.openEmbeddedFromOptions,
                onGalleryClick = controller.openMediaGridFromOptions,
            )
        }

        ImagePickerActiveSheet.MediaGrid -> {
            MediaPickerSheet(
                sheetState = controller.mediaGridSheetState,
                config = config,
                style = MediaPickerSheetStyle.Standard,
                selectedItems = controller.selectedMedia,
                initialSelectedUris = initialSelectedUris,
                onDismiss = controller.dismissMediaGrid,
                onConfirm = { items ->
                    onMediaSelected(items.map { it.uri })
                    controller.dismissMediaGrid()
                },
            )
        }

        ImagePickerActiveSheet.Embedded -> {
            MediaPickerSheet(
                sheetState = controller.embeddedSheetState,
                config = config,
                style = MediaPickerSheetStyle.Embedded,
                selectedItems = controller.selectedMedia,
                initialSelectedUris = initialSelectedUris,
                onDismiss = { controller.dismissEmbedded(true) },
                onConfirm = { items ->
                    onMediaSelected(items.map { it.uri })
                    controller.dismissEmbedded(true)
                },
            )
        }

        ImagePickerActiveSheet.None -> Unit
    }
}
