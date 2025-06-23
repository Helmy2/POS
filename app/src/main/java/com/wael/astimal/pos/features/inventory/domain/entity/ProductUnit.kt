package com.wael.astimal.pos.features.inventory.domain.entity

import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.Item
import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.features.inventory.data.entity.UnitEntity


data class ProductUnit(
    val name: LocalizedString,
    override val id: Id,
    override val isSynced: Boolean = false,
    override val createdAt: Long,
    override val updatedAt: Long = System.currentTimeMillis(),
) : Item

fun ProductUnit.toEntity(): UnitEntity {
    return UnitEntity(
        localId = id.local,
        serverId = id.server,
        arName = name.arName ?: "",
        enName = name.enName ?: "",
        isSynced = isSynced,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}