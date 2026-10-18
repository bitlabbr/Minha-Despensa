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

package com.bitlabbr.minhadespensa.core.domain.usecase

import com.bitlabbr.minhadespensa.core.domain.model.invoice.InvoiceItem
import com.bitlabbr.minhadespensa.core.domain.repository.InvoiceRepository
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants

class ImportInvoiceToPantryUseCase(
    private val invoiceRepository: InvoiceRepository,
) {
    suspend operator fun invoke(
        storeName: String,
        items: List<InvoiceItem>,
    ): Result<Int> = runCatching {
        val trimmedStore = storeName.trim()
        require(trimmedStore.isNotBlank()) { CoreConstants.Validation.ERROR_INVOICE_STORE_NAME_BLANK }

        val selectedItems = items.filter { it.isSelected }
        require(selectedItems.isNotEmpty()) { CoreConstants.Validation.ERROR_INVOICE_ITEMS_EMPTY }

        selectedItems.forEach { item ->
            require(item.name.isNotBlank()) { CoreConstants.Validation.ERROR_PRODUCT_NAME_BLANK }
            require(item.quantity > 0.0) { CoreConstants.Validation.ERROR_PANTRY_QUANTITY_POSITIVE }
            item.unitPriceInCents?.let { price ->
                require(price >= 0L) { CoreConstants.Validation.ERROR_PANTRY_UNIT_PRICE_NON_NEGATIVE }
            }
        }

        invoiceRepository.importInvoiceToPantry(trimmedStore, selectedItems).getOrThrow()
    }
}
