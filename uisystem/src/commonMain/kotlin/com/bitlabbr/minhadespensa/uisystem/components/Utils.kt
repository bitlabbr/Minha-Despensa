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

package com.bitlabbr.minhadespensa.uisystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import com.bitlabbr.minhadespensa.core.domain.model.IconKeys
import org.jetbrains.compose.resources.painterResource
import minhadespensa.uisystem.generated.resources.*

@Composable
fun getIconPainterFromString(iconName: String?): Painter? {
    if (iconName.isNullOrEmpty()) return null

    val resource = when (iconName) {
        // BEBIDAS
        IconKeys.BEVERAGE_BEER -> Res.drawable.icon_beverage_beer
        IconKeys.BEVERAGE_BEER_CAN -> Res.drawable.icon_beverage_beer_can
        IconKeys.BEVERAGE_COFFEE -> Res.drawable.icon_beverage_coffee
        IconKeys.BEVERAGE_JUICE -> Res.drawable.icon_beverage_juice
        IconKeys.BEVERAGE_SODA -> Res.drawable.icon_beverage_soda
        IconKeys.BEVERAGE_SODA_COLA -> Res.drawable.icon_beverage_soda_cola
        IconKeys.BEVERAGE_SODA_LIME -> Res.drawable.icon_beverage_soda_lime
        IconKeys.BEVERAGE_WATER -> Res.drawable.icon_beverage_water
        IconKeys.BEVERAGE_WHISKY -> Res.drawable.icon_beverage_whisky
        IconKeys.BEVERAGE_WINE -> Res.drawable.icon_beverage_wine

        // LIMPEZA
        IconKeys.CLEANING_BLEACH -> Res.drawable.icon_cleaning_bleach
        IconKeys.CLEANING_DETERGENT -> Res.drawable.icon_cleaning_detergent
        IconKeys.CLEANING_DISINFECTANT -> Res.drawable.icon_cleaning_disinfectant
        IconKeys.CLEANING_FABRIC_SOAP_LIQUID -> Res.drawable.icon_cleaning_fabric_soap_liquid
        IconKeys.CLEANING_FABRIC_SOAP_POWDER -> Res.drawable.icon_cleaning_fabric_soap_powder
        IconKeys.CLEANING_FABRIC_SOFTENER -> Res.drawable.icon_cleaning_fabric_softener
        IconKeys.CLEANING_PAPER_TOWEL -> Res.drawable.icon_cleaning_paper_towel

        // GRÃOS
        IconKeys.GRAINS_BEAN_BLACK -> Res.drawable.icon_grains_bean_black
        IconKeys.GRAINS_BEAN_BROWN -> Res.drawable.icon_grains_bean_brown
        IconKeys.GRAINS_CHICKPEA -> Res.drawable.icon_grains_chickpea
        IconKeys.GRAINS_CORN -> Res.drawable.icon_grains_corn
        IconKeys.GRAINS_OAT -> Res.drawable.icon_grains_oat
        IconKeys.GRAINS_RICE -> Res.drawable.icon_grains_rice
        IconKeys.GRAINS_RICE_INTEGRAL -> Res.drawable.icon_grains_rice_integral
        IconKeys.GRAINS_WHEAT_FLOUR -> Res.drawable.icon_grains_wheat_flour

        // HIGIENE
        IconKeys.HYGIENE_SHAMPOO -> Res.drawable.icon_hygiene_shampoo
        IconKeys.HYGIENE_SOAP -> Res.drawable.icon_hygiene_soap
        IconKeys.HYGIENE_TOILET_PAPER -> Res.drawable.icon_hygiene_toilet_paper

        // LATICÍNIOS E OVOS
        IconKeys.MILK_EGGS_BUTTER -> Res.drawable.icon_milk_eggs_butter
        IconKeys.MILK_EGGS_CHEESE_GOUDA -> Res.drawable.icon_milk_eggs_cheese_gouda
        IconKeys.MILK_EGGS_CHEESE_SWISS -> Res.drawable.icon_milk_eggs_cheese_swiss
        IconKeys.MILK_EGGS_CREAM_CHEESE -> Res.drawable.icon_milk_eggs_cream_cheese
        IconKeys.MILK_EGGS_EGG_BOX -> Res.drawable.icon_milk_eggs_egg_box
        IconKeys.MILK_EGGS_MILK_BOX -> Res.drawable.icon_milk_eggs_milk_box
        IconKeys.MILK_EGGS_MILK_FRESH -> Res.drawable.icon_milk_eggs_milk_fresh
        IconKeys.MILK_EGGS_PARMESAN -> Res.drawable.icon_milk_eggs_parmesan
        IconKeys.MILK_EGGS_YOGURT -> Res.drawable.icon_milk_eggs_yogurt

        // PROTEÍNAS
        IconKeys.PROTEIN_CHICKEN -> Res.drawable.icon_protein_chicken
        IconKeys.PROTEIN_CHICKEN_CHEST -> Res.drawable.icon_protein_chicken_chest
        IconKeys.PROTEIN_CHICKEN_LEG -> Res.drawable.icon_protein_chicken_leg
        IconKeys.PROTEIN_FISH_FILLET -> Res.drawable.icon_protein_fish_fillet
        IconKeys.PROTEIN_FISH_POST -> Res.drawable.icon_protein_fish_post
        IconKeys.PROTEIN_MEAT -> Res.drawable.icon_protein_meat
        IconKeys.PROTEIN_MEAT_CHICKEN_WINGS -> Res.drawable.icon_protein_meat_chicken_wings
        IconKeys.PROTEIN_MINCED_MEAT -> Res.drawable.icon_protein_minced_meat
        IconKeys.PROTEIN_MINCED_PORK -> Res.drawable.icon_protein_minced_pork

        // VEGETAIS E FRUTAS
        IconKeys.VEG_APPLE -> Res.drawable.icon_veg_apple
        IconKeys.VEG_BANANA -> Res.drawable.icon_veg_banana
        IconKeys.VEG_CARROT -> Res.drawable.icon_veg_carrot
        IconKeys.VEG_GRAPE -> Res.drawable.icon_veg_grape
        IconKeys.VEG_LETTUCE -> Res.drawable.icon_veg_lettuce
        IconKeys.VEG_ONION -> Res.drawable.icon_veg_onion
        IconKeys.VEG_ORANGE -> Res.drawable.icon_veg_orange
        IconKeys.VEG_POTATO -> Res.drawable.icon_veg_potato
        IconKeys.VEG_STRAWBERRY -> Res.drawable.icon_veg_strawberry
        IconKeys.VEG_TOMATO -> Res.drawable.icon_veg_tomato

        else -> null
    }

    return resource?.let { painterResource(it) }
}