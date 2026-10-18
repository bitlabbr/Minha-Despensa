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

package com.bitlabbr.minhadespensa.core.domain.fakes

import com.bitlabbr.minhadespensa.core.domain.model.invoice.Invoice
import com.bitlabbr.minhadespensa.core.domain.model.invoice.InvoiceItem
import com.bitlabbr.minhadespensa.core.domain.repository.InvoiceRepository

class FakeInvoiceRepository : InvoiceRepository {

    var parseResult: Result<Invoice> = Result.failure(IllegalStateException("Fake result not configured"))
    var importResult: Result<Int> = Result.success(0)

    var lastParsedQrCode: String? = null
    var lastImportedStore: String? = null
    var lastImportedItems: List<InvoiceItem> = emptyList()

    override suspend fun parseInvoiceFromQrCode(qrCodeContent: String): Result<Invoice> {
        lastParsedQrCode = qrCodeContent
        return parseResult
    }

    override suspend fun importInvoiceToPantry(storeName: String, items: List<InvoiceItem>): Result<Int> {
        lastImportedStore = storeName
        lastImportedItems = items
        return importResult
    }
}
