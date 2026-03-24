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

import com.bitlabbr.minhadespensa.data.local.TestFileReader
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.test.runTest
import platform.Foundation.NSBundle
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.writeToFile
import platform.UIKit.UIImage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class IosImageProcessorTest {

    private val processor = ImageProcessorImpl()
    private val fileReader = TestFileReader()

    @OptIn(ExperimentalForeignApi::class)
    @Test
    fun `should resize and compress a small image to approximately 300x300 iOS`() = runTest {
        val largeImageBytes = fileReader.readBytes("low_def_image.jpeg")
        val originalSizeKb = largeImageBytes.size / 1024
        println("Original real image size: ${originalSizeKb}KB")
        val processedBytes = processor.processForThumbnail(largeImageBytes)
        val processedData = processedBytes.toNSData()
        val resultImage = UIImage.imageWithData(data = processedData)

        assertNotNull(resultImage, "Resulting bytes should form a valid UIImage")

        assertEquals(300.0, resultImage.size.useContents { width }, "Width should be 300px")
        assertEquals(300.0, resultImage.size.useContents { height }, "Height should be 300px")

        val finalSizeKb = processedBytes.size / 1024
        println("IOS: Final real image size: ${finalSizeKb}KB")

        assertTrue(finalSizeKb < 100, "The processed real image should have less than 100KB (has $finalSizeKb KB)")
    }

    @OptIn(ExperimentalForeignApi::class)
    @Test
    fun `visual check export ios`() = runTest {
        val largeImageBytes = fileReader.readBytes("hight_def_image.heic")
        val processedBytes = processor.processForThumbnail(largeImageBytes)
        val processedData = processedBytes.toNSData()
        val fileName = "test_output_ios.jpeg"
        val executablePath = NSBundle.mainBundle.executablePath ?: ""
        val executableDir = executablePath.substringBeforeLast("/")
        val buildDir = executableDir
            .substringBefore("/bin/")
            .plus("/build")
        val outputDir = "$buildDir/outputs/visual_checks"
        val finalPath = "$outputDir/$fileName"
        val fileManager = NSFileManager.defaultManager
        fileManager.createDirectoryAtPath(
            outputDir,
            withIntermediateDirectories = true,
            attributes = null,
            error = null
        )
        val success = processedData.writeToFile(finalPath, true)
        if (success) {
            println("Path to file: $finalPath")
        } else {
            val tempPath = NSTemporaryDirectory() + fileName
            processedData.writeToFile(tempPath, true)
            println("Failure while exporting image to $finalPath.")
            println("Path to file: $tempPath")
        }

        assertTrue(success, "Failure while exporting image to $finalPath.")
    }

    @OptIn(ExperimentalForeignApi::class)
    @Test
    fun `should resize and compress a large image to approximately 300x300 on iOS`() = runTest {
        val largeImageBytes = fileReader.readBytes("hight_def_image.heic")
        val originalSizeKb = largeImageBytes.size / 1024
        println("Original real image size: ${originalSizeKb}KB")
        val processedBytes = processor.processForThumbnail(largeImageBytes)
        val processedData = processedBytes.toNSData()
        val resultImage = UIImage.imageWithData(data = processedData)

        assertNotNull(resultImage, "Resulting bytes should form a valid UIImage")

        assertEquals(300.0, resultImage.size.useContents { width }, "Width should be 300px")
        assertEquals(300.0, resultImage.size.useContents { height }, "Height should be 300px")

        val finalSizeKb = processedBytes.size / 1024
        println("IOS: Final real image size: ${finalSizeKb}KB")

        assertTrue(finalSizeKb < 100, "The processed real image should have less than 100KB (has $finalSizeKb KB)")
    }
}