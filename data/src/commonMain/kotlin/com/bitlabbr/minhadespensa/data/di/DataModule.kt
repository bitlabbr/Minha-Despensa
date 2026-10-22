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

package com.bitlabbr.minhadespensa.data.di

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.bitlabbr.minhadespensa.core.domain.repository.AuthRepository
import com.bitlabbr.minhadespensa.core.domain.repository.BudgetRepository
import com.bitlabbr.minhadespensa.core.domain.repository.CatalogRepository
import com.bitlabbr.minhadespensa.core.domain.repository.HouseholdRepository
import com.bitlabbr.minhadespensa.core.domain.repository.PantryMovementRepository
import com.bitlabbr.minhadespensa.core.domain.repository.PantryRepository
import com.bitlabbr.minhadespensa.core.domain.repository.PriceRepository
import com.bitlabbr.minhadespensa.core.domain.repository.ShoppingListRepository
import com.bitlabbr.minhadespensa.core.domain.repository.SyncRepository
import com.bitlabbr.minhadespensa.core.domain.usecase.AddCatalogItemToShoppingListUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.AddOrUpdateCartItemUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.AddPantryItemUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.CheckEanStatusUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.CreatePlannedShoppingListUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.CreateQuickShoppingListUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.DeleteProductCategoryUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.FinalizeShoppingSessionUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.GetMonthlyFinancialSummaryUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.GetMostConsumedProductsUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.GetPantryTotalValueUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.GetYearlyFinancialSummaryUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.ImportInvoiceToPantryUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.JoinHouseholdByInviteCodeUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.LoginUserUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.LogoutUserUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.ManageHouseholdUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.ParseInvoiceQrCodeUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.RegisterUserUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.RemoveCartItemUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.ReplaceCartItemUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.SaveCatalogProductUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.SaveProductCategoryUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.SetMonthlyBudgetUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.StartShoppingSessionUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.SwitchActiveHouseholdUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.SyncHouseholdDataUseCase
import com.bitlabbr.minhadespensa.core.domain.usecase.UpdatePantryItemUseCase
import com.bitlabbr.minhadespensa.core.domain.util.DiQualifiers
import com.bitlabbr.minhadespensa.core.domain.util.SyncClock
import com.bitlabbr.minhadespensa.core.domain.util.SyncCoordinator
import com.bitlabbr.minhadespensa.data.local.AppDatabase
import com.bitlabbr.minhadespensa.data.remote.AppKeyAuthTokenProvider
import com.bitlabbr.minhadespensa.data.remote.AuthRemoteDataSource
import com.bitlabbr.minhadespensa.data.remote.AuthTokenProvider
import com.bitlabbr.minhadespensa.data.remote.HouseholdRemoteDataSource
import com.bitlabbr.minhadespensa.data.remote.KtorAuthRemoteDataSource
import com.bitlabbr.minhadespensa.data.remote.KtorHouseholdRemoteDataSource
import com.bitlabbr.minhadespensa.data.remote.KtorSyncRemoteDataSource
import com.bitlabbr.minhadespensa.data.remote.SyncRemoteDataSource
import com.bitlabbr.minhadespensa.data.remote.SyncServerConfig
import com.bitlabbr.minhadespensa.data.repository.RoomAuthRepository
import com.bitlabbr.minhadespensa.data.repository.RoomBudgetRepository
import com.bitlabbr.minhadespensa.data.repository.RoomCatalogRepository
import com.bitlabbr.minhadespensa.data.repository.RoomHouseholdRepository
import com.bitlabbr.minhadespensa.data.repository.RoomPantryMovementRepository
import com.bitlabbr.minhadespensa.data.repository.RoomPantryRepository
import com.bitlabbr.minhadespensa.data.repository.RoomPriceRepository
import com.bitlabbr.minhadespensa.data.repository.RoomShoppingListRepository
import com.bitlabbr.minhadespensa.data.repository.RoomSyncRepository
import com.bitlabbr.minhadespensa.core.domain.repository.InvoiceRepository
import com.bitlabbr.minhadespensa.data.remote.InvoiceRemoteDataSource
import com.bitlabbr.minhadespensa.data.remote.InvoiceServerConfig
import com.bitlabbr.minhadespensa.data.remote.KtorInvoiceRemoteDataSource
import com.bitlabbr.minhadespensa.data.remote.createPlatformHttpClientEngine
import com.bitlabbr.minhadespensa.data.repository.RoomInvoiceRepository
import com.bitlabbr.minhadespensa.data.local.migration.ALL_MIGRATIONS
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

