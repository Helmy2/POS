package com.wael.astimal.pos.core.data.entity

/**
 * A universal interface for all Room database entities.
 * It ensures that every table has a consistent set of columns for handling
 * local/server IDs, timestamps, and synchronization flags.
 */
interface ItemEntity {
    val localId: Long
    val serverId: Long?
    val createdAt: Long
    val updatedAt: Long
    val isSynced: Boolean
    val isDeletedLocally: Boolean
}