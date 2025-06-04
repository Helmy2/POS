package com.wael.astimal.pos.features.inventory.domain.entity

import com.wael.astimal.pos.features.inventory.data.entity.UnitEntity


data class UnitDetails(
    val localId: Long,
    val serverId: Int?,
    var arName: String,
    var enName: String,
    val rate: Float,
    val isSynced: Boolean,
    val lastModified: Long,
    val isDeletedLocally: Boolean
)

fun UnitEntity.toUnitDetails(): UnitDetails {
    return UnitDetails(
        localId = this.localId,
        serverId = this.serverId,
        arName = this.arName ?: "",
        enName = this.enName ?: "",
        rate = this.rate,
        isSynced = this.isSynced,
        lastModified = this.lastModified,
        isDeletedLocally = this.isDeletedLocally
    )
}