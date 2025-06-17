package com.wael.astimal.pos.features.inventory.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wael.astimal.pos.core.data.entity.ItemEntity
import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.features.inventory.domain.entity.ProductUnit

@Entity(tableName = "units")
data class UnitEntity(
    @PrimaryKey(autoGenerate = true) override val localId: Long = 0L,
    override val serverId: Long?,
    override var isSynced: Boolean = false,
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override var isDeletedLocally: Boolean = false,

    var arName: String?,
    var enName: String?,
) : ItemEntity

fun UnitEntity.toDomain(): ProductUnit {
    return ProductUnit(
        id = Id(localId, serverId), localizedName = LocalizedString(
            arName = arName, enName = enName
        ), isSynced = isSynced, createdAt = createdAt, updatedAt = updatedAt
    )
}