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

package com.bitlabbr.minhadespensa.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bitlabbr.minhadespensa.uisystem.components.core.card.PrimaryContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.snackbar.GlobalNotificationHost
import com.bitlabbr.minhadespensa.uisystem.features.catalog.CatalogScreen
import com.bitlabbr.minhadespensa.uisystem.navigation.*
import com.bitlabbr.minhadespensa.uisystem.screens.HomeScreen
import com.bitlabbr.minhadespensa.uisystem.features.pantry.PantryScreen
import com.bitlabbr.minhadespensa.uisystem.screens.SettingsScreen
import com.bitlabbr.minhadespensa.uisystem.theme.AppBackground
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors

private val bottomNavItems = listOf(
    BottomNavItem("Início", Icons.Default.Home, HomeScreenRoute),
    BottomNavItem("Despensa", Icons.AutoMirrored.Rounded.List, PantryScreenRoute),
    //BottomNavItem("Configurações", Icons.Default.Settings, SettingsRoute),
    BottomNavItem("Catálogo", Icons.Default.ShoppingCart, ProductCatalogRoute),
)

@Composable
fun App() {
    MinhaDespensaTheme {
        AppBackground {
            val navController = rememberNavController()
            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    val appDimens = MinhaDespensaTheme.dimens
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    ) {
                        PrimaryContainerGlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = appDimens.paddingSmall, vertical = appDimens.paddingSmall),
                            shape = RoundedCornerShape(appDimens.cardCorner),
                            borderWidth = 2.dp
                        ) {
                            NavigationBar(
                                containerColor = getAppColors().primaryContainer,
                                tonalElevation = 0.dp,
                                windowInsets = WindowInsets(0, 0, 0, 0)
                            ) {
                                bottomNavItems.forEach { item ->
                                    val isSelected = currentDestination?.hierarchy?.any {
                                        it.hasRoute(item.route::class)
                                    } == true

                                    val appColors = getAppColors()
                                    NavigationBarItem(
                                        icon = {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = item.title,
                                                tint = if (isSelected) appColors.onSecondaryContainer.copy(alpha = .9f) else appColors.onSurface
                                            )
                                        },
                                        selected = isSelected,
                                        colors = NavigationBarItemDefaults.colors(
                                            indicatorColor = appColors.primary.copy(alpha = 0.5f)
                                        ),
                                        onClick = {
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.startDestinationRoute!!) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = HomeScreenRoute,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable<PantryScreenRoute> {
                        PantryScreen(bottomPadding = innerPadding.calculateBottomPadding())
                    }

                    composable<SettingsRoute> {
                        SettingsScreen()
                    }

                    composable<HomeScreenRoute> {
                        HomeScreen(bottomPadding = innerPadding.calculateBottomPadding())
                    }

                    composable<ProductCatalogRoute> {
                        CatalogScreen(
                            bottomPadding = innerPadding.calculateBottomPadding(),
                            onProductClick = { product ->
                                // Ação ao selecionar um produto (ex: navegar para detalhes, edição ou selecionar para despensa)
                            },
                        )
                    }
                }
            }
            GlobalNotificationHost()
        }
    }
}