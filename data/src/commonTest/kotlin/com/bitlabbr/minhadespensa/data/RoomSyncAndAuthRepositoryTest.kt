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

package com.bitlabbr.minhadespensa.data

import com.bitlabbr.minhadespensa.core.domain.model.WorkspaceSwitchCheckResult
import com.bitlabbr.minhadespensa.core.domain.util.SyncClock
import com.bitlabbr.minhadespensa.data.local.AppDatabase
import com.bitlabbr.minhadespensa.data.local.BaseTest
import com.bitlabbr.minhadespensa.data.local.createInMemoryDatabase
import com.bitlabbr.minhadespensa.data.local.entity.CatalogProductEntity
import com.bitlabbr.minhadespensa.data.local.entity.MonthlyBudgetEntity
import com.bitlabbr.minhadespensa.data.local.entity.PantryItemEntity
import com.bitlabbr.minhadespensa.data.local.entity.PriceEntryEntity
import com.bitlabbr.minhadespensa.data.local.getTestDatabaseBuilder
import com.bitlabbr.minhadespensa.data.remote.AuthRemoteDataSource
import com.bitlabbr.minhadespensa.data.remote.HouseholdRemoteDataSource
import com.bitlabbr.minhadespensa.data.remote.SyncRemoteDataSource
import com.bitlabbr.minhadespensa.data.remote.dto.AuthResponseDto
import com.bitlabbr.minhadespensa.data.remote.dto.CatalogProductSyncDto
import com.bitlabbr.minhadespensa.data.remote.dto.HouseholdDto
import com.bitlabbr.minhadespensa.data.remote.dto.PantryItemSyncDto
import com.bitlabbr.minhadespensa.data.remote.dto.SyncEntitiesDto
import com.bitlabbr.minhadespensa.data.remote.dto.SyncPullResponseDto
import com.bitlabbr.minhadespensa.data.remote.dto.SyncPushRequestDto
import com.bitlabbr.minhadespensa.data.remote.dto.SyncPushResponseDto
import com.bitlabbr.minhadespensa.data.remote.dto.TokenRefreshResponseDto
import com.bitlabbr.minhadespensa.data.remote.dto.UserDto
import com.bitlabbr.minhadespensa.data.repository.RoomAuthRepository
import com.bitlabbr.minhadespensa.data.repository.RoomHouseholdRepository
import com.bitlabbr.minhadespensa.data.repository.RoomSyncRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RoomSyncAndAuthRepositoryTest : BaseTest() {

    private lateinit var db: AppDatabase
    private lateinit var authRepository: RoomAuthRepository
    private lateinit var householdRepository: RoomHouseholdRepository
    private lateinit var syncRepository: RoomSyncRepository
    private lateinit var syncClock: SyncClock

    private val fakeAuthRemote = object : AuthRemoteDataSource {
        override suspend fun register(name: String, email: String, password: String): Result<AuthResponseDto> =
            Result.success(
                AuthResponseDto(
                    accessToken = "access_123",
                    refreshToken = "refresh_123",
                    expiresIn = 3600L,
                    user = UserDto("u1", name, email),
                    household = HouseholdDto("hh1", "Minha Casa", "ABCDEFGH", "OWNER"),
                ),
            )

        override suspend fun login(email: String, password: String): Result<AuthResponseDto> =
            Result.success(
                AuthResponseDto(
                    accessToken = "access_login",
                    refreshToken = "refresh_login",
                    expiresIn = 3600L,
                    user = UserDto("u1", "User Login", email),
                    household = HouseholdDto("hh1", "Minha Casa", "ABCDEFGH", "OWNER"),
                ),
            )

        override suspend fun refreshToken(refreshToken: String): Result<TokenRefreshResponseDto> =
            Result.success(TokenRefreshResponseDto("new_acc", "new_ref", "Bearer", 3600L))
    }

    private val fakeHouseholdRemote = object : HouseholdRemoteDataSource {
        override suspend fun listHouseholds(): Result<List<HouseholdDto>> =
            Result.success(listOf(HouseholdDto("hh1", "Minha Casa", "ABCDEFGH", "OWNER")))

        override suspend fun getHousehold(id: String): Result<HouseholdDto> =
            Result.success(HouseholdDto(id, "Casa $id", "CODE1234", "MEMBER"))

        override suspend fun createHousehold(name: String): Result<HouseholdDto> =
            Result.success(HouseholdDto("hh_created", name, "NEWCODE1", "OWNER"))

        override suspend fun updateHousehold(id: String, name: String): Result<HouseholdDto> =
            Result.success(HouseholdDto(id, name, "NEWCODE1", "OWNER"))

        override suspend fun joinHousehold(inviteCode: String): Result<HouseholdDto> =
            Result.success(HouseholdDto("hh_joined", "Casa Convidada", inviteCode, "MEMBER"))

        override suspend fun leaveHousehold(id: String): Result<Unit> = Result.success(Unit)

        override suspend fun regenerateInviteCode(id: String): Result<String> = Result.success("NEWINV88")

        override suspend fun removeMember(householdId: String, memberUserId: String): Result<Unit> = Result.success(Unit)
    }

    private var lastPushedPayload: SyncPushRequestDto? = null
    private var pullResponseToReturn = SyncPullResponseDto(
        serverTimestamp = 2000L,
        currentVersion = 5L,
        entities = SyncEntitiesDto(),
    )

    private val fakeSyncRemote = object : SyncRemoteDataSource {
        override suspend fun push(householdId: String, payload: SyncPushRequestDto): Result<SyncPushResponseDto> {
            lastPushedPayload = payload
            return Result.success(
                SyncPushResponseDto(
                    serverTimestamp = 1500L,
                    serverVersion = 2L,
                    processedCount = payload.mutations.size,
                    productIdMappings = mapOf("old-local-uuid" to "canonical-uuid-5"),
                ),
            )
        }

        override suspend fun pull(
            householdId: String,
            sinceVersion: Long,
            limit: Int,
        ): Result<SyncPullResponseDto> = Result.success(pullResponseToReturn)
    }

    @BeforeTest
    fun setUp() {
        db = createInMemoryDatabase(getTestDatabaseBuilder())
        syncClock = SyncClock()
        authRepository = RoomAuthRepository(fakeAuthRemote, db.appKeyValueDao())
        householdRepository = RoomHouseholdRepository(fakeHouseholdRemote, db.appKeyValueDao(), authRepository)
        syncRepository = RoomSyncRepository(db, fakeSyncRemote, syncClock)
    }

    @AfterTest
    fun tearDown() {
        db.close()
    }

    @Test
    fun authRepository_register_savesSessionAndActiveHousehold() = runTest {
        val session = authRepository.register("João", "joao@teste.com", "senha123").getOrThrow()
        assertEquals("u1", session.user.id)
        assertEquals("hh1", session.activeHousehold.id)

        val storedSession = authRepository.getCurrentSession()
        assertNotNull(storedSession)
        assertEquals("access_123", storedSession.accessToken)

        val activeHousehold = householdRepository.getActiveHousehold()
        assertNotNull(activeHousehold)
        assertEquals("hh1", activeHousehold.id)

        authRepository.logout()
        assertNull(authRepository.getCurrentSession())
    }

    @Test
    fun syncRepository_enqueueAllLocalEntities_enqueuesProductsAndPantryItems() = runTest {
        db.catalogDao().insert(
            CatalogProductEntity(
                id = "prod-local-1",
                ean = "7891234567890",
                name = "Azeite",
                category = "Temperos",
                brand = "Gallo",
                measureUnit = "MILLILITER",
                netWeight = 500.0,
                thumbnailUrl = null,
                updatedAt = 1000L,
                isDeleted = false,
                manuallyAdded = true,
                notes = null,
            ),
        )
        db.pantryDao().insertPantryItem(
            PantryItemEntity(
                id = "pantry-local-1",
                productId = "prod-local-1",
                quantity = 2.0,
                expirationDate = null,
                batchNumber = null,
                unitPriceInCents = 3500L,
                updatedAt = 1000L,
                isDeleted = false,
            ),
        )

        val count = syncRepository.enqueueAllLocalEntitiesForInitialSync().getOrThrow()
        assertTrue(count >= 2)

        val pending = syncRepository.getPendingChangesCount()
        assertTrue(pending >= 2)
    }

    @Test
    fun syncRepository_sync_pushesOutboxAndRemapsProductIdsProtectingCascade() = runTest {
        // Setup authenticated session
        authRepository.register("João", "joao@teste.com", "senha123").getOrThrow()

        // Insert local product with old UUID
        db.catalogDao().insert(
            CatalogProductEntity(
                id = "old-local-uuid",
                ean = null,
                name = "Arroz Sem EAN",
                category = "Grãos",
                brand = null,
                measureUnit = "KILOGRAM",
                netWeight = 1.0,
                thumbnailUrl = null,
                updatedAt = 1000L,
                isDeleted = false,
                manuallyAdded = true,
                notes = null,
            ),
        )
        db.pantryDao().insertPantryItem(
            PantryItemEntity(
                id = "pantry-item-1",
                productId = "old-local-uuid",
                quantity = 5.0,
                expirationDate = null,
                batchNumber = null,
                unitPriceInCents = 600L,
                updatedAt = 1000L,
                isDeleted = false,
            ),
        )
        db.priceDao().insertPriceEntry(
            PriceEntryEntity(
                id = "price-1",
                productId = "old-local-uuid",
                priceInCents = 600L,
                storeName = "Mercado",
                updatedAt = 1000L,
                isDeleted = false,
            ),
        )

        // Enqueue outbox mutation
        syncRepository.enqueueAllLocalEntitiesForInitialSync().getOrThrow()

        // Configure pull response
        pullResponseToReturn = SyncPullResponseDto(
            serverTimestamp = 3000L,
            currentVersion = 10L,
            entities = SyncEntitiesDto(
                products = listOf(
                    CatalogProductSyncDto(
                        id = "canonical-uuid-5",
                        name = "Arroz Sem EAN Canônico",
                        category = "Grãos",
                        measureUnit = "KILOGRAM",
                        netWeight = 1.0,
                        updatedAt = 2000L,
                    ),
                ),
                pantryItems = listOf(
                    PantryItemSyncDto(
                        id = "pantry-remote-2",
                        productId = "canonical-uuid-5",
                        quantity = 10.0,
                        updatedAt = 2000L,
                    ),
                ),
            ),
        )

        // Execute sync
        val syncResult = syncRepository.sync()
        assertTrue(syncResult.isSuccess)

        // Outbox should be drained
        assertEquals(0, syncRepository.getPendingChangesCount())

        // Child foreign keys remapped to canonical ID without cascade deletion
        val pantryItem = db.pantryDao().findPantryItemById("pantry-item-1")
        assertNotNull(pantryItem)
        assertEquals("canonical-uuid-5", pantryItem.productId)

        val priceEntry = db.priceDao().findEntryById("price-1")
        assertNotNull(priceEntry)
        assertEquals("canonical-uuid-5", priceEntry.productId)

        // Pulled product and pantry item exist
        val pulledProduct = db.catalogDao().findById("canonical-uuid-5").first()
        assertNotNull(pulledProduct)
        assertEquals("Arroz Sem EAN Canônico", pulledProduct.name)

        val pulledPantry = db.pantryDao().findPantryItemById("pantry-remote-2")
        assertNotNull(pulledPantry)
        assertEquals(10.0, pulledPantry.quantity)
    }

    @Test
    fun syncRepository_flushOutboxBeforeSwitch_blocksWhenPendingMutationsExist() = runTest {
        authRepository.register("João", "joao@teste.com", "senha123").getOrThrow()

        db.catalogDao().insert(
            CatalogProductEntity(
                id = "p1",
                ean = null,
                name = "P1",
                category = "Cat",
                brand = null,
                measureUnit = "UNIT",
                netWeight = 1.0,
                thumbnailUrl = null,
                updatedAt = 1000L,
                isDeleted = false,
                manuallyAdded = true,
                notes = null,
            ),
        )
        syncRepository.enqueueAllLocalEntitiesForInitialSync().getOrThrow()

        val check = syncRepository.flushOutboxBeforeSwitch(forceDiscard = false)
        assertIs<WorkspaceSwitchCheckResult.HasUnsyncedChanges>(check)

        val ready = syncRepository.flushOutboxBeforeSwitch(forceDiscard = true)
        assertIs<WorkspaceSwitchCheckResult.Ready>(ready)
        assertEquals(0, syncRepository.getPendingChangesCount())
    }

    @Test
    fun syncRepository_clearLocalDataForHouseholdSwitch_wipesTenantTables() = runTest {
        db.budgetDao().insertOrUpdate(
            MonthlyBudgetEntity(
                year = 2026,
                month = 10,
                budgetInCents = 150000L,
                updatedAt = 1000L,
                isDeleted = false,
            ),
        )

        syncRepository.clearLocalDataForHouseholdSwitch()

        val budget = db.budgetDao().findBudget(2026, 10)
        assertNull(budget)
    }
}
