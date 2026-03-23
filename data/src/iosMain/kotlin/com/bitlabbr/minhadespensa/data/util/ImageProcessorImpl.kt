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

package com.bitlabbr.minhadespensa.data.util

import com.bitlabbr.minhadespensa.core.domain.util.ImageProcessor
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pin
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.posix.memcpy

actual class ImageProcessorImpl : ImageProcessor {
    @OptIn(ExperimentalForeignApi::class)
    actual override suspend fun processForThumbnail(input: ByteArray): ByteArray {
        val data = input.toNSData()
        val image = UIImage.imageWithData(data = data) ?: return byteArrayOf()
        val size = CGSizeMake(300.0, 300.0)
        UIGraphicsBeginImageContextWithOptions(size, false, 1.0)
        image.drawInRect(rect = CGRectMake(0.0, 0.0, 300.0, 300.0))
        val resizedImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return resizedImage?.let {
            UIImageJPEGRepresentation(it, 0.8)?.toByteArray()
        } ?: byteArrayOf()
    }

    @OptIn(ExperimentalForeignApi::class)
    fun ByteArray.toNSData(): NSData = memScoped {
        val pinned = this@toNSData.pin()
        NSData.dataWithBytes(pinned.addressOf(0), this@toNSData.size.toULong()).also {
            pinned.unpin()
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    fun NSData.toByteArray(): ByteArray {
        val size = length.toInt()
        val byteArray = ByteArray(size)
        if (size > 0) {
            byteArray.usePinned { pinned ->
                memcpy(pinned.addressOf(0), bytes, length)
            }
        }
        return byteArray
    }
}
