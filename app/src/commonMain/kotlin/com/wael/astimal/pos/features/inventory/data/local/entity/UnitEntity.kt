package com.wael.astimal.pos.features.inventory.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.wael.astimal.pos.core.data.entity.ItemEntity
import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.features.inventory.domain.entity.ProductUnit

/**
 * Represents a measurement unit in the local Room database.
 */
@Entity(
    tableName = "units",
    indices = [Index(value = ["serverId"], unique = true)]
)
data class UnitEntity(
    @PrimaryKey(autoGenerate = true)
    override val localId: Long = 0L,
    override val serverId: Long?,
    override var isSynced: Boolean = false,
    override val createdAt: Long,
    override var updatedAt: Long,
    override var isDeletedLocally: Boolean = false,

    val arName: String?,
    val enName: String,
    val arAbbreviation: String?,
    val enAbbreviation: String?

) : ItemEntity

/**
 * Maps the local UnitEntity from the database to the ProductUnit domain model
 * used throughout the application's business logic.
 */
fun UnitEntity.toDomain(): ProductUnit {
    return ProductUnit(
        id = Id(local = localId, server = serverId),
        name = LocalizedString(arName = arName, enName = enName),
        abbreviation = LocalizedString(arName = arAbbreviation, enName = enAbbreviation),
        isSynced = isSynced,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
