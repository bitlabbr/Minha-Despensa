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

import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import com.bitlabbr.minhadespensa.data.local.TestFileReader
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class AndroidImageProcessorTest {
    private val processor = ImageProcessorImpl()
    private val fileReader = TestFileReader()
    private val lowDefinitionImage = "low_def_image.jpeg"
    private val highDefinitionImage = "galaxy_hight-definition.jpg"

    @Test
    fun `should verify if processed image is not empty`() = runTest {
        val largeImageBytes = fileReader.readBytes(lowDefinitionImage)
        val processedBytes = processor.processForThumbnail(largeImageBytes)

        assertTrue(processedBytes.size > 5000, "Very small image (${processedBytes.size} bytes)")

        val resultBitmap = BitmapFactory.decodeByteArray(processedBytes, 0, processedBytes.size)
        assertNotNull(resultBitmap, "Failure while processing image.")

        val centerPixel = resultBitmap.getPixel(150, 150)

        assertNotEquals(0, centerPixel, "the central pixel is empty")
    }

    @Test
    fun `should resize and compress a SMALL IMAGE to approximately 300x300`() = runTest {
        val smallImageBytes = fileReader.readBytes(lowDefinitionImage)
        val originalSizeKb = smallImageBytes.size / 1024
        println("Android: Original real image size: ${originalSizeKb}KB")
        val processedBytes = processor.processForThumbnail(smallImageBytes)
        val resultBitmap = BitmapFactory.decodeByteArray(processedBytes, 0, processedBytes.size)

        assertNotNull(resultBitmap, "The result should be a valid Bitmap")
        assertEquals(300, resultBitmap.width, "width should be 300px")
        assertEquals(300, resultBitmap.height, "height should be 300px")

        val finalSizeKb = processedBytes.size / 1024
        println("Android: Final real image size: ${finalSizeKb}KB")

        assertTrue(finalSizeKb < 100, "The image should have less than 100KB (it has: $finalSizeKb KB)")
        assertTrue(finalSizeKb > 20, "The image should have more than 20KB (it has: $finalSizeKb KB)")
    }

    @Test
    fun `should resize and compress a LARGE IMAGE to approximately 300x300`() = runTest {
        val largeImageBytes = fileReader.readBytes(highDefinitionImage)
        val originalSizeKb = largeImageBytes.size / 1024
        println("Android: Original real image size: ${originalSizeKb}KB")
        val processedBytes = processor.processForThumbnail(largeImageBytes)
        val resultBitmap = BitmapFactory.decodeByteArray(processedBytes, 0, processedBytes.size)

        assertNotNull(resultBitmap, "The result should be a valid Bitmap")
        assertEquals(300, resultBitmap.width, "width should be 300px")
        assertEquals(300, resultBitmap.height, "height should be 300px")

        val finalSizeKb = processedBytes.size / 1024
        println("Android: Final real image size: ${finalSizeKb}KB")

        assertTrue(finalSizeKb < 100, "The image should have less than 100KB (it has: $finalSizeKb KB)")
        assertTrue(finalSizeKb > 20, "The image should have more than 20KB (it has: $finalSizeKb KB)")
    }

    @Test
    @Ignore("image manual debug test")
    fun `debug jpeg crop pixels`() = runTest {
        val bytes = fileReader.readBytes(highDefinitionImage)

        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        println("mimeType: ${options.outMimeType}")
        println("outWidth: ${options.outWidth}, outHeight: ${options.outHeight}")

        options.inSampleSize = 1
        options.inJustDecodeBounds = false
        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)!!

        println("bitmap: ${bmp.width}x${bmp.height}")

        println("TopLeft     pixel: ${bmp.getPixel(0, 0)}")
        println("TopRight    pixel: ${bmp.getPixel(bmp.width - 1, 0)}")
        println("BottomLeft  pixel: ${bmp.getPixel(0, bmp.height - 1)}")
        println("BottomRight pixel: ${bmp.getPixel(bmp.width - 1, bmp.height - 1)}")
        println("Center      pixel: ${bmp.getPixel(bmp.width / 2, bmp.height / 2)}")

        val startX = (bmp.width - minOf(bmp.width, bmp.height)) / 2
        val startY = (bmp.height - minOf(bmp.width, bmp.height)) / 2

        println("crop startX: $startX, startY: $startY")
        println("crop region TL pixel: ${bmp.getPixel(startX, startY)}")
        println("crop region TR pixel: ${bmp.getPixel(startX + minOf(bmp.width, bmp.height) - 1, startY)}")
    }

    @Test
    @Ignore("image manual debug test")
    fun `visual check export`() = runTest {
        val largeImageBytes = fileReader.readBytes(highDefinitionImage)
        val processedBytes = processor.processForThumbnail(largeImageBytes)

        val outputFile = java.io.File("build/debug/android/test_output_android_ts-${getCurrentTime()}.jpeg")
        outputFile.parentFile?.mkdirs()
        outputFile.writeBytes(processedBytes)
        println("Path to file: ${outputFile.absolutePath}")
    }
}