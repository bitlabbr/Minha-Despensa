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

import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import com.bitlabbr.minhadespensa.data.local.TestFileReader
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.test.runTest
import platform.Foundation.NSBundle
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.writeToFile
import platform.UIKit.UIImage
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class IosImageProcessorTest {

    private val processor = ImageProcessorImpl()
    private val fileReader = TestFileReader()
    private val lowDefinitionImage = "low_def_image.jpeg"
    private val highDefinitionImage = "hight_def_image.heic"

    @OptIn(ExperimentalForeignApi::class)
    @Test
    fun `should resize and compress a SMALL IMAGE to approximately 300x300 iOS`() = runTest {
        val largeImageBytes = fileReader.readBytes(lowDefinitionImage)
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
    fun `should resize and compress a LARGE IMAGE to approximately 300x300 on iOS`() = runTest {
        val largeImageBytes = fileReader.readBytes(highDefinitionImage)
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
    @Ignore
    fun `visual check export HD ios`() = runTest {
        val largeImageBytes = fileReader.readBytes(highDefinitionImage)
        val processedBytes = processor.processForThumbnail(largeImageBytes)
        val processedData = processedBytes.toNSData()
        val fileName = "test_output_ios${getCurrentTime()}.jpeg"
        val executablePath = NSBundle.mainBundle.executablePath ?: ""
        val executableDir = executablePath.substringBeforeLast("/")
        val buildDir = executableDir
            .substringBefore("/bin/")
            .plus("/debug")
        val outputDir = "$buildDir/ios"
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
    @Ignore
    fun `visual check export LD ios`() = runTest {
        val largeImageBytes = fileReader.readBytes(lowDefinitionImage)
        val processedBytes = processor.processForThumbnail(largeImageBytes)
        val processedData = processedBytes.toNSData()
        val fileName = "test_output_ios${getCurrentTime()}.jpeg"
        val executablePath = NSBundle.mainBundle.executablePath ?: ""
        val executableDir = executablePath.substringBeforeLast("/")
        val buildDir = executableDir
            .substringBefore("/bin/")
            .plus("/debug")
        val outputDir = "$buildDir/ios"
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
}