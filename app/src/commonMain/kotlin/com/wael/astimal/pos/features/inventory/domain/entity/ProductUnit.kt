package com.wael.astimal.pos.features.inventory.domain.entity

import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.Item
import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.data.entity.UnitEntity


data class ProductUnit(
    val name: LocalizedString,
    val abbreviation: LocalizedString,
    override val id: Id,
    override val isSynced: Boolean = false,
    override val createdAt: Long,
    override val updatedAt: Long = Clock.now(),
) : Item {
    companion object {
        fun getUnspecifiedUnit(serverId: Long): ProductUnit {
            return ProductUnit(
                name = LocalizedString(arName = "Unspecified", enName = "Unspecified"),
                abbreviation = LocalizedString(arName = "Unspecified", enName = "Unspecified"),
                id = Id.new.copy(server = serverId),
                isSynced = false,
                createdAt = Clock.now(),
                updatedAt = Clock.now(),
            )
        }
    }
}

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