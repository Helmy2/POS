package com.wael.astimal.pos.core.data.entity

interface ItemEntity {
    val localId: Long
    val serverId: Long?
    val createdAt: Long
    val updatedAt: Long
    val isSynced: Boolean
    val isDeletedLocally: Boolean
}