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

package com.bitlabbr.minhadespensa.core.domain.util

object CoreConstants {

    object Product {
        const val NAME_MAX_LENGTH = 30
        const val BRAND_MAX_LENGTH = 30
        const val CATEGORY_MAX_LENGTH = 20
        const val NOTES_MAX_LENGTH = 255
        const val EAN_MAX_LENGTH = 14
        val EAN_VALID_LENGTHS = setOf(8, 13, 14)
        const val DEFAULT_NET_WEIGHT = 1.0
    }

    object Media {
        const val MAX_IMAGE_SIZE_KB = 100
        const val MAX_IMAGE_SIZE_BYTES = 100 * 1024
        const val TARGET_MAX_DIMENSION = 720
    }

    object ShoppingList {
        const val NAME_MAX_LENGTH = 100
    }

    object Pantry {
        const val EXPIRING_THRESHOLD_DAYS = 7
    }
}