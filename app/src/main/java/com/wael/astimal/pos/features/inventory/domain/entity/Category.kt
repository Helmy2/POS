package com.wael.astimal.pos.features.inventory.domain.entity

data class Category(
    val localId: Long,
    val serverId: Int?,
    val localizedName: LocalizedString,
    val isSynced: Boolean,
    val lastModified: Long,
    val isDeletedLocally: Boolean
)


