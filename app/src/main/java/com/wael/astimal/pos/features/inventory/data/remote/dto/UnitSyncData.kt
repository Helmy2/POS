package com.wael.astimal.pos.features.inventory.data.remote.dto

import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.core.util.parseIsoTimestamp
import com.wael.astimal.pos.features.inventory.data.entity.UnitEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class UnitSyncData(
    val units: List<UnitDto>,
    @SerialName("date")
    val nextSyncDate: String
)

@Serializable
data class UnitDto(
    val id: Long,
    val rate: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    val name: String, // The default name
    val translations: List<TranslationDto>
)

@Serializable
data class TranslationDto(
    val locale: String,
    val name: String?
)

fun UnitDto.toEntity(): UnitEntity {
    val arName = translations.find { it.locale == "ar" }?.name
    val enName = translations.find { it.locale == "en" }?.name

    return UnitEntity(
        serverId = this.id,
        localId = 0L,
        arName = arName ?: "",
        enName = enName ?: this.name,
        updatedAt = createdAt.parseIsoTimestamp() ?: Clock.now(),
        createdAt = updatedAt.parseIsoTimestamp() ?: Clock.now(),
        isSynced = true
    )
}
