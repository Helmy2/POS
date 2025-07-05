package com.wael.astimal.pos.features.inventory.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wael.astimal.pos.core.data.entity.ItemEntity
import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.domain.entity.Category

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) override val localId: Long = 0L,
    override val serverId: Long?,
    override var isSynced: Boolean = false,
    override val createdAt: Long = Clock.now(),
    override val updatedAt: Long = Clock.now(),
    override var isDeletedLocally: Boolean = false,

    val arName: String?,
    val enName: String?,
) : ItemEntity

fun CategoryEntity.toDomain(): Category {
    return Category(
        id = Id(localId, serverId),
        name = LocalizedString(arName = arName, enName = enName),
        isSynced = isSynced,
        updatedAt = updatedAt,
        createdAt = createdAt
    )
}