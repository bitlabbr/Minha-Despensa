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
import androidx.compose.material.icons.filled.Settings
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
import com.bitlabbr.minhadespensa.uisystem.components.*
import com.bitlabbr.minhadespensa.uisystem.features.home.HomeScreen
import com.bitlabbr.minhadespensa.uisystem.features.list.ProductListScreen
import com.bitlabbr.minhadespensa.uisystem.features.list.SettingsScreen
import com.bitlabbr.minhadespensa.uisystem.theme.AppBackground
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme

private val bottomNavItems = listOf(
    BottomNavItem("Início", Icons.Default.Home, HomeScreenRoute),
    BottomNavItem("Despensa", Icons.AutoMirrored.Rounded.List, ProductListRoute),
    BottomNavItem("Configurações", Icons.Default.Settings, SettingsRoute)
)

@OptIn(ExperimentalMaterial3Api::class)
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
                            borderWidth = 1.dp
                        ) {
                            NavigationBar(
                                containerColor = Color.Transparent,
                                tonalElevation = 0.dp,
                                windowInsets = WindowInsets(0, 0, 0, 0)
                            ) {
                                bottomNavItems.forEach { item ->
                                    val isSelected = currentDestination?.hierarchy?.any {
                                        it.hasRoute(item.route::class)
                                    } == true

                                    val appColors = MinhaDespensaTheme.color
                                    NavigationBarItem(
                                        icon = {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = item.title,
                                                tint = if (isSelected) appColors.primary else appColors.onSurfaceVariant
                                            )
                                        },
                                        selected = isSelected,
                                        colors = NavigationBarItemDefaults.colors(
                                            indicatorColor = appColors.primary.copy(alpha = 0.2f)
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
                    composable<ProductListRoute> {
                        ProductListScreen()
                    }

                    composable<SettingsRoute> {
                        SettingsScreen()
                    }

                    composable<HomeScreenRoute> {
                        HomeScreen(bottomPadding = innerPadding.calculateBottomPadding())
                    }
                }
            }
        }
    }
}