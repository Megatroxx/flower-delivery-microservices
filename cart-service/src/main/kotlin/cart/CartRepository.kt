package com.example.cart

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class CartRepository {

    fun addItem(userId: String, productId: String, quantity: Int): CartItemResponse {
        val id = UUID.randomUUID().toString()

        transaction {
            CartItemsTable.insert {
                it[CartItemsTable.id] = id
                it[CartItemsTable.userId] = userId
                it[CartItemsTable.productId] = productId
                it[CartItemsTable.quantity] = quantity
            }
        }

        return CartItemResponse(id, productId, quantity)
    }

    fun updateQuantity(userId: String, productId: String, quantity: Int): Boolean =
        transaction {
            CartItemsTable.update(
                { (CartItemsTable.userId eq userId) and (CartItemsTable.productId eq productId) }
            ) {
                it[CartItemsTable.quantity] = quantity
            } > 0
        }

    fun deleteItem(userId: String, productId: String): Boolean =
        transaction {
            CartItemsTable.deleteWhere {
                (CartItemsTable.userId eq userId) and (CartItemsTable.productId eq productId)
            } > 0
        }

    fun clearCart(userId: String): Boolean =
        transaction {
            CartItemsTable.deleteWhere { CartItemsTable.userId eq userId } > 0
        }
}
