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

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Build
import com.bitlabbr.minhadespensa.core.domain.util.ImageProcessor
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

actual class ImageProcessorImpl : ImageProcessor {
    actual override suspend fun processForThumbnail(input: ByteArray): ByteArray {
        val decodedBitmap = decodeByteArray(input)
            ?: throw IllegalArgumentException("Failure trying to decode image.")

        val orientedBitmap = fixExifOrientation(input, decodedBitmap)
        val croppedBitmap = centerCrop(orientedBitmap)

        val finalBitmap = Bitmap.createScaledBitmap(croppedBitmap, 300, 300, true)

        val outputStream = ByteArrayOutputStream()
        finalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)

        if (orientedBitmap != decodedBitmap) decodedBitmap.recycle()
        if (croppedBitmap != orientedBitmap) orientedBitmap.recycle()
        if (finalBitmap != croppedBitmap) croppedBitmap.recycle()

        finalBitmap.recycle()
        return outputStream.toByteArray()
    }

    private fun decodeByteArray(input: ByteArray): Bitmap? {
        val bitmap = BitmapFactory.decodeByteArray(input, 0, input.size)
        if (bitmap != null) return bitmap
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return try {
                val source = android.graphics.ImageDecoder.createSource(
                    java.nio.ByteBuffer.wrap(input)
                )
                android.graphics.ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE
                }
            } catch (e: Exception) {
                null
            }
        }

        return null
    }

    private fun fixExifOrientation(input: ByteArray, bitmap: Bitmap): Bitmap {
        return try {
            val exif = ExifInterface(ByteArrayInputStream(input))
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    matrix.preScale(-1f, 1f)
                    matrix.postRotate(270f)
                }

                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    matrix.preScale(-1f, 1f)
                    matrix.postRotate(90f)
                }

                else -> return bitmap
            }

            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: Exception) {
            bitmap
        }
    }

    private fun centerCrop(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width == height) return bitmap

        val minEdge = minOf(width, height)
        val startX = (width - minEdge) / 2
        val startY = (height - minEdge) / 2

        return Bitmap.createBitmap(bitmap, startX, startY, minEdge, minEdge)
    }
}
