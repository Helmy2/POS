package com.wael.astimal.pos.features.inventory.data.remote.dto

import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.core.util.parseIsoTimestamp
import com.wael.astimal.pos.features.inventory.data.entity.StoreEntity
import com.wael.astimal.pos.features.inventory.data.entity.StoreType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StoreDto(
    val id: Long,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("ar_name")
    val arName: String?,
    @SerialName("en_name")
    val enName: String,
    val address: String?,
    val type: String
)

fun StoreDto.toEntity(): StoreEntity {
    return StoreEntity(
        serverId = this.id,
        arName = this.arName ?: "",
        enName = this.enName,
        address = this.address ?: "",
        type = if (this.type.equals("MAIN", true)) StoreType.MAIN else StoreType.SUB,
        createdAt = this.createdAt.parseIsoTimestamp() ?: Clock.now(),
        updatedAt = this.updatedAt.parseIsoTimestamp() ?: Clock.now()
    )
}