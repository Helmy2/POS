package com.wael.astimal.pos.features.inventory.domain.entity

import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.core.util.toISOString
import com.wael.astimal.pos.features.inventory.data.local.entity.CategoryEntity
import com.wael.astimal.pos.features.inventory.data.remote.dto.CategoryDto

data class Category(
    val name: LocalizedString,
    val id: String,
    val isSynced: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long = Clock.now()
)

fun Category.toEntity(): CategoryEntity {
    return CategoryEntity(
        localId = id,
        arName = name.arName,
        enName = name.enName,
        isSynced = isSynced,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Category.toDto(): CategoryDto {
    return CategoryDto(
        id = id,
        arName = name.arName,
        enName = name.enName ?: "",
        createdAt = createdAt.toISOString(),
        updatedAt = updatedAt.toISOString()
    )
}

