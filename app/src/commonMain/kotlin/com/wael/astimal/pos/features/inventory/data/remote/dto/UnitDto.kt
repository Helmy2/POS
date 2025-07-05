package com.wael.astimal.pos.features.inventory.data.remote.dto

import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.core.util.parseIsoTimestamp
import com.wael.astimal.pos.features.inventory.data.entity.UnitEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a single unit object fetched from the Supabase 'units' table.
 */
@Serializable
data class UnitDto(
    val id: Long,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("ar_name")
    val arName: String?,
    @SerialName("en_name")
    val enName: String,
    @SerialName("ar_abbreviation")
    val arAbbreviation: String?,
    @SerialName("en_abbreviation")
    val enAbbreviation: String?
)

/**
 * Maps a UnitDto from the network to our local database UnitEntity.
 */
fun UnitDto.toEntity(): UnitEntity {
    return UnitEntity(
        serverId = this.id,
        localId = 0L,
        arName = this.arName ?: "",
        enName = this.enName,
        arAbbreviation = this.arAbbreviation,
        enAbbreviation = this.enAbbreviation,
        createdAt = this.createdAt.parseIsoTimestamp() ?: Clock.now(),
        updatedAt = this.updatedAt.parseIsoTimestamp() ?: Clock.now()
    )
}
