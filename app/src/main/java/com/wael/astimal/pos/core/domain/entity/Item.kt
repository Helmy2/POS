package com.wael.astimal.pos.core.domain.entity

/**
 * A universal interface for all domain models.
 * It ensures that every domain entity has a consistent set of base properties,
 * including a composite ID, timestamps, and synchronization status.
 */
interface Item {
    val id: Id
    val createdAt: Long
    val updatedAt: Long
    val isSynced: Boolean
}


/**
 * A universal identifier that encapsulates both the local and server IDs for an entity.
 * This provides a single, consistent way to reference an item's identity throughout the domain layer.
 *
 * @property local The unique ID assigned by the local Room database (auto-incrementing Long).
 * @property server The unique ID assigned by the remote server, which may be null for new offline items.
 */
data class Id(
    val local: Long,
    val server: Long?
)