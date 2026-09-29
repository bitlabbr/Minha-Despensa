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

package com.bitlabbr.minhadespensa.uisystem.features.catalog.widgets.register

import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.core.domain.usecase.CheckEanStatusUseCase
import com.bitlabbr.minhadespensa.core.domain.util.CoreConstants
import com.bitlabbr.minhadespensa.uisystem.fakes.FakeCatalogRepository
import com.bitlabbr.minhadespensa.uisystem.model.UiText
import kotlinx.coroutines.test.runTest
import minhadespensa.uisystem.generated.resources.Res
import minhadespensa.uisystem.generated.resources.error_ean_already_exists
import minhadespensa.uisystem.generated.resources.error_ean_invalid_length
import minhadespensa.uisystem.generated.resources.error_invalid_number
import minhadespensa.uisystem.generated.resources.error_name_max_length
import minhadespensa.uisystem.generated.resources.error_name_required
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ProductFormValidatorTest {

    private val catalogRepository = FakeCatalogRepository()
    private val checkEanStatusUseCase = CheckEanStatusUseCase(catalogRepository)

    @Test
    fun validateFormFields_validatesNameAndWeight() {
        val current = ProductFormState(name = "Arroz", netWeight = "1.0")

        // 1. Name cleared
        val emptyName = validateFormFields(current, current.copy(name = "  "))
        assertNotNull(emptyName.nameError)
        assertEquals(Res.string.error_name_required, (emptyName.nameError as UiText.Resource).resource)

        // 2. Name too long
        val longName = validateFormFields(current, current.copy(name = "A".repeat(CoreConstants.Product.NAME_MAX_LENGTH + 1)))
        assertNotNull(longName.nameError)
        assertEquals(Res.string.error_name_max_length, (longName.nameError as UiText.Resource).resource)

        // 3. Invalid weight
        val invalidWeight = validateFormFields(current, current.copy(netWeight = "abc"))
        assertNotNull(invalidWeight.netWeightError)
        assertEquals(Res.string.error_invalid_number, (invalidWeight.netWeightError as UiText.Resource).resource)

        // 4. EAN change resets EAN error
        val withEanError = current.copy(eanError = UiText.Resource(Res.string.error_ean_invalid_length), isCheckingEan = true)
        val eanChanged = validateFormFields(withEanError, withEanError.copy(ean = "7891234567890"))
        assertNull(eanChanged.eanError)
        assertEquals(false, eanChanged.isCheckingEan)
    }

    @Test
    fun checkEanValidity_validatesLengthAndDuplicateStatus() = runTest {
        val existing = CatalogProduct(
            id = "prod-1",
            name = "Café Especial",
            ean = "7891234567890",
            updatedAt = 1000L,
        )
        catalogRepository.insertProduct(existing, null)

        // 1. Blank EAN returns null
        assertNull(checkEanValidity("   ", checkEanStatusUseCase))

        // 2. Invalid length returns error
        val invalidLengthError = checkEanValidity("12345", checkEanStatusUseCase)
        assertNotNull(invalidLengthError)
        assertIs<UiText.Resource>(invalidLengthError)
        assertEquals(Res.string.error_ean_invalid_length, invalidLengthError.resource)

        // 3. Duplicate EAN returns already exists error with product name
        val duplicateError = checkEanValidity("7891234567890", checkEanStatusUseCase)
        assertNotNull(duplicateError)
        assertIs<UiText.Resource>(duplicateError)
        assertEquals(Res.string.error_ean_already_exists, duplicateError.resource)
        assertEquals(listOf("Café Especial"), duplicateError.args)

        // 4. Valid and unique EAN returns null
        assertNull(checkEanValidity("7899999999999", checkEanStatusUseCase))

        // 5. Matches UPC-A 12-digit vs EAN-13 variation
        val upcAProduct = CatalogProduct(
            id = "prod-upc",
            name = "Suco de Uva",
            ean = "012345678905", // 12 digits
            updatedAt = 1000L,
        )
        catalogRepository.insertProduct(upcAProduct, null)

        val variationDuplicate = checkEanValidity("0012345678905", checkEanStatusUseCase)
        assertNotNull(variationDuplicate)
        assertIs<UiText.Resource>(variationDuplicate)
        assertEquals(Res.string.error_ean_already_exists, variationDuplicate.resource)
        assertEquals(listOf("Suco de Uva"), variationDuplicate.args)
    }
}
