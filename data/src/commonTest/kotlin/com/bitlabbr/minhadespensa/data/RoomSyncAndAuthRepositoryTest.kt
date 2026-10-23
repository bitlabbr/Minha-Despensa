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

import com.bitlabbr.minhadespensa.core.domain.model.CatalogProduct
import com.bitlabbr.minhadespensa.core.domain.model.MeasureUnit
import com.bitlabbr.minhadespensa.core.domain.model.PantryItem
import com.bitlabbr.minhadespensa.core.domain.model.WorkspaceSwitchCheckResult
import com.bitlabbr.minhadespensa.core.domain.util.AppLogger
import com.bitlabbr.minhadespensa.core.domain.util.SyncClock
import com.bitlabbr.minhadespensa.core.domain.util.getCurrentTime
import com.bitlabbr.minhadespensa.data.local.AppDatabase
import com.bitlabbr.minhadespensa.data.local.BaseTest
import com.bitlabbr.minhadespensa.data.local.createInMemoryDatabase
import com.bitlabbr.minhadespensa.data.local.entity.CatalogProductEntity
import com.bitlabbr.minhadespensa.data.local.entity.MonthlyBudgetEntity
import com.bitlabbr.minhadespensa.data.local.entity.PantryItemEntity
import com.bitlabbr.minhadespensa.data.local.entity.PriceEntryEntity
import com.bitlabbr.minhadespensa.data.local.entity.ProductMediaEntity
import com.bitlabbr.minhadespensa.data.local.getTestDatabaseBuilder
import com.bitlabbr.minhadespensa.data.remote.AuthRemoteDataSource
import com.bitlabbr.minhadespensa.data.remote.HouseholdRemoteDataSource
import com.bitlabbr.minhadespensa.data.remote.SyncRemoteDataSource
import com.bitlabbr.minhadespensa.data.remote.dto.AuthResponseDto
import com.bitlabbr.minhadespensa.data.remote.dto.CatalogProductSyncDto
import com.bitlabbr.minhadespensa.data.remote.dto.HouseholdDto
import com.bitlabbr.minhadespensa.data.remote.dto.PantryItemSyncDto
import com.bitlabbr.minhadespensa.data.remote.dto.PriceEntrySyncDto
import com.bitlabbr.minhadespensa.data.remote.dto.ShoppingItemSyncDto
import com.bitlabbr.minhadespensa.data.remote.dto.ShoppingListSyncDto
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
import kotlin.test.assertFalse
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
    private var pushResponseToReturn: SyncPushResponseDto? = null
    private var pullResponseToReturn = SyncPullResponseDto(
        serverTimestamp = 2000L,
        currentVersion = 5L,
        entities = SyncEntitiesDto(),
    )

    private val fakeSyncRemote = object : SyncRemoteDataSource {
        override suspend fun push(householdId: String, payload: SyncPushRequestDto): Result<SyncPushResponseDto> {
            lastPushedPayload = payload
            return Result.success(
                pushResponseToReturn ?: SyncPushResponseDto(
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

        val remoteMediaStorage = mutableMapOf<String, ByteArray>()

        override suspend fun uploadProductMedia(
            householdId: String,
            productId: String,
            bytes: ByteArray,
        ): Result<String> {
            remoteMediaStorage[productId] = bytes
            return Result.success("hash-$productId")
        }

        override suspend fun downloadProductMedia(
            householdId: String,
            productId: String,
        ): Result<ByteArray?> {
            return Result.success(remoteMediaStorage[productId])
        }
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
        db.productMediaDao().insertOrUpdate(
            ProductMediaEntity(
                productId = "old-local-uuid",
                blob = byteArrayOf(1, 2, 3),
                updatedAt = 1000L,
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

        // Product media successfully remapped to canonical ID without UNIQUE constraint error
        val remappedMedia = db.productMediaDao().getByProductId("canonical-uuid-5")
        assertNotNull(remappedMedia)
        assertTrue(byteArrayOf(1, 2, 3).contentEquals(remappedMedia.blob))
        assertNull(db.productMediaDao().getByProductId("old-local-uuid"))
    }

    @Test
    fun syncRepository_sync_reconcilesProductIdMappings_whenNewMediaAlreadyExists() = runTest {
        authRepository.register("Maria", "maria@teste.com", "senha123").getOrThrow()

        // Local product 1 (old UUID)
        db.catalogDao().insert(
            CatalogProductEntity(
                id = "temp-prod-1",
                ean = null,
                name = "Feijao",
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
        // Canonical product already has media
        db.catalogDao().insert(
            CatalogProductEntity(
                id = "canonical-prod-1",
                ean = "7891234567890",
                name = "Feijao Canônico",
                category = "Grãos",
                brand = null,
                measureUnit = "KILOGRAM",
                netWeight = 1.0,
                thumbnailUrl = null,
                updatedAt = 2000L,
                isDeleted = false,
                manuallyAdded = false,
                notes = null,
            ),
        )
        db.productMediaDao().insertOrUpdate(
            ProductMediaEntity(
                productId = "temp-prod-1",
                blob = byteArrayOf(9, 9, 9),
                updatedAt = 1000L,
            ),
        )
        db.productMediaDao().insertOrUpdate(
            ProductMediaEntity(
                productId = "canonical-prod-1",
                blob = byteArrayOf(8, 8, 8),
                updatedAt = 2000L,
            ),
        )

        syncRepository.enqueueAllLocalEntitiesForInitialSync().getOrThrow()

        pushResponseToReturn = SyncPushResponseDto(
            serverTimestamp = 2500L,
            serverVersion = 5L,
            processedCount = 1,
            productIdMappings = mapOf("temp-prod-1" to "canonical-prod-1"),
        )
        pullResponseToReturn = SyncPullResponseDto(
            serverTimestamp = 2500L,
            currentVersion = 5L,
            entities = SyncEntitiesDto(),
        )

        val result = syncRepository.sync()
        assertTrue(result.isSuccess)

        // Newer media on canonical-prod-1 should be retained, temp-prod-1 media deleted
        val canonicalMedia = db.productMediaDao().getByProductId("canonical-prod-1")
        assertNotNull(canonicalMedia)
        assertTrue(byteArrayOf(8, 8, 8).contentEquals(canonicalMedia.blob))
        assertNull(db.productMediaDao().getByProductId("temp-prod-1"))
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

    @Test
    fun catalogAndPantryRepositories_mutationsAutomaticallyEnqueueToOutboxAndPushOnSync() = runTest {
        val fakeLogger = object : AppLogger {
            override fun d(tag: String, message: String) {}
            override fun i(tag: String, message: String) {}
            override fun w(tag: String, message: String, error: Throwable?) {}
            override fun e(tag: String, message: String, error: Throwable?) {}
        }
        val outboxEnqueuer = com.bitlabbr.minhadespensa.data.sync.OutboxEnqueuer(db.pendingSyncDao(), syncClock)
        val catalogRepo = com.bitlabbr.minhadespensa.data.repository.RoomCatalogRepository(db, fakeLogger, outboxEnqueuer)
        val pantryRepo = com.bitlabbr.minhadespensa.data.repository.RoomPantryRepository(db, fakeLogger, outboxEnqueuer)

        authRepository.register("Test", "test@example.com", "pass123").getOrThrow()
        db.pendingSyncDao().clearAll()

        val prodId = "00000000-0000-0000-0000-000000000101"
        val pantryId = "00000000-0000-0000-0000-000000000201"
        val now = getCurrentTime()

        // Insert product through domain repository
        catalogRepo.insertProduct(
            CatalogProduct(
                id = prodId,
                ean = "7891234567890",
                name = "Café Especial",
                category = "Mercearia",
                measureUnit = MeasureUnit.UNIT,
                updatedAt = now,
            ),
            imageBytes = null,
        )

        // Insert pantry item through domain repository
        pantryRepo.insertPantryItem(
            PantryItem(
                id = pantryId,
                productId = prodId,
                quantity = 2.0,
                expirationDate = null,
                updatedAt = now,
                unitPriceInCents = 1500L,
            ),
        )

        // Verify outbox automatically populated
        val pending = db.pendingSyncDao().getAllPending(10)
        assertTrue(pending.any { it.entityType == "PRODUCT" && it.entityId == prodId })
        assertTrue(pending.any { it.entityType == "PANTRY_ITEM" && it.entityId == pantryId })

        // Trigger sync
        syncRepository.sync().getOrThrow()

        // Verify last pushed payload received the product and pantry item!
        assertNotNull(lastPushedPayload)
        assertEquals(1, lastPushedPayload!!.products.size)
        assertEquals("Café Especial", lastPushedPayload!!.products[0].name)
        assertEquals(1, lastPushedPayload!!.pantryItems.size)
        assertEquals(pantryId, lastPushedPayload!!.pantryItems[0].id)
    }

    @Test
    fun pulledShoppingListsAndPriceEntriesAndMedia_areProperlyInsertedAndDownloadedWhenNotPresentLocally() = runTest {
        authRepository.register("Test", "test@example.com", "pass123").getOrThrow()
        db.pendingSyncDao().clearAll()

        val listId = "00000000-0000-0000-0000-000000000301"
        val itemId = "00000000-0000-0000-0000-000000000302"
        val priceId = "00000000-0000-0000-0000-000000000303"
        val prodId = "00000000-0000-0000-0000-000000000304"
        val now = getCurrentTime()

        // Prepare remote media in fake
        val testImageBytes = byteArrayOf(1, 2, 3, 4, 5)
        fakeSyncRemote.remoteMediaStorage[prodId] = testImageBytes

        pullResponseToReturn = SyncPullResponseDto(
            serverTimestamp = now,
            currentVersion = 10L,
            products = listOf(
                CatalogProductSyncDto(
                    id = prodId,
                    name = "Bolo de Cenoura",
                    category = "Padaria",
                    measureUnit = "UNIT",
                    netWeight = 1.0,
                    updatedAt = now,
                    imageHash = "hash-$prodId",
                ),
            ),
            shoppingLists = listOf(
                ShoppingListSyncDto(
                    id = listId,
                    name = "Lista de Sábado",
                    listType = "PLANNED",
                    listStatus = "DRAFT",
                    budgetInCents = 10000L,
                    updatedAt = now,
                ),
            ),
            shoppingItems = listOf(
                ShoppingItemSyncDto(
                    id = itemId,
                    listId = listId,
                    productId = prodId,
                    quantity = 3.0,
                    priceAtTime = 1200L,
                    updatedAt = now,
                ),
            ),
            priceEntries = listOf(
                PriceEntrySyncDto(
                    id = priceId,
                    productId = prodId,
                    priceInCents = 1200L,
                    storeName = "Padaria Central",
                    updatedAt = now,
                ),
            ),
        )

        // Trigger sync to pull
        syncRepository.sync().getOrThrow()

        // Verify shopping list, shopping item, and price entry are inserted in local DB!
        val localList = db.shoppingListDao().findSnapshotById(listId)
        assertNotNull(localList)
        assertEquals("Lista de Sábado", localList.name)
        assertEquals(10000L, localList.budgetInCents)

        val localItem = db.shoppingItemDao().findSnapshotById(itemId)
        assertNotNull(localItem)
        assertEquals(3.0, localItem.quantity)
        assertEquals(listId, localItem.listId)

        val localPrice = db.priceDao().findEntryById(priceId)
        assertNotNull(localPrice)
        assertEquals(1200L, localPrice.priceInCents)
        assertEquals("Padaria Central", localPrice.storeName)

        // Verify media was downloaded and inserted into local product_media!
        val localMedia = db.productMediaDao().getByProductId(prodId)
        assertNotNull(localMedia)
        assertTrue(testImageBytes.contentEquals(localMedia.blob))
    }

    @Test
    fun syncRepository_syncProductMedia_uploadsLocalMediaAndPersistsSyncedHash() = runTest {
        authRepository.register("Test Media", "media@test.com", "pass123").getOrThrow()
        db.pendingSyncDao().clearAll()

        val prodId = "00000000-0000-0000-0000-000000000401"
        val now = getCurrentTime()
        val imageBytes = byteArrayOf(9, 8, 7, 6)

        // Insert product and local media
        db.catalogDao().insert(
            CatalogProductEntity(
                id = prodId,
                ean = "7890000000401",
                name = "Café Gourmet",
                category = "Mercearia",
                brand = "Marca X",
                measureUnit = "UNIT",
                netWeight = 1.0,
                thumbnailUrl = null,
                updatedAt = now,
                isDeleted = false,
                manuallyAdded = true,
                notes = null,
                imageHash = "some-local-hash",
            ),
        )
        db.productMediaDao().insertOrUpdate(
            ProductMediaEntity(
                productId = prodId,
                blob = imageBytes,
                updatedAt = now,
            ),
        )

        fakeSyncRemote.remoteMediaStorage.clear()

        // Sync triggers upload because synced_media_hash_prodId is not recorded
        syncRepository.sync().getOrThrow()

        // Verify media was uploaded to remote storage
        assertNotNull(fakeSyncRemote.remoteMediaStorage[prodId])
        assertTrue(imageBytes.contentEquals(fakeSyncRemote.remoteMediaStorage[prodId]))

        // Verify synced_media_hash key was stored
        val syncedHash = db.appKeyValueDao().get("synced_media_hash_$prodId")
        assertNotNull(syncedHash)
    }

    @Test
    fun syncRepository_hasLocalData_and_clearLocalDataForHouseholdSwitch() = runTest {
        // Initially empty
        assertFalse(syncRepository.hasLocalData())

        // Insert a product
        val prodId = "prod-test-has-data"
        db.catalogDao().insert(
            CatalogProductEntity(
                id = prodId,
                ean = "7890000000999",
                name = "Arroz",
                category = "Mercearia",
                brand = null,
                measureUnit = "UNIT",
                netWeight = 1.0,
                thumbnailUrl = null,
                updatedAt = getCurrentTime(),
                isDeleted = false,
                manuallyAdded = true,
                notes = null,
            ),
        )
        assertTrue(syncRepository.hasLocalData())

        // Clear local data
        syncRepository.clearLocalDataForHouseholdSwitch()
        assertFalse(syncRepository.hasLocalData())
        assertEquals(0, db.catalogDao().getAllSnapshot().size)
    }
}
