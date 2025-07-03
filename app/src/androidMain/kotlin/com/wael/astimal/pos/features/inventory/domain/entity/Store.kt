package com.wael.astimal.pos.features.inventory.domain.entity

import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.Item
import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.data.entity.StoreEntity
import com.wael.astimal.pos.features.inventory.data.entity.StoreType

data class Store(
    val name: LocalizedString,
    val type: StoreType,
    override val id: Id,
    override val createdAt: Long,
    override val isSynced: Boolean = false,
    override val updatedAt: Long = Clock.now()
) : Item {
    companion object {
        fun getUnspecifiedStore(serverId: Long): Store {
            return Store(
                name = LocalizedString(arName = "Unspecified", enName = "Unspecified"),
                type = StoreType.UNSPECIFIED,
                id = Id.new.copy(server = serverId),
                createdAt = Clock.now(),
                isSynced = false
            )
        }
    }
}

fun Store.toEntity(): StoreEntity {
    return StoreEntity(
        localId = this.id.local,
        serverId = this.id.server,
        arName = this.name.arName ?: "",
        enName = this.name.enName ?: "",
        type = this.type,
        isSynced = this.isSynced,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}
