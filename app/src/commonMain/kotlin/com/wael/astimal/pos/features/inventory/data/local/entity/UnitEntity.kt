package com.wael.astimal.pos.features.inventory.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.features.inventory.domain.entity.ProductUnit

/**
 * Represents a measurement unit in the local Room database.
 */
@Entity(
    tableName = "units"
)
data class UnitEntity(
    @PrimaryKey
    val localId: String,
    var isSynced: Boolean = false,
    val createdAt: Long,
    var updatedAt: Long,
    var isDeletedLocally: Boolean = false,

    val arName: String?,
    val enName: String,
    val arAbbreviation: String?,
    val enAbbreviation: String?

)

/**
 * Maps the local UnitEntity from the database to the ProductUnit domain model
 * used throughout the application's business logic.
 */
fun UnitEntity.toDomain(): ProductUnit {
    return ProductUnit(
        id = localId,
        name = LocalizedString(arName = arName, enName = enName),
        abbreviation = LocalizedString(arName = arAbbreviation, enName = enAbbreviation),
        isSynced = isSynced,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
