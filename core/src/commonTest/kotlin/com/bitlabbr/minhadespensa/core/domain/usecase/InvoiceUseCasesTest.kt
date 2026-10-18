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

import com.bitlabbr.minhadespensa.core.domain.fakes.FakeInvoiceRepository
import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import com.bitlabbr.minhadespensa.core.domain.model.invoice.Invoice
import com.bitlabbr.minhadespensa.core.domain.model.invoice.InvoiceItem
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InvoiceUseCasesTest {

    private val invoiceRepository = FakeInvoiceRepository()
    private val parseInvoiceQrCodeUseCase = ParseInvoiceQrCodeUseCase(invoiceRepository)
    private val importInvoiceToPantryUseCase = ImportInvoiceToPantryUseCase(invoiceRepository)

    @Test
    fun `ParseInvoiceQrCodeUseCase should fail when qrCodeContent is blank`() = runTest {
        val result = parseInvoiceQrCodeUseCase("   ")
        assertTrue(result.isFailure)
        assertEquals(CoreConstants.Validation.ERROR_INVOICE_QR_CODE_BLANK, result.exceptionOrNull()?.message)
    }

    @Test
    fun `ParseInvoiceQrCodeUseCase should trim and delegate to repository on success`() = runTest {
        val expectedInvoice = Invoice(
            accessKey = "26241012345678000190650010001234561001234567",
            storeName = "Supermercado Bom Preço",
            items = listOf(
                InvoiceItem(
                    id = "item-1",
                    ean = "7891234567890",
                    name = "Arroz Branco 1kg",
                    quantity = 2.0,
                    measureUnit = MeasureUnit.UNIT,
                    unitPriceInCents = 599,
                    totalPriceInCents = 1198,
                )
            ),
        )
        invoiceRepository.parseResult = Result.success(expectedInvoice)

        val result = parseInvoiceQrCodeUseCase("  http://nfce.sefaz.pe.gov.br/nfce/consulta?p=123  ")
        assertTrue(result.isSuccess)
        assertEquals(expectedInvoice, result.getOrNull())
        assertEquals("http://nfce.sefaz.pe.gov.br/nfce/consulta?p=123", invoiceRepository.lastParsedQrCode)
    }

    @Test
    fun `ParseInvoiceQrCodeUseCase should propagate repository failure`() = runTest {
        invoiceRepository.parseResult = Result.failure(RuntimeException("Falha de conexão com servidor"))

        val result = parseInvoiceQrCodeUseCase("http://nfce.sefaz.pe.gov.br")
        assertTrue(result.isFailure)
        assertEquals("Falha de conexão com servidor", result.exceptionOrNull()?.message)
    }

    @Test
    fun `ImportInvoiceToPantryUseCase should fail when storeName is blank`() = runTest {
        val items = listOf(
            InvoiceItem(id = "1", name = "Feijão", quantity = 1.0)
        )
        val result = importInvoiceToPantryUseCase("  ", items)
        assertTrue(result.isFailure)
        assertEquals(CoreConstants.Validation.ERROR_INVOICE_STORE_NAME_BLANK, result.exceptionOrNull()?.message)
    }

    @Test
    fun `ImportInvoiceToPantryUseCase should fail when no items are selected`() = runTest {
        val items = listOf(
            InvoiceItem(id = "1", name = "Feijão", quantity = 1.0, isSelected = false),
            InvoiceItem(id = "2", name = "Arroz", quantity = 2.0, isSelected = false),
        )
        val result = importInvoiceToPantryUseCase("Mercado Central", items)
        assertTrue(result.isFailure)
        assertEquals(CoreConstants.Validation.ERROR_INVOICE_ITEMS_EMPTY, result.exceptionOrNull()?.message)
    }

    @Test
    fun `ImportInvoiceToPantryUseCase should fail when an item has blank name`() = runTest {
        val items = listOf(
            InvoiceItem(id = "1", name = "   ", quantity = 1.0, isSelected = true)
        )
        val result = importInvoiceToPantryUseCase("Mercado Central", items)
        assertTrue(result.isFailure)
        assertEquals(CoreConstants.Validation.ERROR_PRODUCT_NAME_BLANK, result.exceptionOrNull()?.message)
    }

    @Test
    fun `ImportInvoiceToPantryUseCase should fail when an item has zero or negative quantity`() = runTest {
        val items = listOf(
            InvoiceItem(id = "1", name = "Macarrão", quantity = 0.0, isSelected = true)
        )
        val result = importInvoiceToPantryUseCase("Mercado Central", items)
        assertTrue(result.isFailure)
        assertEquals(CoreConstants.Validation.ERROR_PANTRY_QUANTITY_POSITIVE, result.exceptionOrNull()?.message)
    }

    @Test
    fun `ImportInvoiceToPantryUseCase should fail when an item has negative unit price`() = runTest {
        val items = listOf(
            InvoiceItem(id = "1", name = "Macarrão", quantity = 1.0, unitPriceInCents = -10L, isSelected = true)
        )
        val result = importInvoiceToPantryUseCase("Mercado Central", items)
        assertTrue(result.isFailure)
        assertEquals(CoreConstants.Validation.ERROR_PANTRY_UNIT_PRICE_NON_NEGATIVE, result.exceptionOrNull()?.message)
    }

    @Test
    fun `ImportInvoiceToPantryUseCase should only pass selected items and trim storeName`() = runTest {
        val selectedItem = InvoiceItem(id = "1", name = "Azeite", quantity = 1.0, unitPriceInCents = 3500L, isSelected = true)
        val unselectedItem = InvoiceItem(id = "2", name = "Sacola", quantity = 1.0, unitPriceInCents = 15L, isSelected = false)

        invoiceRepository.importResult = Result.success(1)

        val result = importInvoiceToPantryUseCase("  Hipermercado Extra  ", listOf(selectedItem, unselectedItem))
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull())
        assertEquals("Hipermercado Extra", invoiceRepository.lastImportedStore)
        assertEquals(listOf(selectedItem), invoiceRepository.lastImportedItems)
    }

    @Test
    fun `Invoice and InvoiceItem computed properties should calculate totals and selections correctly`() {
        val item1 = InvoiceItem(
            id = "1",
            name = "Leite 1L",
            quantity = 3.0,
            unitPriceInCents = 500L,
            totalPriceInCents = null, // fallback calculation: 3.0 * 500 = 1500
            isSelected = true,
        )
        val item2 = InvoiceItem(
            id = "2",
            name = "Queijo 500g",
            quantity = 1.0,
            unitPriceInCents = 2400L,
            totalPriceInCents = 2400L,
            isSelected = false,
        )

        assertEquals(1500L, item1.computedTotalPriceInCents)
        assertEquals(2400L, item2.computedTotalPriceInCents)

        val invoice = Invoice(
            storeName = "Padaria",
            items = listOf(item1, item2),
        )

        assertEquals(3900L, invoice.totalInvoiceInCents)
        assertEquals(1, invoice.totalSelectedItems)
        assertEquals(1500L, invoice.totalSelectedInCents)
    }
}
