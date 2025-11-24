package com.example.flowers

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CatalogIntegrationTest {

    private lateinit var repo: CatalogRepository
    private lateinit var service: CatalogService
    private lateinit var dbFile: Path

    @BeforeTest
    fun setup() {
        dbFile = Files.createTempFile("catalog-test", ".db").apply {
            toFile().deleteOnExit()
        }

        Database.connect(
            url = "jdbc:sqlite:${dbFile.toAbsolutePath()}",
            driver = "org.sqlite.JDBC"
        )

        transaction {
            SchemaUtils.create(FlowersTable)
            FlowersTable.deleteAll()
        }

        repo = CatalogRepository()
        service = CatalogService(repo)
    }

    @AfterTest
    fun tearDown() {
        kotlin.runCatching { Files.deleteIfExists(dbFile) }
    }

    @Test
    fun addAndRetrieveFlower() {
        val response = service.addFlower(
            AddFlowerRequest(
                name = "Peony",
                description = "Pink flower",
                price = 250,
                imageUrl = "https://example.com/peony.png"
            )
        )

        val stored = repo.findById(response.flowerId)
        assertNotNull(stored)
        assertEquals("Peony", stored.name)
        assertEquals(250, stored.price)
    }

    @Test
    fun updateAndDeleteFlow() {
        val created = repo.addFlower(
            AddFlowerRequest(
                name = "Camellia",
                description = "White",
                price = 180,
                imageUrl = "https://example.com/camellia.png"
            )
        )

        val updateResponse = service.updateFlower(
            created.id,
            UpdateFlowerRequest(
                name = "Camellia Deluxe",
                description = "Updated",
                price = 210,
                imageUrl = "https://example.com/camellia-deluxe.png"
            )
        )
        assertEquals("updated", updateResponse.status)

        val deleteResponse = service.deleteFlower(created.id)
        assertEquals("deleted", deleteResponse.status)

        val lookup = repo.findById(created.id)
        assertNull(lookup)
    }
}

