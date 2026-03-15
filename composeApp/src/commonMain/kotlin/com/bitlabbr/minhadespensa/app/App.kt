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

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bitlabbr.minhadespensa.uisystem.components.BottomNavItem
import com.bitlabbr.minhadespensa.uisystem.components.CustomText
import com.bitlabbr.minhadespensa.uisystem.components.ProductListRoute
import com.bitlabbr.minhadespensa.uisystem.components.SettingsRoute
import com.bitlabbr.minhadespensa.uisystem.features.list.ProductListScreen
import com.bitlabbr.minhadespensa.uisystem.features.list.SettingsScreen
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import androidx.compose.foundation.layout.consumeWindowInsets

private val bottomNavItems = listOf(
    BottomNavItem("Despensa", Icons.Default.Home, ProductListRoute),
    BottomNavItem("Configurações", Icons.Default.Settings, SettingsRoute)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    MinhaDespensaTheme {
        val navController = rememberNavController()
        Scaffold(
            bottomBar = {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentDestination?.hierarchy?.any {
                            it.hasRoute(item.route::class)
                        } == true

                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { CustomText(text = item.title) },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationRoute!!) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = ProductListRoute,
                modifier = Modifier.padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
            ) {
                composable<ProductListRoute> {
                    ProductListScreen()
                }

                composable<SettingsRoute> {
                    SettingsScreen()
                }
            }
        }
    }
}