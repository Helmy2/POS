package com.wael.astimal.pos.features.inventory.domain.entity

import com.wael.astimal.pos.features.inventory.data.entity.StoreType

data class Store(
    val localId: Long,
    val serverId: Int?,
    val localizedName: LocalizedString,
    val type: StoreType,
    val isSynced: Boolean,
    val lastModified: Long,
    val isDeletedLocally: Boolean
)

