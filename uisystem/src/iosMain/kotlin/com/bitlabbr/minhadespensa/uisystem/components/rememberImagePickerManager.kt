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

package com.bitlabbr.minhadespensa.uisystem.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import platform.AVFoundation.*
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.UIKit.*
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.posix.memcpy

@Composable
actual fun rememberImagePickerManager(
    onImagePicked: (ByteArray?) -> Unit
): ImagePickerManager {
    val delegate = remember {
        object : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
            override fun imagePickerController(
                picker: UIImagePickerController,
                didFinishPickingMediaWithInfo: Map<Any?, *>
            ) {
                val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
                if (image != null) {
                    val processedBytes = processUIImage(
                        image = image,
                        targetMaxDimension = 720.0,
                        maxBytes = 100 * 1024
                    )
                    onImagePicked(processedBytes)
                } else {
                    onImagePicked(null)
                }
                picker.dismissViewControllerAnimated(true, null)
            }

            override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                onImagePicked(null)
                picker.dismissViewControllerAnimated(true, null)
            }
        }
    }

    return remember {
        object : ImagePickerManager {
            override fun launchCamera() {
                val rootController = UIApplication.sharedApplication.keyWindow?.rootViewController

                if (!UIImagePickerController.isSourceTypeAvailable(
                        UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
                    )
                ) {
                    launchGallery()
                    return
                }

                val authStatus = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
                when (authStatus) {
                    AVAuthorizationStatusAuthorized -> {
                        openCameraPicker(rootController, delegate)
                    }

                    AVAuthorizationStatusNotDetermined -> {
                        AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                            if (granted) {
                                dispatch_async(dispatch_get_main_queue()) {
                                    openCameraPicker(rootController, delegate)
                                }
                            } else {
                                onImagePicked(null)
                            }
                        }
                    }

                    else -> {
                        onImagePicked(null)
                    }
                }
            }

            override fun launchGallery() {
                val rootController = UIApplication.sharedApplication.keyWindow?.rootViewController
                val picker = UIImagePickerController().apply {
                    sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
                    this.delegate = delegate as? UINavigationControllerDelegateProtocol
                }
                rootController?.presentViewController(picker, animated = true, completion = null)
            }
        }
    }
}

private fun openCameraPicker(
    rootController: UIViewController?,
    delegate: UINavigationControllerDelegateProtocol
) {
    val picker = UIImagePickerController().apply {
        sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
        this.delegate = delegate
    }
    rootController?.presentViewController(picker, animated = true, completion = null)
}

@OptIn(ExperimentalForeignApi::class)
private fun processUIImage(
    image: UIImage,
    targetMaxDimension: Double,
    maxBytes: Int
): ByteArray? {
    return try {
        val originalWidth = image.size.useContents { width }
        val originalHeight = image.size.useContents { height }
        val currentMax = maxOf(originalWidth, originalHeight)

        val scaledImage = if (currentMax > targetMaxDimension) {
            val scale = targetMaxDimension / currentMax
            val targetWidth = originalWidth * scale
            val targetHeight = originalHeight * scale
            val targetSize = CGSizeMake(targetWidth, targetHeight)

            UIGraphicsBeginImageContextWithOptions(targetSize, false, 1.0)
            image.drawInRect(CGRectMake(0.0, 0.0, targetWidth, targetHeight))
            val newImage = UIGraphicsGetImageFromCurrentImageContext()
            UIGraphicsEndImageContext()
            newImage ?: image
        } else {
            image
        }

        var quality = 0.85
        var jpegData: NSData? = UIImageJPEGRepresentation(scaledImage, quality)

        while (jpegData != null && jpegData.length.toInt() > maxBytes && quality > 0.20) {
            quality -= 0.15
            jpegData = UIImageJPEGRepresentation(scaledImage, quality)
        }

        jpegData?.let { data ->
            val length = data.length.toInt()
            if (length > maxBytes) return null

            ByteArray(length).apply {
                usePinned { pinned ->
                    memcpy(pinned.addressOf(0), data.bytes, data.length)
                }
            }
        }
    } catch (_: Exception) {
        null
    }
}