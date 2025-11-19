package com.example.flowers

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.UUID

class CatalogRepository {

    fun addFlower(req: AddFlowerRequest): Flower = transaction {
        val id = UUID.randomUUID().toString()
        val now = Instant.now().toString()

        FlowersTable.insert {
            it[FlowersTable.id] = id
            it[name] = req.name
            it[description] = req.description
            it[price] = req.price
            it[imageUrl] = req.imageUrl
            it[createdAt] = now
            it[updatedAt] = null
        }

        Flower(
            id = id,
            name = req.name,
            description = req.description,
            price = req.price,
            imageUrl = req.imageUrl,
            createdAt = now,
            updatedAt = null
        )
    }

    fun findById(flowerId: String): Flower? = transaction {
        FlowersTable
            .selectAll()
            .where { FlowersTable.id eq flowerId }
            .singleOrNull()
            ?.let {
                Flower(
                    id = it[FlowersTable.id],
                    name = it[FlowersTable.name],
                    description = it[FlowersTable.description],
                    price = it[FlowersTable.price],
                    imageUrl = it[FlowersTable.imageUrl],
                    createdAt = it[FlowersTable.createdAt],
                    updatedAt = it[FlowersTable.updatedAt]
                )
            }
    }

    fun updateFlower(flowerId: String, req: UpdateFlowerRequest): Flower? = transaction {
        val existing = FlowersTable
            .selectAll()
            .where { FlowersTable.id eq flowerId }
            .singleOrNull() ?: return@transaction null

        val now = Instant.now().toString()

        FlowersTable.update(where = { FlowersTable.id eq flowerId }) {
            it[name] = req.name
            it[description] = req.description
            it[price] = req.price
            it[imageUrl] = req.imageUrl
            it[updatedAt] = now
        }

        val updated = FlowersTable
            .selectAll()
            .where { FlowersTable.id eq flowerId }
            .single()

        Flower(
            id = updated[FlowersTable.id],
            name = updated[FlowersTable.name],
            description = updated[FlowersTable.description],
            price = updated[FlowersTable.price],
            imageUrl = updated[FlowersTable.imageUrl],
            createdAt = updated[FlowersTable.createdAt],
            updatedAt = updated[FlowersTable.updatedAt]
        )
    }

    fun deleteFlower(flowerId: String): Boolean = transaction {
        FlowersTable.deleteWhere { FlowersTable.id eq flowerId } > 0
    }
}