package com.lightkit.compose.ui

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.mutableStateListOf
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(org.robolectric.RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class ImagePickerLogicTest {

    private val uriA = Uri.parse("content://media/1")
    private val uriB = Uri.parse("content://media/2")
    private val uriC = Uri.parse("content://media/3")

    private val itemA = MediaItem(uriA, isVideo = false)
    private val itemB = MediaItem(uriB, isVideo = false)
    private val itemC = MediaItem(uriC, isVideo = true)

    @Test
    fun maxSelectionCount_singleMode_returnsOne() {
        val config = ImagePickerConfig(selectionMode = SelectionMode.Single, maxSelectionCount = 9)
        assertEquals(1, maxSelectionCount(config))
    }

    @Test
    fun maxSelectionCount_multipleMode_returnsConfiguredMax() {
        val config = ImagePickerConfig(selectionMode = SelectionMode.Multiple, maxSelectionCount = 5)
        assertEquals(5, maxSelectionCount(config))
    }

    @Test
    fun galleryPermissions_tiramisu_imageOnly() {
        val perms = ImagePickerPermissions.galleryPermissionsForSdk(
            sdkInt = Build.VERSION_CODES.TIRAMISU,
            mediaType = MediaType.Image,
        )
        assertEquals(listOf(Manifest.permission.READ_MEDIA_IMAGES), perms)
    }

    @Test
    fun galleryPermissions_tiramisu_imageAndVideo() {
        val perms = ImagePickerPermissions.galleryPermissionsForSdk(
            sdkInt = Build.VERSION_CODES.TIRAMISU,
            mediaType = MediaType.ImageAndVideo,
        )
        assertEquals(
            listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
            ),
            perms,
        )
    }

    @Test
    fun galleryPermissions_legacy_usesExternalStorage() {
        val perms = ImagePickerPermissions.galleryPermissionsForSdk(
            sdkInt = Build.VERSION_CODES.S_V2,
            mediaType = MediaType.ImageAndVideo,
        )
        assertEquals(listOf(Manifest.permission.READ_EXTERNAL_STORAGE), perms)
    }

    @Test
    fun toggleMultipleSelection_addsWhenUnderMax() {
        val result = toggleMultipleSelection(
            current = listOf(itemA),
            item = itemB,
            selectedUris = setOf(uriA),
            maxCount = 3,
        )
        assertEquals(listOf(itemA, itemB), result)
    }

    @Test
    fun toggleMultipleSelection_removesWhenAlreadySelected() {
        val result = toggleMultipleSelection(
            current = listOf(itemA, itemB),
            item = itemA,
            selectedUris = setOf(uriA, uriB),
            maxCount = 3,
        )
        assertEquals(listOf(itemB), result)
    }

    @Test
    fun toggleMultipleSelection_doesNotExceedMax() {
        val result = toggleMultipleSelection(
            current = listOf(itemA, itemB),
            item = itemC,
            selectedUris = setOf(uriA, uriB),
            maxCount = 2,
        )
        assertEquals(listOf(itemA, itemB), result)
    }

    @Test
    fun matchInitialSelectedItems_preservesOrderAndFiltersUnknown() {
        val matched = matchInitialSelectedItems(
            initialSelectedUris = listOf(uriC, uriA, Uri.parse("content://media/unknown")),
            mediaItems = listOf(itemA, itemB, itemC),
        )
        assertEquals(listOf(itemC, itemA), matched)
    }

    @Test
    fun applyMediaItemSelection_single_invokesConfirm() {
        var confirmed: List<MediaItem>? = null
        applyMediaItemSelection(
            selectedItems = emptyList(),
            item = itemA,
            selectedUris = emptySet(),
            config = ImagePickerConfig(selectionMode = SelectionMode.Single),
            onSingleConfirm = { confirmed = it },
        )
        assertEquals(listOf(itemA), confirmed)
    }

    @Test
    fun applyMediaItemSelection_multiple_updatesSnapshotList() {
        val selected = mutableStateListOf<MediaItem>()
        applyMediaItemSelection(
            selectedItems = selected,
            item = itemA,
            selectedUris = emptySet(),
            config = ImagePickerConfig(
                selectionMode = SelectionMode.Multiple,
                maxSelectionCount = 2,
            ),
            onSingleConfirm = { error("single confirm should not run") },
        )
        assertEquals(listOf(itemA), selected.toList())
    }

    @Test
    fun syncInitialSelectedItems_populatesEmptySnapshotList() {
        val selected = mutableStateListOf<MediaItem>()
        syncInitialSelectedItems(
            initialSelectedUris = listOf(uriB),
            selectedItems = selected,
            mediaItems = listOf(itemA, itemB),
        )
        assertEquals(listOf(itemB), selected.toList())
    }

    @Test
    fun syncInitialSelectedItems_skipsWhenAlreadyHasSelection() {
        val selected = mutableStateListOf(itemA)
        syncInitialSelectedItems(
            initialSelectedUris = listOf(uriB),
            selectedItems = selected,
            mediaItems = listOf(itemA, itemB),
        )
        assertEquals(listOf(itemA), selected.toList())
    }

    @Test
    fun imagePickerStrings_selectedCount_formatsCorrectly() {
        val strings = ImagePickerStrings()
        assertEquals("已选 2/9", strings.selectedCount(2, 9))
    }
}
