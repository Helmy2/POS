package com.wael.astimal.pos.features.inventory.domain.entity


data class Unit(
    val localId: Long,
    val serverId: Int?,
    val localizedName: LocalizedString,
    val rate: Float,
    val isSynced: Boolean,
    val lastModified: Long,
    val isDeletedLocally: Boolean
)