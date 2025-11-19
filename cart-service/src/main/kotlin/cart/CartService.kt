package com.example.cart

class CartService(private val repo: CartRepository) {

    fun addItem(userId: String, req: AddItemRequest): CartItemResponse {
        return repo.addItem(userId, req.productId, req.quantity)
    }

    fun updateQuantity(userId: String, productId: String, req: UpdateQuantityRequest) {
        val ok = repo.updateQuantity(userId, productId, req.quantity)
        if (!ok) throw NoSuchElementException("Item not found")
    }

    fun deleteItem(userId: String, productId: String) {
        val ok = repo.deleteItem(userId, productId)
        if (!ok) throw NoSuchElementException("Item not found")
    }

    fun clearCart(userId: String) {
        repo.clearCart(userId)
    }
}