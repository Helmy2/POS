package com.wael.astimal.pos.features.inventory.domain.entity

data class Category(
    val localId: Long,
    val serverId: Int?,
    val arName: String?,
    val enName: String?,
    val isSynced: Boolean,
    val lastModified: Long,
    val isDeletedLocally: Boolean
)
