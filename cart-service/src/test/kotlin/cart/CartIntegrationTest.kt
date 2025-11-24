package com.example.cart

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CartIntegrationTest {

    private lateinit var repo: CartRepository
    private lateinit var service: CartService
    private lateinit var dbFile: Path

    @BeforeTest
    fun setup() {
        dbFile = Files.createTempFile("cart-test", ".db").apply {
            toFile().deleteOnExit()
        }

        Database.connect(
            url = "jdbc:sqlite:${dbFile.toAbsolutePath()}",
            driver = "org.sqlite.JDBC"
        )

        transaction {
            SchemaUtils.create(CartItemsTable)
            CartItemsTable.deleteAll()
        }

        repo = CartRepository()
        service = CartService(repo)
    }

    @AfterTest
    fun tearDown() {
        kotlin.runCatching { Files.deleteIfExists(dbFile) }
    }

    @Test
    fun addUpdateAndDeleteFlowPersistsInDatabase() {
        val added = service.addItem("user-1", AddItemRequest("prod-1", 2))
        assertEquals("prod-1", added.productId)
        assertEquals(2, added.quantity)

        service.updateQuantity("user-1", "prod-1", UpdateQuantityRequest(5))

        val quantityInDb = transaction {
            CartItemsTable
                .select {
                    (CartItemsTable.userId eq "user-1") and (CartItemsTable.productId eq "prod-1")
                }
                .single()[CartItemsTable.quantity]
        }
        assertEquals(5, quantityInDb)

        service.deleteItem("user-1", "prod-1")

        val remaining = transaction { CartItemsTable.selectAll().count() }
        assertEquals(0, remaining)
    }

    @Test
    fun clearCartRemovesAllRowsForUser() {
        service.addItem("user-1", AddItemRequest("prod-1", 1))
        service.addItem("user-1", AddItemRequest("prod-2", 3))
        service.addItem("user-2", AddItemRequest("prod-3", 2))

        service.clearCart("user-1")

        val user1Count = transaction {
            CartItemsTable
                .select { CartItemsTable.userId eq "user-1" }
                .count()
        }
        val user2Count = transaction {
            CartItemsTable
                .select { CartItemsTable.userId eq "user-2" }
                .count()
        }

        assertEquals(0L, user1Count)
        assertEquals(1L, user2Count)
    }
}

