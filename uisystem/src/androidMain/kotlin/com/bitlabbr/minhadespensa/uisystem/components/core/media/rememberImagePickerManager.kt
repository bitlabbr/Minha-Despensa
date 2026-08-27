/*
 *   Copyright (c) 2026 Willian Santos
 *
 *   This work is licensed under the Creative Commons
 *   Attribution-NonCommercial 4.0 International License (CC BY-NC 4.0).
 *
 *   You are free to:
 *     - Share  — copy and redistribute the material in any medium or format
 *     - Adapt  — remix, transform, and build upon the material
 *
 *   Under the following terms:
 *     - Attribution    — You must give appropriate credit, provide a link to
 *                        the license, and indicate if changes were made.
 *     - NonCommercial  — You may not use the material for commercial purposes.
 *
 *   Owner rights:
 *     - Willian Santos retains all commercial rights.
 *    - The copyright holder may use, sell, sublicense, or relicense this
 *       work under different terms at any time.
 *
 *   Full license: https://creativecommons.org/licenses/by-nc/4.0/legalcode
 */

package com.bitlabbr.minhadespensa.uisystem.components.core.media

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File

@Composable
actual fun rememberImagePickerManager(
    onImagePicked: (ByteArray?) -> Unit
): ImagePickerManager {
    val context = LocalContext.current
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        val currentUri = tempPhotoUri
        if (success && currentUri != null) {
            val processedBytes = processUriImage(
                context = context,
                uri = currentUri,
                targetMaxDimension = 720,
                maxBytes = 100 * 1024
            )
            onImagePicked(processedBytes)
            cleanupTempUri(context, currentUri)
        } else {
            currentUri?.let { cleanupTempUri(context, it) }
            onImagePicked(null)
        }
    }

    val launchCameraWithUri = {
        val uri = createTempImageUri(context)
        tempPhotoUri = uri
        if (uri != null) {
            cameraLauncher.launch(uri)
        } else {
            onImagePicked(null)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            launchCameraWithUri()
        } else {
            onImagePicked(null)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val processedBytes = processUriImage(
                context = context,
                uri = uri,
                targetMaxDimension = 720,
                maxBytes = 100 * 1024
            )
            onImagePicked(processedBytes)
        } else {
            onImagePicked(null)
        }
    }

    return remember {
        object : ImagePickerManager {
            override fun launchCamera() {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED

                if (hasPermission) {
                    launchCameraWithUri()
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }

            override fun launchGallery() = galleryLauncher.launch("image/*")
        }
    }
}

private fun processUriImage(
    context: Context,
    uri: Uri,
    targetMaxDimension: Int,
    maxBytes: Int
): ByteArray? {
    return try {
        val rotationDegrees = context.contentResolver.openInputStream(uri)?.use { stream ->
            val exif = ExifInterface(stream)
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f

        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, boundsOptions)
        }

        var sampleSize = 1
        while (boundsOptions.outWidth / sampleSize > targetMaxDimension * 2 ||
            boundsOptions.outHeight / sampleSize > targetMaxDimension * 2
        ) {
            sampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val rawBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        } ?: return null

        val matrix = Matrix().apply {
            if (rotationDegrees != 0f) postRotate(rotationDegrees)
            val currentMax = maxOf(rawBitmap.width, rawBitmap.height)
            if (currentMax > targetMaxDimension) {
                val scale = targetMaxDimension.toFloat() / currentMax
                postScale(scale, scale)
            }
        }

        val finalBitmap = Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)

        compressBitmapWithinLimit(finalBitmap, maxBytes)
    } catch (e: Exception) {
        null
    }
}

private fun compressBitmapWithinLimit(bitmap: Bitmap, maxBytes: Int): ByteArray {
    val stream = ByteArrayOutputStream()
    var quality = 85
    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
    var bytes = stream.toByteArray()

    while (bytes.size > maxBytes && quality > 25) {
        stream.reset()
        quality -= 10
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        bytes = stream.toByteArray()
    }
    return bytes
}

private fun createTempImageUri(context: Context): Uri? {
    return try {
        val tempFile = File.createTempFile(
            "camera_temp_",
            ".jpg",
            context.cacheDir
        ).apply {
            createNewFile()
            deleteOnExit()
        }
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
    } catch (e: Exception) {
        null
    }
}

private fun cleanupTempUri(context: Context, uri: Uri) {
    try {
        context.contentResolver.delete(uri, null, null)
    } catch (_: Exception) {

    }
}