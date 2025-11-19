package com.example.flowers

import java.time.Instant

class CatalogService(
    private val repo: CatalogRepository
) {

    fun addFlower(req: AddFlowerRequest): AddFlowerResponse {
        if (req.name.isBlank()) throw IllegalArgumentException("Name must not be blank")
        if (req.price <= 0) throw IllegalArgumentException("Price must be positive")
        if (req.imageUrl.isBlank()) throw IllegalArgumentException("imageUrl must not be blank")

        val flower = repo.addFlower(req)

        return AddFlowerResponse(
            flowerId = flower.id,
            status = "added",
            addedAt = flower.createdAt
        )
    }

    fun updateFlower(flowerId: String, req: UpdateFlowerRequest): UpdateFlowerResponse {
        if (req.name.isBlank()) throw IllegalArgumentException("Name must not be blank")
        if (req.price <= 0) throw IllegalArgumentException("Price must be positive")
        if (req.imageUrl.isBlank()) throw IllegalArgumentException("imageUrl must not be blank")

        val updated = repo.updateFlower(flowerId, req)
            ?: throw NoSuchElementException("Flower not found")

        return UpdateFlowerResponse(
            flowerId = updated.id,
            status = "updated",
            updatedAt = updated.updatedAt ?: Instant.now().toString()
        )
    }

    fun deleteFlower(flowerId: String): DeleteFlowerResponse {
        val existed = repo.findById(flowerId)
            ?: throw NoSuchElementException("Flower not found")

        val ok = repo.deleteFlower(flowerId)
        if (!ok) throw IllegalStateException("Failed to delete flower")

        val now = Instant.now().toString()

        return DeleteFlowerResponse(
            flowerId = existed.id,
            status = "deleted",
            deletedAt = now
        )
    }
}