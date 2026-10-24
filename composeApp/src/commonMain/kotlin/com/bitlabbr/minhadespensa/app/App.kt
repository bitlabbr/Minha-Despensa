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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Kitchen
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.bitlabbr.minhadespensa.uisystem.components.core.card.PrimaryContainerGlassCard
import com.bitlabbr.minhadespensa.uisystem.components.core.snackbar.GlobalNotificationHost
import com.bitlabbr.minhadespensa.uisystem.features.account.HouseholdAccountScreen
import com.bitlabbr.minhadespensa.uisystem.features.catalog.CatalogScreen
import com.bitlabbr.minhadespensa.uisystem.features.details.ProductDetailsScreen
import com.bitlabbr.minhadespensa.uisystem.features.home.HomeScreen
import com.bitlabbr.minhadespensa.uisystem.features.pantry.PantryScreen
import com.bitlabbr.minhadespensa.uisystem.features.shopping.assistant.ShoppingAssistantScreen
import com.bitlabbr.minhadespensa.uisystem.features.shopping.overview.ShoppingListsScreen
import com.bitlabbr.minhadespensa.uisystem.features.shopping.planned.CreatePlannedListScreen
import com.bitlabbr.minhadespensa.uisystem.features.shopping.quicklist.QuickListScreen
import com.bitlabbr.minhadespensa.uisystem.navigation.BottomNavItem
import com.bitlabbr.minhadespensa.uisystem.navigation.CreatePlannedListRoute
import com.bitlabbr.minhadespensa.uisystem.navigation.HomeScreenRoute
import com.bitlabbr.minhadespensa.uisystem.navigation.HouseholdAccountRoute
import com.bitlabbr.minhadespensa.uisystem.navigation.PantryScreenRoute
import com.bitlabbr.minhadespensa.uisystem.navigation.ProductCatalogRoute
import com.bitlabbr.minhadespensa.uisystem.navigation.ProductDetailsRoute
import com.bitlabbr.minhadespensa.uisystem.navigation.QuickListRoute
import com.bitlabbr.minhadespensa.uisystem.navigation.ShoppingAssistantRoute
import com.bitlabbr.minhadespensa.uisystem.navigation.ShoppingListsRoute
import com.bitlabbr.minhadespensa.uisystem.theme.AppBackground
import com.bitlabbr.minhadespensa.uisystem.theme.MinhaDespensaTheme
import com.bitlabbr.minhadespensa.uisystem.theme.getAppColors
import minhadespensa.uisystem.generated.resources.Res
import minhadespensa.uisystem.generated.resources.nav_catalog
import minhadespensa.uisystem.generated.resources.nav_home
import minhadespensa.uisystem.generated.resources.nav_pantry
import minhadespensa.uisystem.generated.resources.nav_shopping_lists
import org.jetbrains.compose.resources.stringResource

private val bottomNavItems = listOf(
    BottomNavItem(Res.string.nav_home, Icons.Rounded.Home, HomeScreenRoute),
    BottomNavItem(Res.string.nav_pantry, Icons.Rounded.Kitchen, PantryScreenRoute),
    BottomNavItem(Res.string.nav_catalog, Icons.Rounded.Category, ProductCatalogRoute),
    BottomNavItem(Res.string.nav_shopping_lists, Icons.Rounded.ShoppingCart, ShoppingListsRoute),
)

@Composable
fun App() {
    MinhaDespensaTheme {
        AppBackground {
            val navController = rememberNavController()
            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = { AppNavigationBar(navController = navController) }
            ) { innerPadding ->
                AppNavHost(
                    navController = navController,
                    bottomPadding = innerPadding.calculateBottomPadding(),
                )
            }
            GlobalNotificationHost()
        }
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    bottomPadding: Dp,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HomeScreenRoute,
        modifier = modifier.fillMaxSize()
    ) {
        composable<PantryScreenRoute> {
            PantryScreen(
                bottomPadding = bottomPadding,
                onProductClick = { product ->
                    navController.navigate(
                        ProductDetailsRoute(productId = product.id, fromPantry = true)
                    )
                },
            )
        }

        composable<HomeScreenRoute> {
            HomeScreen(
                bottomPadding = bottomPadding,
                onNavigateToAccount = {
                    navController.navigate(HouseholdAccountRoute)
                },
            )
        }

        composable<HouseholdAccountRoute> {
            HouseholdAccountScreen(
                onNavigateBack = { navController.popBackStack() },
                bottomPadding = bottomPadding,
            )
        }

        composable<ProductCatalogRoute> {
            CatalogScreen(
                bottomPadding = bottomPadding,
                onProductClick = { product ->
                    navController.navigate(
                        ProductDetailsRoute(productId = product.id, fromPantry = false)
                    )
                },
            )
        }

        composable<ProductDetailsRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ProductDetailsRoute>()
            ProductDetailsScreen(
                productId = route.productId,
                fromPantry = route.fromPantry,
                onNavigateBack = { navController.popBackStack() },
                bottomPadding = bottomPadding,
            )
        }

        composable<ShoppingListsRoute> {
            ShoppingListsScreen(
                bottomPadding = bottomPadding,
                onNavigateToQuickList = {
                    navController.navigate(QuickListRoute)
                },
                onNavigateToPlannedList = {
                    navController.navigate(CreatePlannedListRoute)
                },
                onNavigateToAssistant = { listId ->
                    navController.navigate(ShoppingAssistantRoute(listId = listId))
                },
                onNavigateToListDetails = { _ -> }
            )
        }

        composable<ShoppingAssistantRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ShoppingAssistantRoute>()
            ShoppingAssistantScreen(
                listId = route.listId,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable<CreatePlannedListRoute> {
            CreatePlannedListScreen(
                onNavigateBack = { navController.popBackStack() },
                onListSaved = { _ ->
                    navController.popBackStack()
                },
            )
        }

        composable<QuickListRoute> {
            QuickListScreen(
                onNavigateBack = { navController.popBackStack() },
                onListSaved = { _ ->
                    navController.popBackStack()
                },
            )
        }
    }
}

@Composable
private fun AppNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val appDimens = MinhaDespensaTheme.dimens
    val appColors = getAppColors()
    val isTopLevelDestination = remember(currentDestination) {
        bottomNavItems.any { item ->
            currentDestination?.hierarchy?.any { it.hasRoute(item.route::class) } == true
        }
    }
    AnimatedVisibility(
        visible = isTopLevelDestination,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    ) {
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
                    containerColor = appColors.primaryContainer,
                    tonalElevation = 0.dp,
                    windowInsets = WindowInsets(0, 0, 0, 0)
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentDestination?.hierarchy?.any {
                            it.hasRoute(item.route::class)
                        } == true

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = stringResource(item.titleRes),
                                    tint = if (isSelected) {
                                        appColors.onSecondaryContainer.copy(alpha = .9f)
                                    } else {
                                        appColors.onSurface
                                    }
                                )
                            },
                            selected = isSelected,
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = appColors.primary.copy(alpha = 0.5f)
                            ),
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo<HomeScreenRoute> {
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
}
