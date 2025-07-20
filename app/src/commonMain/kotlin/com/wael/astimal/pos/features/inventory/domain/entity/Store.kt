package com.wael.astimal.pos.features.inventory.domain.entity

import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.core.util.toDateString
import com.wael.astimal.pos.features.inventory.data.local.entity.StoreEntity
import com.wael.astimal.pos.features.inventory.data.local.entity.StoreType
import com.wael.astimal.pos.features.inventory.data.remote.dto.StoreDto
import com.wael.astimal.pos.features.user.domain.entity.User

data class Store(
    val name: LocalizedString,
    val address: String,
    val type: StoreType,
    val employee: User,
    val id: String,
    val createdAt: Long,
    val isSynced: Boolean = false,
    val updatedAt: Long = Clock.now(),
)

fun Store.toEntity(): StoreEntity {
    return StoreEntity(
        localId = id,
        arName = name.arName ?: "",
        enName = name.enName ?: "",
        type = type,
        isSynced = isSynced,
        createdAt = createdAt,
        updatedAt = updatedAt,
        address = address,
        employeeId = employee.id
    )
}

fun Store.toDto(): StoreDto {
    return StoreDto(
        id = id,
        arName = name.arName,
        enName = name.enName ?: "",
        address = address,
        type = type.name.lowercase(),
        createdAt = createdAt.toDateString(),
        updatedAt = updatedAt.toDateString(),
        employeeId = employee.id
    )
}