val dataModule = module {
    single<AppDatabase> {
        val builder = get<RoomDatabase.Builder<AppDatabase>>()
        builder
            .addMigrations(*ALL_MIGRATIONS)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }

    single { get<AppDatabase>().catalogDao() }
    single { get<AppDatabase>().pantryDao() }
    single { get<AppDatabase>().priceDao() }
    single { get<AppDatabase>().shoppingListDao() }
    single { get<AppDatabase>().budgetDao() }
    single { get<AppDatabase>().movementDao() }
    single { get<AppDatabase>().categoryDao() }
    single { get<AppDatabase>().pendingSyncDao() }
    single { get<AppDatabase>().appKeyValueDao() }

    single<CatalogRepository> {
        RoomCatalogRepository(get(), get(named(DiQualifiers.DATA_LOGGER)))
    }

    single<PantryRepository> {
        RoomPantryRepository(get(), get(named(DiQualifiers.DATA_LOGGER)))
    }

    single<PriceRepository> {
        RoomPriceRepository(get(), get(named(DiQualifiers.DATA_LOGGER)))
    }

    single<ShoppingListRepository> {
        RoomShoppingListRepository(get(), get(named(DiQualifiers.DATA_LOGGER)))
    }

    single<BudgetRepository> {
        RoomBudgetRepository(get(), get(named(DiQualifiers.DATA_LOGGER)))
    }

    single<PantryMovementRepository> {
        RoomPantryMovementRepository(get(), get(named(DiQualifiers.DATA_LOGGER)))
    }

    single { InvoiceServerConfig() }
    single { SyncServerConfig() }
    single { SyncClock() }

    single<AuthTokenProvider> {
        AppKeyAuthTokenProvider(appKeyValueDao = get())
    }

    single<AuthRemoteDataSource> {
        KtorAuthRemoteDataSource(
            httpClient = get(),
            config = get(),
            logger = get(named(DiQualifiers.DATA_LOGGER)),
        )
    }

    single<HouseholdRemoteDataSource> {
        KtorHouseholdRemoteDataSource(
            httpClient = get(),
            config = get(),
            authTokenProvider = get(),
            logger = get(named(DiQualifiers.DATA_LOGGER)),
        )
    }

    single<SyncRemoteDataSource> {
        KtorSyncRemoteDataSource(
            httpClient = get(),
            config = get(),
            authTokenProvider = get(),
            logger = get(named(DiQualifiers.DATA_LOGGER)),
        )
    }

    single<AuthRepository> {
        RoomAuthRepository(
            authRemoteDataSource = get(),
            appKeyValueDao = get(),
            logger = get(named(DiQualifiers.DATA_LOGGER)),
        )
    }

    single<HouseholdRepository> {
        RoomHouseholdRepository(
            householdRemoteDataSource = get(),
            appKeyValueDao = get(),
            authRepository = get(),
            logger = get(named(DiQualifiers.DATA_LOGGER)),
        )
    }

    single<SyncRepository> {
        RoomSyncRepository(
            database = get(),
            syncRemoteDataSource = get(),
            syncClock = get(),
            logger = get(named(DiQualifiers.DATA_LOGGER)),
        )
    }

    single {
        HttpClient(createPlatformHttpClientEngine()) {
            followRedirects = true
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        prettyPrint = false
                    }
                )
            }
        }
    }

    single<InvoiceRemoteDataSource> {
        KtorInvoiceRemoteDataSource(
            httpClient = get(),
            serverConfig = get(),
        )
    }

    single<InvoiceRepository> {
        RoomInvoiceRepository(
            remoteDataSource = get(),
            db = get(),
            logger = get(named(DiQualifiers.DATA_LOGGER)),
        )
    }

    factory { CheckEanStatusUseCase(catalogRepository = get()) }
    factory { SaveCatalogProductUseCase(catalogRepository = get()) }
    factory { SaveProductCategoryUseCase(catalogRepository = get()) }
    factory { DeleteProductCategoryUseCase(catalogRepository = get()) }
    factory { AddPantryItemUseCase(pantryRepository = get()) }
    factory { UpdatePantryItemUseCase(pantryRepository = get()) }
    factory { CreateQuickShoppingListUseCase(shoppingListRepository = get()) }
    factory { StartShoppingSessionUseCase(shoppingListRepository = get()) }
    factory { AddOrUpdateCartItemUseCase(shoppingListRepository = get()) }
    factory { ReplaceCartItemUseCase(shoppingListRepository = get()) }
    factory { RemoveCartItemUseCase(shoppingListRepository = get()) }
    factory { FinalizeShoppingSessionUseCase(shoppingListRepository = get()) }
    factory { CreatePlannedShoppingListUseCase(shoppingListRepository = get()) }
    factory { AddCatalogItemToShoppingListUseCase(shoppingListRepository = get()) }
    factory { SetMonthlyBudgetUseCase(budgetRepository = get()) }
    factory { GetMonthlyFinancialSummaryUseCase(budgetRepository = get()) }
    factory { GetMostConsumedProductsUseCase(movementRepository = get()) }
    factory { GetPantryTotalValueUseCase(pantryRepository = get()) }
    factory { GetYearlyFinancialSummaryUseCase(budgetRepository = get()) }
    factory { ParseInvoiceQrCodeUseCase(invoiceRepository = get()) }
    factory { ImportInvoiceToPantryUseCase(invoiceRepository = get()) }
    factory { RegisterUserUseCase(authRepository = get(), syncRepository = get()) }
    factory { LoginUserUseCase(authRepository = get(), syncRepository = get()) }
    factory { LogoutUserUseCase(authRepository = get()) }
    factory { JoinHouseholdByInviteCodeUseCase(householdRepository = get(), syncRepository = get()) }
    factory { SwitchActiveHouseholdUseCase(householdRepository = get(), syncRepository = get()) }
    factory { ManageHouseholdUseCase(householdRepository = get(), syncRepository = get()) }
    factory { SyncHouseholdDataUseCase(householdRepository = get(), syncRepository = get()) }
    single {
        SyncCoordinator(
            syncHouseholdDataUseCase = get(),
            syncRepository = get(),
            logger = get(named(DiQualifiers.DATA_LOGGER)),
        )
    }
}
