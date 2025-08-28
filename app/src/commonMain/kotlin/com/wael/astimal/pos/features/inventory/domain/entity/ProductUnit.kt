package com.wael.astimal.pos.features.inventory.domain.entity

import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.core.util.toISOString
import com.wael.astimal.pos.features.inventory.data.local.entity.UnitEntity
import com.wael.astimal.pos.features.inventory.data.remote.dto.UnitDto


data class ProductUnit(
    val name: LocalizedString,
    val abbreviation: LocalizedString,
    val id: String,
    val isSynced: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long = Clock.now(),
)

fun ProductUnit.toEntity(): UnitEntity {
    return UnitEntity(
        localId = id,
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
        id = id,
        arName = name.arName,
        enName = name.enName ?: "",
        arAbbreviation = abbreviation.arName,
        enAbbreviation = abbreviation.enName,
        createdAt = createdAt.toISOString(),
        updatedAt = updatedAt.toISOString()
    )
}