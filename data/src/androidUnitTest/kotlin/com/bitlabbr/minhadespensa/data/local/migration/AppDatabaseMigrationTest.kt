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

package com.bitlabbr.minhadespensa.data.local.migration

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteStatement
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.execSQL
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.bitlabbr.minhadespensa.data.local.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppDatabaseMigrationTest {

    private lateinit var connection: SQLiteConnection

    @Before
    fun setUp() {
        connection = AndroidSQLiteDriver().open(":memory:")
    }

    @After
    fun tearDown() {
        connection.close()
    }

    @Test
    fun room_migrates_database_from_version_2_to_4_and_validates_schema() = runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val dbFile = File(context.cacheDir, "test_v2_to_v4.db")
        if (dbFile.exists()) dbFile.delete()

        // 1. Create a version 2 database on disk with legacy definitions (e.g. missing CASCADE or index differences)
        val rawConn = AndroidSQLiteDriver().open(dbFile.absolutePath)
        rawConn.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `catalog_products` (
                `id` TEXT NOT NULL,
                `ean` TEXT,
                `name` TEXT NOT NULL,
                `brand` TEXT,
                `measureUnit` TEXT NOT NULL,
                `netWeight` REAL NOT NULL,
                `thumbnailUrl` TEXT,
                `updatedAt` INTEGER NOT NULL,
                `isDeleted` INTEGER NOT NULL,
                `manuallyAdded` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        // Legacy pantry_items without CASCADE or without foreign key constraint
        rawConn.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `pantry_items` (
                `id` TEXT NOT NULL,
                `productId` TEXT NOT NULL,
                `quantity` REAL NOT NULL,
                `expirationDate` INTEGER,
                `batchNumber` TEXT,
                `updatedAt` INTEGER NOT NULL,
                `isDeleted` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        rawConn.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `price_entries` (
                `id` TEXT NOT NULL,
                `productId` TEXT NOT NULL,
                `priceInCents` INTEGER NOT NULL,
                `storeName` TEXT,
                `updatedAt` INTEGER NOT NULL,
                `isDeleted` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        rawConn.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `shopping_items` (
                `id` TEXT NOT NULL,
                `productId` TEXT NOT NULL,
                `quantity` REAL NOT NULL,
                `priceAtTime` INTEGER,
                `isChecked` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                `isDeleted` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )

        // Insert real user data in v2 format
        rawConn.execSQL(
            """
            INSERT INTO catalog_products (id, ean, name, brand, measureUnit, netWeight, thumbnailUrl, updatedAt, isDeleted, manuallyAdded)
            VALUES ('prod-disk-1', '7891234567890', 'Arroz Parboilizado', 'Tio João', 'KG', 1.0, 'https://img.com/arroz.png', 1000, 0, 1)
            """.trimIndent()
        )
        rawConn.execSQL(
            """
            INSERT INTO pantry_items (id, productId, quantity, expirationDate, batchNumber, updatedAt, isDeleted)
            VALUES ('pantry-disk-1', 'prod-disk-1', 4.0, 1735689600000, 'LOTE-TEST', 2000, 0)
            """.trimIndent()
        )
        rawConn.execSQL(
            """
            INSERT INTO price_entries (id, productId, priceInCents, storeName, updatedAt, isDeleted)
            VALUES ('price-disk-1', 'prod-disk-1', 599, 'Mercado Silva', 3000, 0)
            """.trimIndent()
        )
        rawConn.execSQL(
            """
            INSERT INTO shopping_items (id, productId, quantity, priceAtTime, isChecked, updatedAt, isDeleted)
            VALUES ('shop-disk-1', 'prod-disk-1', 1.5, 599, 0, 4000, 0)
            """.trimIndent()
        )

        rawConn.execSQL("PRAGMA user_version = 2")
        rawConn.close()

        // 2. Open with Room and ALL_MIGRATIONS - this triggers migration 2 -> 4 and Room schema validation
        val roomDb = Room.databaseBuilder<AppDatabase>(context, dbFile.absolutePath)
            .addMigrations(*ALL_MIGRATIONS)
            .setDriver(AndroidSQLiteDriver())
            .build()

        // 3. Verify all tables validate successfully and all DAOs return user data
        val product = checkNotNull(roomDb.catalogDao().findById("prod-disk-1").first())
        assertEquals("prod-disk-1", product.id)
        assertEquals("Arroz Parboilizado", product.name)
        assertEquals("Geral", product.category)
        assertNull(product.notes)

        val pantryItems = roomDb.pantryDao().getPantryItemsByProductId("prod-disk-1").first()
        assertEquals(1, pantryItems.size)
        assertEquals("pantry-disk-1", pantryItems[0].id)
        assertEquals(4.0, pantryItems[0].quantity, 0.001)
        assertEquals("LOTE-TEST", pantryItems[0].batchNumber)
        assertEquals(1735689600000L, pantryItems[0].expirationDate)

        val priceEntries = roomDb.priceDao().getPriceHistoryByProductId("prod-disk-1").first()
        assertEquals(1, priceEntries.size)
        assertEquals(599L, priceEntries[0].priceInCents)
        assertEquals("Mercado Silva", priceEntries[0].storeName)

        val shoppingLists = roomDb.shoppingListDao().getAllActiveShoppingLists().first()
        assertEquals(1, shoppingLists.size)
        assertEquals("default_list", shoppingLists[0].list.id)

        val shoppingItems = roomDb.shoppingItemDao().getItemsByListId("default_list").first()
        assertEquals(1, shoppingItems.size)
        assertEquals("shop-disk-1", shoppingItems[0].id)
        assertEquals("prod-disk-1", shoppingItems[0].productId)

        roomDb.close()
    }

    @Test
    fun migration_2_to_4_handles_database_where_category_already_exists() {
        createV2Schema(connection)
        connection.execSQL("ALTER TABLE catalog_products ADD COLUMN category TEXT NOT NULL DEFAULT 'Alimentos'")

        connection.execSQL(
            """
            INSERT INTO catalog_products (id, ean, name, brand, measureUnit, netWeight, thumbnailUrl, updatedAt, isDeleted, manuallyAdded, category)
            VALUES ('prod-existing-cat', '7891234567890', 'Macarrão', 'Barilla', 'KG', 0.5, NULL, 1000, 0, 1, 'Massas')
            """.trimIndent()
        )

        // Must NOT throw "duplicate column name: category"
        MIGRATION_2_4.migrate(connection)

        connection.prepare("SELECT name, category, notes FROM catalog_products WHERE id = 'prod-existing-cat'").useStatement { stmt ->
            assertTrue(stmt.step())
            assertEquals("Macarrão", stmt.getText(0))
            assertEquals("Massas", stmt.getText(1))
            assertTrue(stmt.isNull(2))
        }
    }

    private inline fun <T> SQLiteStatement.useStatement(block: (SQLiteStatement) -> T): T {
        try {
            return block(this)
        } finally {
            close()
        }
    }

    @Test
    fun migration_2_to_4_preserves_all_existing_user_data() {
        // 1. Create v2 schema
        createV2Schema(connection)

        // 2. Insert existing real user data
        connection.execSQL(
            """
            INSERT INTO catalog_products (id, ean, name, brand, measureUnit, netWeight, thumbnailUrl, updatedAt, isDeleted, manuallyAdded)
            VALUES ('prod-1', '7891234567890', 'Arroz Parboilizado', 'Tio João', 'KG', 1.0, 'https://img.com/arroz.png', 1000, 0, 1)
            """.trimIndent()
        )
        connection.execSQL(
            """
            INSERT INTO pantry_items (id, productId, quantity, expirationDate, batchNumber, updatedAt, isDeleted)
            VALUES ('pantry-1', 'prod-1', 3.5, 1735689600000, 'LOTE-A1', 2000, 0)
            """.trimIndent()
        )
        connection.execSQL(
            """
            INSERT INTO price_entries (id, productId, priceInCents, storeName, updatedAt, isDeleted)
            VALUES ('price-1', 'prod-1', 699, 'Supermercado Central', 3000, 0)
            """.trimIndent()
        )
        connection.execSQL(
            """
            INSERT INTO product_media (productId, blob, updatedAt)
            VALUES ('prod-1', X'CAFEBABE', 4000)
            """.trimIndent()
        )
        connection.execSQL(
            """
            INSERT INTO shopping_items (id, productId, quantity, priceAtTime, isChecked, updatedAt, isDeleted)
            VALUES ('shop-1', 'prod-1', 2.0, 650, 1, 5000, 0)
            """.trimIndent()
        )

        // 3. Execute Migration 2 -> 4
        MIGRATION_2_4.migrate(connection)

        // 4. Verify catalog_products data is preserved with new default category and nullable notes
        connection.prepare("SELECT id, ean, name, brand, measureUnit, netWeight, thumbnailUrl, updatedAt, isDeleted, manuallyAdded, category, notes FROM catalog_products WHERE id = 'prod-1'").useStatement { stmt ->
            assertTrue("Product should exist", stmt.step())
            assertEquals("prod-1", stmt.getText(0))
            assertEquals("7891234567890", stmt.getText(1))
            assertEquals("Arroz Parboilizado", stmt.getText(2))
            assertEquals("Tio João", stmt.getText(3))
            assertEquals("KG", stmt.getText(4))
            assertEquals(1.0, stmt.getDouble(5), 0.001)
            assertEquals("https://img.com/arroz.png", stmt.getText(6))
            assertEquals(1000L, stmt.getLong(7))
            assertEquals(0L, stmt.getLong(8))
            assertEquals(1L, stmt.getLong(9))
            assertEquals("Geral", stmt.getText(10))
            assertTrue("notes should be NULL for migrated products", stmt.isNull(11))
        }

        // 5. Verify pantry_items data is 100% intact
        connection.prepare("SELECT id, productId, quantity, expirationDate, batchNumber, updatedAt, isDeleted FROM pantry_items WHERE id = 'pantry-1'").useStatement { stmt ->
            assertTrue("Pantry item should exist", stmt.step())
            assertEquals("pantry-1", stmt.getText(0))
            assertEquals("prod-1", stmt.getText(1))
            assertEquals(3.5, stmt.getDouble(2), 0.001)
            assertEquals(1735689600000L, stmt.getLong(3))
            assertEquals("LOTE-A1", stmt.getText(4))
            assertEquals(2000L, stmt.getLong(5))
            assertEquals(0L, stmt.getLong(6))
        }

        // 6. Verify price_entries data is 100% intact
        connection.prepare("SELECT id, productId, priceInCents, storeName, updatedAt, isDeleted FROM price_entries WHERE id = 'price-1'").useStatement { stmt ->
            assertTrue("Price entry should exist", stmt.step())
            assertEquals("price-1", stmt.getText(0))
            assertEquals("prod-1", stmt.getText(1))
            assertEquals(699L, stmt.getLong(2))
            assertEquals("Supermercado Central", stmt.getText(3))
            assertEquals(3000L, stmt.getLong(4))
            assertEquals(0L, stmt.getLong(5))
        }

        // 7. Verify product_media is 100% intact
        connection.prepare("SELECT productId, updatedAt FROM product_media WHERE productId = 'prod-1'").useStatement { stmt ->
            assertTrue("Product media should exist", stmt.step())
            assertEquals("prod-1", stmt.getText(0))
            assertEquals(4000L, stmt.getLong(1))
        }

        // 8. Verify default shopping list was created and shopping item mapped correctly
        connection.prepare("SELECT id, name, list_type, list_status FROM shopping_lists WHERE id = 'default_list'").useStatement { stmt ->
            assertTrue("Default shopping list should exist", stmt.step())
            assertEquals("default_list", stmt.getText(0))
            assertEquals("Lista de Compras", stmt.getText(1))
            assertEquals("PLANNED", stmt.getText(2))
            assertEquals("OPEN", stmt.getText(3))
        }

        connection.prepare("SELECT id, list_id, product_id, raw_text, quantity, price_at_time, is_checked, updated_at, is_deleted FROM shopping_items WHERE id = 'shop-1'").useStatement { stmt ->
            assertTrue("Shopping item should exist", stmt.step())
            assertEquals("shop-1", stmt.getText(0))
            assertEquals("default_list", stmt.getText(1))
            assertEquals("prod-1", stmt.getText(2))
            assertTrue(stmt.isNull(3))
            assertEquals(2.0, stmt.getDouble(4), 0.001)
            assertEquals(650L, stmt.getLong(5))
            assertEquals(1L, stmt.getLong(6))
            assertEquals(5000L, stmt.getLong(7))
            assertEquals(0L, stmt.getLong(8))
        }
    }

    @Test
    fun migration_1_to_4_creates_media_and_migrates_all_data() {
        // 1. Create v1 schema (no product_media)
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `catalog_products` (
                `id` TEXT NOT NULL,
                `ean` TEXT,
                `name` TEXT NOT NULL,
                `brand` TEXT,
                `measureUnit` TEXT NOT NULL,
                `netWeight` REAL NOT NULL,
                `thumbnailUrl` TEXT,
                `updatedAt` INTEGER NOT NULL,
                `isDeleted` INTEGER NOT NULL,
                `manuallyAdded` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_catalog_products_ean` ON `catalog_products` (`ean`)")

        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `pantry_items` (
                `id` TEXT NOT NULL,
                `productId` TEXT NOT NULL,
                `quantity` REAL NOT NULL,
                `expirationDate` INTEGER,
                `batchNumber` TEXT,
                `updatedAt` INTEGER NOT NULL,
                `isDeleted` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )

        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `price_entries` (
                `id` TEXT NOT NULL,
                `productId` TEXT NOT NULL,
                `priceInCents` INTEGER NOT NULL,
                `storeName` TEXT,
                `updatedAt` INTEGER NOT NULL,
                `isDeleted` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )

        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `shopping_items` (
                `id` TEXT NOT NULL,
                `productId` TEXT NOT NULL,
                `quantity` REAL NOT NULL,
                `priceAtTime` INTEGER,
                `isChecked` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                `isDeleted` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )

        connection.execSQL(
            """
            INSERT INTO catalog_products (id, ean, name, brand, measureUnit, netWeight, thumbnailUrl, updatedAt, isDeleted, manuallyAdded)
            VALUES ('prod-v1', '111222333', 'Feijão Preto', 'Camil', 'KG', 1.0, NULL, 500, 0, 1)
            """.trimIndent()
        )

        // 2. Execute Migration 1 -> 4
        MIGRATION_1_4.migrate(connection)

        // 3. Verify product_media table was created and can accept data
        connection.execSQL(
            """
            INSERT INTO product_media (productId, blob, updatedAt)
            VALUES ('prod-v1', X'010203', 1000)
            """.trimIndent()
        )

        connection.prepare("SELECT productId, updatedAt FROM product_media WHERE productId = 'prod-v1'").useStatement { stmt ->
            assertTrue(stmt.step())
            assertEquals("prod-v1", stmt.getText(0))
            assertEquals(1000L, stmt.getLong(1))
        }

        // 4. Verify catalog product preserved with category and notes
        connection.prepare("SELECT name, category, notes FROM catalog_products WHERE id = 'prod-v1'").useStatement { stmt ->
            assertTrue(stmt.step())
            assertEquals("Feijão Preto", stmt.getText(0))
            assertEquals("Geral", stmt.getText(1))
            assertTrue(stmt.isNull(2))
        }
    }

    @Test
    fun migration_3_to_4_migrates_shopping_lists_and_items_to_snake_case() {
        // 1. Create v3 schema
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `catalog_products` (
                `id` TEXT NOT NULL,
                `ean` TEXT,
                `name` TEXT NOT NULL,
                `category` TEXT NOT NULL,
                `brand` TEXT,
                `measureUnit` TEXT NOT NULL,
                `netWeight` REAL NOT NULL,
                `thumbnailUrl` TEXT,
                `updatedAt` INTEGER NOT NULL,
                `isDeleted` INTEGER NOT NULL,
                `manuallyAdded` INTEGER NOT NULL,
                `notes` TEXT,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `shopping_lists` (
                `id` TEXT NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                `name` TEXT NOT NULL,
                `budgetInCents` INTEGER,
                `isDeleted` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `shopping_items` (
                `id` TEXT NOT NULL,
                `productId` TEXT NOT NULL,
                `listId` TEXT NOT NULL,
                `quantity` REAL NOT NULL,
                `priceAtTime` INTEGER,
                `isChecked` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                `isDeleted` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )

        connection.execSQL(
            """
            INSERT INTO shopping_lists (id, updatedAt, name, budgetInCents, isDeleted)
            VALUES ('list-3', 12345, 'Minha Lista V3', 50000, 0)
            """.trimIndent()
        )
        connection.execSQL(
            """
            INSERT INTO shopping_items (id, productId, listId, quantity, priceAtTime, isChecked, updatedAt, isDeleted)
            VALUES ('item-3', 'prod-3', 'list-3', 1.5, 399, 0, 12346, 0)
            """.trimIndent()
        )

        // 2. Execute Migration 3 -> 4
        MIGRATION_3_4.migrate(connection)

        // 3. Verify shopping_lists has snake_case columns with data preserved
        connection.prepare("SELECT id, name, list_type, list_status, budget_in_cents, updated_at, is_deleted FROM shopping_lists WHERE id = 'list-3'").useStatement { stmt ->
            assertTrue(stmt.step())
            assertEquals("list-3", stmt.getText(0))
            assertEquals("Minha Lista V3", stmt.getText(1))
            assertEquals("PLANNED", stmt.getText(2))
            assertEquals("OPEN", stmt.getText(3))
            assertEquals(50000L, stmt.getLong(4))
            assertEquals(12345L, stmt.getLong(5))
            assertEquals(0L, stmt.getLong(6))
        }

        // 4. Verify shopping_items has snake_case columns with data preserved
        connection.prepare("SELECT id, list_id, product_id, raw_text, quantity, price_at_time, is_checked, updated_at, is_deleted FROM shopping_items WHERE id = 'item-3'").useStatement { stmt ->
            assertTrue(stmt.step())
            assertEquals("item-3", stmt.getText(0))
            assertEquals("list-3", stmt.getText(1))
            assertEquals("prod-3", stmt.getText(2))
            assertTrue(stmt.isNull(3))
            assertEquals(1.5, stmt.getDouble(4), 0.001)
            assertEquals(399L, stmt.getLong(5))
            assertEquals(0L, stmt.getLong(6))
            assertEquals(12346L, stmt.getLong(7))
            assertEquals(0L, stmt.getLong(8))
        }
    }

    private fun createV2Schema(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `catalog_products` (
                `id` TEXT NOT NULL,
                `ean` TEXT,
                `name` TEXT NOT NULL,
                `brand` TEXT,
                `measureUnit` TEXT NOT NULL,
                `netWeight` REAL NOT NULL,
                `thumbnailUrl` TEXT,
                `updatedAt` INTEGER NOT NULL,
                `isDeleted` INTEGER NOT NULL,
                `manuallyAdded` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_catalog_products_ean` ON `catalog_products` (`ean`)")

        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `pantry_items` (
                `id` TEXT NOT NULL,
                `productId` TEXT NOT NULL,
                `quantity` REAL NOT NULL,
                `expirationDate` INTEGER,
                `batchNumber` TEXT,
                `updatedAt` INTEGER NOT NULL,
                `isDeleted` INTEGER NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`productId`) REFERENCES `catalog_products`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_pantry_items_productId` ON `pantry_items` (`productId`)")

        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `price_entries` (
                `id` TEXT NOT NULL,
                `productId` TEXT NOT NULL,
                `priceInCents` INTEGER NOT NULL,
                `storeName` TEXT,
                `updatedAt` INTEGER NOT NULL,
                `isDeleted` INTEGER NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`productId`) REFERENCES `catalog_products`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_price_entries_productId` ON `price_entries` (`productId`)")

        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `shopping_items` (
                `id` TEXT NOT NULL,
                `productId` TEXT NOT NULL,
                `quantity` REAL NOT NULL,
                `priceAtTime` INTEGER,
                `isChecked` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                `isDeleted` INTEGER NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`productId`) REFERENCES `catalog_products`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_shopping_items_productId` ON `shopping_items` (`productId`)")

        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `product_media` (
                `productId` TEXT NOT NULL,
                `blob` BLOB NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                PRIMARY KEY(`productId`),
                FOREIGN KEY(`productId`) REFERENCES `catalog_products`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_product_media_productId` ON `product_media` (`productId`)")
    }
}
