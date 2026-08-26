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
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.UIKit.*
import platform.darwin.NSObject
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
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
                    val jpegData: NSData? = UIImageJPEGRepresentation(image, 0.6)
                    if (jpegData != null) {
                        val bytes = ByteArray(jpegData.length.toInt()).apply {
                            usePinned { pinned ->
                                memcpy(pinned.addressOf(0), jpegData.bytes, jpegData.length)
                            }
                        }
                        onImagePicked(bytes)
                    } else {
                        onImagePicked(null)
                    }
                }
                picker.dismissViewControllerAnimated(true, null)
            }

            override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                picker.dismissViewControllerAnimated(true, null)
            }
        }
    }

    return remember {
        object : ImagePickerManager {
            override fun launchCamera() {
                val rootController = UIApplication.sharedApplication.keyWindow?.rootViewController
                if (UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera)) {
                    val picker = UIImagePickerController().apply {
                        sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
                        this.delegate = delegate
                    }
                    rootController?.presentViewController(picker, animated = true, completion = null)
                } else {
                    launchGallery()
                }
            }

            override fun launchGallery() {
                val rootController = UIApplication.sharedApplication.keyWindow?.rootViewController
                val picker = UIImagePickerController().apply {
                    sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
                    this.delegate = delegate
                }
                rootController?.presentViewController(picker, animated = true, completion = null)
            }
        }
    }
}