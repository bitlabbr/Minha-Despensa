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
import com.bitlabbr.minhadespensa.data.local.TestFileReader
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class AndroidImageProcessorTest {
    private val processor = ImageProcessorImpl()
    private val fileReader = TestFileReader()

    @Test
    fun `should verify if processed image is not empty`() = runTest {
        val largeImageBytes = fileReader.readBytes("low_def_image.jpeg")
        val processedBytes = processor.processForThumbnail(largeImageBytes)

        assertTrue(processedBytes.size > 5000, "Imagem muito pequena (${processedBytes.size} bytes). Provável falha de processamento.")

        val resultBitmap = BitmapFactory.decodeByteArray(processedBytes, 0, processedBytes.size)
        assertNotNull(resultBitmap, "Falha ao decodificar o resultado processado.")

        val centerPixel = resultBitmap.getPixel(150, 150)

        assertNotEquals(0, centerPixel, "O pixel central está vazio (transparente). A imagem é inválida.")
    }

    @Test
    fun `visual check export`() = runTest {
        val largeImageBytes = fileReader.readBytes("low_def_image.jpeg")
        val processedBytes = processor.processForThumbnail(largeImageBytes)

        val outputFile = java.io.File("build/test_output_android.jpeg")
        outputFile.writeBytes(processedBytes)
        println("📸 Verifique a imagem gerada em: ${outputFile.absolutePath}")
    }

    @Test
    fun `should resize and compress a small image to approximately 300x300`() = runTest {
        val smallImageBytes = fileReader.readBytes("low_def_image.jpeg")
        val originalSizeKb = smallImageBytes.size / 1024
        println("Android: Original real image size: ${originalSizeKb}KB")
        val processedBytes = processor.processForThumbnail(smallImageBytes)
        val resultBitmap = BitmapFactory.decodeByteArray(processedBytes, 0, processedBytes.size)

        assertNotNull(resultBitmap, "O resultado deve ser um Bitmap válido")
        assertEquals(300, resultBitmap.width, "A largura deve ser 300px")
        assertEquals(300, resultBitmap.height, "A altura deve ser 300px")

        val finalSizeKb = processedBytes.size / 1024
        println("Android: Final real image size: ${finalSizeKb}KB")

        assertTrue(finalSizeKb < 100, "A imagem final deve ter menos de 100KB (teve $finalSizeKb KB)")
    }

    @Test
    fun `should resize and compress a large image to approximately 300x300`() = runTest {
        val largeImageBytes = fileReader.readBytes("hight_def_image.heic")
        val originalSizeKb = largeImageBytes.size / 1024
        println("Android: Original real image size: ${originalSizeKb}KB")
        val processedBytes = processor.processForThumbnail(largeImageBytes)
        val resultBitmap = BitmapFactory.decodeByteArray(processedBytes, 0, processedBytes.size)

        assertNotNull(resultBitmap, "O resultado deve ser um Bitmap válido")
        assertEquals(300, resultBitmap.width, "A largura deve ser 300px")
        assertEquals(300, resultBitmap.height, "A altura deve ser 300px")

        val finalSizeKb = processedBytes.size / 1024
        println("Android: Final real image size: ${finalSizeKb}KB")

        assertTrue(finalSizeKb < 100, "A imagem final deve ter menos de 100KB (teve $finalSizeKb KB)")
    }
}