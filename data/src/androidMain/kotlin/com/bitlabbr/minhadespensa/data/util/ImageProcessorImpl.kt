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
import com.bitlabbr.minhadespensa.core.domain.util.ImageProcessor
import java.io.ByteArrayOutputStream

actual class ImageProcessorImpl : ImageProcessor {
    actual override suspend fun processForThumbnail(input: ByteArray): ByteArray {
        // 1. Decodifica apenas as bordas para descobrir o tamanho original sem carregar na RAM
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(input, 0, input.size, options)

        // 2. Calcula o downsampling inicial (economiza memória)
        options.inSampleSize = calculateInSampleSize(options, 300, 300)
        options.inJustDecodeBounds = false

        // 3. Decodifica a imagem real (com o tamanho já reduzido pelo inSampleSize)
        val sampledBitmap = BitmapFactory.decodeByteArray(input, 0, input.size, options)
            ?: throw IllegalArgumentException("Falha ao decodificar a imagem. Formato incompatível ou corrompido.")

        // 4. Lógica de Center Crop (Igual ao iOS)
        val width = sampledBitmap.width
        val height = sampledBitmap.height
        val minEdge = minOf(width, height) // Identifica o menor lado para ser a base do quadrado

        // Calcula o ponto inicial para o corte ser no centro
        val startX = (width - minEdge) / 2
        val startY = (height - minEdge) / 2

        // Cria o bitmap quadrado a partir do centro
        val croppedBitmap = Bitmap.createBitmap(
            sampledBitmap,
            startX,
            startY,
            minEdge,
            minEdge
        )

        // 5. Redimensiona o quadrado centralizado para os 300x300 finais
        // O parâmetro 'filter = true' ativa a filtragem bilinear (mais nitidez)
        val finalBitmap = Bitmap.createScaledBitmap(croppedBitmap, 300, 300, true)

        // 6. Comprime para JPEG com 80% de qualidade
        val outputStream = ByteArrayOutputStream()
        finalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)

        // Limpeza de memória manual para Bitmaps intermediários
        if (sampledBitmap != croppedBitmap) sampledBitmap.recycle()
        // O finalBitmap será reciclado pelo GC após o retorno

        return outputStream.toByteArray()
    }
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
