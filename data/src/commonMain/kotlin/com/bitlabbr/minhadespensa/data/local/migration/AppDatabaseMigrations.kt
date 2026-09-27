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

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

private fun hasColumn(connection: SQLiteConnection, tableName: String, columnName: String): Boolean {
    val stmt = connection.prepare("PRAGMA table_info(`$tableName`)")
    try {
        while (stmt.step()) {
            val name = stmt.getText(1)
            if (name.equals(columnName, ignoreCase = true)) {
                return true
            }
        }
        return false
    } finally {
        stmt.close()
    }
}

private fun tableExists(connection: SQLiteConnection, tableName: String): Boolean {
    val stmt = connection.prepare("SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ?")
    try {
        stmt.bindText(1, tableName)
        return stmt.step()
    } finally {
        stmt.close()
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        if (!tableExists(connection, "product_media")) {
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
            connection.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_product_media_productId` ON `product_media` (`productId`)"
            )
        }
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        if (!hasColumn(connection, "catalog_products", "category")) {
            connection.execSQL("ALTER TABLE `catalog_products` ADD COLUMN `category` TEXT NOT NULL DEFAULT 'Geral'")
        }
        if (!hasColumn(connection, "catalog_products", "notes")) {
            connection.execSQL("ALTER TABLE `catalog_products` ADD COLUMN `notes` TEXT DEFAULT NULL")
        }

        if (!tableExists(connection, "shopping_lists")) {
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
                INSERT OR IGNORE INTO `shopping_lists` (`id`, `updatedAt`, `name`, `budgetInCents`, `isDeleted`)
                SELECT 'default_list', 0, 'Lista de Compras', NULL, 0
                WHERE EXISTS (SELECT 1 FROM `shopping_items`)
                """.trimIndent()
            )
        }

        if (tableExists(connection, "shopping_items") && !hasColumn(connection, "shopping_items", "listId") && !hasColumn(connection, "shopping_items", "list_id")) {
            connection.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `shopping_items_v3` (
                    `id` TEXT NOT NULL,
                    `productId` TEXT NOT NULL,
                    `listId` TEXT NOT NULL,
                    `quantity` REAL NOT NULL,
                    `priceAtTime` INTEGER,
                    `isChecked` INTEGER NOT NULL,
                    `updatedAt` INTEGER NOT NULL,
                    `isDeleted` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`productId`) REFERENCES `catalog_products`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(`listId`) REFERENCES `shopping_lists`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            connection.execSQL(
                """
                INSERT INTO `shopping_items_v3` (`id`, `productId`, `listId`, `quantity`, `priceAtTime`, `isChecked`, `updatedAt`, `isDeleted`)
                SELECT `id`, `productId`, 'default_list', `quantity`, `priceAtTime`, `isChecked`, `updatedAt`, `isDeleted`
                FROM `shopping_items`
                """.trimIndent()
            )
            connection.execSQL("DROP TABLE `shopping_items`")
            connection.execSQL("ALTER TABLE `shopping_items_v3` RENAME TO `shopping_items`")
            connection.execSQL("CREATE INDEX IF NOT EXISTS `index_shopping_items_productId` ON `shopping_items` (`productId`)")
            connection.execSQL("CREATE INDEX IF NOT EXISTS `index_shopping_items_listId` ON `shopping_items` (`listId`)")
        }
    }
}

fun migrateToSchemaV4(connection: SQLiteConnection) {
    // Disable foreign key constraints during table recreation and data copying
    connection.execSQL("PRAGMA foreign_keys = OFF")

    try {
        // 1. catalog_products: Add columns defensively if not already present
        if (!hasColumn(connection, "catalog_products", "category")) {
            connection.execSQL("ALTER TABLE `catalog_products` ADD COLUMN `category` TEXT NOT NULL DEFAULT 'Geral'")
        }
        if (!hasColumn(connection, "catalog_products", "notes")) {
            connection.execSQL("ALTER TABLE `catalog_products` ADD COLUMN `notes` TEXT DEFAULT NULL")
        }
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_catalog_products_ean` ON `catalog_products` (`ean`)")

        // 2. pantry_items: Recreate table with exact Room DDL and copy all existing data
        connection.execSQL("DROP TABLE IF EXISTS `pantry_items_v4`")
        connection.execSQL(
            """
            CREATE TABLE `pantry_items_v4` (
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
        if (tableExists(connection, "pantry_items")) {
            val qtyCol = if (hasColumn(connection, "pantry_items", "quantity")) "`quantity`" else "0.0"
            val expCol = if (hasColumn(connection, "pantry_items", "expirationDate")) "`expirationDate`" else "NULL"
            val batchCol = if (hasColumn(connection, "pantry_items", "batchNumber")) "`batchNumber`" else "NULL"
            val updatedCol = if (hasColumn(connection, "pantry_items", "updatedAt")) "`updatedAt`" else "0"
            val isDeletedCol = if (hasColumn(connection, "pantry_items", "isDeleted")) "`isDeleted`" else "0"

            connection.execSQL(
                """
                INSERT INTO `pantry_items_v4` (`id`, `productId`, `quantity`, `expirationDate`, `batchNumber`, `updatedAt`, `isDeleted`)
                SELECT `id`, `productId`, $qtyCol, $expCol, $batchCol, $updatedCol, $isDeletedCol
                FROM `pantry_items`
                """.trimIndent()
            )
            connection.execSQL("DROP TABLE `pantry_items`")
        }
        connection.execSQL("ALTER TABLE `pantry_items_v4` RENAME TO `pantry_items`")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_pantry_items_productId` ON `pantry_items` (`productId`)")

        // 3. price_entries: Recreate table with exact Room DDL and copy all existing data
        connection.execSQL("DROP TABLE IF EXISTS `price_entries_v4`")
        connection.execSQL(
            """
            CREATE TABLE `price_entries_v4` (
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
        if (tableExists(connection, "price_entries")) {
            val priceCol = when {
                hasColumn(connection, "price_entries", "priceInCents") -> "`priceInCents`"
                hasColumn(connection, "price_entries", "price_in_cents") -> "`price_in_cents`"
                hasColumn(connection, "price_entries", "price") -> "`price`"
                else -> "0"
            }
            val storeCol = when {
                hasColumn(connection, "price_entries", "storeName") -> "`storeName`"
                hasColumn(connection, "price_entries", "store_name") -> "`store_name`"
                else -> "NULL"
            }
            val updatedCol = when {
                hasColumn(connection, "price_entries", "updatedAt") -> "`updatedAt`"
                hasColumn(connection, "price_entries", "updated_at") -> "`updated_at`"
                else -> "0"
            }
            val isDeletedCol = when {
                hasColumn(connection, "price_entries", "isDeleted") -> "`isDeleted`"
                hasColumn(connection, "price_entries", "is_deleted") -> "`is_deleted`"
                else -> "0"
            }

            connection.execSQL(
                """
                INSERT INTO `price_entries_v4` (`id`, `productId`, `priceInCents`, `storeName`, `updatedAt`, `isDeleted`)
                SELECT `id`, `productId`, $priceCol, $storeCol, $updatedCol, $isDeletedCol
                FROM `price_entries`
                """.trimIndent()
            )
            connection.execSQL("DROP TABLE `price_entries`")
        }
        connection.execSQL("ALTER TABLE `price_entries_v4` RENAME TO `price_entries`")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_price_entries_productId` ON `price_entries` (`productId`)")

        // 4. product_media: Recreate table with exact Room DDL and copy all existing data
        connection.execSQL("DROP TABLE IF EXISTS `product_media_v4`")
        connection.execSQL(
            """
            CREATE TABLE `product_media_v4` (
                `productId` TEXT NOT NULL,
                `blob` BLOB NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                PRIMARY KEY(`productId`),
                FOREIGN KEY(`productId`) REFERENCES `catalog_products`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        if (tableExists(connection, "product_media")) {
            val updatedCol = when {
                hasColumn(connection, "product_media", "updatedAt") -> "`updatedAt`"
                hasColumn(connection, "product_media", "updated_at") -> "`updated_at`"
                else -> "0"
            }
            connection.execSQL(
                """
                INSERT INTO `product_media_v4` (`productId`, `blob`, `updatedAt`)
                SELECT `productId`, `blob`, $updatedCol
                FROM `product_media`
                """.trimIndent()
            )
            connection.execSQL("DROP TABLE `product_media`")
        }
        connection.execSQL("ALTER TABLE `product_media_v4` RENAME TO `product_media`")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_product_media_productId` ON `product_media` (`productId`)")

        // 5. shopping_lists: Recreate table with exact Room DDL and copy all existing data
        connection.execSQL("DROP TABLE IF EXISTS `shopping_lists_v4`")
        connection.execSQL(
            """
            CREATE TABLE `shopping_lists_v4` (
                `id` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `list_type` TEXT NOT NULL,
                `list_status` TEXT NOT NULL,
                `budget_in_cents` INTEGER,
                `updated_at` INTEGER NOT NULL,
                `is_deleted` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        if (tableExists(connection, "shopping_lists")) {
            val listTypeCol = when {
                hasColumn(connection, "shopping_lists", "list_type") -> "`list_type`"
                hasColumn(connection, "shopping_lists", "type") -> "`type`"
                else -> "'PLANNED'"
            }
            val listStatusCol = when {
                hasColumn(connection, "shopping_lists", "list_status") -> "`list_status`"
                hasColumn(connection, "shopping_lists", "status") -> "`status`"
                else -> "'OPEN'"
            }
            val budgetCol = when {
                hasColumn(connection, "shopping_lists", "budget_in_cents") -> "`budget_in_cents`"
                hasColumn(connection, "shopping_lists", "budgetInCents") -> "`budgetInCents`"
                else -> "NULL"
            }
            val updatedCol = when {
                hasColumn(connection, "shopping_lists", "updated_at") -> "`updated_at`"
                hasColumn(connection, "shopping_lists", "updatedAt") -> "`updatedAt`"
                else -> "0"
            }
            val isDeletedCol = when {
                hasColumn(connection, "shopping_lists", "is_deleted") -> "`is_deleted`"
                hasColumn(connection, "shopping_lists", "isDeleted") -> "`isDeleted`"
                else -> "0"
            }

            connection.execSQL(
                """
                INSERT INTO `shopping_lists_v4` (`id`, `name`, `list_type`, `list_status`, `budget_in_cents`, `updated_at`, `is_deleted`)
                SELECT `id`, `name`, $listTypeCol, $listStatusCol, $budgetCol, $updatedCol, $isDeletedCol
                FROM `shopping_lists`
                """.trimIndent()
            )
            connection.execSQL("DROP TABLE `shopping_lists`")
        }
        connection.execSQL("ALTER TABLE `shopping_lists_v4` RENAME TO `shopping_lists`")

        // 6. shopping_items: Ensure default shopping list exists if items exist without list_id
        if (tableExists(connection, "shopping_items")) {
            val hasListId = hasColumn(connection, "shopping_items", "list_id") || hasColumn(connection, "shopping_items", "listId")
            if (!hasListId) {
                connection.execSQL(
                    """
                    INSERT OR IGNORE INTO `shopping_lists` (`id`, `name`, `list_type`, `list_status`, `budget_in_cents`, `updated_at`, `is_deleted`)
                    SELECT 'default_list', 'Lista de Compras', 'PLANNED', 'OPEN', NULL, 0, 0
                    WHERE EXISTS (SELECT 1 FROM `shopping_items`)
                    """.trimIndent()
                )
            }
        }

        connection.execSQL("DROP TABLE IF EXISTS `shopping_items_v4`")
        connection.execSQL(
            """
            CREATE TABLE `shopping_items_v4` (
                `id` TEXT NOT NULL,
                `list_id` TEXT NOT NULL,
                `product_id` TEXT,
                `raw_text` TEXT,
                `quantity` REAL NOT NULL,
                `price_at_time` INTEGER,
                `is_checked` INTEGER NOT NULL,
                `updated_at` INTEGER NOT NULL,
                `is_deleted` INTEGER NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`list_id`) REFERENCES `shopping_lists`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`product_id`) REFERENCES `catalog_products`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
            )
            """.trimIndent()
        )
        if (tableExists(connection, "shopping_items")) {
            val listIdCol = when {
                hasColumn(connection, "shopping_items", "list_id") -> "`list_id`"
                hasColumn(connection, "shopping_items", "listId") -> "`listId`"
                else -> "'default_list'"
            }
            val prodIdCol = when {
                hasColumn(connection, "shopping_items", "product_id") -> "`product_id`"
                hasColumn(connection, "shopping_items", "productId") -> "`productId`"
                else -> "NULL"
            }
            val rawTextCol = if (hasColumn(connection, "shopping_items", "raw_text")) "`raw_text`" else "NULL"
            val qtyCol = if (hasColumn(connection, "shopping_items", "quantity")) "`quantity`" else "1.0"
            val priceCol = when {
                hasColumn(connection, "shopping_items", "price_at_time") -> "`price_at_time`"
                hasColumn(connection, "shopping_items", "priceAtTime") -> "`priceAtTime`"
                else -> "NULL"
            }
            val isCheckedCol = when {
                hasColumn(connection, "shopping_items", "is_checked") -> "`is_checked`"
                hasColumn(connection, "shopping_items", "isChecked") -> "`isChecked`"
                else -> "0"
            }
            val updatedCol = when {
                hasColumn(connection, "shopping_items", "updated_at") -> "`updated_at`"
                hasColumn(connection, "shopping_items", "updatedAt") -> "`updatedAt`"
                else -> "0"
            }
            val isDeletedCol = when {
                hasColumn(connection, "shopping_items", "is_deleted") -> "`is_deleted`"
                hasColumn(connection, "shopping_items", "isDeleted") -> "`isDeleted`"
                else -> "0"
            }

            connection.execSQL(
                """
                INSERT INTO `shopping_items_v4` (`id`, `list_id`, `product_id`, `raw_text`, `quantity`, `price_at_time`, `is_checked`, `updated_at`, `is_deleted`)
                SELECT `id`, $listIdCol, $prodIdCol, $rawTextCol, $qtyCol, $priceCol, $isCheckedCol, $updatedCol, $isDeletedCol
                FROM `shopping_items`
                """.trimIndent()
            )
            connection.execSQL("DROP TABLE `shopping_items`")
        }
        connection.execSQL("ALTER TABLE `shopping_items_v4` RENAME TO `shopping_items`")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_shopping_items_list_id` ON `shopping_items` (`list_id`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_shopping_items_product_id` ON `shopping_items` (`product_id`)")
    } finally {
        connection.execSQL("PRAGMA foreign_keys = ON")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(connection: SQLiteConnection) {
        migrateToSchemaV4(connection)
    }
}

val MIGRATION_2_4 = object : Migration(2, 4) {
    override fun migrate(connection: SQLiteConnection) {
        migrateToSchemaV4(connection)
    }
}

val MIGRATION_1_4 = object : Migration(1, 4) {
    override fun migrate(connection: SQLiteConnection) {
        migrateToSchemaV4(connection)
    }
}

val ALL_MIGRATIONS = arrayOf(
    MIGRATION_1_2,
    MIGRATION_2_3,
    MIGRATION_3_4,
    MIGRATION_1_4,
    MIGRATION_2_4
)
