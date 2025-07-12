package com.wael.astimal.pos.features.inventory.domain.entity

import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.Item
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
    override val id: Id,
    override val createdAt: Long,
    override val isSynced: Boolean = false,
    override val updatedAt: Long = Clock.now(),
) : Item

fun Store.toEntity(): StoreEntity {
    return StoreEntity(
        localId = id.local,
        serverId = id.server,
        arName = name.arName ?: "",
        enName = name.enName ?: "",
        type = type,
        isSynced = isSynced,
        createdAt = createdAt,
        updatedAt = updatedAt,
        address = address,
        employeeId = employee.id.local
    )
}

fun Store.toDto(): StoreDto {
    return StoreDto(
        id = id.server ?: 0,
        arName = name.arName,
        enName = name.enName ?: "",
        address = address,
        type = type.name.lowercase(),
        createdAt = createdAt.toDateString(),
        updatedAt = updatedAt.toDateString(),
        employeeId = employee.id.serverStringId!!
    )
}
