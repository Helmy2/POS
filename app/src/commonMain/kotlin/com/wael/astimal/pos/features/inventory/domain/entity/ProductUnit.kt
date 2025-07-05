package com.wael.astimal.pos.features.inventory.domain.entity

import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.Item
import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.core.util.toDateString
import com.wael.astimal.pos.features.inventory.data.local.entity.UnitEntity
import com.wael.astimal.pos.features.inventory.data.remote.dto.UnitDto


data class ProductUnit(
    val name: LocalizedString,
    val abbreviation: LocalizedString,
    override val id: Id,
    override val isSynced: Boolean = false,
    override val createdAt: Long,
    override val updatedAt: Long = Clock.now(),
) : Item

fun ProductUnit.toEntity(): UnitEntity {
    return UnitEntity(
        localId = id.local,
        serverId = id.server,
        arName = name.arName ?: "",
        enName = name.enName ?: "",
        isSynced = isSynced,
        createdAt = createdAt,
        updatedAt = updatedAt,
        arAbbreviation = abbreviation.arName ?: "",
        enAbbreviation = abbreviation.enName ?: ""
    )
}

fun ProductUnit.toDto(): UnitDto {
    return UnitDto(
        id = id.server ?: 0,
        arName = name.arName,
        enName = name.enName ?: "",
        arAbbreviation = abbreviation.arName,
        enAbbreviation = abbreviation.enName,
        createdAt = createdAt.toDateString(),
        updatedAt = updatedAt.toDateString()
    )
}