package com.lightkit.compose.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
internal data class ImagePickerControllerState(
    val activeSheet: ImagePickerActiveSheet,
    val selectedMedia: SnapshotStateList<MediaItem>,
    val optionsSheetState: SheetState,
    val mediaGridSheetState: SheetState,
    val embeddedSheetState: SheetState,
    val showPicker: () -> Unit,
    val launchCameraFlow: () -> Unit,
    val openGalleryDirect: () -> Unit,
    val openEmbeddedFromOptions: () -> Unit,
    val openMediaGridFromOptions: () -> Unit,
    val dismissOptions: () -> Unit,
    val dismissMediaGrid: () -> Unit,
    val dismissEmbedded: (clearSelection: Boolean) -> Unit,
    val actions: ImagePickerActions,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun rememberImagePickerController(
    config: ImagePickerConfig,
    hostConfig: ImagePickerHostConfig,
    initialSelectedUris: List<Uri>,
    onMediaSelected: (List<Uri>) -> Unit,
): ImagePickerControllerState {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val strings = LocalUiKitTheme.current.imagePickerStrings
    val galleryPermissions = remember(config) {
        ImagePickerPermissions.galleryPermissionsForConfig(config)
    }

    var activeSheet by remember { mutableStateOf(ImagePickerActiveSheet.None) }
    var hasGalleryPermissions by remember(galleryPermissions) {
        mutableStateOf(ImagePickerPermissions.hasAll(context, galleryPermissions))
    }
    var pendingGalleryAction by remember {
        mutableStateOf<ImagePickerPendingGalleryAction?>(null)
    }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    val selectedMedia = remember { mutableStateListOf<MediaItem>() }

    val optionsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val mediaGridSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val embeddedSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(initialSelectedUris) {
        if (initialSelectedUris.isNotEmpty() && selectedMedia.isEmpty()) {
            selectedMedia.clear()
            selectedMedia.addAll(urisToMediaItems(context, initialSelectedUris))
        }
    }

    fun openGallerySheet(action: ImagePickerPendingGalleryAction) {
        activeSheet = when (action) {
            ImagePickerPendingGalleryAction.Gallery -> ImagePickerActiveSheet.MediaGrid
            ImagePickerPendingGalleryAction.Embedded -> ImagePickerActiveSheet.Embedded
        }
    }

    val galleryPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val granted = galleryPermissions.all { perm -> result[perm] == true }
            hasGalleryPermissions = granted
            if (granted) {
                pendingGalleryAction?.let { action ->
                    scope.launch {
                        optionsSheetState.hide()
                        activeSheet = ImagePickerActiveSheet.None
                        openGallerySheet(action)
                    }
                }
            } else {
                hostConfig.showMessage(strings.galleryPermissionDenied)
            }
            pendingGalleryAction = null
        }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) {
            cameraUri?.let { uri -> onMediaSelected(listOf(uri)) }
        }
        activeSheet = ImagePickerActiveSheet.None
    }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                val uri = createCameraPhotoUri(context, hostConfig.fileProviderAuthority)
                cameraUri = uri
                cameraLauncher.launch(uri)
            } else {
                hostConfig.showMessage(strings.cameraPermissionDenied)
            }
        }

    fun launchCameraFlow() {
        if (ImagePickerPermissions.hasAll(context, listOf(ImagePickerPermissions.camera))) {
            val uri = createCameraPhotoUri(context, hostConfig.fileProviderAuthority)
            cameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(ImagePickerPermissions.camera)
        }
    }

    fun requestGalleryAndOpen(action: ImagePickerPendingGalleryAction) {
        if (hasGalleryPermissions) {
            scope.launch {
                optionsSheetState.hide()
                activeSheet = ImagePickerActiveSheet.None
                openGallerySheet(action)
            }
        } else {
            pendingGalleryAction = action
            galleryPermissionLauncher.launch(galleryPermissions.toTypedArray())
        }
    }

    fun openGalleryDirect() {
        activeSheet = ImagePickerActiveSheet.None
        val action = if (config.showEmbeddedPicker) {
            ImagePickerPendingGalleryAction.Embedded
        } else {
            ImagePickerPendingGalleryAction.Gallery
        }
        if (hasGalleryPermissions) {
            openGallerySheet(action)
        } else {
            pendingGalleryAction = action
            galleryPermissionLauncher.launch(galleryPermissions.toTypedArray())
        }
    }

    val showPicker = { activeSheet = ImagePickerActiveSheet.Options }

    return ImagePickerControllerState(
        activeSheet = activeSheet,
        selectedMedia = selectedMedia,
        optionsSheetState = optionsSheetState,
        mediaGridSheetState = mediaGridSheetState,
        embeddedSheetState = embeddedSheetState,
        showPicker = showPicker,
        launchCameraFlow = ::launchCameraFlow,
        openGalleryDirect = ::openGalleryDirect,
        openEmbeddedFromOptions = {
            requestGalleryAndOpen(ImagePickerPendingGalleryAction.Embedded)
        },
        openMediaGridFromOptions = {
            requestGalleryAndOpen(ImagePickerPendingGalleryAction.Gallery)
        },
        dismissOptions = { activeSheet = ImagePickerActiveSheet.None },
        dismissMediaGrid = { activeSheet = ImagePickerActiveSheet.None },
        dismissEmbedded = { clearSelection ->
            activeSheet = ImagePickerActiveSheet.None
            if (clearSelection) selectedMedia.clear()
        },
        actions = ImagePickerActions(
            showPicker = showPicker,
            openCamera = ::launchCameraFlow,
            openGallery = ::openGalleryDirect,
        ),
    )
}
