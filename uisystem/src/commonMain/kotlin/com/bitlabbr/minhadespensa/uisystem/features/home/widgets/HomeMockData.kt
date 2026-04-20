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

package com.bitlabbr.minhadespensa.uisystem.features.home.widgets

import com.bitlabbr.minhadespensa.core.domain.model.ConsumptionTrendItemCard
import com.bitlabbr.minhadespensa.core.domain.model.ExpiringItemCard
import com.bitlabbr.minhadespensa.core.domain.model.IconKeys

object HomeMockData {
    val widgets = listOf(
        HomeWidget.FinancialSummary(
            consumptionProgress = 0.85f,
            consumptionTargetChartLabel = "R$1000,00",
            currentConsumption = "R$850,00",
            budget = "R$1000,00",
        ),
        HomeWidget.ExpiringSoon(
            items = listOf(
                ExpiringItemCard(
                    expirationLabel = "Hoje",
                    productName = "Suco de Laranja",
                    productCategory = "Frutas e verduras",
                    anchoredTargetLabel = "04/03",
                    anchoredGaugeProgress = 1.0f,
                    iconPainterURI = IconKeys.BEVERAGE_JUICE
                ),
                ExpiringItemCard(
                    expirationLabel = "Este mês",
                    productName = "Arroz parboilizado",
                    productCategory = "Grãos",
                    anchoredTargetLabel = "03/03",
                    anchoredGaugeProgress = 0.5f,
                    iconPainterURI = IconKeys.GRAINS_RICE
                ),
                ExpiringItemCard(
                    expirationLabel = "Esta semana",
                    productName = "Cerveja Pilsen",
                    anchoredTargetLabel = "04/03",
                    productCategory = "Bebidas",
                    anchoredGaugeProgress = 0.9f,
                    iconPainterURI = IconKeys.BEVERAGE_BEER
                ),
                ExpiringItemCard(
                    expirationLabel = "Hoje",
                    productName = "Suco de Laranja",
                    productCategory = "Frutas e verduras",
                    anchoredTargetLabel = "04/03",
                    anchoredGaugeProgress = 1.0f,
                    iconPainterURI = IconKeys.BEVERAGE_JUICE
                ),
                ExpiringItemCard(
                    expirationLabel = "Amanhã",
                    productName = "Coxa de frango",
                    anchoredTargetLabel = "04/03",
                    productCategory = "Proteína animal",
                    anchoredGaugeProgress = 0.95f,
                    iconPainterURI = IconKeys.PROTEIN_CHICKEN_LEG
                ),
                ExpiringItemCard(
                    expirationLabel = "15 dias",
                    productName = "Queijo Parmesão",
                    anchoredTargetLabel = "04/03",
                    productCategory = "Laticíneos",
                    anchoredGaugeProgress = 0.7f,
                    iconPainterURI = IconKeys.MILK_EGGS_PARMESAN
                ),
                ExpiringItemCard(
                    expirationLabel = "Semana passada",
                    productName = "Café",
                    anchoredTargetLabel = "04/03",
                    productCategory = "Bebidas",
                    anchoredGaugeProgress = 1.3f,
                    iconPainterURI = IconKeys.BEVERAGE_COFFEE
                ),
                ExpiringItemCard(
                    expirationLabel = "teste",
                    productName = "teste icone ausente",
                    anchoredTargetLabel = "04/03",
                    productCategory = "teste",
                    anchoredGaugeProgress = 1.3f,
                    iconPainterURI = null
                ),
            )
        ),
        HomeWidget.ConsumptionTrend(
            items = listOf(
                ConsumptionTrendItemCard(
                    productName = "Café",
                    productCategory = "Bebidas",
                    iconPainterURI = IconKeys.BEVERAGE_COFFEE,
                    productMeasureUnity = "Pacotes",
                    consumptionAmount = "8",
                ),
                ConsumptionTrendItemCard(
                    productName = "Arroz Parboilizado",
                    productCategory = "Grãos",
                    iconPainterURI = IconKeys.GRAINS_RICE,
                    productMeasureUnity = "Quilos",
                    consumptionAmount = "4",
                ),
                ConsumptionTrendItemCard(
                    productName = "Feijão Mulatinho",
                    productCategory = "Grãos",
                    iconPainterURI = IconKeys.GRAINS_BEAN_BROWN,
                    productMeasureUnity = "Quilos",
                    consumptionAmount = "2",
                ),
                ConsumptionTrendItemCard(
                    productName = "Coca-Cola",
                    productCategory = "Bebidas",
                    iconPainterURI = IconKeys.BEVERAGE_SODA_COLA,
                    productMeasureUnity = "Litros",
                    consumptionAmount = "2,5",
                ),
                ConsumptionTrendItemCard(
                    productName = "Vinho Tinto",
                    productCategory = "Bebidas",
                    iconPainterURI = IconKeys.BEVERAGE_WINE,
                    productMeasureUnity = "Unidade",
                    consumptionAmount = "1",
                ),
                ConsumptionTrendItemCard(
                    productName = "Papel Toalha",
                    productCategory = "Produtos de Limpeza",
                    iconPainterURI = IconKeys.CLEANING_PAPER_TOWEL,
                    productMeasureUnity = "Pacote",
                    consumptionAmount = "0",
                )
            )
        )
    )
}